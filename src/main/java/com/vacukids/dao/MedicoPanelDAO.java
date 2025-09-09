package com.vacukids.dao;

import com.vacukids.utils.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO del Panel Médico (ajustado a tu esquema). Métodos: - obtenerOverview() -
 * listarPacientesAplicacionesPaged(...) - listarAplicacionesPorPaciente(int
 * idPaciente)
 */
public class MedicoPanelDAO implements AutoCloseable {

    private final Connection cn;

    // Umbral para "stock bajo" (filas de stock_centro con cantidad <= THRESHOLD)
    private static final int STOCK_LOW_THRESHOLD = 10;

    public MedicoPanelDAO() throws SQLException {
        this.cn = Conexion.getConnection();
    }

    // =========================
    //  Resumen (cards) overview
    // =========================
    public Map<String, Object> obtenerOverview() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalPacientes", 0);
        out.put("citasHoy", 0);
        out.put("confirmadasHoy", 0);
        out.put("aplicacionesHoy", 0);
        out.put("stockBajo", 0);

        // totalPacientes (no hay campo 'activo' en pacientes)
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT COUNT(*) FROM pacientes")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out.put("totalPacientes", rs.getInt(1));
                }
            }
        } catch (SQLException ignored) {
        }

        // citasHoy: fecha_hora (excluimos canceladas)
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT COUNT(*) "
                + "FROM citas "
                + "WHERE DATE(fecha_hora) = CURRENT_DATE() "
                + "  AND estado IN ('pendiente','confirmada','asistio')")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out.put("citasHoy", rs.getInt(1));
                }
            }
        } catch (SQLException ignored) {
        }

        // confirmadasHoy: confirmada hoy
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT COUNT(*) "
                + "FROM citas "
                + "WHERE DATE(fecha_hora) = CURRENT_DATE() "
                + "  AND estado = 'confirmada'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out.put("confirmadasHoy", rs.getInt(1));
                }
            }
        } catch (SQLException ignored) {
        }

        // aplicacionesHoy: fecha_aplicacion es DATE
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT COUNT(*) "
                + "FROM aplicaciones "
                + "WHERE fecha_aplicacion = CURRENT_DATE()")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out.put("aplicacionesHoy", rs.getInt(1));
                }
            }
        } catch (SQLException ignored) {
        }

        // stockBajo: desde stock_centro (cantidad_disponible <= THRESHOLD)
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT COUNT(*) "
                + "FROM stock_centro "
                + "WHERE cantidad_disponible <= ?")) {
            ps.setInt(1, STOCK_LOW_THRESHOLD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out.put("stockBajo", rs.getInt(1));
                }
            }
        } catch (SQLException ignored) {
        }

        return out;
    }

    // ============================================
    //  Listado paginado para la tabla
    //  Devuelve: { total, items: [...] }
    //  items: paciente, cedula, tutor, total_aplicadas, ultima_aplicacion, id_paciente
    // ============================================
    public Map<String, Object> listarPacientesAplicacionesPaged(String q,
            Integer vacunaId,
            LocalDate desde,
            LocalDate hasta,
            int page,
            int size) {
        int limit = (size <= 0 ? 10 : size);
        int offset = Math.max(0, (page - 1) * limit);

        // Subconsulta de aplicaciones agrupadas por paciente (ap)
        // Filtro por vacuna y rango de fechas (hasta exclusivo -> incluye todo el día sumando 1)
        StringBuilder sub = new StringBuilder();
        sub.append(" SELECT a.id_paciente, COUNT(*) AS total_aplicadas, MAX(a.fecha_aplicacion) AS ultima_aplicacion ")
                .append("   FROM aplicaciones a ");

        List<Object> subParams = new ArrayList<>();
        List<String> whereSub = new ArrayList<>();

        if (vacunaId != null) {
            whereSub.add(" a.id_vacuna = ? ");
            subParams.add(vacunaId);
        }
        if (desde != null) {
            whereSub.add(" a.fecha_aplicacion >= ? ");
            subParams.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            whereSub.add(" a.fecha_aplicacion < ? ");
            subParams.add(Date.valueOf(hasta.plusDays(1)));
        }
        if (!whereSub.isEmpty()) {
            sub.append(" WHERE ").append(String.join(" AND ", whereSub));
        }
        sub.append(" GROUP BY a.id_paciente ");

        // COUNT (solo pacientes con aplicaciones)
        StringBuilder sqlCount = new StringBuilder();
        sqlCount.append(" SELECT COUNT(*) ")
                .append("  FROM (").append(sub).append(") ap ")
                .append("  JOIN pacientes p ON p.id_paciente = ap.id_paciente ");
        List<Object> countParams = new ArrayList<>(subParams);

        List<String> whereMain = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            whereMain.add(" (p.nombres LIKE ? OR p.apellidos LIKE ? OR p.cedula LIKE ? OR CONCAT(t.nombres,' ',t.apellidos) LIKE ?) ");
        }
        if (!whereMain.isEmpty()) {
            // necesitamos JOIN a tutores para filtrar por nombre del tutor
            sqlCount.append("  LEFT JOIN tutores t ON t.id_tutor = p.id_tutor ")
                    .append(" WHERE ").append(String.join(" AND ", whereMain));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim() + "%";
                countParams.add(like);
                countParams.add(like);
                countParams.add(like);
                countParams.add(like);
            }
        }

        int total = 0;
        try (PreparedStatement ps = cn.prepareStatement(sqlCount.toString())) {
            bindParams(ps, countParams);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            total = 0;
        }

        // LISTADO
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ")
                .append("   p.id_paciente, ")
                .append("   CONCAT(p.nombres, ' ', p.apellidos) AS paciente, ")
                .append("   p.cedula, ")
                .append("   COALESCE(CONCAT(t.nombres,' ',t.apellidos), '') AS tutor, ")
                .append("   ap.total_aplicadas, ")
                .append("   ap.ultima_aplicacion ")
                .append("  FROM (").append(sub).append(") ap ")
                .append("  JOIN pacientes p ON p.id_paciente = ap.id_paciente ")
                .append("  LEFT JOIN tutores t ON t.id_tutor = p.id_tutor ");

        List<Object> listParams = new ArrayList<>(subParams);
        if (q != null && !q.isBlank()) {
            sql.append(" WHERE (p.nombres LIKE ? OR p.apellidos LIKE ? OR p.cedula LIKE ? OR CONCAT(t.nombres,' ',t.apellidos) LIKE ?) ");
            String like = "%" + q.trim() + "%";
            listParams.add(like);
            listParams.add(like);
            listParams.add(like);
            listParams.add(like);
        }

        sql.append(" ORDER BY ap.ultima_aplicacion DESC, p.id_paciente DESC ")
                .append(" LIMIT ? OFFSET ? ");

        listParams.add(limit);
        listParams.add(offset);

        List<Map<String, Object>> items = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            bindParams(ps, listParams);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id_paciente", rs.getInt("id_paciente"));
                    row.put("paciente", rs.getString("paciente"));
                    row.put("cedula", rs.getString("cedula"));
                    row.put("tutor", rs.getString("tutor"));
                    row.put("total_aplicadas", rs.getInt("total_aplicadas"));

                    Timestamp ult = rs.getTimestamp("ultima_aplicacion");
                    String ultima = (ult != null ? ult.toLocalDateTime().toLocalDate().toString() : null);
                    row.put("ultima_aplicacion", ultima);

                    items.add(row);
                }
            }
        } catch (SQLException e) {
            items.clear();
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", total);
        out.put("items", items);
        return out;
    }

    // ============================================
    //  Detalle de aplicaciones por paciente
    // ============================================
    public List<Map<String, Object>> listarAplicacionesPorPaciente(int idPaciente) {
        String sql = "SELECT a.fecha_aplicacion, "
                + "       COALESCE(v.nombre, '') AS vacuna, "
                + "       COALESCE(a.dosis_numero, 0) AS dosis_numero, "
                + "       COALESCE(c.nombre, '') AS centro, "
                + "       COALESCE(a.efectos_secundarios, '') AS efectos "
                + // <-- nombre correcto
                "  FROM aplicaciones a "
                + "  JOIN vacunas v       ON v.id_vacuna  = a.id_vacuna "
                + "  JOIN centros_salud c ON c.id_centro = a.id_centro "
                + // <-- tabla correcta
                " WHERE a.id_paciente = ? "
                + " ORDER BY a.fecha_aplicacion DESC";

        List<Map<String, Object>> out = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    Date f = rs.getDate("fecha_aplicacion");
                    row.put("fecha_aplicacion", (f != null ? f.toLocalDate().toString() : null));
                    row.put("vacuna", rs.getString("vacuna"));
                    row.put("dosis_numero", rs.getInt("dosis_numero"));
                    row.put("centro", rs.getString("centro"));
                    row.put("efectos", rs.getString("efectos"));
                    out.add(row);
                }
            }
        } catch (SQLException e) {
            // devolver vacío para no romper UI
        }
        return out;
    }

    @Override
    public void close() {
        try {
            if (cn != null && !cn.isClosed()) {
                cn.close();
            }
        } catch (SQLException ignored) {
        }
    }

    // =========================
    // Helpers
    // =========================
    private static void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        int idx = 1;
        for (Object val : params) {
            if (val == null) {
                ps.setNull(idx++, Types.NULL);
            } else if (val instanceof Integer) {
                ps.setInt(idx++, (Integer) val);
            } else if (val instanceof Date) { // java.sql.Date
                ps.setDate(idx++, (Date) val);
            } else if (val instanceof LocalDate) {
                ps.setDate(idx++, Date.valueOf((LocalDate) val));
            } else if (val instanceof String) {
                ps.setString(idx++, (String) val);
            } else {
                ps.setObject(idx++, val);
            }
        }
    }
}

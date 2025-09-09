package com.vacukids.dao;

import com.vacukids.modelo.CentroOpcion;
import com.vacukids.modelo.Vacuna;
import com.vacukids.utils.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO de reportes (estadísticas y listados) sin acoplar a frameworks. Devuelve
 * listas de Map<String,Object> para usarlas directo en JSTL: - stats: keys =
 * fecha(LocalDate), centro, vacuna, totalDosis(Integer) - pacientes: keys =
 * fechaAplicacion(LocalDate), centro, paciente, cedulaPaciente, vacuna,
 * dosisNumero(Integer), efectosSecundarios
 */
public class ReporteDAO {

    // ======================
    // Opciones de filtros UI
    // ======================
    /**
     * Centros activos para combos
     */
    public List<CentroOpcion> centrosActivos() throws Exception {
        CentroDAO cdao = new CentroDAO();
        return cdao.listarOpciones();
    }

    /**
     * Vacunas para combos (id, nombre)
     */
    public List<Vacuna> vacunasBasicas() throws Exception {
        String sql = "SELECT id_vacuna, nombre FROM vacunas ORDER BY nombre";
        List<Vacuna> out = new ArrayList<>();
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Vacuna v = new Vacuna();
                v.setIdVacuna(rs.getInt("id_vacuna"));
                v.setNombre(rs.getString("nombre"));
                out.add(v);
            }
        }
        return out;
    }

    // =========================
    // Estadísticas (agrupado)
    // =========================
    /**
     * Estadísticas: total de dosis por fecha (y opcionalmente filtro por
     * centro/vacuna)
     *
     * @param desde inclusive; si es null, se asume hoy-30 días
     * @param hasta inclusive; si es null, se asume hoy
     */
    public List<Map<String, Object>> stats(LocalDate desde, LocalDate hasta,
            Integer idCentro, Integer idVacuna) throws Exception {
        if (hasta == null) {
            hasta = LocalDate.now();
        }
        if (desde == null) {
            desde = hasta.minusDays(30);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT DATE(a.fecha_aplicacion) AS f, ");
        sb.append("       c.nombre AS centro, v.nombre AS vacuna, COUNT(*) AS total ");
        sb.append("FROM aplicaciones a ");
        sb.append("JOIN centros_salud c ON c.id_centro = a.id_centro ");
        sb.append("JOIN vacunas v       ON v.id_vacuna = a.id_vacuna ");
        sb.append("WHERE a.fecha_aplicacion >= ? AND a.fecha_aplicacion <= ? ");

        List<Object> params = new ArrayList<>();
        params.add(java.sql.Date.valueOf(desde));
        params.add(java.sql.Date.valueOf(hasta));

        if (idCentro != null) {
            sb.append("AND a.id_centro = ? ");
            params.add(idCentro);
        }
        if (idVacuna != null) {
            sb.append("AND a.id_vacuna = ? ");
            params.add(idVacuna);
        }

        sb.append("GROUP BY DATE(a.fecha_aplicacion), c.nombre, v.nombre ");
        sb.append("ORDER BY f DESC, centro ASC, vacuna ASC");

        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("fecha", rs.getDate("f").toLocalDate());
                    row.put("centro", rs.getString("centro"));
                    row.put("vacuna", rs.getString("vacuna"));
                    row.put("totalDosis", rs.getInt("total"));
                    out.add(row);
                }
            }
        }
        return out;
    }

    // =======================================
    // Listado de pacientes vacunados (detalle)
    // =======================================
    /**
     * Listado de pacientes vacunados con filtros y paginación básica
     *
     * @param q búsqueda libre en nombre o cédula (opcional)
     * @param limit número máximo (p.ej. 500 para exportar, 100 en pantalla)
     * @param offset para paginación (0 por defecto)
     */
    public List<Map<String, Object>> pacientesVacunados(LocalDate desde, LocalDate hasta,
            Integer idCentro, Integer idVacuna,
            String q, int limit, int offset) throws Exception {
        if (hasta == null) {
            hasta = LocalDate.now();
        }
        if (desde == null) {
            desde = hasta.minusDays(30);
        }
        if (limit <= 0) {
            limit = 100;
        }
        if (offset < 0) {
            offset = 0;
        }

        StringBuilder sb = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sb.append("SELECT a.fecha_aplicacion AS f, ");
        sb.append("       c.nombre AS centro, ");
        sb.append("       CONCAT(p.nombres,' ',p.apellidos) AS paciente, ");
        sb.append("       p.cedula AS cedula, ");
        sb.append("       v.nombre AS vacuna, ");
        sb.append("       a.dosis_numero AS dosis, ");
        sb.append("       a.efectos_secundarios AS efectos ");
        sb.append("FROM aplicaciones a ");
        sb.append("JOIN pacientes p     ON p.id_paciente = a.id_paciente ");
        sb.append("JOIN centros_salud c ON c.id_centro   = a.id_centro ");
        sb.append("JOIN vacunas v       ON v.id_vacuna   = a.id_vacuna ");
        sb.append("WHERE a.fecha_aplicacion >= ? AND a.fecha_aplicacion <= ? ");
        params.add(java.sql.Date.valueOf(desde));
        params.add(java.sql.Date.valueOf(hasta));

        if (idCentro != null) {
            sb.append("AND a.id_centro = ? ");
            params.add(idCentro);
        }
        if (idVacuna != null) {
            sb.append("AND a.id_vacuna = ? ");
            params.add(idVacuna);
        }
        if (q != null && !q.isBlank()) {
            sb.append("AND (p.cedula = ? OR CONCAT(p.nombres,' ',p.apellidos) LIKE ?) ");
            params.add(q.trim());
            params.add("%" + q.trim() + "%");
        }

        sb.append("ORDER BY a.fecha_aplicacion DESC, c.nombre ASC, v.nombre ASC ");
        sb.append("LIMIT ? OFFSET ? ");
        params.add(limit);
        params.add(offset);

        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("fechaAplicacion", rs.getDate("f").toLocalDate());
                    row.put("centro", rs.getString("centro"));
                    row.put("paciente", rs.getString("paciente"));
                    row.put("cedulaPaciente", rs.getString("cedula"));
                    row.put("vacuna", rs.getString("vacuna"));
                    row.put("dosisNumero", rs.getInt("dosis"));
                    row.put("efectosSecundarios", rs.getString("efectos"));
                    out.add(row);
                }
            }
        }
        return out;
    }
}

package com.vacukids.dao;

import com.vacukids.modelo.Vacuna;
import com.vacukids.utils.Conexion;
import com.vacukids.utils.TextUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VacunaDAO {

    private static LocalDateTime getLdt(ResultSet rs, String col) throws SQLException {
        // Para MySQL 8 y JDBC 4.2, esto funciona bien:
        try {
            return rs.getObject(col, LocalDateTime.class);
        } catch (SQLException e) {
            // Fallback si el driver no soporta getObject(..., LocalDateTime.class)
            Timestamp ts = rs.getTimestamp(col);
            return (ts != null) ? ts.toLocalDateTime() : null;
        }
    }

    private static Vacuna mapRow(ResultSet rs) throws SQLException {
        Vacuna v = new Vacuna();
        v.setIdVacuna(rs.getInt("id_vacuna"));
        v.setNombre(rs.getString("nombre"));
        v.setDescripcion(rs.getString("descripcion"));
        v.setFabricante(rs.getString("fabricante"));
        v.setCreadoEn(getLdt(rs, "creado_en"));        // <-- FIX: LocalDateTime
        v.setActualizadoEn(getLdt(rs, "actualizado_en")); // <-- FIX: LocalDateTime
        return v;
    }

    // ==========
    // LISTAR
    // ==========
    public List<Vacuna> listar(String q, int limit, int offset) throws SQLException {
        String base
                = "SELECT id_vacuna, nombre, descripcion, fabricante, creado_en, actualizado_en "
                + "  FROM vacunas ";
        String where = "";
        if (q != null && !q.isBlank()) {
            where = "WHERE (LOWER(nombre) LIKE ? OR LOWER(fabricante) LIKE ?) ";
        }
        String tail = "ORDER BY nombre ASC LIMIT ? OFFSET ?";

        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(base + where + tail)) {

            int idx = 1;
            if (!where.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            List<Vacuna> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
            }
            return out;
        }
    }

    // ==========
    // OBTENER
    // ==========
    public Vacuna obtener(int idVacuna) throws SQLException {
        String sql
                = "SELECT id_vacuna, nombre, descripcion, fabricante, creado_en, actualizado_en "
                + "  FROM vacunas WHERE id_vacuna = ?";

        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVacuna);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    // ============================================
    // CHEQUEO DE DUPLICADO POR CLAVE NORMALIZADA
    // ============================================
    public boolean existsNombreKey(String nombreKey, Integer excludeId) throws SQLException {
        // Si creaste la columna nombre_key, usamos comparación directa:
        final String sqlWithColumn
                = "SELECT 1 FROM vacunas WHERE nombre_key = ? "
                + (excludeId != null ? "AND id_vacuna <> ?" : "")
                + " LIMIT 1";

        // Si NO creaste la columna, normalizamos en SQL (fallback):
        final String sqlFallback
                = "SELECT 1 FROM vacunas "
                + " WHERE LOWER(REGEXP_REPLACE(TRIM(nombre), '\\\\s+', ' ')) = ? "
                + (excludeId != null ? "AND id_vacuna <> ?" : "")
                + " LIMIT 1";

        try (Connection cn = Conexion.getConnection()) {
            // Intento con columna nombre_key
            try (PreparedStatement ps = cn.prepareStatement(sqlWithColumn)) {
                int i = 1;
                ps.setString(i++, nombreKey);
                if (excludeId != null) {
                    ps.setInt(i, excludeId);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                // 1054 = unknown column (no existe nombre_key): usamos fallback
                if (e.getErrorCode() != 1054) {
                    throw e;
                }
                try (PreparedStatement ps2 = cn.prepareStatement(sqlFallback)) {
                    int j = 1;
                    ps2.setString(j++, nombreKey);
                    if (excludeId != null) {
                        ps2.setInt(j, excludeId);
                    }
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        return rs2.next();
                    }
                }
            }
        }
    }

    // ==========
    // CREAR
    // ==========
    public void crear(Vacuna v) throws SQLException {
        if (v == null || v.getNombre() == null || v.getNombre().isBlank()) {
            throw new SQLException("El nombre de la vacuna es requerido.");
        }
        final String key = TextUtils.vacunaKey(v.getNombre());
        if (existsNombreKey(key, null)) {
            throw new SQLIntegrityConstraintViolationException(
                    "Ya existe una vacuna con un nombre equivalente (ignora mayúsculas/espacios): " + v.getNombre()
            );
        }

        String sql = "INSERT INTO vacunas(nombre, descripcion, fabricante) VALUES (?,?,?)";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, v.getNombre().trim());
            ps.setString(2, v.getDescripcion());
            ps.setString(3, v.getFabricante());
            ps.executeUpdate();
        }
    }

    // ==========
    // ACTUALIZAR
    // ==========
    public void actualizar(Vacuna v) throws SQLException {
        if (v == null || v.getNombre() == null || v.getNombre().isBlank()) {
            throw new SQLException("El nombre de la vacuna es requerido.");
        }
        final String key = TextUtils.vacunaKey(v.getNombre());
        if (existsNombreKey(key, v.getIdVacuna())) {
            throw new SQLIntegrityConstraintViolationException(
                    "Ya existe otra vacuna con un nombre equivalente (ignora mayúsculas/espacios): " + v.getNombre()
            );
        }

        String sql = "UPDATE vacunas SET nombre=?, descripcion=?, fabricante=? WHERE id_vacuna=?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, v.getNombre().trim());
            ps.setString(2, v.getDescripcion());
            ps.setString(3, v.getFabricante());
            ps.setInt(4, v.getIdVacuna());
            ps.executeUpdate();
        }
    }

    // ==========
    // ELIMINAR
    // ==========
    public void eliminar(int idVacuna) throws SQLException {
        String sql = "DELETE FROM vacunas WHERE id_vacuna=?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVacuna);
            ps.executeUpdate();
        }
    }

}

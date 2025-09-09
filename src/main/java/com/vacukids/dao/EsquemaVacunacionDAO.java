package com.vacukids.dao;

import com.vacukids.model.EsquemaDetalle;
import com.vacukids.model.EsquemaVacunacion;
import com.vacukids.utils.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EsquemaVacunacionDAO {

    // Usa TU helper de conexión
    private Connection getConnection() throws SQLException {
        return Conexion.getConnection();
    }

    /**
     * Crear esquema (Java 11 compatible, sin text blocks)
     */
    public int crear(EsquemaVacunacion e) throws SQLException {
        final String sql
                = "INSERT INTO esquemas_vacunacion "
                + "(nombre, descripcion, vigente_desde, vigente_hasta, activo, version) "
                + "VALUES (?,?,?,?,?,?)";

        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getDescripcion());
            ps.setDate(3, Date.valueOf(e.getVigenteDesde()));
            if (e.getVigenteHasta() != null) {
                ps.setDate(4, Date.valueOf(e.getVigenteHasta()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setBoolean(5, e.isActivo());
            ps.setInt(6, e.getVersion());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Actualizar esquema
     */
    public boolean actualizar(EsquemaVacunacion e) throws SQLException {
        final String sql
                = "UPDATE esquemas_vacunacion "
                + "SET nombre=?, descripcion=?, vigente_desde=?, vigente_hasta=?, activo=?, version=? "
                + "WHERE id_esquema=?";

        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getDescripcion());
            ps.setDate(3, Date.valueOf(e.getVigenteDesde()));
            if (e.getVigenteHasta() != null) {
                ps.setDate(4, Date.valueOf(e.getVigenteHasta()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setBoolean(5, e.isActivo());
            ps.setInt(6, e.getVersion());
            ps.setInt(7, e.getIdEsquema());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Eliminar esquema
     */
    public boolean eliminar(int idEsquema) throws SQLException {
        final String sql = "DELETE FROM esquemas_vacunacion WHERE id_esquema=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEsquema);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Obtener por id
     */
    public EsquemaVacunacion obtenerPorId(int id) throws SQLException {
        final String sql = "SELECT * FROM esquemas_vacunacion WHERE id_esquema=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    /**
     * Listar con filtro + paginación
     */
    public List<EsquemaVacunacion> listar(String filtro, int limit, int offset) throws SQLException {
        String base = "SELECT * FROM esquemas_vacunacion ";
        String where = (filtro != null && !filtro.trim().isEmpty()) ? "WHERE nombre LIKE ? " : "";
        String order = "ORDER BY activo DESC, vigente_desde DESC, version DESC ";
        String pag = "LIMIT ? OFFSET ?";

        List<EsquemaVacunacion> out = new ArrayList<>();
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(base + where + order + pag)) {
            int i = 1;
            if (!where.isEmpty()) {
                ps.setString(i++, "%" + filtro + "%");
            }
            ps.setInt(i++, limit);
            ps.setInt(i, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }

    /**
     * Marcar un esquema como el único activo
     */
    public boolean marcarActivoUnico(int idEsquema) throws SQLException {
        final String desact = "UPDATE esquemas_vacunacion SET activo=0";
        final String act = "UPDATE esquemas_vacunacion SET activo=1 WHERE id_esquema=?";
        try (Connection cn = getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement p1 = cn.prepareStatement(desact); PreparedStatement p2 = cn.prepareStatement(act)) {
                p1.executeUpdate();
                p2.setInt(1, idEsquema);
                int rows = p2.executeUpdate();
                cn.commit();
                return rows > 0;
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    /**
     * Obtener el esquema activo
     */
    public EsquemaVacunacion obtenerActivo() throws SQLException {
        final String sql = "SELECT * FROM esquemas_vacunacion WHERE activo=1 ORDER BY version DESC LIMIT 1";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return map(rs);
            }
        }
        return null;
    }

    // ===== mapper =====
    private EsquemaVacunacion map(ResultSet rs) throws SQLException {
        EsquemaVacunacion e = new EsquemaVacunacion();
        e.setIdEsquema(rs.getInt("id_esquema"));
        e.setNombre(rs.getString("nombre"));
        e.setDescripcion(rs.getString("descripcion"));
        Date dsd = rs.getDate("vigente_desde");
        if (dsd != null) {
            e.setVigenteDesde(dsd.toLocalDate());
        }
        Date hst = rs.getDate("vigente_hasta");
        if (hst != null) {
            e.setVigenteHasta(hst.toLocalDate());
        }
        e.setActivo(rs.getBoolean("activo"));
        e.setVersion(rs.getInt("version"));
        return e;
    }

    public void reemplazarDetalle(Integer idEsquema, List<EsquemaDetalle> detalles) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}

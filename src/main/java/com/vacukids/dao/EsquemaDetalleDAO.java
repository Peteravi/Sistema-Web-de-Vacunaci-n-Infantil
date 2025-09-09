package com.vacukids.dao;

import com.vacukids.model.EsquemaDetalle;
import com.vacukids.utils.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EsquemaDetalleDAO {

    private Connection getConnection() throws SQLException {
        return Conexion.getConnection();
    }

    /**
     * Crear detalle (Java 11: sin text blocks)
     */
    public int crear(EsquemaDetalle d) throws SQLException {
        final String sql
                = "INSERT INTO esquema_detalle "
                + "(id_esquema, id_vacuna, nro_dosis, edad_min_meses, edad_max_meses, "
                + " intervalo_min_dias, intervalo_max_dias, requisito_dosis_previa, observaciones) "
                + "VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, d.getIdEsquema());
            ps.setInt(2, d.getIdVacuna());
            ps.setInt(3, d.getNroDosis());
            setNullableInt(ps, 4, d.getEdadMinMeses());
            setNullableInt(ps, 5, d.getEdadMaxMeses());
            setNullableInt(ps, 6, d.getIntervaloMinDias());
            setNullableInt(ps, 7, d.getIntervaloMaxDias());
            ps.setBoolean(8, d.isRequisitoDosisPrevia());
            ps.setString(9, d.getObservaciones());

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
     * Actualizar detalle
     */
    public boolean actualizar(EsquemaDetalle d) throws SQLException {
        final String sql
                = "UPDATE esquema_detalle "
                + "SET id_vacuna=?, nro_dosis=?, edad_min_meses=?, edad_max_meses=?, "
                + "    intervalo_min_dias=?, intervalo_max_dias=?, requisito_dosis_previa=?, observaciones=? "
                + "WHERE id_detalle=? AND id_esquema=?";

        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, d.getIdVacuna());
            ps.setInt(2, d.getNroDosis());
            setNullableInt(ps, 3, d.getEdadMinMeses());
            setNullableInt(ps, 4, d.getEdadMaxMeses());
            setNullableInt(ps, 5, d.getIntervaloMinDias());
            setNullableInt(ps, 6, d.getIntervaloMaxDias());
            ps.setBoolean(7, d.isRequisitoDosisPrevia());
            ps.setString(8, d.getObservaciones());
            ps.setInt(9, d.getIdDetalle());
            ps.setInt(10, d.getIdEsquema());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Eliminar un detalle por id
     */
    public boolean eliminar(int idDetalle) throws SQLException {
        final String sql = "DELETE FROM esquema_detalle WHERE id_detalle=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Eliminar todos los detalles de un esquema
     */
    public boolean eliminarPorEsquema(int idEsquema) throws SQLException {
        final String sql = "DELETE FROM esquema_detalle WHERE id_esquema=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEsquema);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Listar detalles por esquema (orden: vacuna, dosis)
     */
    public List<EsquemaDetalle> listarPorEsquema(int idEsquema) throws SQLException {
        final String sql
                = "SELECT * FROM esquema_detalle "
                + "WHERE id_esquema=? "
                + "ORDER BY id_vacuna, nro_dosis";

        List<EsquemaDetalle> out = new ArrayList<>();
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idEsquema);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapDetalle(rs));
                }
            }
        }
        return out;
    }

    // ===== Helpers =====
    private static void setNullableInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v == null) {
            ps.setNull(idx, Types.INTEGER);
        } else {
            ps.setInt(idx, v);
        }
    }

    private EsquemaDetalle mapDetalle(ResultSet rs) throws SQLException {
        EsquemaDetalle d = new EsquemaDetalle();
        d.setIdDetalle(rs.getInt("id_detalle"));
        d.setIdEsquema(rs.getInt("id_esquema"));
        d.setIdVacuna(rs.getInt("id_vacuna"));
        d.setNroDosis(rs.getInt("nro_dosis"));
        d.setEdadMinMeses((Integer) rs.getObject("edad_min_meses"));
        d.setEdadMaxMeses((Integer) rs.getObject("edad_max_meses"));
        d.setIntervaloMinDias((Integer) rs.getObject("intervalo_min_dias"));
        d.setIntervaloMaxDias((Integer) rs.getObject("intervalo_max_dias"));
        d.setRequisitoDosisPrevia(rs.getBoolean("requisito_dosis_previa"));
        d.setObservaciones(rs.getString("observaciones"));
        return d;
    }
}

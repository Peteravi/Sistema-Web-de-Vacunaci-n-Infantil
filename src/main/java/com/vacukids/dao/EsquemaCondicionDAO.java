package com.vacukids.dao;

import com.vacukids.model.EsquemaCondicion;
import com.vacukids.utils.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EsquemaCondicionDAO {

    private Connection getConnection() throws SQLException {
        return Conexion.getConnection();
    }

    public List<EsquemaCondicion> listarPorDetalle(int idDetalle) throws SQLException {
        String sql = "SELECT * FROM esquema_condicion WHERE id_detalle=?";
        List<EsquemaCondicion> out = new ArrayList<>();
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }

    public int crear(EsquemaCondicion c) throws SQLException {
        String sql = "INSERT INTO esquema_condicion (id_detalle, tipo, descripcion) VALUES (?,?,?)";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getIdDetalle());
            ps.setString(2, c.getTipo());
            ps.setString(3, c.getDescripcion());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean eliminar(int idCondicion) throws SQLException {
        String sql = "DELETE FROM esquema_condicion WHERE id_condicion=?";
        try (Connection cn = getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCondicion);
            return ps.executeUpdate() > 0;
        }
    }

    private EsquemaCondicion map(ResultSet rs) throws SQLException {
        EsquemaCondicion c = new EsquemaCondicion();
        c.setIdCondicion(rs.getInt("id_condicion"));
        c.setIdDetalle(rs.getInt("id_detalle"));
        c.setTipo(rs.getString("tipo"));
        c.setDescripcion(rs.getString("descripcion"));
        return c;
    }
}

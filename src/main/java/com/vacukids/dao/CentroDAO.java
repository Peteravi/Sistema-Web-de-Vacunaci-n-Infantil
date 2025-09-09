package com.vacukids.dao;

import com.vacukids.modelo.CentroOpcion;
import com.vacukids.utils.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CentroDAO {

    public List<CentroOpcion> listarOpciones() throws Exception {
        String sql = "SELECT id_centro, nombre FROM centros_salud WHERE activo=1 ORDER BY nombre";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<CentroOpcion> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new CentroOpcion(rs.getInt(1), rs.getString(2)));
            }
            return out;
        }
    }

    public List<CentroOpcion> listarActivosOpciones() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}

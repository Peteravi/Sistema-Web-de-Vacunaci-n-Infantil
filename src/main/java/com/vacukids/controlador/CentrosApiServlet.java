package com.vacukids.controlador;

import com.vacukids.utils.Conexion;
import static com.vacukids.utils.Jsons.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/representante/centros")
public class CentrosApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String sql = "SELECT id_centro, nombre, direccion, telefono FROM centros_salud WHERE activo=1 ORDER BY nombre";
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("idCentro", rs.getInt("id_centro"));
                m.put("nombre", rs.getString("nombre"));
                m.put("direccion", rs.getString("direccion"));
                m.put("telefono", rs.getString("telefono"));
                items.add(m);
            }
            Map<String, Object> out = new HashMap<String, Object>();
            out.put("ok", true);
            out.put("items", items);
            ok(resp, out);
        } catch (SQLException e) {
            serverError(resp, "Error consultando centros: " + e.getMessage());
        }
    }
}

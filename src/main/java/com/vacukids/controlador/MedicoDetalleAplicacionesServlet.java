// src/main/java/com/vacukids/controlador/MedicoDetalleAplicacionesServlet.java
package com.vacukids.controlador;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vacukids.utils.Conexion;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/api/medico/panel/aplicaciones/detalle")
public class MedicoDetalleAplicacionesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sId = req.getParameter("idPaciente");
        if (sId == null) {
            resp.sendError(400, "Falta idPaciente");
            return;
        }
        int idPaciente;
        try {
            idPaciente = Integer.parseInt(sId);
        } catch (NumberFormatException e) {
            resp.sendError(400, "idPaciente inválido");
            return;
        }

        String sql
                = "SELECT a.id_aplicacion,\n"
                + "       a.fecha_aplicacion,\n"
                + "       v.nombre AS vacuna,\n"
                + "       a.dosis_numero,\n"
                + "       c.nombre AS centro,\n"
                + "       a.efectos_secundarios AS efectos\n"
                + "FROM aplicaciones a\n"
                + "JOIN vacunas v       ON v.id_vacuna = a.id_vacuna\n"
                + "JOIN centros_salud c ON c.id_centro = a.id_centro\n"
                + "WHERE a.id_paciente = ?\n"
                + "ORDER BY a.fecha_aplicacion DESC";

        JsonArray arr = new JsonArray();
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObject o = new JsonObject();
                    o.addProperty("id_aplicacion", rs.getInt("id_aplicacion"));
                    Date f = rs.getDate("fecha_aplicacion");
                    o.addProperty("fecha_aplicacion", (f != null ? f.toString() : null));
                    o.addProperty("vacuna", rs.getString("vacuna"));
                    o.addProperty("dosis_numero", rs.getInt("dosis_numero"));
                    o.addProperty("centro", rs.getString("centro"));
                    o.addProperty("efectos", rs.getString("efectos"));
                    arr.add(o);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(500, "Error consultando detalle");
            return;
        }

        resp.setContentType("application/json; charset=UTF-8");
        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.add("data", arr);
        resp.getWriter().write(out.toString());
    }
}

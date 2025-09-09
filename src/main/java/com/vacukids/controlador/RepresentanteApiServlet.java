package com.vacukids.controlador;

import com.vacukids.dao.PacienteDAO;
import com.vacukids.dao.PacienteDAO.PacienteMini;
import com.vacukids.modelo.Paciente;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static com.vacukids.utils.Jsons.*;

@WebServlet(urlPatterns = {
    "/api/representante/ping",
    "/api/representante/hijos",
    "/api/representante/pacientes" // alias
})
public class RepresentanteApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final PacienteDAO pacienteDAO = new PacienteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath(); // e.g. /api/representante/hijos

        if ("/api/representante/ping".equals(path)) {
            Map<String, Object> out = new HashMap<>();
            HttpSession s = req.getSession(false);
            out.put("ok", true);
            out.put("session", s != null);
            if (s != null) {
                out.put("idUsuario", s.getAttribute("idUsuario"));
                out.put("idRol", s.getAttribute("idRol"));
                out.put("idTutor", s.getAttribute("idTutor"));
            }
            ok(resp, out);
            return;
        }

        if ("/api/representante/hijos".equals(path) || "/api/representante/pacientes".equals(path)) {
            HttpSession s = req.getSession(false);
            if (s == null || s.getAttribute("idTutor") == null) {
                unauthorized(resp, "Sesión inválida: sin idTutor");
                return;
            }
            int idTutor = (Integer) s.getAttribute("idTutor");
            try {
                // Usamos PacienteMini (todo String/Integer) para evitar LocalDate en JSON
                List<PacienteMini> items = pacienteDAO.listarPorTutor(idTutor);
                Map<String, Object> out = new HashMap<>();
                out.put("ok", true);
                out.put("items", items);
                ok(resp, out);
            } catch (SQLException e) {
                serverError(resp, "Error listando pacientes: " + e.getMessage());
            }
            return;
        }

        error(resp, 404, "Ruta no encontrada: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        if (!("/api/representante/hijos".equals(path) || "/api/representante/pacientes".equals(path))) {
            error(resp, 404, "Ruta no encontrada: " + path);
            return;
        }

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("idTutor") == null) {
            unauthorized(resp, "Sesión inválida: sin idTutor");
            return;
        }
        Integer idTutor = (Integer) s.getAttribute("idTutor");

        try {
            // Espera JSON: { cedula, nombres, apellidos, fechaNacimiento: "yyyy-MM-dd", sexo, direccion, telefono }
            Paciente body = fromJson(req.getReader(), Paciente.class);
            if (body == null) {
                error(resp, 400, "JSON inválido");
                return;
            }

            int idGen = pacienteDAO.crearParaTutor(body, idTutor);

            Map<String, Object> out = new HashMap<>();
            out.put("ok", true);
            out.put("id_paciente", idGen);
            ok(resp, out);
        } catch (SQLException e) {
            error(resp, 409, "No se pudo crear el paciente (posible duplicado): " + e.getMessage());
        } catch (Exception e) {
            serverError(resp, "Error procesando solicitud: " + e.getMessage());
        }
    }
}

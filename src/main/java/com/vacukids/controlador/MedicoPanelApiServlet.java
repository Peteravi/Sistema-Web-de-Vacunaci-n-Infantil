package com.vacukids.controlador;

import com.vacukids.dao.MedicoPanelDAO;
import static com.vacukids.utils.Jsons.ok;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * API del Panel del Médico.
 * Endpoints (GET):
 *   - /api/medico/panel/overview
 *   - /api/medico/panel/aplicaciones?q=&vacunaId=&desde=YYYY-MM-DD&hasta=YYYY-MM-DD&page=1&size=10
 *   - /api/medico/panel/aplicaciones/detalle?idPaciente=123
 */
@WebServlet(name = "MedicoPanelApiServlet", urlPatterns = {"/api/medico/panel/*"})
public class MedicoPanelApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo(); // "/overview", "/aplicaciones", "/aplicaciones/detalle", o null
        if (path == null) path = "/";

        try (MedicoPanelDAO dao = new MedicoPanelDAO()) {
            switch (path) {
                case "/overview": {
                    Map<String,Object> out = dao.obtenerOverview();
                    ok(resp, out != null ? out : Collections.emptyMap());
                    break;
                }

                case "/aplicaciones": {
                    String q = trimOrNull(req.getParameter("q"));
                    Integer vacunaId = parseIntOrNull(req.getParameter("vacunaId"));
                    LocalDate desde = parseDateOrNull(req.getParameter("desde"));
                    LocalDate hasta = parseDateOrNull(req.getParameter("hasta"));
                    int page = parseIntOrDefault(req.getParameter("page"), 1);
                    int size = parseIntOrDefault(req.getParameter("size"), 10);

                    Map<String,Object> paged = dao.listarPacientesAplicacionesPaged(q, vacunaId, desde, hasta, page, size);
                    if (paged == null) {
                        Map<String,Object> empty = new LinkedHashMap<>();
                        empty.put("total", 0);
                        empty.put("items", Collections.emptyList());
                        ok(resp, empty);
                    } else {
                        ok(resp, paged);
                    }
                    break;
                }

                case "/aplicaciones/detalle": {
                    Integer idPaciente = parseIntOrNull(req.getParameter("idPaciente"));
                    if (idPaciente == null) {
                        err(resp, 400, "idPaciente es requerido");
                        return;
                    }
                    ok(resp, dao.listarAplicacionesPorPaciente(idPaciente));
                    break;
                }

                default:
                    err(resp, 404, "Endpoint no encontrado: " + path);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            err(resp, 500, "Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            err(resp, 500, "Error: " + e.getMessage());
        }
    }

    // -------- helpers --------
    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Integer parseIntOrNull(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.valueOf(s); }
        catch (Exception e) { return null; }
    }

    private static int parseIntOrDefault(String s, int def) {
        try { return (s == null || s.isBlank()) ? def : Integer.parseInt(s); }
        catch (Exception e) { return def; }
    }

    private static LocalDate parseDateOrNull(String s) {
        try { return (s == null || s.isBlank()) ? null : LocalDate.parse(s); }
        catch (Exception e) { return null; }
    }

    private static void err(HttpServletResponse resp, int status, String message) throws IOException {
        // Usa el helper centralizado de JSON
        com.vacukids.utils.Jsons.error(resp, status, message == null ? "Error" : message);
    }
}

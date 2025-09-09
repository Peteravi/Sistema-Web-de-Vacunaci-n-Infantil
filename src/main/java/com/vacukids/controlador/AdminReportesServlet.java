package com.vacukids.controlador;

import com.vacukids.dao.ReporteDAO;
import com.vacukids.modelo.CentroOpcion;
import com.vacukids.modelo.Vacuna;
import static com.vacukids.utils.Jsons.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador de Reportes (vista: /Pages/reportes.jsp) Path protegido por
 * AuthFilter: /admin/*
 */
@WebServlet("/admin/reportes")
public class AdminReportesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ReporteDAO dao = new ReporteDAO();

    // ==========
    // Utilities
    // ==========
    private LocalDate dateOrNull(HttpServletRequest req, String p) {
        String v = req.getParameter(p);
        if (v == null || v.isBlank()) {
            return null;
        }
        return LocalDate.parse(v.trim()); // yyyy-MM-dd
    }

    private Integer intOrNull(HttpServletRequest req, String p) {
        try {
            String v = req.getParameter(p);
            return (v == null || v.isBlank()) ? null : Integer.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    private int intDefault(HttpServletRequest req, String p, int def) {
        try {
            String v = req.getParameter(p);
            return (v == null || v.isBlank()) ? def : Integer.parseInt(v);
        } catch (Exception e) {
            return def;
        }
    }

    // =====
    // GET
    // =====
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) {
            action = "view";
        }

        // Filtros comunes
        LocalDate desde = dateOrNull(req, "desde");
        LocalDate hasta = dateOrNull(req, "hasta");
        Integer idCentro = intOrNull(req, "idCentro");
        Integer idVacuna = intOrNull(req, "idVacuna");
        String q = req.getParameter("q");
        int limit = intDefault(req, "limit", ("listPacientesExport".equals(action) ? 10000 : 200));
        int offset = intDefault(req, "offset", 0);

        try {
            // Combos
            List<CentroOpcion> centros = dao.centrosActivos();
            List<Vacuna> vacunas = dao.vacunasBasicas();
            req.setAttribute("centros", centros);
            req.setAttribute("vacunas", vacunas);

            switch (action) {
                case "statsExport": {
                    List<Map<String, Object>> stats = dao.stats(desde, hasta, idCentro, idVacuna);
                    exportStatsCsv(stats, resp);
                    return;
                }
                case "listPacientesExport": {
                    List<Map<String, Object>> items = dao.pacientesVacunados(desde, hasta, idCentro, idVacuna, q, 10000, 0);
                    exportPacientesCsv(items, resp);
                    return;
                }
                case "view":
                case "stats":
                case "listPacientes":
                default: {
                    // Cargar ambas secciones para la vista
                    List<Map<String, Object>> stats = dao.stats(desde, hasta, idCentro, idVacuna);
                    List<Map<String, Object>> items = dao.pacientesVacunados(desde, hasta, idCentro, idVacuna, q, limit, offset);
                    req.setAttribute("stats", stats);
                    req.setAttribute("pacientesVacunados", items);
                    req.getRequestDispatcher("/Pages/reportes.jsp").forward(req, resp);
                }
            }
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage() == null ? "Error en reportes" : e.getMessage());
            req.getRequestDispatcher("/Pages/reportes.jsp").forward(req, resp);
        }
    }

    // =================
    // CSV Export helpers
    // =================
    private void exportStatsCsv(List<Map<String, Object>> stats, HttpServletResponse resp) throws IOException {
        String filename = "estadisticas_" + LocalDate.now() + ".csv";
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\""
                + URLEncoder.encode(filename, StandardCharsets.UTF_8.name()) + "\"");

        try (PrintWriter w = resp.getWriter()) {
            w.println("Fecha,Centro,Vacuna,TotalDosis");
            for (Map<String, Object> r : stats) {
                String fecha = String.valueOf(r.get("fecha"));
                String centro = sanitizeCsv(String.valueOf(r.get("centro")));
                String vacuna = sanitizeCsv(String.valueOf(r.get("vacuna")));
                String total = String.valueOf(r.get("totalDosis"));
                w.printf("%s,%s,%s,%s%n", fecha, centro, vacuna, total);
            }
        }
    }

    private void exportPacientesCsv(List<Map<String, Object>> rows, HttpServletResponse resp) throws IOException {
        String filename = "pacientes_vacunados_" + LocalDate.now() + ".csv";
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\""
                + URLEncoder.encode(filename, StandardCharsets.UTF_8.name()) + "\"");

        try (PrintWriter w = resp.getWriter()) {
            w.println("FechaAplicacion,Centro,Paciente,Cedula,Vacuna,DosisNumero,EfectosSecundarios");
            for (Map<String, Object> r : rows) {
                String f = String.valueOf(r.get("fechaAplicacion"));
                String c = sanitizeCsv(String.valueOf(r.get("centro")));
                String p = sanitizeCsv(String.valueOf(r.get("paciente")));
                String ce = sanitizeCsv(String.valueOf(r.get("cedulaPaciente")));
                String v = sanitizeCsv(String.valueOf(r.get("vacuna")));
                String d = String.valueOf(r.get("dosisNumero"));
                String e = sanitizeCsv(String.valueOf(r.get("efectosSecundarios")));
                w.printf("%s,%s,%s,%s,%s,%s,%s%n", f, c, p, ce, v, d, e);
            }
        }
    }

    private String sanitizeCsv(String s) {
        if (s == null) {
            return "";
        }
        // Comillas y comas seguras
        String val = s.replace("\"", "\"\"");
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val + "\"";
        }
        return val;
    }
}

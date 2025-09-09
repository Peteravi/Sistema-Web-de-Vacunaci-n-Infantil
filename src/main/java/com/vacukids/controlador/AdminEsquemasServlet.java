package com.vacukids.controlador;

import com.vacukids.dao.EsquemaVacunacionDAO;
import com.vacukids.dao.EsquemaDetalleDAO;
import com.vacukids.model.EsquemaVacunacion;
import com.vacukids.model.EsquemaDetalle;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;
import java.sql.SQLException;

@WebServlet(name = "AdminEsquemasServlet", urlPatterns = {"/admin/esquemas"})
public class AdminEsquemasServlet extends HttpServlet {

    private EsquemaVacunacionDAO esquemaDAO;
    private EsquemaDetalleDAO detalleDAO;

    @Override
    public void init() throws ServletException {
        esquemaDAO = new EsquemaVacunacionDAO();
        detalleDAO = new EsquemaDetalleDAO();
    }

    // ---------- Helpers JSON ----------
    private void json(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(json);
        }
    }

    private void ok(HttpServletResponse resp, String msg) throws IOException {
        json(resp, 200, "{\"ok\":true,\"msg\":\"" + escape(msg) + "\"}");
    }

    private void okData(HttpServletResponse resp, String jsonData) throws IOException {
        json(resp, 200, "{\"ok\":true,\"data\":" + jsonData + "}");
    }

    private void err(HttpServletResponse resp, String msg) throws IOException {
        json(resp, 400, "{\"ok\":false,\"msg\":\"" + escape(msg) + "\"}");
    }

    private String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private Integer parseIntOrNull(String s) {
        try {
            return (s == null || s.trim().isEmpty()) ? null : Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean parseBoolOrNull(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        return "1".equals(s) || "true".equalsIgnoreCase(s) || "on".equalsIgnoreCase(s);
    }

    private LocalDate parseDateOrNull(String s) {
        try {
            return (s == null || s.trim().isEmpty()) ? null : LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- Routing ----------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = Optional.ofNullable(req.getParameter("action")).orElse("list");
        try {
            switch (action) {
                case "get":
                    handleGet(req, resp);
                    break;
                case "listDetalle":
                    handleListDetalle(req, resp);
                    break;
                case "list":
                default:
                    handleList(req, resp);
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            err(resp, "Error de BD: " + e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            err(resp, "Error: " + ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = Optional.ofNullable(req.getParameter("action")).orElse("");
        try {
            switch (action) {
                case "create":
                    handleCreate(req, resp);
                    break;
                case "update":
                    handleUpdate(req, resp);
                    break;
                case "delete":
                    handleDelete(req, resp);
                    break;
                case "activate":
                    handleActivate(req, resp);
                    break;
                case "replaceDetalle":
                    handleReplaceDetalle(req, resp);
                    break;
                default:
                    err(resp, "Acción no válida");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            err(resp, "Error de BD: " + e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            err(resp, "Error: " + ex.getMessage());
        }
    }

    // ---------- Handlers: Esquema ----------
    private void handleList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String filtro = Optional.ofNullable(req.getParameter("q")).orElse("").trim();
        Integer limit = parseIntOrNull(req.getParameter("limit"));
        Integer offset = parseIntOrNull(req.getParameter("offset"));
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        if (offset == null || offset < 0) {
            offset = 0;
        }

        var list = esquemaDAO.listar(filtro, limit, offset);

        // JSON simple
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(esquemaToJson(list.get(i)));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        okData(resp, sb.toString());
    }

    private void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer id = parseIntOrNull(req.getParameter("id"));
        if (id == null) {
            err(resp, "Parámetro id requerido");
            return;
        }
        EsquemaVacunacion e = esquemaDAO.obtenerPorId(id);
        if (e == null) {
            err(resp, "Esquema no encontrado");
            return;
        }
        okData(resp, esquemaToJson(e));
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        EsquemaVacunacion e = fromRequest(req);
        int idNew = esquemaDAO.crear(e);
        okData(resp, "{\"id\":" + idNew + "}");
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer id = parseIntOrNull(req.getParameter("id_esquema"));
        if (id == null) {
            err(resp, "id_esquema requerido");
            return;
        }
        EsquemaVacunacion e = fromRequest(req);
        e.setIdEsquema(id);
        boolean ok = esquemaDAO.actualizar(e);
        if (ok) {
            ok(resp, "Actualizado");
        } else {
            err(resp, "No se actualizó");
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer id = parseIntOrNull(req.getParameter("id_esquema"));
        if (id == null) {
            err(resp, "id_esquema requerido");
            return;
        }
        boolean ok = esquemaDAO.eliminar(id);
        if (ok) {
            ok(resp, "Eliminado");
        } else {
            err(resp, "No se eliminó");
        }
    }

    private void handleActivate(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer id = parseIntOrNull(req.getParameter("id_esquema"));
        if (id == null) {
            err(resp, "id_esquema requerido");
            return;
        }
        boolean ok = esquemaDAO.marcarActivoUnico(id);
        if (ok) {
            ok(resp, "Activado como esquema vigente");
        } else {
            err(resp, "No se pudo activar");
        }
    }

    private EsquemaVacunacion fromRequest(HttpServletRequest req) {
        EsquemaVacunacion e = new EsquemaVacunacion();
        e.setNombre(Optional.ofNullable(req.getParameter("nombre")).orElse("").trim());
        e.setDescripcion(Optional.ofNullable(req.getParameter("descripcion")).orElse("").trim());

        LocalDate vigDesde = parseDateOrNull(req.getParameter("vigente_desde"));
        LocalDate vigHasta = parseDateOrNull(req.getParameter("vigente_hasta"));
        if (vigDesde == null) {
            vigDesde = LocalDate.now();
        }

        e.setVigenteDesde(vigDesde);
        e.setVigenteHasta(vigHasta);

        Boolean activo = parseBoolOrNull(req.getParameter("activo"));
        e.setActivo(Boolean.TRUE.equals(activo));

        Integer version = parseIntOrNull(req.getParameter("version"));
        e.setVersion(version == null ? 1 : version);
        return e;
    }

    private String esquemaToJson(EsquemaVacunacion e) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id_esquema\":").append(e.getIdEsquema() == null ? "null" : e.getIdEsquema()).append(",");
        sb.append("\"nombre\":\"").append(escape(e.getNombre())).append("\",");
        sb.append("\"descripcion\":\"").append(escape(e.getDescripcion())).append("\",");
        sb.append("\"vigente_desde\":\"").append(e.getVigenteDesde() == null ? "" : e.getVigenteDesde()).append("\",");
        sb.append("\"vigente_hasta\":\"").append(e.getVigenteHasta() == null ? "" : e.getVigenteHasta()).append("\",");
        sb.append("\"activo\":").append(e.isActivo()).append(",");
        sb.append("\"version\":").append(e.getVersion());
        sb.append("}");
        return sb.toString();
    }

    // ---------- Handlers: Detalle ----------
    private void handleListDetalle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer idEsquema = parseIntOrNull(req.getParameter("id_esquema"));
        if (idEsquema == null) {
            err(resp, "id_esquema requerido");
            return;
        }

        var list = detalleDAO.listarPorEsquema(idEsquema);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(detalleToJson(list.get(i)));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        okData(resp, sb.toString());
    }

    /**
     * Reemplaza (borra e inserta) todo el detalle del esquema. Espera arrays
     * paralelos en el body (application/x-www-form-urlencoded): id_vacuna[],
     * nro_dosis[], edad_min_meses[], edad_max_meses[], intervalo_min_dias[],
     * intervalo_max_dias[], requisito_dosis_previa[], observaciones[]
     *
     * Solo son obligatorios: id_vacuna[], nro_dosis[]
     */
    private void handleReplaceDetalle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer idEsquema = parseIntOrNull(req.getParameter("id_esquema"));
        if (idEsquema == null) {
            err(resp, "id_esquema requerido");
            return;
        }

        String[] idVacunaArr = req.getParameterValues("id_vacuna[]");
        String[] nroDosisArr = req.getParameterValues("nro_dosis[]");
        String[] eminArr = req.getParameterValues("edad_min_meses[]");
        String[] emaxArr = req.getParameterValues("edad_max_meses[]");
        String[] iminArr = req.getParameterValues("intervalo_min_dias[]");
        String[] imaxArr = req.getParameterValues("intervalo_max_dias[]");
        String[] reqPrevArr = req.getParameterValues("requisito_dosis_previa[]");
        String[] obsArr = req.getParameterValues("observaciones[]");

        if (idVacunaArr == null || nroDosisArr == null || idVacunaArr.length != nroDosisArr.length) {
            err(resp, "Arrays id_vacuna[] y nro_dosis[] son requeridos y deben tener igual longitud");
            return;
        }

        List<EsquemaDetalle> detalles = new ArrayList<>();
        int n = idVacunaArr.length;
        for (int i = 0; i < n; i++) {
            EsquemaDetalle d = new EsquemaDetalle();
            d.setIdEsquema(idEsquema);
            d.setIdVacuna(parseIntOrNull(idVacunaArr[i]));
            d.setNroDosis(parseIntOrNull(nroDosisArr[i]));
            d.setEdadMinMeses(valArrayInt(eminArr, i));
            d.setEdadMaxMeses(valArrayInt(emaxArr, i));
            d.setIntervaloMinDias(valArrayInt(iminArr, i));
            d.setIntervaloMaxDias(valArrayInt(imaxArr, i));
            d.setRequisitoDosisPrevia(valArrayBool(reqPrevArr, i));
            d.setObservaciones(valArrayStr(obsArr, i));

            if (d.getIdVacuna() == null || d.getNroDosis() == null) {
                err(resp, "Cada fila requiere id_vacuna y nro_dosis");
                return;
            }
            detalles.add(d);
        }

        esquemaDAO.reemplazarDetalle(idEsquema, detalles);
        ok(resp, "Detalle reemplazado");
    }

    private Integer valArrayInt(String[] arr, int i) {
        if (arr == null || i >= arr.length) {
            return null;
        }
        return parseIntOrNull(arr[i]);
    }

    private boolean valArrayBool(String[] arr, int i) {
        if (arr == null || i >= arr.length) {
            return false;
        }
        Boolean b = parseBoolOrNull(arr[i]);
        return Boolean.TRUE.equals(b);
    }

    private String valArrayStr(String[] arr, int i) {
        if (arr == null || i >= arr.length) {
            return null;
        }
        String s = arr[i];
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private String detalleToJson(EsquemaDetalle d) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id_detalle\":").append(d.getIdDetalle() == null ? "null" : d.getIdDetalle()).append(",");
        sb.append("\"id_esquema\":").append(d.getIdEsquema()).append(",");
        sb.append("\"id_vacuna\":").append(d.getIdVacuna()).append(",");
        sb.append("\"nro_dosis\":").append(d.getNroDosis()).append(",");
        sb.append("\"edad_min_meses\":").append(d.getEdadMinMeses() == null ? "null" : d.getEdadMinMeses()).append(",");
        sb.append("\"edad_max_meses\":").append(d.getEdadMaxMeses() == null ? "null" : d.getEdadMaxMeses()).append(",");
        sb.append("\"intervalo_min_dias\":").append(d.getIntervaloMinDias() == null ? "null" : d.getIntervaloMinDias()).append(",");
        sb.append("\"intervalo_max_dias\":").append(d.getIntervaloMaxDias() == null ? "null" : d.getIntervaloMaxDias()).append(",");
        sb.append("\"requisito_dosis_previa\":").append(d.isRequisitoDosisPrevia()).append(",");
        sb.append("\"observaciones\":").append(d.getObservaciones() == null ? "null" : "\"" + escape(d.getObservaciones()) + "\"");
        sb.append("}");
        return sb.toString();
    }
}

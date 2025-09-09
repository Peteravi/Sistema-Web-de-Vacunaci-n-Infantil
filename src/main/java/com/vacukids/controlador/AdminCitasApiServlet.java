package com.vacukids.controlador;

import static com.vacukids.utils.Jsons.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vacukids.utils.Conexion;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * API Admin para CITAS GET /api/admin/citas -> listado (q, estado, cedula,
 * desde, hasta, page, size) POST /api/admin/citas -> crear cita
 * /api/admin/citas {action:'estado', id_cita, estado} -> cambiar estado
 * (fallback POST) PUT /api/admin/citas -> actualizar cita DELETE
 * /api/admin/citas?id=... -> eliminar cita
 *
 * Respuestas: - Listado: { ok:true, data:{ items:[...], total:N, page:X, size:Y
 * } } - Crear: { ok:true, id_cita:<id> } - Update: { ok:true } - Estado: {
 * ok:true, patched:true } - Delete: { ok:true }
 */
@WebServlet(urlPatterns = {"/api/admin/citas"})
public class AdminCitasApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    // ===== Helpers =====
    private static String t(String s) {
        return s == null ? null : s.trim();
    }

    private static boolean has(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static int intOr(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object v = params.get(i);
            if (v instanceof Timestamp) {
                ps.setTimestamp(i + 1, (Timestamp) v);
            } else {
                ps.setObject(i + 1, v);
            }
        }
    }

    private static String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = req.getReader()) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static boolean isAdmin(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        Integer idRol = (s == null) ? null : (Integer) s.getAttribute("idRol");
        return idRol != null && idRol.intValue() == 1;
    }

    private static String fmtTS(Timestamp ts) {
        if (ts == null) {
            return null;
        }
        return ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    // "YYYY-MM-DD" | "YYYY-MM-DD HH:mm" | "YYYY-MM-DDTHH:mm[:ss]"
    private static Timestamp parseStart(String s) {
        if (!has(s)) {
            return null;
        }
        s = s.replace('T', ' ').trim();
        try {
            if (s.length() == 10) {
                return Timestamp.valueOf(LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay());
            }
            if (s.length() == 16) {
                return Timestamp.valueOf(LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return Timestamp.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static Timestamp parseEnd(String s) {
        if (!has(s)) {
            return null;
        }
        s = s.replace('T', ' ').trim();
        try {
            if (s.length() == 10) {
                return Timestamp.valueOf(LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59, 59));
            }
            if (s.length() == 16) {
                return Timestamp.valueOf(LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return Timestamp.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer toInt(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private static String toStr(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String toStrOrNull(Object o) {
        if (o == null) {
            return null;
        }
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? null : s;
    }

    // ===== GET: listado =====
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req)) {
            error(resp, 403, "No autorizado: se requiere rol Admin.");
            return;
        }

        String q = t(req.getParameter("q"));
        String estado = t(req.getParameter("estado"));
        String cedula = t(req.getParameter("cedula"));
        String desde = t(req.getParameter("desde"));
        String hasta = t(req.getParameter("hasta"));
        int page = Math.max(1, intOr(req.getParameter("page"), 1));
        int size = Math.max(1, Math.min(100, intOr(req.getParameter("size"), 10)));
        int offset = (page - 1) * size;

        Timestamp tsDesde = parseStart(desde);
        Timestamp tsHasta = parseEnd(hasta);

        StringBuilder fw = new StringBuilder();
        fw.append(" FROM citas c ")
                .append(" JOIN pacientes p ON p.id_paciente = c.id_paciente ")
                .append(" JOIN centros_salud cs ON cs.id_centro = c.id_centro ")
                .append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (has(estado)) {
            fw.append(" AND c.estado = ? ");
            params.add(estado);
        }
        if (has(cedula)) {
            fw.append(" AND p.cedula LIKE ? ");
            params.add("%" + cedula + "%");
        }
        if (tsDesde != null) {
            fw.append(" AND c.fecha_hora >= ? ");
            params.add(tsDesde);
        }
        if (tsHasta != null) {
            fw.append(" AND c.fecha_hora <= ? ");
            params.add(tsHasta);
        }
        if (has(q)) {
            fw.append(" AND (")
                    .append("  p.nombres LIKE ? OR p.apellidos LIKE ? ")
                    .append("  OR p.cedula LIKE ? ")
                    .append("  OR cs.nombre LIKE ? ")
                    .append("  OR c.observaciones LIKE ? ")
                    .append("  OR c.estado LIKE ? ")
                    .append(" ) ");
            String like = "%" + q + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        String SELECT
                = "SELECT c.id_cita, c.id_paciente, c.id_centro, c.fecha_hora, c.estado, c.observaciones, "
                + "       p.cedula AS paciente_cedula, p.nombres AS paciente_nombres, p.apellidos AS paciente_apellidos, "
                + "       cs.nombre AS centro_nombre ";
        String ORDER = " ORDER BY c.fecha_hora DESC ";
        String LIMIT = " LIMIT ? OFFSET ? ";

        List<Map<String, Object>> items = new ArrayList<>();
        long total = 0;

        try (Connection cn = Conexion.getConnection()) {
            // total
            try (PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) " + fw)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getLong(1);
                    }
                }
            }
            // page
            try (PreparedStatement ps = cn.prepareStatement(SELECT + fw + ORDER + LIMIT)) {
                List<Object> p2 = new ArrayList<>(params);
                p2.add(size);
                p2.add(offset);
                bind(ps, p2);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("id_cita", rs.getInt("id_cita"));
                        row.put("id_paciente", rs.getInt("id_paciente"));
                        row.put("id_centro", rs.getInt("id_centro"));
                        row.put("fecha_hora", fmtTS(rs.getTimestamp("fecha_hora")));
                        row.put("estado", rs.getString("estado"));
                        row.put("observaciones", rs.getString("observaciones"));
                        String pacNom = (safe(rs.getString("paciente_apellidos")) + " " + safe(rs.getString("paciente_nombres"))).trim();
                        row.put("pacienteNombre", pacNom);
                        row.put("pacienteCedula", rs.getString("paciente_cedula"));
                        row.put("centroNombre", rs.getString("centro_nombre"));
                        items.add(row);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error(resp, 500, "AdminCitasApiServlet/GET: " + ex.getMessage());
            return;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        ok(resp, data);
    }

    // ===== POST: crear ó cambiar estado (fallback) =====
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req)) {
            error(resp, 403, "No autorizado: se requiere rol Admin.");
            return;
        }

        Map<String, Object> m;
        try {
            m = GSON.fromJson(readBody(req), MAP_TYPE);
        } catch (Exception e) {
            error(resp, 400, "JSON inválido");
            return;
        }

        // Fallback de método: action=estado (lo usamos en el front para evitar PATCH)
        String action = toStr(m.get("action"));
        if ("estado".equalsIgnoreCase(action)) {
            Integer idCita = toInt(m.get("id_cita"));
            String estado = toStr(m.get("estado"));
            if (idCita == null || !has(estado)) {
                error(resp, 400, "id_cita y estado son obligatorios");
                return;
            }
            final String sql = "UPDATE citas SET estado=? WHERE id_cita=?";
            try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, estado);
                ps.setInt(2, idCita);
                int n = ps.executeUpdate();
                if (n == 0) {
                    error(resp, 404, "Cita no encontrada");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                error(resp, 500, "AdminCitasApiServlet/POST(estado): " + ex.getMessage());
                return;
            }
            ok(resp, Collections.singletonMap("patched", true));
            return;
        }

        // Crear cita
        Integer idPaciente = toInt(m.get("id_paciente"));
        Integer idCentro = toInt(m.get("id_centro"));
        String fechaHora = toStr(m.get("fecha_hora"));
        String estado = has(toStr(m.get("estado"))) ? toStr(m.get("estado")) : "pendiente";
        String obs = toStrOrNull(m.get("observaciones"));
        if (idPaciente == null || idCentro == null || !has(fechaHora)) {
            error(resp, 400, "id_paciente, id_centro y fecha_hora son obligatorios");
            return;
        }

        int newId = -1;
        final String sql = "INSERT INTO citas (id_paciente, id_centro, fecha_hora, estado, observaciones) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idPaciente);
            ps.setInt(2, idCentro);
            // normaliza "YYYY-MM-DD HH:mm" a segundos
            String fh = fechaHora.replace('T', ' ');
            if (fh.length() == 16) {
                fh = fh + ":00";
            }
            ps.setTimestamp(3, Timestamp.valueOf(fh));
            ps.setString(4, estado);
            if (obs == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, obs);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    newId = rs.getInt(1);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error(resp, 500, "AdminCitasApiServlet/POST: " + ex.getMessage());
            return;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id_cita", newId);
        ok(resp, out);
    }

    // ===== PUT: actualizar =====
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req)) {
            error(resp, 403, "No autorizado: se requiere rol Admin.");
            return;
        }

        Map<String, Object> m;
        try {
            m = GSON.fromJson(readBody(req), MAP_TYPE);
        } catch (Exception e) {
            error(resp, 400, "JSON inválido");
            return;
        }

        Integer idCita = toInt(m.get("id_cita"));
        Integer idPaciente = toInt(m.get("id_paciente"));
        Integer idCentro = toInt(m.get("id_centro"));
        String fechaHora = toStr(m.get("fecha_hora"));
        String estado = toStr(m.get("estado"));
        String obs = toStrOrNull(m.get("observaciones"));
        if (idCita == null) {
            error(resp, 400, "id_cita es obligatorio");
            return;
        }

        StringBuilder sb = new StringBuilder("UPDATE citas SET ");
        List<Object> params = new ArrayList<>();
        boolean first = true;

        if (idPaciente != null) {
            sb.append(first ? "" : " ,").append(" id_paciente=?");
            params.add(idPaciente);
            first = false;
        }
        if (idCentro != null) {
            sb.append(first ? "" : " ,").append(" id_centro=?");
            params.add(idCentro);
            first = false;
        }
        if (has(fechaHora)) {
            sb.append(first ? "" : " ,").append(" fecha_hora=?");
            String fh = fechaHora.replace('T', ' ');
            if (fh.length() == 16) {
                fh += ":00";
            }
            params.add(Timestamp.valueOf(fh));
            first = false;
        }
        if (has(estado)) {
            sb.append(first ? "" : " ,").append(" estado=?");
            params.add(estado);
            first = false;
        }
        if (m.containsKey("observaciones")) {
            sb.append(first ? "" : " ,").append(" observaciones=?");
            params.add(obs);
            first = false;
        }
        if (first) {
            ok(resp, Collections.singletonMap("unchanged", true));
            return;
        }

        sb.append(" WHERE id_cita=?");
        params.add(idCita);

        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                if (v == null) {
                    ps.setNull(i + 1, Types.VARCHAR);
                } else if (v instanceof Timestamp) {
                    ps.setTimestamp(i + 1, (Timestamp) v);
                } else if (v instanceof Integer) {
                    ps.setInt(i + 1, (Integer) v);
                } else {
                    ps.setObject(i + 1, v);
                }
            }
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            error(resp, 500, "AdminCitasApiServlet/PUT: " + ex.getMessage());
            return;
        }
        ok(resp, Collections.singletonMap("updated", true));
    }

    // ===== DELETE: eliminar =====
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req)) {
            error(resp, 403, "No autorizado: se requiere rol Admin.");
            return;
        }
        int id = intOr(req.getParameter("id"), -1);
        if (id <= 0) {
            error(resp, 400, "id es obligatorio");
            return;
        }

        final String sql = "DELETE FROM citas WHERE id_cita=?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int n = ps.executeUpdate();
            if (n == 0) {
                error(resp, 404, "Cita no encontrada");
                return;
            }
        } catch (SQLException ex) {
            error(resp, 409, "No se puede eliminar la cita: " + ex.getMessage());
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            error(resp, 500, "AdminCitasApiServlet/DELETE: " + ex.getMessage());
            return;
        }
        ok(resp, Collections.singletonMap("deleted", true));
    }
}

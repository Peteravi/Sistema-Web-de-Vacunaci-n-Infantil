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
 * API Representante/Tutor para CITAS (rol 5)
 *
 * GET /api/representante/citas -> listado (scoped por idTutor) params: q,
 * estado, cedula, desde, hasta, page, size GET /api/representante/citas?id=...
 * -> detalle (validando pertenencia)
 *
 * POST /api/representante/citas -> crear cita de un hijo del tutor payload: {
 * id_paciente|idPaciente, id_centro|idCentro, fecha_hora|fechaHora,
 * observaciones?, estado? }
 *
 * POST /api/representante/citas -> cambiar estado (fallback) payload: {
 * action:'estado', id_cita, estado }
 *
 * PUT /api/representante/citas -> actualizar campos de una cita del tutor
 * payload: { id_cita, id_paciente?, id_centro?, fecha_hora?, estado?,
 * observaciones? } (acepta camelCase)
 *
 * DELETE /api/representante/citas?id=... -> eliminar (solo si es su hijo)
 *
 * Respuesta OK: { ok:true, ... }
 */
@WebServlet(urlPatterns = {"/api/representante/citas"})
public class CitasApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    /* =========================
       Helpers genéricos
       ========================= */
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

    /**
     * bind seguro que respeta Timestamps
     */
    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object v = params.get(i);
            if (v == null) {
                ps.setNull(i + 1, Types.VARCHAR);
            } else if (v instanceof Timestamp) {
                ps.setTimestamp(i + 1, (Timestamp) v);
            } else if (v instanceof Integer) {
                ps.setInt(i + 1, (Integer) v);
            } else if (v instanceof Long) {
                ps.setLong(i + 1, (Long) v);
            } else {
                ps.setObject(i + 1, v);
            }
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String fmtTS(Timestamp ts) {
        if (ts == null) {
            return null;
        }
        return ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /**
     * Parse de fecha inicio (00:00 si solo fecha)
     */
    private static Timestamp parseStart(String s) {
        if (!has(s)) {
            return null;
        }
        s = s.replace('T', ' ').trim();
        try {
            if (s.length() == 10) { // yyyy-MM-dd
                return Timestamp.valueOf(LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay());
            }
            if (s.length() == 16) { // yyyy-MM-dd HH:mm
                return Timestamp.valueOf(LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return Timestamp.valueOf(s); // yyyy-MM-dd HH:mm:ss
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse de fecha fin (23:59:59 si solo fecha)
     */
    private static Timestamp parseEnd(String s) {
        if (!has(s)) {
            return null;
        }
        s = s.replace('T', ' ').trim();
        try {
            if (s.length() == 10) { // yyyy-MM-dd
                return Timestamp.valueOf(LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59, 59));
            }
            if (s.length() == 16) { // yyyy-MM-dd HH:mm
                return Timestamp.valueOf(LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return Timestamp.valueOf(s); // yyyy-MM-dd HH:mm:ss
        } catch (Exception e) {
            return null;
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

    private static Integer getIdTutor(HttpSession s) {
        return (s == null) ? null : (Integer) s.getAttribute("idTutor");
    }

    /**
     * Convierte cualquier cosa que venga del JSON a Integer con seguridad
     */
    private static Integer toInt(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof Long) {
            long v = (Long) o;
            if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE) {
                return null;
            }
            return (int) v;
        }
        if (o instanceof Double) {
            // Gson frecuentemente parsea números como Double
            double d = (Double) o;
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return null;
            }
            return (int) Math.floor(d);
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (Exception e) {
            return null;
        }
    }

    /* =========================
       GET
       ========================= */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        Integer idTutor = getIdTutor(s);
        if (idTutor == null) {
            error(resp, 401, "Sesión inválida o tutor no autenticado.");
            return;
        }

        // Detalle
        int id = intOr(req.getParameter("id"), -1);
        if (id > 0) {
            final String sql
                    = "SELECT c.id_cita, c.id_paciente, c.id_centro, c.fecha_hora, c.estado, c.observaciones, "
                    + "       p.nombres AS paciente_nombres, p.apellidos AS paciente_apellidos, p.cedula AS paciente_cedula, "
                    + "       cs.nombre AS centro_nombre "
                    + "FROM citas c "
                    + "JOIN pacientes p    ON p.id_paciente = c.id_paciente "
                    + "JOIN centros_salud cs ON cs.id_centro = c.id_centro "
                    + "WHERE c.id_cita=? AND p.id_tutor=?";
            try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setInt(2, idTutor);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        error(resp, 404, "Cita no encontrada");
                        return;
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id_cita", rs.getInt("id_cita"));
                    row.put("id_paciente", rs.getInt("id_paciente"));
                    row.put("id_centro", rs.getInt("id_centro"));
                    row.put("fecha_hora", fmtTS(rs.getTimestamp("fecha_hora")));
                    row.put("estado", rs.getString("estado"));
                    row.put("observaciones", rs.getString("observaciones"));
                    row.put("pacienteNombre", (safe(rs.getString("paciente_apellidos")) + " " + safe(rs.getString("paciente_nombres"))).trim());
                    row.put("pacienteCedula", rs.getString("paciente_cedula"));
                    row.put("centroNombre", rs.getString("centro_nombre"));
                    ok(resp, row);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                error(resp, 500, "CitasApiServlet/GET(id): " + ex.getMessage());
                return;
            }
        }

        // Listado
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
                .append(" WHERE p.id_tutor = ? ");
        List<Object> params = new ArrayList<>();
        params.add(idTutor);

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
                    .append(" p.nombres LIKE ? OR p.apellidos LIKE ? ")
                    .append(" OR p.cedula LIKE ? ")
                    .append(" OR cs.nombre LIKE ? ")
                    .append(" OR c.observaciones LIKE ? ")
                    .append(" OR c.estado LIKE ? ) ");
            String like = "%" + q + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        String SELECT = "SELECT c.id_cita, c.id_paciente, c.id_centro, c.fecha_hora, c.estado, c.observaciones, "
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
                        row.put("pacienteNombre", (safe(rs.getString("paciente_apellidos")) + " " + safe(rs.getString("paciente_nombres"))).trim());
                        row.put("pacienteCedula", rs.getString("paciente_cedula"));
                        row.put("centroNombre", rs.getString("centro_nombre"));
                        items.add(row);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error(resp, 500, "CitasApiServlet/GET: " + ex.getMessage());
            return;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        ok(resp, data);
    }

    /* =========================
       POST: crear o cambiar estado
       ========================= */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        Integer idTutor = getIdTutor(s);
        if (idTutor == null) {
            error(resp, 401, "Sesión inválida o tutor no autenticado.");
            return;
        }

        Map<String, Object> m;
        try {
            m = GSON.fromJson(readBody(req), MAP_TYPE);
        } catch (Exception e) {
            error(resp, 400, "JSON inválido");
            return;
        }

        // Fallback de método: action=estado
        String action = t((String) m.get("action"));
        if ("estado".equalsIgnoreCase(action)) {
            Integer idCita = toInt(m.get("id_cita"));
            String estado = t((String) m.get("estado"));
            if (idCita == null || !has(estado)) {
                error(resp, 400, "id_cita y estado son obligatorios");
                return;
            }

            final String chk
                    = "SELECT 1 FROM citas c JOIN pacientes p ON p.id_paciente=c.id_paciente WHERE c.id_cita=? AND p.id_tutor=?";
            try (Connection cn = Conexion.getConnection(); PreparedStatement ps1 = cn.prepareStatement(chk)) {
                ps1.setInt(1, idCita);
                ps1.setInt(2, idTutor);
                try (ResultSet rs = ps1.executeQuery()) {
                    if (!rs.next()) {
                        error(resp, 403, "No autorizado.");
                        return;
                    }
                }
                try (PreparedStatement ps2 = cn.prepareStatement("UPDATE citas SET estado=? WHERE id_cita=?")) {
                    ps2.setString(1, estado);
                    ps2.setInt(2, idCita);
                    ps2.executeUpdate();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                error(resp, 500, "CitasApiServlet/POST(estado): " + ex.getMessage());
                return;
            }
            ok(resp, Collections.singletonMap("patched", true));
            return;
        }

        // Crear (acepta snake_case y camelCase)
        Integer idPaciente = toInt(m.get("id_paciente") != null ? m.get("id_paciente") : m.get("idPaciente"));
        Integer idCentro = toInt(m.get("id_centro") != null ? m.get("id_centro") : m.get("idCentro"));
        String fechaHora = t((String) (m.get("fecha_hora") != null ? m.get("fecha_hora") : m.get("fechaHora")));
        String estado = has(t((String) m.get("estado"))) ? t((String) m.get("estado")) : "pendiente";
        String obs = t((String) m.get("observaciones"));

        if (idPaciente == null || idCentro == null || !has(fechaHora)) {
            error(resp, 400, "id_paciente, id_centro y fecha_hora son obligatorios");
            return;
        }

        // valida que el paciente pertenezca al tutor
        final String chkPac = "SELECT 1 FROM pacientes WHERE id_paciente=? AND id_tutor=?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps0 = cn.prepareStatement(chkPac)) {
            ps0.setInt(1, idPaciente);
            ps0.setInt(2, idTutor);
            try (ResultSet rs = ps0.executeQuery()) {
                if (!rs.next()) {
                    error(resp, 403, "Paciente no pertenece al tutor");
                    return;
                }
            }

            // normalizar fecha
            String fh = fechaHora.replace('T', ' ').trim();
            if (fh.length() == 16) {
                fh += ":00";
            }
            Timestamp ts;
            try {
                ts = Timestamp.valueOf(fh);
            } catch (IllegalArgumentException ex) {
                error(resp, 400, "Formato de fecha inválido (usa YYYY-MM-DD HH:mm o YYYY-MM-DD HH:mm:ss)");
                return;
            }

            int newId = -1;
            final String ins = "INSERT INTO citas (id_paciente, id_centro, fecha_hora, estado, observaciones) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = cn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idPaciente);
                ps.setInt(2, idCentro);
                ps.setTimestamp(3, ts);
                ps.setString(4, estado);
                if (!has(obs)) {
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
            }

            ok(resp, Collections.singletonMap("id_cita", newId));
        } catch (SQLException ex) {
            error(resp, 409, "Error de base de datos: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            error(resp, 500, "CitasApiServlet/POST: " + ex.getMessage());
        }
    }

    /* =========================
       PUT: actualizar
       ========================= */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        Integer idTutor = getIdTutor(s);
        if (idTutor == null) {
            error(resp, 401, "Sesión inválida o tutor no autenticado.");
            return;
        }

        Map<String, Object> m;
        try {
            m = GSON.fromJson(readBody(req), MAP_TYPE);
        } catch (Exception e) {
            error(resp, 400, "JSON inválido");
            return;
        }

        Integer idCita = toInt(m.get("id_cita") != null ? m.get("id_cita") : m.get("idCita"));
        Integer idPaciente = toInt(m.get("id_paciente") != null ? m.get("id_paciente") : m.get("idPaciente"));
        Integer idCentro = toInt(m.get("id_centro") != null ? m.get("id_centro") : m.get("idCentro"));
        String fechaHora = t((String) (m.get("fecha_hora") != null ? m.get("fecha_hora") : m.get("fechaHora")));
        String estado = t((String) m.get("estado"));
        String obs = t((String) m.get("observaciones"));

        if (idCita == null) {
            error(resp, 400, "id_cita es obligatorio");
            return;
        }

        final String chk = "SELECT p.id_tutor FROM citas c JOIN pacientes p ON p.id_paciente=c.id_paciente WHERE c.id_cita=?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps0 = cn.prepareStatement(chk)) {
            ps0.setInt(1, idCita);
            try (ResultSet rs = ps0.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != idTutor) {
                    error(resp, 403, "No autorizado.");
                    return;
                }
            }

            StringBuilder sb = new StringBuilder("UPDATE citas SET ");
            List<Object> params = new ArrayList<>();
            boolean first = true;

            if (idPaciente != null) {
                // valida pertenencia del nuevo paciente
                try (PreparedStatement psChk = cn.prepareStatement("SELECT 1 FROM pacientes WHERE id_paciente=? AND id_tutor=?")) {
                    psChk.setInt(1, idPaciente);
                    psChk.setInt(2, idTutor);
                    try (ResultSet rs = psChk.executeQuery()) {
                        if (!rs.next()) {
                            error(resp, 403, "Paciente no pertenece al tutor");
                            return;
                        }
                    }
                }
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
                String fh = fechaHora.replace('T', ' ').trim();
                if (fh.length() == 16) {
                    fh += ":00";
                }
                Timestamp ts;
                try {
                    ts = Timestamp.valueOf(fh);
                } catch (IllegalArgumentException ex) {
                    error(resp, 400, "Formato de fecha inválido");
                    return;
                }
                sb.append(first ? "" : " ,").append(" fecha_hora=?");
                params.add(ts);
                first = false;
            }
            if (has(estado)) {
                sb.append(first ? "" : " ,").append(" estado=?");
                params.add(estado);
                first = false;
            }
            if (m.containsKey("observaciones") || m.containsKey("obs")) {
                sb.append(first ? "" : " ,").append(" observaciones=?");
                params.add(has(obs) ? obs : null);
                first = false;
            }

            if (first) {
                ok(resp, Collections.singletonMap("unchanged", true));
                return;
            }

            sb.append(" WHERE id_cita=?");
            params.add(idCita);

            try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
                bind(ps, params);
                ps.executeUpdate();
            }
            ok(resp, Collections.singletonMap("updated", true));
        } catch (SQLException ex) {
            error(resp, 409, "Error de base de datos: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            error(resp, 500, "CitasApiServlet/PUT: " + ex.getMessage());
        }
    }

    /* =========================
       DELETE
       ========================= */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        Integer idTutor = getIdTutor(s);
        if (idTutor == null) {
            error(resp, 401, "Sesión inválida o tutor no autenticado.");
            return;
        }

        int id = intOr(req.getParameter("id"), -1);
        if (id <= 0) {
            error(resp, 400, "id es obligatorio");
            return;
        }

        final String chk = "SELECT p.id_tutor FROM citas c JOIN pacientes p ON p.id_paciente=c.id_paciente WHERE c.id_cita=?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps0 = cn.prepareStatement(chk)) {
            ps0.setInt(1, id);
            try (ResultSet rs = ps0.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != idTutor) {
                    error(resp, 403, "No autorizado.");
                    return;
                }
            }

            try (PreparedStatement ps = cn.prepareStatement("DELETE FROM citas WHERE id_cita=?")) {
                ps.setInt(1, id);
                int n = ps.executeUpdate();
                if (n == 0) {
                    error(resp, 404, "Cita no encontrada");
                    return;
                }
            }
            ok(resp, Collections.singletonMap("deleted", true));
        } catch (SQLException ex) {
            error(resp, 409, "No se puede eliminar la cita: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            error(resp, 500, "CitasApiServlet/DELETE: " + ex.getMessage());
        }
    }
}

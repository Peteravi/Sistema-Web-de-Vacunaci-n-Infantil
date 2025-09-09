package com.vacukids.controlador;

import com.google.gson.Gson;
import com.vacukids.dao.UsuarioDAO;
import com.vacukids.modelo.Usuario;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static com.vacukids.utils.Jsons.ok;
import static com.vacukids.utils.Jsons.error;
import com.vacukids.utils.PasswordUtils;
import com.vacukids.utils.Conexion;

@WebServlet("/api/admin/usuarios/*")
public class AdminUsuariosApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final Gson gson = new Gson();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // ------------------------ utils ------------------------
    private boolean isAdmin(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return false;
        }
        Object role = s.getAttribute("idRol");
        return (role instanceof Integer) && ((Integer) role) == 1;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJson(HttpServletRequest req) throws IOException {
        Map<String, Object> m = gson.fromJson(req.getReader(), Map.class);
        return (m == null) ? new LinkedHashMap<>() : m;
    }

    private static String str(Object... xs) {
        for (Object x : xs) {
            if (x == null) {
                continue;
            }
            String s = String.valueOf(x);
            return s;
        }
        return null;
    }

    private static Integer toInt(Object... xs) {
        for (Object x : xs) {
            if (x == null) {
                continue;
            }
            try {
                if (x instanceof Number) {
                    return ((Number) x).intValue();
                }
                String s = String.valueOf(x).trim();
                if (s.isEmpty()) {
                    continue;
                }
                return Integer.parseInt(s);
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private static Boolean toBool(Object... xs) {
        for (Object x : xs) {
            if (x == null) {
                continue;
            }
            if (x instanceof Boolean) {
                return (Boolean) x;
            }
            String s = String.valueOf(x).trim().toLowerCase();
            if (s.equals("true") || s.equals("1") || s.equals("on")) {
                return true;
            }
            if (s.equals("false") || s.equals("0") || s.equals("off")) {
                return false;
            }
        }
        return null;
    }

    // ------------------------ GET ------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req)) {
            error(resp, HttpServletResponse.SC_FORBIDDEN, "No autorizado");
            return;
        }
        String path = req.getPathInfo(); // null | "/" | "/{id}"
        try {
            if (path == null || "/".equals(path)) {
                // ===== Listado =====
                List<Map<String, Object>> out = new ArrayList<>();

                // MySQL / MariaDB (sin text blocks)
                final String sql
                        = "SELECT u.id_usuario, "
                        + "       u.usuario, "
                        + "       u.id_rol, "
                        + "       r.nombre AS rol, "
                        + "       u.id_personal, "
                        + "       u.id_tutor, "
                        + "       CASE WHEN u.id_personal IS NOT NULL THEN 'Personal' "
                        + "            WHEN u.id_tutor IS NOT NULL THEN 'Tutor' "
                        + "            ELSE '' END AS actor, "
                        + "       u.activo, "
                        + "       u.must_change_password, "
                        + "       u.intentos_fallidos, "
                        + "       DATE_FORMAT(u.creado_en, '%Y-%m-%d %H:%i:%s') AS creado_en "
                        + "  FROM usuarios u "
                        + "  LEFT JOIN roles r ON r.id_rol = u.id_rol "
                        + " ORDER BY u.id_usuario ASC";

                try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("idUsuario", rs.getInt("id_usuario"));
                        m.put("usuario", rs.getString("usuario"));
                        m.put("idRol", rs.getInt("id_rol"));
                        m.put("rol", rs.getString("rol"));

                        int p = rs.getInt("id_personal");
                        m.put("idPersonal", rs.wasNull() ? null : p);
                        int t = rs.getInt("id_tutor");
                        m.put("idTutor", rs.wasNull() ? null : t);

                        m.put("actor", rs.getString("actor"));
                        m.put("activo", rs.getInt("activo") == 1);
                        m.put("mustChangePassword", rs.getInt("must_change_password") == 1);
                        m.put("intentosFallidos", rs.getInt("intentos_fallidos"));
                        m.put("creadoEn", rs.getString("creado_en"));
                        out.add(m);
                    }
                }
                ok(resp, out);
                return;
            }

            // ===== Detalle por ID =====
            String pid = path.startsWith("/") ? path.substring(1) : path;
            Integer id = toInt(pid);
            if (id == null) {
                error(resp, HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
                return;
            }

            // MySQL / MariaDB (sin text blocks)
            final String sqlDet
                    = "SELECT u.id_usuario, "
                    + "       u.usuario, "
                    + "       u.id_rol, "
                    + "       r.nombre AS rol, "
                    + "       u.id_personal, "
                    + "       u.id_tutor, "
                    + "       CASE WHEN u.id_personal IS NOT NULL THEN 'Personal' "
                    + "            WHEN u.id_tutor IS NOT NULL THEN 'Tutor' "
                    + "            ELSE '' END AS actor, "
                    + "       u.activo, "
                    + "       u.must_change_password, "
                    + "       DATE_FORMAT(u.creado_en, '%Y-%m-%d %H:%i:%s') AS creado_en "
                    + "  FROM usuarios u "
                    + "  LEFT JOIN roles r ON r.id_rol = u.id_rol "
                    + " WHERE u.id_usuario = ? "
                    + " LIMIT 1";

            Map<String, Object> m = null;
            try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sqlDet)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        m = new LinkedHashMap<>();
                        m.put("idUsuario", rs.getInt("id_usuario"));
                        m.put("usuario", rs.getString("usuario"));
                        m.put("idRol", rs.getInt("id_rol"));
                        m.put("rol", rs.getString("rol"));

                        int p = rs.getInt("id_personal");
                        m.put("idPersonal", rs.wasNull() ? null : p);
                        int t = rs.getInt("id_tutor");
                        m.put("idTutor", rs.wasNull() ? null : t);

                        m.put("actor", rs.getString("actor"));
                        m.put("activo", rs.getInt("activo") == 1);
                        m.put("mustChangePassword", rs.getInt("must_change_password") == 1);
                        m.put("creadoEn", rs.getString("creado_en"));
                    }
                }
            }

            if (m == null) {
                error(resp, HttpServletResponse.SC_NOT_FOUND, "No encontrado");
                return;
            }

            ok(resp, m);
        } catch (Exception e) {
            error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage() == null ? "error" : e.getMessage());
        }
    }

    // ------------------------ POST (action=...) ------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req)) {
            error(resp, HttpServletResponse.SC_FORBIDDEN, "No autorizado");
            return;
        }
        try {
            Map<String, Object> body = readJson(req);
            String action = req.getParameter("action");
            if (action == null || action.isBlank()) {
                action = "crear";
            }

            switch (action) {
                case "crear": {
                    String usuario = str(body.get("usuario"));
                    String password = str(body.get("password"), body.get("contrasena"));
                    Integer idRol = toInt(body.get("idRol"), body.get("id_rol"));
                    Integer idPersonal = toInt(body.get("idPersonal"), body.get("id_personal"));
                    Integer idTutor = toInt(body.get("idTutor"), body.get("id_tutor"));
                    Boolean activo = toBool(body.get("activo"));
                    Boolean mustChange = toBool(body.get("mustChangePassword"), body.get("must_change_password"));

                    if (usuario == null || idRol == null) {
                        error(resp, HttpServletResponse.SC_BAD_REQUEST, "usuario e idRol son requeridos");
                        return;
                    }
                    usuario = usuario.trim().toLowerCase();

                    if (password == null || password.trim().isEmpty()) {
                        password = (usuario.length() >= 6 ? usuario.substring(0, 6) : "Temporal") + "123*";
                        mustChange = true;
                    }
                    if (idPersonal != null && idTutor != null) {
                        error(resp, HttpServletResponse.SC_BAD_REQUEST, "id_personal y id_tutor no pueden venir ambos a la vez");
                        return;
                    }

                    String hash = PasswordUtils.hash(password);
                    int id = usuarioDAO.crearUsuario(
                            usuario,
                            hash,
                            idRol,
                            idPersonal,
                            idTutor,
                            (activo != null) ? activo : true,
                            (mustChange != null) ? mustChange : true
                    );
                    if (id <= 0) {
                        error(resp, HttpServletResponse.SC_BAD_REQUEST, "No fue posible crear el usuario");
                        return;
                    }
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("idUsuario", id);
                    ok(resp, out);
                    return;
                }
                case "actualizarBasico": {
                    Integer id = toInt(body.get("idUsuario"), body.get("id_usuario"), body.get("id"));
                    if (id == null) {
                        error(resp, HttpServletResponse.SC_BAD_REQUEST, "idUsuario es requerido");
                        return;
                    }
                    String nuevoUsuario = str(body.get("usuario"));
                    Integer idRol = toInt(body.get("idRol"), body.get("id_rol"));
                    Boolean activo = toBool(body.get("activo"));
                    Boolean mustChange = toBool(body.get("mustChangePassword"), body.get("must_change_password"));

                    boolean updated = false;

                    if (nuevoUsuario != null && !nuevoUsuario.trim().isEmpty()) {
                        String sql = "UPDATE usuarios SET usuario=?, actualizado_en=NOW() WHERE id_usuario=?";
                        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
                            ps.setString(1, nuevoUsuario.trim().toLowerCase());
                            ps.setInt(2, id);
                            updated = ps.executeUpdate() > 0 || updated;
                        } catch (Exception ex) {
                            error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage() == null ? "error" : ex.getMessage());
                            return;
                        }
                    }

                    if (activo != null || mustChange != null || idRol != null) {
                        try {
                            updated = usuarioDAO.actualizarFlagsYRol(id, activo, mustChange, idRol) || updated;
                        } catch (Exception ex) {
                            error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage() == null ? "error" : ex.getMessage());
                            return;
                        }
                    }

                    if (!updated) {
                        error(resp, HttpServletResponse.SC_BAD_REQUEST, "No se actualizó ningún campo");
                        return;
                    }
                    ok(resp, Collections.singletonMap("idUsuario", id));
                    return;
                }
                case "setActivo": {
                    Integer id = toInt(body.get("idUsuario"), body.get("id_usuario"), body.get("id"));
                    Boolean activo = toBool(body.get("activo"));
                    if (id == null || activo == null) {
                        error(resp, HttpServletResponse.SC_BAD_REQUEST, "idUsuario y activo son requeridos");
                        return;
                    }
                    try {
                        boolean okUpd = usuarioDAO.actualizarFlagsYRol(id, activo, null, null);
                        if (!okUpd) {
                            error(resp, HttpServletResponse.SC_BAD_REQUEST, "No fue posible actualizar el estado");
                            return;
                        }
                    } catch (Exception ex) {
                        error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage() == null ? "error" : ex.getMessage());
                        return;
                    }
                    ok(resp, Collections.singletonMap("idUsuario", id));
                    return;
                }
                case "eliminar": {
                    Integer id = toInt(body.get("idUsuario"), body.get("id_usuario"), body.get("id"));
                    if (id == null) {
                        error(resp, HttpServletResponse.SC_BAD_REQUEST, "idUsuario es requerido");
                        return;
                    }
                    try {
                        boolean okUpd = usuarioDAO.actualizarFlagsYRol(id, false, null, null);
                        if (!okUpd) {
                            error(resp, HttpServletResponse.SC_BAD_REQUEST, "No fue posible eliminar (baja lógica)");
                            return;
                        }
                    } catch (Exception ex) {
                        error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage() == null ? "error" : ex.getMessage());
                        return;
                    }
                    ok(resp, Collections.singletonMap("idUsuario", id));
                    return;
                }
                default:
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, "action inválido");
                    return;
            }
        } catch (Exception e) {
            error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage() == null ? "error" : e.getMessage());
        }
    }

    // ------------------------ PUT /{id} (opcional) ------------------------
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req)) {
            error(resp, HttpServletResponse.SC_FORBIDDEN, "No autorizado");
            return;
        }
        String path = req.getPathInfo();
        if (path == null || path.length() <= 1) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, "ID requerido");
            return;
        }
        Integer id;
        try {
            id = Integer.parseInt(path.substring(1));
        } catch (Exception e) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
            return;
        }

        try {
            Map<String, Object> body = readJson(req);
            String nuevoUsuario = str(body.get("usuario"));
            Integer idRol = toInt(body.get("idRol"), body.get("id_rol"));
            Boolean activo = toBool(body.get("activo"));
            Boolean mustChange = toBool(body.get("mustChangePassword"), body.get("must_change_password"));
            String newPassword = str(body.get("newPassword"), body.get("new_password"), body.get("password"));

            boolean upd = false;

            if (nuevoUsuario != null && !nuevoUsuario.trim().isEmpty()) {
                String sql = "UPDATE usuarios SET usuario=?, actualizado_en=NOW() WHERE id_usuario=?";
                try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, nuevoUsuario.trim().toLowerCase());
                    ps.setInt(2, id);
                    upd = ps.executeUpdate() > 0 || upd;
                }
            }
            if (activo != null || mustChange != null || idRol != null) {
                upd = usuarioDAO.actualizarFlagsYRol(id, activo, mustChange, idRol) || upd;
            }
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                String hash = PasswordUtils.hash(newPassword.trim());
                upd = usuarioDAO.actualizarPassword(id, hash) || upd;
            }

            if (!upd) {
                error(resp, HttpServletResponse.SC_BAD_REQUEST, "No se actualizó ningún campo");
                return;
            }
            ok(resp, Collections.singletonMap("idUsuario", id));
        } catch (Exception e) {
            error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage() == null ? "error" : e.getMessage());
        }
    }
}

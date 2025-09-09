package com.vacukids.controlador;

import com.vacukids.dao.UsuarioDAO;
import com.vacukids.modelo.Usuario;
import com.vacukids.utils.PasswordUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Home por rol: 1=Admin, 2=Médico, 3=Enfermero, 4=Recepción, 5=Representante
    private static final Map<Integer, String> HOME_BY_ROLE = new HashMap<>();
    static {
        // ⚠️ Apunta a JSP visibles
        HOME_BY_ROLE.put(1, "/Pages/admin.jsp");
        HOME_BY_ROLE.put(2, "/Pages/medico.jsp");        // <- médico a medico.jsp
        HOME_BY_ROLE.put(3, "/Pages/enfermero.jsp");
        HOME_BY_ROLE.put(4, "/Pages/recepcion.jsp");
        HOME_BY_ROLE.put(5, "/Pages/representante.jsp");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            // Si ya está logueado, manda a su home
            HttpSession s = req.getSession(false);
            if (s != null && s.getAttribute("usuarioId") != null && s.getAttribute("idRol") != null) {
                Integer rol = (Integer) s.getAttribute("idRol");
                String home = HOME_BY_ROLE.getOrDefault(rol, "/");
                resp.sendRedirect(req.getContextPath() + home);
                return;
            }

            // Mostrar formulario de login (JSP pública)
            req.getRequestDispatcher("/Pages/login.jsp").forward(req, resp);

        } catch (ServletException e) {
            resp.sendError(500, e.getMessage() == null ? "Error" : e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        final String ctx = req.getContextPath();
        String usuario = param(req, "usuario");
        String password = req.getParameter("password");

        try {
            if (usuario == null || password == null) {
                redirectMsg(resp, ctx + "/Pages/login.jsp", "Credenciales requeridas");
                return;
            }

            usuario = usuario.trim().toLowerCase();
            Usuario u = usuarioDAO.buscarPorUsuario(usuario);

            // 1) No existe
            if (u == null) {
                redirectMsg(resp, ctx + "/Pages/login.jsp", "Usuario o contraseña inválidos");
                return;
            }

            // 2) PRIMER INGRESO / RESET (ignora 'activo' aquí)
            if (u.isMustChangePassword()) {
                if (!PasswordUtils.verify(password, u.getPasswordHash())) {
                    redirectMsg(resp, ctx + "/Pages/login.jsp", "Usuario o contraseña inválidos");
                    return;
                }
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();

                HttpSession s = req.getSession(true);
                // ✅ Usa SIEMPRE "usuarioId" (no "userId")
                s.setAttribute("usuarioId", u.getIdUsuario());   // sesión temporal para cambiar contraseña
                s.setAttribute("idRol", u.getIdRol());
                // (opcional) marca de sesión parcial
                s.setAttribute("authPartial", true);

                resp.sendRedirect(ctx + "/cambiar-password");
                return;
            }

            // 3) Flujo normal: debe estar activo
            if (!u.isActivo()) {
                redirectMsg(resp, ctx + "/Pages/login.jsp", "Tu cuenta está inactiva. Si es tu primer ingreso, cambia tu contraseña.");
                return;
            }
            if (!PasswordUtils.verify(password, u.getPasswordHash())) {
                redirectMsg(resp, ctx + "/Pages/login.jsp", "Usuario o contraseña inválidos");
                return;
            }

            // (opcional) auto-upgrade del hash si needsRehash
            // if (PasswordUtils.needsRehash(u.getPasswordHash())) {
            //     String newHash = PasswordUtils.hash(password);
            //     usuarioDAO.actualizarPassword(u.getIdUsuario(), newHash);
            // }

            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();

            HttpSession s = req.getSession(true);
            // ✅ atributos consistentes
            s.setAttribute("usuarioId", u.getIdUsuario());
            s.setAttribute("idRol", u.getIdRol());
            s.setAttribute("auth", true);
            if (u.getIdTutor() != null) s.setAttribute("idTutor", u.getIdTutor());

            // Redirección por rol
            String home = HOME_BY_ROLE.getOrDefault(u.getIdRol(), "/");
            resp.sendRedirect(ctx + home); // p.ej. /Pages/medico.jsp
        } catch (Exception e) {
            redirectMsg(resp, ctx + "/Pages/login.jsp",
                    "Error: " + (e.getMessage() == null ? "Error" : e.getMessage()));
        }
    }

    // ===================== Helpers =====================
    private static String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }

    private static void redirectMsg(HttpServletResponse resp, String url, String msg) throws IOException {
        if (msg == null || msg.isEmpty()) {
            resp.sendRedirect(url);
        } else {
            String enc = URLEncoder.encode(msg, StandardCharsets.UTF_8.name());
            String sep = url.contains("?") ? "&" : "?";
            resp.sendRedirect(url + sep + "msg=" + enc);
        }
    }
}

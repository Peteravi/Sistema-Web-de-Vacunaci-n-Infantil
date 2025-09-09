package com.vacukids.controlador;

import com.vacukids.dao.UsuarioDAO;
import com.vacukids.modelo.Usuario;
import com.vacukids.utils.PasswordUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/cambiar-password")
public class ChangePasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private static final Map<Integer, String> HOME_BY_ROLE = new HashMap<Integer, String>();

    static {
        HOME_BY_ROLE.put(1, "/admin");
        HOME_BY_ROLE.put(2, "/medico");
        HOME_BY_ROLE.put(3, "/enfermero");
        HOME_BY_ROLE.put(4, "/recepcion");
        HOME_BY_ROLE.put(5, "/representante");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Primer ingreso: se requiere sesión con userId (seteada en LoginServlet)
            HttpSession s = req.getSession(false);
            if (s == null || s.getAttribute("userId") == null) {
                // Si no hay sesión, redirige a login
                resp.sendRedirect(req.getContextPath() + "/login?msg=Inicia+sesion+para+cambiar+tu+contraseña");
                return;
            }
            // FORWARD CORRECTO a JSP protegido bajo /WEB-INF
            req.getRequestDispatcher("/WEB-INF/Pages/change_password.jsp").forward(req, resp);
        } catch (ServletException e) {
            resp.sendError(500, e.getMessage() == null ? "Error" : e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String ctx = req.getContextPath();

        // --- MODO B: desde el modal del login (correo + actual + nueva + repetir) ---
        // names del form del modal: usuario, actual, nueva, repetir
        String usuarioCorreo = trimOrNull(req.getParameter("usuario"));
        String passActual = req.getParameter("actual");  // sin trim
        String passNuevaB = req.getParameter("nueva");   // sin trim
        String passRepetirB = req.getParameter("repetir"); // sin trim

        if (usuarioCorreo != null && passActual != null && passNuevaB != null && passRepetirB != null) {
            if (!passNuevaB.equals(passRepetirB) || passNuevaB.length() < 8) {
                resp.sendRedirect(ctx + "/login?msg=La+nueva+contraseña+no+coincide+o+es+menor+a+8+caracteres");
                return;
            }
            try {
                Usuario u = usuarioDAO.buscarPorUsuario(usuarioCorreo);
                if (u == null || u.getPasswordHash() == null) {
                    resp.sendRedirect(ctx + "/login?msg=Usuario+no+encontrado");
                    return;
                }
                if (!PasswordUtils.verify(passActual, u.getPasswordHash())) {
                    resp.sendRedirect(ctx + "/login?msg=La+contraseña+actual+no+es+correcta");
                    return;
                }
                String hash = PasswordUtils.hash(passNuevaB);
                boolean ok = usuarioDAO.actualizarPasswordYHabilitar(u.getIdUsuario(), hash);
                if (!ok) {
                    resp.sendRedirect(ctx + "/login?msg=No+fue+posible+actualizar+tu+contraseña");
                    return;
                }
                resp.sendRedirect(ctx + "/login?changed=1");
                return;
            } catch (Exception e) {
                resp.sendRedirect(ctx + "/login?msg=" + urlEncode("Error: " + e.getMessage()));
                return;
            }
        }

        // --- MODO A: primer ingreso (desde página /cambiar-password con sesión userId) ---
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("userId") == null) {
            resp.sendRedirect(ctx + "/login?msg=Sesion+no+valida");
            return;
        }
        Integer userId = (Integer) s.getAttribute("userId");

        String passNuevaA = req.getParameter("newPassword");      // sin trim
        String passConfirmA = req.getParameter("confirmPassword");  // sin trim
        if (passNuevaA == null || passConfirmA == null || !passNuevaA.equals(passConfirmA) || passNuevaA.length() < 8) {
            resp.sendRedirect(ctx + "/cambiar-password?msg=Contraseña+inválida+o+no+coincide+(mínimo+8+caracteres)");
            return;
        }

        try {
            String hash = PasswordUtils.hash(passNuevaA);
            boolean ok = usuarioDAO.actualizarPasswordYHabilitar(userId, hash);
            if (!ok) {
                resp.sendRedirect(ctx + "/cambiar-password?msg=No+fue+posible+actualizar");
                return;
            }
            // Recupera usuario para redirigir según rol
            Usuario u = usuarioDAO.buscarPorId(userId);
            if (u == null) {
                resp.sendRedirect(ctx + "/login?msg=Vuelve+a+iniciar+sesión");
                return;
            }

            // Reabrir sesión "normal"
            s.invalidate();
            HttpSession s2 = req.getSession(true);
            // pon ambos nombres de atributo para compatibilidad con otros filtros
            s2.setAttribute("userId", u.getIdUsuario());
            s2.setAttribute("usuarioId", u.getIdUsuario());
            s2.setAttribute("idRol", u.getIdRol());
            if (u.getIdTutor() != null) {
                s2.setAttribute("idTutor", u.getIdTutor());
            }

            String home = HOME_BY_ROLE.getOrDefault(u.getIdRol(), "/");
            resp.sendRedirect(ctx + home);
        } catch (Exception e) {
            resp.sendRedirect(ctx + "/cambiar-password?msg=" + urlEncode("Error: " + e.getMessage()));
        }
    }

    private static String trimOrNull(String s) {
        return (s == null) ? null : (s.trim().isEmpty() ? null : s.trim());
    }

    private static String urlEncode(String s) {
        return s == null ? "" : s.replace(" ", "+");
    }
}

package com.vacukids.controlador;

import com.vacukids.dao.UsuarioDAO;
import com.vacukids.dao.UsuarioDAO.ResetToken;
import com.vacukids.modelo.Usuario;
import com.vacukids.utils.PasswordUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String token = req.getParameter("token");
        try {
            if (token == null || token.trim().isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/forgot-password?msg=Token+inválido");
                return;
            }
            ResetToken rt = usuarioDAO.obtenerTokenReset(token);
            if (rt == null || rt.usado || rt.expiraEn == null || LocalDateTime.now().isAfter(rt.expiraEn)) {
                resp.sendRedirect(req.getContextPath() + "/forgot-password?msg=Token+no+válido+o+expirado");
                return;
            }
            // Muestra formulario para nueva contraseña
            req.setAttribute("token", token);
            req.getRequestDispatcher("/Pages/reset_password.jsp").forward(req, resp);
        } catch (ServletException e) {
            resp.sendError(500, e.getMessage() == null ? "Error" : e.getMessage());
        } catch (Exception e) {
            resp.sendError(500, e.getMessage() == null ? "Error" : e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String ctx = req.getContextPath();
        String token = req.getParameter("token");
        String pass1 = val(req.getParameter("newPassword"));
        String pass2 = val(req.getParameter("confirmPassword"));

        try {
            if (token == null || token.trim().isEmpty()) {
                resp.sendRedirect(ctx + "/forgot-password?msg=Token+inválido");
                return;
            }
            if (pass1 == null || pass2 == null || !pass1.equals(pass2) || pass1.length() < 8) {
                resp.sendRedirect(ctx + "/reset-password?token=" + token + "&msg=Contraseña+inválida+o+no+coincide+(min+8)");
                return;
            }

            ResetToken rt = usuarioDAO.obtenerTokenReset(token);
            if (rt == null || rt.usado || rt.expiraEn == null || LocalDateTime.now().isAfter(rt.expiraEn)) {
                resp.sendRedirect(ctx + "/forgot-password?msg=Token+no+válido+o+expirado");
                return;
            }

            String hash = PasswordUtils.hash(pass1);
            boolean ok1 = usuarioDAO.actualizarPasswordYHabilitar(rt.userId, hash);
            boolean ok2 = usuarioDAO.marcarTokenUsado(token);

            if (!ok1 || !ok2) {
                resp.sendRedirect(ctx + "/reset-password?token=" + token + "&msg=No+fue+posible+actualizar");
                return;
            }

            // Opcional: iniciar sesión automáticamente
            Usuario u = usuarioDAO.buscarPorId(rt.userId);
            if (u != null && u.isActivo()) {
                HttpSession s = req.getSession(true);
                s.setAttribute("usuarioId", u.getIdUsuario());
                s.setAttribute("idRol", u.getIdRol());
                if (u.getIdTutor() != null) {
                    s.setAttribute("idTutor", u.getIdTutor());
                }
                resp.sendRedirect(ctx + homeByRole(u.getIdRol()));
                return;
            }

            resp.sendRedirect(ctx + "/login?msg=Contraseña+actualizada.+Inicia+sesión");

        } catch (Exception e) {
            resp.sendRedirect(ctx + "/reset-password?token=" + (token == null ? "" : token) + "&msg=Error");
        }
    }

    private static String val(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private static String homeByRole(Integer rol) {
        if (rol == null) {
            return "/";
        }
        switch (rol) {
            case 1:
                return "/admin";
            case 2:
                return "/medico";
            case 3:
                return "/enfermero";
            case 4:
                return "/recepcion";
            case 5:
                return "/representante";
            default:
                return "/";
        }
    }
}

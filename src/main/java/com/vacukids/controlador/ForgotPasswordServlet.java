package com.vacukids.controlador;

import com.vacukids.dao.UsuarioDAO;
import com.vacukids.modelo.Usuario;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final int TOKEN_BYTES = 32;           // 256 bits
    private static final int EXP_MINUTES = 30;           // vence en 30 min

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            req.getRequestDispatcher("/Pages/forgot_password.jsp").forward(req, resp);
        } catch (ServletException e) {
            resp.sendError(500, e.getMessage() == null ? "Error" : e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String ctx = req.getContextPath();
        String usuario = param(req, "usuario"); // correo

        try {
            if (usuario == null) {
                resp.sendRedirect(ctx + "/forgot-password?msg=Ingresa+tu+correo");
                return;
            }
            usuario = usuario.trim().toLowerCase();
            Usuario u = usuarioDAO.buscarPorUsuario(usuario);
            if (u == null || !u.isActivo()) {
                // Siempre devolvemos OK para no filtrar si existe o no (buena práctica)
                resp.sendRedirect(ctx + "/forgot-password?msg=Si+el+correo+existe+recibirás+un+enlace");
                return;
            }

            String token = generarTokenSeguro();
            LocalDateTime expira = LocalDateTime.now().plusMinutes(EXP_MINUTES);

            boolean ok = usuarioDAO.crearTokenReset(u.getIdUsuario(), token, expira);
            if (!ok) {
                resp.sendRedirect(ctx + "/forgot-password?msg=No+fue+posible+generar+enlace");
                return;
            }

            // Aquí enviarías el e-mail. Para desarrollo, mostramos el enlace por querystring:
            String link = ctx + "/reset-password?token=" + urlSafe(token);
            resp.sendRedirect(ctx + "/forgot-password?msg=Revisa+tu+correo.&dev=" + urlSafe(link));

        } catch (Exception e) {
            resp.sendRedirect(ctx + "/forgot-password?msg=Error");
        }
    }

    private static String param(HttpServletRequest req, String n) {
        String v = req.getParameter(n);
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }

    private static String generarTokenSeguro() {
        byte[] buf = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private static String urlSafe(String s) {
        return s.replace(" ", "%20");
    }
}

package com.vacukids.controlador;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;

@WebServlet("/representante")
public class RepresentanteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("usuarioId") == null) {
                resp.sendRedirect(req.getContextPath() + "/Pages/login.jsp?msg=Sesion%20expirada");
                return;
            }
            Object rolObj = session.getAttribute("idRol");
            if (!(rolObj instanceof Integer) || ((Integer) rolObj) != 5) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso restringido: requiere rol Representante (5).");
                return;
            }

            // No-cache para evitar back/forward mostrando viejo
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setDateHeader("Expires", 0);

            // JSP real está bajo /Pages
            req.getRequestDispatcher("/Pages/representante.jsp").forward(req, resp);

        } catch (ServletException e) {
            resp.sendError(500, e.getMessage() == null ? "Error al mostrar la página" : e.getMessage());
        } catch (Exception e) {
            resp.sendError(500, e.getMessage() == null ? "Error" : e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }
}

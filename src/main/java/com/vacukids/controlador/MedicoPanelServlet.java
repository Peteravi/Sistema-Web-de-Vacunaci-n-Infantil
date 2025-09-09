package com.vacukids.controlador;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Renderiza la vista JSP del panel del médico.
 * El JS del JSP llama a los endpoints del MedicoPanelApiServlet.
 */
@WebServlet(name = "MedicoPanelServlet", urlPatterns = {"/medico/panel"})
public class MedicoPanelServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Si tienes filtro de auth/roles, ya protegerá este path por rol=2 (médico).
        // Aquí solo despachamos la vista.
        req.getRequestDispatcher("/Pages/medico.jsp").forward(req, resp);
    }
}

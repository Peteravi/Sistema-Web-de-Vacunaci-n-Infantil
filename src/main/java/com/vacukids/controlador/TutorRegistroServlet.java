package com.vacukids.controlador;

import com.vacukids.dao.TutorDAO;
import com.vacukids.modelo.Tutor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet("/registro-tutor")
public class TutorRegistroServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(TutorRegistroServlet.class.getName());
    private final TutorDAO tutorDAO = new TutorDAO();

    // Cédula ecuatoriana: exactamente 10 dígitos
    private static final Pattern RE_CEDULA = Pattern.compile("^\\d{10}$");
    // Email razonable
    private static final Pattern RE_EMAIL = Pattern.compile("^[\\w.+\\-]+@([\\w\\-]+\\.)+[A-Za-z]{2,}$");

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Lee y normaliza
        String cedula = nz(req.getParameter("cedula")).trim();
        String nombres = nz(req.getParameter("nombres")).trim();
        String apellidos = nz(req.getParameter("apellidos")).trim();
        String direccion = nz(req.getParameter("direccion")).trim();
        String telefono = nz(req.getParameter("telefono")).trim();
        String correoRaw = nz(req.getParameter("correo")).trim();
        String correo = correoRaw.toLowerCase();

        // Log de depuración (solo en desarrollo)
        LOG.info(() -> String.format(
                "POST /registro-tutor; cedula='%s', nombres='%s', apellidos='%s', direccion='%s', telefono='%s', correo='%s'",
                cedula, nombres, apellidos, direccion, telefono, correo
        ));

        // Validaciones
        if (correo.isEmpty() || !RE_EMAIL.matcher(correo).matches()) {
            resp.sendError(400, "Correo electrónico inválido o vacío. Asegúrate de usar el campo 'correo'.");
            return;
        }
        if (!RE_CEDULA.matcher(cedula).matches()) {
            resp.sendError(400, "La cédula debe tener exactamente 10 dígitos.");
            return;
        }
        if (nombres.isEmpty() || apellidos.isEmpty()) {
            resp.sendError(400, "Nombres y apellidos son obligatorios.");
            return;
        }

        Tutor t = new Tutor();
        t.setCedula(cedula);
        t.setNombres(nombres);
        t.setApellidos(apellidos);
        t.setDireccion(direccion);
        t.setTelefono(telefono);
        t.setCorreo(correo);

        try {
            tutorDAO.registrarTutorYCuenta(t); // Debe: usuario = correo (lower), pass = BCrypt(cedula), rol Tutor, must_change_password=1
            resp.sendRedirect(req.getContextPath()
                    + "/login?msg=Cuenta creada. Ingresa con tu correo y tu cédula. Luego deberás cambiar la contraseña.");
        } catch (SQLException e) {
            String m = (e.getMessage() == null ? "" : e.getMessage().toLowerCase());
            if (m.contains("duplicate") || m.contains("uq_")) {
                resp.sendError(409, "Ya existe un tutor o usuario con esa cédula/correo.");
            } else {
                LOG.log(Level.SEVERE, "Error SQL registrando tutor", e);
                resp.sendError(500, "Error en el servidor al registrar al tutor.");
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error registrando tutor", ex);
            resp.sendError(500, "Error inesperado en el servidor.");
        }
    }
}

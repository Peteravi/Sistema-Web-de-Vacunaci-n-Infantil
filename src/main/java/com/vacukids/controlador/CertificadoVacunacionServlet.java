package com.vacukids.controlador;

import com.vacukids.dao.CertificadoDAO;
import com.vacukids.dto.CertVacunacion;
import com.vacukids.pdf.CertificadoPDF;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/api/certificados/vacunacion")
public class CertificadoVacunacionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String sId = req.getParameter("id_aplicacion");
        if (sId == null) {
            resp.sendError(400, "Falta id_aplicacion");
            return;
        }

        final int idAplicacion;
        try {
            idAplicacion = Integer.parseInt(sId);
        } catch (NumberFormatException e) {
            resp.sendError(400, "id_aplicacion inválido");
            return;
        }

        try (CertificadoDAO dao = new CertificadoDAO()) {
            CertVacunacion d = dao.findByAplicacionId(idAplicacion);
            if (d == null) {
                resp.sendError(404, "Aplicación no encontrada");
                return;
            }

            d.codigoVerificacion = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            String basePath = getServletContext().getRealPath("/");
            String verifyUrl = req.getScheme() + "://" + req.getServerName()
                    + (req.getServerPort() == 80 || req.getServerPort() == 443 ? "" : ":" + req.getServerPort())
                    + req.getContextPath() + "/certificados/verificar";

            byte[] pdf = CertificadoPDF.build(d, basePath, verifyUrl);

            resp.setContentType("application/pdf");
            String filename = ("certificado_" + d.cedulaPaciente + "_ap" + d.idAplicacion + ".pdf").replaceAll("\\s+", "_");
            resp.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
            resp.setContentLength(pdf.length);
            resp.getOutputStream().write(pdf);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, "Error generando certificado");
        }
    }
}

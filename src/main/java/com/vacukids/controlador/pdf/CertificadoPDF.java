package com.vacukids.pdf;

import com.vacukids.dto.CertVacunacion;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CertificadoPDF {

    public static byte[] build(CertVacunacion d, String basePath, String verifyUrl) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        // Fuentes (con fallback si la TTF no existe)
        PDFont font;
        PDFont fontBold;
        try {
            font = PDType0Font.load(doc, new File(basePath + "/WEB-INF/fonts/DejaVuSans.ttf"));
            fontBold = font;
        } catch (IOException ex) {
            font = PDType1Font.HELVETICA;
            fontBold = PDType1Font.HELVETICA_BOLD;
        }

        // Logo (opcional)
        PDImageXObject logo1 = null;
        File f1 = new File(basePath + "/img/logo_ug.png");
        if (f1.exists()) {
            logo1 = PDImageXObject.createFromFileByContent(f1, doc);
        }

        // QR
        String qrData = verifyUrl + "?aplicacion=" + d.idAplicacion + "&code=" + d.codigoVerificacion;
        PDImageXObject qrImg = toQR(doc, qrData, 180);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            float margin = 50;
            float y = page.getMediaBox().getHeight() - margin;

            // Encabezado
            if (logo1 != null) {
                cs.drawImage(logo1, margin, y - 70, 120, 60);
            }
            cs.beginText();
            cs.setFont(fontBold, 20);
            cs.newLineAtOffset(margin + 140, y - 30);
            cs.showText("VacuKids - Certificado de Vacunación");
            cs.endText();

            y -= 100;
            drawLine(cs, margin, y, page.getMediaBox().getWidth() - margin, y);

            y -= 30;
            cs.beginText();
            cs.setFont(fontBold, 14);
            cs.newLineAtOffset(margin, y);
            cs.showText("Se certifica que:");
            cs.endText();

            y -= 20;
            text(cs, font, 13, margin, y, "Paciente: " + safe(d.paciente) + "   C.I.: " + safe(d.cedulaPaciente));
            y -= 18;
            text(cs, font, 13, margin, y, "Representante: " + safe(d.tutor) + "   C.I.: " + safe(d.cedulaTutor));

            y -= 30;
            cs.beginText();
            cs.setFont(fontBold, 14);
            cs.newLineAtOffset(margin, y);
            cs.showText("Ha recibido la siguiente aplicación de vacuna:");
            cs.endText();

            y -= 22;
            text(cs, font, 13, margin, y, "Vacuna: " + safe(d.vacuna));
            y -= 18;
            text(cs, font, 13, margin, y, "Dosis: " + d.dosisNumero);
            y -= 18;
            String f = (d.fechaAplicacion != null) ? d.fechaAplicacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-";
            text(cs, font, 13, margin, y, "Fecha de aplicación: " + f);
            y -= 18;
            text(cs, font, 13, margin, y, "Centro de salud: " + safe(d.centro));
            y -= 18;
            text(cs, font, 13, margin, y, "Lote: " + safe(d.lote));
            y -= 18;
            text(cs, font, 13, margin, y, "Profesional responsable: " + safe(d.profesional)
                    + (d.correoProfesional != null ? " (" + d.correoProfesional + ")" : ""));

            y -= 30;
            drawLine(cs, margin, y, page.getMediaBox().getWidth() - margin, y);

            // Pie + QR
            y -= 160;
            if (qrImg != null) {
                cs.drawImage(qrImg, page.getMediaBox().getWidth() - margin - 140, y, 140, 140);
            }
            cs.beginText();
            cs.setFont(font, 11);
            cs.newLineAtOffset(margin, y + 120);
            cs.showText("Código de verificación: " + safe(d.codigoVerificacion));
            cs.endText();

            cs.beginText();
            cs.setFont(font, 10);
            cs.newLineAtOffset(margin, y + 100);
            cs.showText("Verifique este certificado escaneando el QR o visitando: " + verifyUrl);
            cs.endText();
        }

        byte[] bytes;
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            doc.save(baos);
            bytes = baos.toByteArray();
        } finally {
            doc.close();
        }
        return bytes;
    }

    private static void text(PDPageContentStream cs, PDFont font, int size, float x, float y, String s) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(s);
        cs.endText();
    }

    private static void drawLine(PDPageContentStream cs, float x1, float y1, float x2, float y2) throws IOException {
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    private static PDImageXObject toQR(PDDocument doc, String data, int size) throws IOException {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);
            var matrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);
            return PDImageXObject.createFromByteArray(doc, toPNG(img), "qr");
        } catch (WriterException e) {
            throw new IOException(e);
        }
    }

    private static byte[] toPNG(BufferedImage img) throws IOException {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        }
    }

    private static String safe(String s) {
        return (s == null ? "-" : s);
    }
}

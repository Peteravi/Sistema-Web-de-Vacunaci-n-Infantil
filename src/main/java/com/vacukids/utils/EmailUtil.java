package com.vacukids.utils;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {

    // LÉELOS DE VARIABLES DE ENTORNO O CONFIG LOCAL
    private static final String SMTP_HOST = System.getenv().getOrDefault("SMTP_HOST", "smtp.gmail.com");
    private static final String SMTP_PORT = System.getenv().getOrDefault("SMTP_PORT", "587");
    private static final String SMTP_USER = System.getenv().getOrDefault("SMTP_USER", "tu_correo@gmail.com");
    private static final String SMTP_PASS = System.getenv().getOrDefault("SMTP_PASS", "tu_clave_o_app_password");

    private static Session buildSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // TLS
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // ¡OJO! Usamos javax.mail.Authenticator y javax.mail.PasswordAuthentication
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        };

        return Session.getInstance(props, auth);
    }

    public static void enviar(String para, String asunto, String cuerpoHtml) throws MessagingException {
        Session session = buildSession();

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SMTP_USER, false));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(para));
        msg.setSubject(asunto, "UTF-8");
        msg.setContent(cuerpoHtml, "text/html; charset=UTF-8");

        Transport.send(msg);
    }

    public static void enviarCorreo(String correo, String código_de_verificación, String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}

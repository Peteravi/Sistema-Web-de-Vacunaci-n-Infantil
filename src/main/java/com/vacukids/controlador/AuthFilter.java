package com.vacukids.controlador;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebFilter("/*")
public class AuthFilter implements Filter {

    // ====== MODO DEV: permite acceder sin sesión a ciertas rutas ======
    private static final boolean DEV_MODE = true; // <-- CAMBIAR A false EN PRODUCCIÓN

    // Rutas exactas permitidas en DEV (por ejemplo tu JSP de panel)
    private static final Set<String> DEV_ALLOW_EXACT = new HashSet<>(Arrays.asList(
            "/Pages/medico.jsp" // ajusta si tu ruta es distinta
    ));

    // Prefijos permitidos en DEV para que el panel funcione sin login
    private static final List<String> DEV_ALLOW_PREFIX = new ArrayList<>(Arrays.asList(
            "/api/medico/panel", // overview, aplicaciones, detalle
            "/api/certificados" // <-- AÑADIDO: permite descargar PDF en DEV
    ));

    // ===== Prefijos protegidos por rol (producción) =====
    private static final Map<String, Integer> ROLE_BY_PATH = new LinkedHashMap<>();

    // Rutas públicas exactas (sin sesión)
    private static final Set<String> PUBLIC_EXACT = new HashSet<>();

    // Rutas públicas por prefijo (assets, etc.)
    private static final List<String> PUBLIC_PREFIX = new ArrayList<>();

    static {
        // --- Prefijos protegidos por rol ---
        ROLE_BY_PATH.put("/admin", 1);
        ROLE_BY_PATH.put("/api/admin", 1);

        ROLE_BY_PATH.put("/medico", 2);
        ROLE_BY_PATH.put("/api/medico", 2);

        ROLE_BY_PATH.put("/enfermero", 3);
        ROLE_BY_PATH.put("/api/enfermero", 3);

        ROLE_BY_PATH.put("/recepcion", 4);
        ROLE_BY_PATH.put("/api/recepcion", 4);

        ROLE_BY_PATH.put("/representante", 5);
        ROLE_BY_PATH.put("/api/representante", 5);

        // --- Públicas exactas ---
        PUBLIC_EXACT.add("/");
        PUBLIC_EXACT.add("/login");
        PUBLIC_EXACT.add("/Pages/login.jsp");
        PUBLIC_EXACT.add("/registro-tutor");
        PUBLIC_EXACT.add("/forgot");
        PUBLIC_EXACT.add("/forgot-password");
        PUBLIC_EXACT.add("/health");
        PUBLIC_EXACT.add("/favicon.ico");

        // --- Públicas por prefijo ---
        PUBLIC_PREFIX.add("/assets/");
        PUBLIC_PREFIX.add("/img/");
        PUBLIC_PREFIX.add("/css/");
        PUBLIC_PREFIX.add("/js/");
        PUBLIC_PREFIX.add("/webjars/");
        // Si expones verificación por QR como página pública, podrías permitir:
        // PUBLIC_PREFIX.add("/certificados/verificar");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        final String ctx = req.getContextPath();   // p.ej. /VacukidsWeb
        final String uri = req.getRequestURI();    // p.ej. /VacukidsWeb/Pages/medico.jsp
        final String path = uri.substring(ctx.length());

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        // --- Público (siempre)
        if (PUBLIC_EXACT.contains(path) || startsWithAny(path, PUBLIC_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        // --- DEV MODE: permitir panel y APIs necesarias sin sesión
        if (DEV_MODE && (DEV_ALLOW_EXACT.contains(path) || startsWithAny(path, DEV_ALLOW_PREFIX))) {
            chain.doFilter(request, response);
            return;
        }

        // ====== Validación de sesión ======
        HttpSession s = req.getSession(false);

        Object idUsuarioObj = (s == null) ? null : (s.getAttribute("idUsuario") != null
                ? s.getAttribute("idUsuario")
                : s.getAttribute("usuarioId"));

        Integer idRol = (s == null) ? null : (Integer) s.getAttribute("idRol");
        Integer idTutor = (s == null) ? null : (Integer) s.getAttribute("idTutor");

        boolean hasSession = (idUsuarioObj != null);
        boolean logged = hasSession && idRol != null;

        boolean wantsJson = wantsJsonOrPdf(req); // <-- actualizado

        if (path.startsWith("/cambiar-password")) {
            if (hasSession) {
                chain.doFilter(request, response);
                return;
            }
            sendUnauthorized(resp, wantsJson, ctx + "/login", "Sesión requerida.");
            return;
        }

        if (!logged) {
            sendUnauthorized(resp, wantsJson, ctx + "/login", "Sesión requerida.");
            return;
        }

        // ====== Autorización por rol ======
        for (Map.Entry<String, Integer> e : ROLE_BY_PATH.entrySet()) {
            String prefijo = e.getKey();
            Integer rolRequerido = e.getValue();

            if (path.startsWith(prefijo)) {
                if (!idRol.equals(rolRequerido)) {
                    sendForbidden(resp, wantsJson, "No autorizado para este recurso.");
                    return;
                }
                if (rolRequerido == 5 && idTutor == null) {
                    sendForbidden(resp, wantsJson, "Tutor no asociado a la sesión.");
                    return;
                }
                break; // autorizado
            }
        }

        chain.doFilter(request, response);
    }

    // ===== Helpers =====
    private static boolean startsWithAny(String path, List<String> prefixes) {
        for (String p : prefixes) {
            if (path.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    // Considera JSON y PDF como "respuesta directa" (evita redirect HTML en fetch)
    private boolean wantsJsonOrPdf(HttpServletRequest req) {
        String xrw = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        String ct = req.getContentType();
        return "XMLHttpRequest".equalsIgnoreCase(xrw)
                || (accept != null && (accept.toLowerCase().contains("application/json")
                || accept.toLowerCase().contains("application/pdf")))
                || (ct != null && ct.toLowerCase().contains("application/json"));
    }

    private void sendUnauthorized(HttpServletResponse resp, boolean jsonOrPdf, String loginUrl, String msg) throws IOException {
        if (jsonOrPdf) {
            // Para fetch: devolvemos 401 JSON, no redirección HTML
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"ok\":false,\"error\":\"" + esc(msg) + "\"}");
        } else {
            resp.sendRedirect(loginUrl + "?msg=" + urlEnc(msg));
        }
    }

    private void sendForbidden(HttpServletResponse resp, boolean json, String msg) throws IOException {
        if (json) {
            writeJson(resp, HttpServletResponse.SC_FORBIDDEN,
                    "{\"ok\":false,\"error\":\"" + esc(msg) + "\"}");
        } else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
        }
    }

    private static void writeJson(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.write(json);
        }
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String urlEnc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace(" ", "%20");
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}

package com.vacukids.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidades JSON + un GSON compartido con TypeAdapters para
 * LocalDate/LocalDateTime.
 */
public final class Jsons {

    private Jsons() {
    }

    // Formatos ISO legibles por el front
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;           // yyyy-MM-dd
    private static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;  // yyyy-MM-dd'T'HH:mm:ss

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    /* ===== Helpers de respuesta ===== */
    public static void ok(HttpServletResponse resp, Object body) throws IOException {
        writeJson(resp, HttpServletResponse.SC_OK, body);
    }

    public static void error(HttpServletResponse resp, int status, String message) throws IOException {
        Map<String, Object> m = new HashMap<>();
        m.put("ok", false);
        m.put("error", message);
        writeJson(resp, status, m);
    }

    public static void unauthorized(HttpServletResponse resp, String message) throws IOException {
        error(resp, HttpServletResponse.SC_UNAUTHORIZED, message == null ? "Unauthorized" : message);
    }

    public static void serverError(HttpServletResponse resp, String message) throws IOException {
        error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message == null ? "Server error" : message);
    }

    public static void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(body));
    }

    /* ===== Deserialización ===== */
    public static <T> T fromJson(Reader reader, Class<T> type) {
        return GSON.fromJson(reader, type);
    }

    public static <T> T fromJson(Reader reader, Type type) {
        return GSON.fromJson(reader, type);
    }

    public static <T> T fromJson(Reader reader, TypeToken<T> type) {
        return GSON.fromJson(reader, type.getType());
    }

    /* ===== Adapters ===== */
    public static final class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return JsonNull.INSTANCE;
            }
            return new JsonPrimitive(ISO_DATE.format(src)); // "yyyy-MM-dd"
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json.isJsonNull()) {
                return null;
            }
            String s = json.getAsString();
            if (s == null || s.trim().isEmpty()) {
                return null;
            }
            return LocalDate.parse(s, ISO_DATE);
        }
    }

    public static final class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return JsonNull.INSTANCE;
            }
            return new JsonPrimitive(ISO_DATETIME.format(src)); // "yyyy-MM-dd'T'HH:mm:ss"
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json.isJsonNull()) {
                return null;
            }
            String s = json.getAsString();
            if (s == null || s.trim().isEmpty()) {
                return null;
            }
            // Permite strings sin segundos también
            try {
                return LocalDateTime.parse(s, ISO_DATETIME);
            } catch (Exception ignore) {
                return LocalDateTime.parse(s);
            }
        }
    }
}

package com.vacukids.dao;

import com.vacukids.utils.Conexion;

import java.sql.*;
import java.time.*;
import java.util.*;

public class HorarioDAO {

    public static class HorarioCfg {

        public LocalTime inicio;
        public LocalTime fin;
        public int intervaloMin;
        public int capacidad;
    }

    /**
     * Obtiene configuración de horarios para el centro y día de semana (1..7
     * ISO)
     */
    public Optional<HorarioCfg> getConfig(int idCentro, int isoDayOfWeek) throws Exception {
        // Nota: si usaste 1=Lunes..7=Domingo al insertar, mapea DayOfWeek.MONDAY=1 ... SUNDAY=7
        String sql = "SELECT hora_inicio, hora_fin, intervalo_min, capacidad_slot "
                + "FROM horarios_centro WHERE id_centro=? AND dia_semana=? AND activo=1";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCentro);
            ps.setInt(2, isoDayOfWeek);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                HorarioCfg h = new HorarioCfg();
                h.inicio = rs.getTime(1).toLocalTime();
                h.fin = rs.getTime(2).toLocalTime();
                h.intervaloMin = rs.getInt(3);
                h.capacidad = rs.getInt(4);
                return Optional.of(h);
            }
        }
    }

    /**
     * Cuenta citas ocupadas por slot exacto de una fecha dada
     */
    public Map<LocalTime, Integer> ocupacionPorHora(int idCentro, LocalDate fecha) throws Exception {
        String sql = "SELECT TIME(fecha_hora) AS hh, COUNT(*) "
                + "FROM citas "
                + "WHERE id_centro=? AND DATE(fecha_hora)=? AND estado IN ('pendiente','confirmada') "
                + "GROUP BY TIME(fecha_hora)";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCentro);
            ps.setDate(2, java.sql.Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                Map<LocalTime, Integer> m = new HashMap<>();
                while (rs.next()) {
                    m.put(rs.getTime(1).toLocalTime(), rs.getInt(2));
                }
                return m;
            }
        }
    }

    /**
     * Genera slots disponibles para un centro/fecha
     */
    public List<LocalDateTime> slotsDisponibles(int idCentro, LocalDate fecha) throws Exception {
        DayOfWeek dow = fecha.getDayOfWeek(); // MON..SUN
        int iso = dow.getValue();            // 1..7

        // Config (o fallback)
        HorarioCfg cfg = getConfig(idCentro, iso).orElseGet(() -> {
            HorarioCfg f = new HorarioCfg();
            f.inicio = LocalTime.of(8, 0);
            f.fin = LocalTime.of(16, 0);
            f.intervaloMin = 15;
            f.capacidad = 1;
            return f;
        });

        Map<LocalTime, Integer> ocupados = ocupacionPorHora(idCentro, fecha);
        List<LocalDateTime> out = new ArrayList<>();

        LocalDateTime nowGye = LocalDateTime.now(ZoneId.of("America/Guayaquil"));

        for (LocalTime t = cfg.inicio; !t.isAfter(cfg.fin.minusMinutes(cfg.intervaloMin)); t = t.plusMinutes(cfg.intervaloMin)) {
            LocalDateTime slot = LocalDateTime.of(fecha, t);
            // No permitir slots pasados (si es hoy)
            if (!slot.isAfter(nowGye)) {
                continue;
            }
            int usados = ocupados.getOrDefault(t, 0);
            if (usados < cfg.capacidad) {
                out.add(slot);
            }
        }
        return out;
    }

    /**
     * ¿Queda cupo en ese slot exacto?
     */
    public boolean hayCupo(int idCentro, LocalDateTime fechaHora) throws Exception {
        LocalDate fecha = fechaHora.toLocalDate();
        LocalTime t = fechaHora.toLocalTime();

        // capacidad
        int iso = fecha.getDayOfWeek().getValue();
        int capacidad = getConfig(idCentro, iso).map(h -> h.capacidad).orElse(1);

        String sql = "SELECT COUNT(*) FROM citas "
                + "WHERE id_centro=? AND fecha_hora=? AND estado IN ('pendiente','confirmada')";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCentro);
            ps.setTimestamp(2, Timestamp.valueOf(fechaHora));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int usados = rs.getInt(1);
                return usados < capacidad;
            }
        }
    }
}

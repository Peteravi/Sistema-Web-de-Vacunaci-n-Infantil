package com.vacukids.dao;

import com.vacukids.modelo.Cita;
import com.vacukids.utils.Conexion;

import java.sql.*;
import java.util.*;

public class CitaDAO {

    /**
     * Item de listado para tabla del representante
     */
    public static class CitaItem {

        public Integer idCita;
        public Integer idPaciente;
        public Integer idCentro;
        public String fechaHora;    // ISO-8601
        public String estado;
        public String observaciones;
        public String paciente;     // "Apellidos, Nombres"
        public String centro;       // nombre centro
    }

    /**
     * Lista las citas de todos los pacientes del tutor
     */
    public List<CitaItem> listarPorTutor(int idTutor) throws SQLException {
        final String sql
                = "SELECT c.id_cita, c.id_paciente, c.id_centro, c.fecha_hora, c.estado, c.observaciones, "
                + "       p.nombres, p.apellidos, "
                + "       ce.nombre AS centro_nombre "
                + "FROM citas c "
                + "JOIN pacientes p ON p.id_paciente = c.id_paciente AND p.id_tutor = ? "
                + "JOIN centros_salud ce ON ce.id_centro = c.id_centro "
                + "ORDER BY c.fecha_hora DESC";

        List<CitaItem> out = new ArrayList<>();
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idTutor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CitaItem m = new CitaItem();
                    m.idCita = rs.getInt("id_cita");
                    m.idPaciente = rs.getInt("id_paciente");
                    m.idCentro = rs.getInt("id_centro");
                    Timestamp ts = rs.getTimestamp("fecha_hora");
                    m.fechaHora = (ts == null ? null : ts.toLocalDateTime().toString());
                    m.estado = rs.getString("estado");
                    m.observaciones = rs.getString("observaciones");
                    String nom = rs.getString("nombres");
                    String ape = rs.getString("apellidos");
                    m.paciente = ((ape == null ? "" : ape.trim()) + ", " + (nom == null ? "" : nom.trim())).trim();
                    m.centro = rs.getString("centro_nombre");
                    out.add(m);
                }
            }
        }
        return out;
    }

    /**
     * Crea cita verificando que el paciente pertenezca al tutor.
     *
     * @return id generado
     */
    public int crearParaTutor(Cita c, int idTutor) throws SQLException {
        if (c.getIdPaciente() == null || c.getIdCentro() == null || c.getFechaHora() == null) {
            throw new SQLException("Campos obligatorios faltantes");
        }

        final String sqlCheck
                = "SELECT 1 FROM pacientes WHERE id_paciente = ? AND id_tutor = ?";

        final String sqlInsert
                = "INSERT INTO citas (id_paciente, id_centro, fecha_hora, estado, observaciones) "
                + "VALUES (?,?,?,?,?)";

        try (Connection cn = Conexion.getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psCheck = cn.prepareStatement(sqlCheck)) {
                // 1) Verifica pertenencia
                psCheck.setInt(1, c.getIdPaciente());
                psCheck.setInt(2, idTutor);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Paciente no pertenece al tutor");
                    }
                }
            }

            // 2) Inserta cita
            try (PreparedStatement psIns = cn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                psIns.setInt(1, c.getIdPaciente());
                psIns.setInt(2, c.getIdCentro());
                psIns.setTimestamp(3, Timestamp.valueOf(c.getFechaHora())); // LocalDateTime → Timestamp
                psIns.setString(4, (c.getEstado() == null || c.getEstado().trim().isEmpty()) ? "pendiente" : c.getEstado());
                psIns.setString(5, c.getObservaciones());
                psIns.executeUpdate();

                int idGen = 0;
                try (ResultSet keys = psIns.getGeneratedKeys()) {
                    if (keys.next()) {
                        idGen = keys.getInt(1);
                    }
                }

                cn.commit();
                return idGen;
            } catch (SQLException e) {
                cn.rollback();
                // Si agregaste UNIQUE (id_paciente, fecha_hora) en MySQL, mapea mensaje amigable:
                String msg = e.getMessage();
                if (msg != null && msg.toLowerCase().contains("duplicate")) {
                    throw new SQLException("Ya existe una cita para el paciente en ese mismo horario (duplicado).");
                }
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public boolean actualizarEstadoParaTutor(int idCita, int idTutor, String estado) throws SQLException {
        final String sql
                = "UPDATE citas c "
                + "JOIN pacientes p ON p.id_paciente = c.id_paciente "
                + "SET c.estado = ? "
                + "WHERE c.id_cita = ? AND p.id_tutor = ?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idCita);
            ps.setInt(3, idTutor);
            return ps.executeUpdate() > 0;
        }
    }

}

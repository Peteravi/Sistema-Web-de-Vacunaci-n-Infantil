package com.vacukids.dao;

import com.vacukids.modelo.Paciente;
import com.vacukids.utils.Conexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    /**
     * DTO ligero para listar en el panel (evita LocalDate en JSON)
     */
    public static class PacienteMini {

        public Integer idPaciente;
        public String cedula;
        public String nombres;
        public String apellidos;
        public String fechaNacimiento; // yyyy-MM-dd
        public String sexo;
        public String telefono;
        public String direccion;

        public String nombreCompleto() {
            String n = (nombres == null ? "" : nombres.trim());
            String a = (apellidos == null ? "" : apellidos.trim());
            return (a + ", " + n).trim();
        }
    }

    /**
     * Lista pacientes del tutor (aislamiento por id_tutor)
     */
    public List<PacienteMini> listarPorTutor(int idTutor) throws SQLException {
        final String sql
                = "SELECT id_paciente, cedula, nombres, apellidos, "
                + "       fecha_nacimiento, sexo, telefono, direccion "
                + "FROM pacientes "
                + "WHERE id_tutor = ? "
                + "ORDER BY apellidos, nombres";

        List<PacienteMini> out = new ArrayList<PacienteMini>();
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idTutor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PacienteMini p = new PacienteMini();
                    p.idPaciente = rs.getInt("id_paciente");
                    p.cedula = rs.getString("cedula");
                    p.nombres = rs.getString("nombres");
                    p.apellidos = rs.getString("apellidos");
                    Date f = rs.getDate("fecha_nacimiento");
                    p.fechaNacimiento = (f == null ? null : f.toString()); // yyyy-MM-dd
                    p.sexo = rs.getString("sexo");
                    p.telefono = rs.getString("telefono");
                    p.direccion = rs.getString("direccion");
                    out.add(p);
                }
            }
        }
        return out;
    }

    /**
     * Inserta paciente asociado al tutor de sesión
     */
    public int crearParaTutor(Paciente p, int idTutor) throws SQLException {
        final String sql
                = "INSERT INTO pacientes "
                + "(cedula, nombres, apellidos, fecha_nacimiento, sexo, direccion, telefono, id_tutor) "
                + "VALUES (?,?,?,?,?,?,?,?)";

        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getCedula());
            ps.setString(2, p.getNombres());
            ps.setString(3, p.getApellidos());

            java.sql.Date fechaSql = null;
            LocalDate ld = p.getFechaNacimiento();
            if (ld != null) {
                fechaSql = java.sql.Date.valueOf(ld);
            }
            ps.setDate(4, fechaSql);

            ps.setString(5, p.getSexo());
            ps.setString(6, p.getDireccion());
            ps.setString(7, p.getTelefono());
            ps.setInt(8, idTutor);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    /**
     * (Opcional) Verifica pertenencia de un paciente a un tutor
     */
    public boolean perteneceATutor(int idPaciente, int idTutor) throws Exception {
        final String sql = "SELECT COUNT(*) FROM pacientes WHERE id_paciente = ? AND id_tutor = ?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            ps.setInt(2, idTutor);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }
}

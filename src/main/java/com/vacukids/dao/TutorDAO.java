package com.vacukids.dao;

import com.vacukids.modelo.Tutor;
import com.vacukids.utils.Conexion;
import com.vacukids.utils.PasswordUtils;

import java.sql.*;

public class TutorDAO {

    /**
     * Inserta un tutor y crea su usuario asociado en una sola transacción: -
     * usuarios.usuario = correo (lowercase) - usuarios.password_hash =
     * BCrypt(cedula) - usuarios.id_rol = (SELECT id_rol FROM roles WHERE
     * nombre='Tutor') - usuarios.must_change_password = 1
     */
    public int registrarTutorYCuenta(Tutor t) throws Exception {
        String sqlTutor = "INSERT INTO tutores "
                + " (cedula, nombres, apellidos, direccion, telefono, correo, activo, creado_en) "
                + " VALUES (?, ?, ?, ?, ?, ?, 1, NOW())";

        try (Connection cn = Conexion.getConnection()) {
            cn.setAutoCommit(false);

            try {
                // 1) Insertar tutor
                int idTutor;
                try (PreparedStatement ps = cn.prepareStatement(sqlTutor, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, t.getCedula());      // CHECK 10 dígitos en BD
                    ps.setString(2, t.getNombres());
                    ps.setString(3, t.getApellidos());
                    ps.setString(4, nullIfEmpty(t.getDireccion()));
                    ps.setString(5, nullIfEmpty(t.getTelefono()));
                    ps.setString(6, t.getCorreo().toLowerCase()); // UNIQUE en BD
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("No se generó id_tutor");
                        }
                        idTutor = rs.getInt(1);
                    }
                }

                // 2) Crear usuario para ese tutor
                crearUsuarioTutor(cn, idTutor, t.getCorreo(), t.getCedula());

                cn.commit();
                return idTutor;

            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void crearUsuarioTutor(Connection cn, int idTutor, String correo, String cedula) throws Exception {
        int idRolTutor = obtenerIdRolTutor(cn); // nombre='Tutor' (consulta al rol)  ← ver cita
        String usuario = correo.trim().toLowerCase();
        String hash = PasswordUtils.hash(cedula); // contraseña inicial = cédula (hasheada)

        String sqlUser = "INSERT INTO usuarios "
                + " (usuario, password_hash, id_rol, id_personal, id_tutor, activo, must_change_password, intentos_fallidos, creado_en) "
                + " VALUES (?, ?, ?, NULL, ?, 1, 1, 0, NOW())";

        try (PreparedStatement ps = cn.prepareStatement(sqlUser)) {
            ps.setString(1, usuario);
            ps.setString(2, hash);
            ps.setInt(3, idRolTutor);
            ps.setInt(4, idTutor);
            ps.executeUpdate();
        }
    }

    private int obtenerIdRolTutor(Connection cn) throws Exception {
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT id_rol FROM roles WHERE id_rol = 5")) { // ← usa 'Tutor'
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Rol 'Tutor' no existe");
                }
                return rs.getInt(1);
            }
        }
    }

    private static String nullIfEmpty(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

package com.vacukids.dao;

import com.vacukids.modelo.Usuario;
import com.vacukids.utils.Conexion;

import java.sql.*;
import java.time.LocalDateTime;

public class UsuarioDAO {

    // ====== Mapeo básico ======
    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setUsuario(rs.getString("usuario"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setIdRol(rs.getInt("id_rol"));

        int idPers = rs.getInt("id_personal");
        u.setIdPersonal(rs.wasNull() ? null : idPers);

        int idTut = rs.getInt("id_tutor");
        u.setIdTutor(rs.wasNull() ? null : idTut);

        u.setActivo(rs.getInt("activo") == 1);
        u.setMustChangePassword(rs.getInt("must_change_password") == 1);
        u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        return u;
    }

    // ====== Búsquedas ======
    /**
     * Busca por usuario (correo) exacto; normaliza a minúsculas
     */
    // src/main/java/com/vacukids/dao/UsuarioDAO.java
    public Usuario buscarPorUsuario(String usuario) throws Exception {
        if (usuario == null) {
            return null;
        }
        String uNorm = usuario.trim().toLowerCase();

        String sql = "SELECT id_usuario, usuario, password_hash, id_rol, id_personal, id_tutor, "
                + "       activo, must_change_password, intentos_fallidos "
                + "FROM usuarios WHERE LOWER(usuario) = ? LIMIT 1";

        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, uNorm);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }

    /**
     * Busca por ID
     */
    public Usuario buscarPorId(int idUsuario) throws Exception {
        String sql = "SELECT id_usuario, usuario, password_hash, id_rol, id_personal, id_tutor, "
                + "       activo, must_change_password, intentos_fallidos "
                + "FROM usuarios WHERE id_usuario = ? LIMIT 1";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }

    // ====== Actualizaciones ======
    /**
     * Actualiza password y HABILITA al usuario: - password_hash = newHash -
     * must_change_password = 0 - activo = 1 - intentos_fallidos = 0 -
     * actualizado_en = NOW()
     *
     * Úsalo tras /cambiar-password o un reset exitoso.
     */
    public boolean actualizarPasswordYHabilitar(int userId, String newHash) throws Exception {
        String sql = "UPDATE usuarios "
                + "SET password_hash = ?, "
                + "    must_change_password = 0, "
                + "    activo = 1, "
                + "    intentos_fallidos = 0, "
                + "    actualizado_en = NOW() "
                + "WHERE id_usuario = ?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }

    /**
     * Actualiza password y deja must_change_password como esté (por si lo
     * necesitas en admin)
     */
    public boolean actualizarPassword(int userId, String newHash) throws Exception {
        String sql = "UPDATE usuarios SET password_hash = ?, actualizado_en = NOW() WHERE id_usuario = ?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }

    /**
     * Cambia flags activo / must_change_password / rol (admin)
     */
    public boolean actualizarFlagsYRol(int idUsuario, Boolean activo, Boolean mustChange, Integer idRol) throws Exception {
        StringBuilder sb = new StringBuilder("UPDATE usuarios SET ");
        boolean first = true;
        if (activo != null) {
            sb.append("activo=").append(activo ? "1" : "0");
            first = false;
        }
        if (mustChange != null) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("must_change_password=").append(mustChange ? "1" : "0");
            first = false;
        }
        if (idRol != null) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("id_rol=").append(idRol);
            first = false;
        }
        sb.append(", actualizado_en=NOW() WHERE id_usuario=?");
        String sql = sb.toString();

        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }

    // ====== Creación de usuario (admin) ======
    public int crearUsuario(String usuario, String hash, int idRol, Integer idPersonal, Integer idTutor,
            boolean activo, boolean mustChangePassword) throws Exception {
        String sql = "INSERT INTO usuarios(usuario, password_hash, id_rol, id_personal, id_tutor, activo, must_change_password, intentos_fallidos, creado_en, actualizado_en) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, 0, NOW(), NOW())";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.trim().toLowerCase());
            ps.setString(2, hash);
            ps.setInt(3, idRol);

            if (idPersonal == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, idPersonal);
            }
            if (idTutor == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, idTutor);
            }

            ps.setInt(6, activo ? 1 : 0);
            ps.setInt(7, mustChangePassword ? 1 : 0);

            int n = ps.executeUpdate();
            if (n <= 0) {
                return 0;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }

    // ====== Password reset tokens ======
    public static class ResetToken {

        public String token;
        public int userId;
        public LocalDateTime expiraEn;
        public boolean usado;
    }

    public boolean crearTokenReset(int userId, String token, LocalDateTime expira) throws Exception {
        String sql = "INSERT INTO password_resets(token, user_id, expira_en, usado, creado_en) VALUES(?, ?, ?, 0, NOW())";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setInt(2, userId);
            ps.setTimestamp(3, Timestamp.valueOf(expira));
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }

    public ResetToken obtenerTokenReset(String token) throws Exception {
        String sql = "SELECT token, user_id, expira_en, usado FROM password_resets WHERE token=? LIMIT 1";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ResetToken rt = new ResetToken();
                rt.token = rs.getString("token");
                rt.userId = rs.getInt("user_id");
                Timestamp ts = rs.getTimestamp("expira_en");
                rt.expiraEn = ts == null ? null : ts.toLocalDateTime();
                rt.usado = rs.getInt("usado") == 1;
                return rt;
            }
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }

    public boolean marcarTokenUsado(String token) throws Exception {
        String sql = "UPDATE password_resets SET usado=1 WHERE token=?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }
}

package com.vacukids.dao;

import com.vacukids.dto.CertVacunacion;
import com.vacukids.utils.Conexion;

import java.sql.*;
import java.time.LocalDate;

public class CertificadoDAO implements AutoCloseable {

    private final Connection cn;

    public CertificadoDAO() throws SQLException {
        this.cn = Conexion.getConnection();
    }

    public CertVacunacion findByAplicacionId(int idAplicacion) throws SQLException {
        final String sql
                = "SELECT a.id_aplicacion,\n"
                + "       p.cedula AS cedula_paciente,\n"
                + "       CONCAT(p.nombres,' ',p.apellidos) AS paciente,\n"
                + "       t.cedula AS cedula_tutor,\n"
                + "       CONCAT(t.nombres,' ',t.apellidos) AS tutor,\n"
                + "       v.nombre AS vacuna,\n"
                + "       c.nombre AS centro,\n"
                + "       a.fecha_aplicacion,\n"
                + "       a.dosis_numero,\n"
                + "       a.efectos_secundarios,\n"
                + "       lv.lote_codigo,\n"
                + "       CONCAT(pe.nombres,' ',pe.apellidos) AS profesional,\n"
                + "       pe.correo AS correo_profesional\n"
                + "FROM aplicaciones a\n"
                + "JOIN pacientes p        ON p.id_paciente = a.id_paciente\n"
                + "LEFT JOIN tutores t     ON t.id_tutor    = p.id_tutor\n"
                + "JOIN vacunas v          ON v.id_vacuna   = a.id_vacuna\n"
                + "JOIN centros_salud c    ON c.id_centro   = a.id_centro\n"
                + "LEFT JOIN lotes_vacunas lv ON lv.id_lote = a.id_lote\n"
                + "LEFT JOIN personal pe      ON pe.id_personal = a.id_profesional\n"
                + "WHERE a.id_aplicacion = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAplicacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                CertVacunacion x = new CertVacunacion();
                x.idAplicacion = rs.getInt("id_aplicacion");
                x.paciente = rs.getString("paciente");
                x.cedulaPaciente = rs.getString("cedula_paciente");
                x.tutor = rs.getString("tutor");
                x.cedulaTutor = rs.getString("cedula_tutor");
                x.vacuna = rs.getString("vacuna");
                x.centro = rs.getString("centro");
                java.sql.Date f = rs.getDate("fecha_aplicacion");
                x.fechaAplicacion = (f != null ? f.toLocalDate() : null);
                x.dosisNumero = rs.getInt("dosis_numero");
                x.lote = rs.getString("lote_codigo");
                x.profesional = rs.getString("profesional");
                x.correoProfesional = rs.getString("correo_profesional");
                return x;
            }
        }
    }

    @Override
    public void close() throws SQLException {
        if (cn != null) {
            cn.close();
        }
    }
}

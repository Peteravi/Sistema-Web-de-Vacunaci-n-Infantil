package com.vacukids.dao;

import com.vacukids.modelo.LoteVacuna;
import com.vacukids.utils.Conexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoteDAO {

    private LoteVacuna map(ResultSet rs) throws SQLException {
        LoteVacuna l = new LoteVacuna();
        l.setIdLote(rs.getInt("id_lote"));
        l.setIdVacuna(rs.getInt("id_vacuna"));
        l.setVacuna(rs.getString("vacuna"));
        l.setLoteCodigo(rs.getString("lote_codigo"));
        Date ff = rs.getDate("fecha_fabricacion");
        Date fv = rs.getDate("fecha_vencimiento");
        l.setFechaFabricacion(ff != null ? ff.toLocalDate() : null);
        l.setFechaVencimiento(fv != null ? fv.toLocalDate() : null);
        l.setProveedor(rs.getString("proveedor"));
        Date hoy = new Date(System.currentTimeMillis());
        l.setVigente(fv == null || !fv.before(hoy));
        return l;
    }

    public List<LoteVacuna> listar(Integer idVacuna, String vigencia) throws Exception {
        String sql = "SELECT l.id_lote, l.id_vacuna, v.nombre AS vacuna, l.lote_codigo, "
                + "l.fecha_fabricacion, l.fecha_vencimiento, l.proveedor "
                + "FROM lotes_vacunas l JOIN vacunas v ON v.id_vacuna = l.id_vacuna ";
        List<Object> params = new ArrayList<>();
        List<String> cond = new ArrayList<>();

        if (idVacuna != null) {
            cond.add("l.id_vacuna = ?");
            params.add(idVacuna);
        }
        if (vigencia != null && !vigencia.isBlank()) {
            if ("vigente".equalsIgnoreCase(vigencia)) {
                cond.add("(l.fecha_vencimiento IS NULL OR l.fecha_vencimiento >= CURRENT_DATE())");
            } else if ("vencido".equalsIgnoreCase(vigencia)) {
                cond.add("(l.fecha_vencimiento IS NOT NULL AND l.fecha_vencimiento < CURRENT_DATE())");
            }
        }
        if (!cond.isEmpty()) {
            sql += " WHERE " + String.join(" AND ", cond);
        }
        sql += " ORDER BY v.nombre, l.lote_codigo";

        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            for (Object p : params) {
                ps.setObject(i++, p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<LoteVacuna> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        }
    }

    public List<LoteVacuna> listarTodos() throws Exception {
        return listar(null, null);
    }

    public int crear(LoteVacuna l) throws Exception {
        String sql = "INSERT INTO lotes_vacunas(id_vacuna, lote_codigo, fecha_fabricacion, fecha_vencimiento, proveedor) "
                + "VALUES (?,?,?,?,?)";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, l.getIdVacuna());
            ps.setString(2, l.getLoteCodigo());
            if (l.getFechaFabricacion() != null) {
                ps.setDate(3, Date.valueOf(l.getFechaFabricacion()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            if (l.getFechaVencimiento() != null) {
                ps.setDate(4, Date.valueOf(l.getFechaVencimiento()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, l.getProveedor());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean editar(LoteVacuna l) throws Exception {
        String sql = "UPDATE lotes_vacunas SET id_vacuna=?, lote_codigo=?, fecha_fabricacion=?, fecha_vencimiento=?, proveedor=? "
                + "WHERE id_lote=?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, l.getIdVacuna());
            ps.setString(2, l.getLoteCodigo());
            if (l.getFechaFabricacion() != null) {
                ps.setDate(3, Date.valueOf(l.getFechaFabricacion()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            if (l.getFechaVencimiento() != null) {
                ps.setDate(4, Date.valueOf(l.getFechaVencimiento()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, l.getProveedor());
            ps.setInt(6, l.getIdLote());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws Exception {
        String sql = "DELETE FROM lotes_vacunas WHERE id_lote=?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}

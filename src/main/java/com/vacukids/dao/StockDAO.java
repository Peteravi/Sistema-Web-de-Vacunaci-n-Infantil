package com.vacukids.dao;

import com.vacukids.modelo.StockItem;
import com.vacukids.utils.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    public List<StockItem> listar(Integer idCentro, Integer idVacuna) throws Exception {
        String sql = "SELECT sc.id_lote, sc.id_centro, c.nombre AS centro, v.nombre AS vacuna, "
                + "       l.lote_codigo, sc.cantidad_disponible "
                + "FROM stock_centro sc "
                + "JOIN centros_salud c ON c.id_centro = sc.id_centro "
                + "JOIN lotes_vacunas l ON l.id_lote = sc.id_lote "
                + "JOIN vacunas v ON v.id_vacuna = l.id_vacuna ";
        List<String> cond = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (idCentro != null) {
            cond.add("sc.id_centro=?");
            params.add(idCentro);
        }
        if (idVacuna != null) {
            cond.add("l.id_vacuna=?");
            params.add(idVacuna);
        }
        if (!cond.isEmpty()) {
            sql += " WHERE " + String.join(" AND ", cond);
        }
        sql += " ORDER BY c.nombre, v.nombre, l.lote_codigo";

        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            for (Object p : params) {
                ps.setObject(i++, p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<StockItem> out = new ArrayList<>();
                while (rs.next()) {
                    StockItem s = new StockItem();
                    s.setIdLote(rs.getInt("id_lote"));
                    s.setIdCentro(rs.getInt("id_centro"));
                    s.setCentro(rs.getString("centro"));
                    s.setVacuna(rs.getString("vacuna"));
                    s.setLoteCodigo(rs.getString("lote_codigo"));
                    s.setCantidadDisponible(rs.getInt("cantidad_disponible"));
                    out.add(s);
                }
                return out;
            }
        }
    }

    /**
     * Inserta o actualiza cantidad (depende del UNIQUE(id_centro,id_lote))
     */
    public void upsert(int idCentro, int idLote, int cantidad) throws Exception {
        String sql = "INSERT INTO stock_centro(id_centro, id_lote, cantidad_disponible) VALUES (?,?,?) "
                + "ON DUPLICATE KEY UPDATE cantidad_disponible = VALUES(cantidad_disponible)";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCentro);
            ps.setInt(2, idLote);
            ps.setInt(3, cantidad);
            ps.executeUpdate();
        }
    }
}

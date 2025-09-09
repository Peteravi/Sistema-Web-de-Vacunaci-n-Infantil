package com.vacukids.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/vacukids?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "piteravi07";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver de MySQL", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Método de prueba
    public static void main(String[] args) {
        try (Connection cn = Conexion.getConnection()) {
            if (cn != null && !cn.isClosed()) {
                System.out.println("✅ Conexión exitosa a la base de datos: " + URL);
            } else {
                System.out.println("⚠️ La conexión se cerró inesperadamente.");
            }
        } catch (SQLException ex) {
            System.err.println("❌ Error al conectar con la base de datos:");
            ex.printStackTrace();
        }
    }
}

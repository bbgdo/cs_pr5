package org.example.utils;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private final String url = "jdbc:postgresql://localhost:5432/csproducts";
    private final String user = "postgres";
    private final String password = "postgre";
    private BasicConnectionPool connectionPool;

    public DatabaseConnection() throws SQLException {
        connectionPool = BasicConnectionPool.create(url, user, password);
    }

    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    public void shutdown() {
        try {
            connectionPool.shutdown();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

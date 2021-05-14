package org.shivacorp.dao.dbutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnection {
    private PostgreSQLConnection() {}

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "P16ostgre!";
        connection = DriverManager.getConnection(url, user, password);
        return connection;
    }
}

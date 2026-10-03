package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/smartlibrary";
    private static final String USER = System.getenv("SMARTLIB_DB_USER");
    private static final String PASSWORD = System.getenv("SMARTLIB_DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        if (USER == null || PASSWORD == null) {
            throw new SQLException("Database credentials are not set.");
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
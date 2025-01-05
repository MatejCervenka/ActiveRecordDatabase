package cz.cervenka.databaseproject.utils;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnection {

    private final Environment env;

    public DatabaseConnection(Environment env) {
        this.env = env;
    }

    public Connection getConnection() throws SQLException {
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        String driver = env.getProperty("spring.datasource.driver-class-name");

        try {
            Class.forName(driver);
            assert url != null;
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Successfully connected to the database!");
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("Database driver not found: " + driver);
            throw new SQLException("Driver not found", e);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw e;
        }
    }
}
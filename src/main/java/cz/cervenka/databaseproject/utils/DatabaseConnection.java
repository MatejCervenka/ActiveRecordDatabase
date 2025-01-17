package cz.cervenka.databaseproject.utils;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections using Spring's environment properties.
 * The class fetches connection details (URL, username, password, and driver) from the
 * application properties and establishes a connection to the database using JDBC.
 */
@Component
public class DatabaseConnection {

    private final Environment env;

    /**
     * Constructor that initializes the DatabaseConnection object with the Spring Environment.
     *
     * @param env The Spring Environment object that provides access to configuration properties.
     */
    public DatabaseConnection(Environment env) {
        this.env = env;
    }

    /**
     * Establishes and returns a JDBC connection to the database using the properties
     * specified in the Spring application environment.
     * The method retrieves the database URL, username, password, and driver class name
     * from the environment and attempts to establish a connection.
     *
     * @return A Connection object representing the established database connection.
     * @throws SQLException If an error occurs during the connection process, an SQLException is thrown.
     * @throws ClassNotFoundException If the JDBC driver class is not found, a ClassNotFoundException is thrown.
     */
    public Connection getConnection() throws SQLException {
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        String driver = env.getProperty("spring.datasource.driver-class-name");

        try {
            Class.forName(driver);

            assert url != null;

            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            System.err.println("Database driver not found: " + driver);
            throw new SQLException("Driver not found", e);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw e;
        }
    }
}
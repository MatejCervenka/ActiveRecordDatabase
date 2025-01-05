package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogEntity {
    private int id;
    private String logType;
    private String logMessage;
    private Timestamp createdAt;

    // Constructor
    public LogEntity(int logId, String logType, String logMessage, Timestamp createdAt) {
        this.id = logId;
        this.logType = logType;
        this.logMessage = logMessage;
        this.createdAt = createdAt;
    }

    // Save log entry
    public void save(Connection conn) throws SQLException {
        String sql = "INSERT INTO log (LogType, LogMessage) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, this.logType);
            stmt.setString(2, this.logMessage);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                this.id = keys.getInt(1);
            }
        }
    }

    // Find all log
    public static List<LogEntity> findAll(Connection conn) throws SQLException {
        List<LogEntity> log = new ArrayList<>();
        String sql = "SELECT * FROM log";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                log.add(new LogEntity(
                        rs.getInt("LogID"),
                        rs.getString("LogType"),
                        rs.getString("LogMessage"),
                        rs.getTimestamp("CreatedAt")
                ));
            }
        }
        return log;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}

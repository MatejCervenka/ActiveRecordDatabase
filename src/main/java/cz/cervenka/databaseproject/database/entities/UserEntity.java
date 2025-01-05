package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserEntity {
    private int userId;
    private String name;
    private String email;
    private boolean isActive;

    // Konstruktor
    public UserEntity(int userId, String name, String email, boolean isActive) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.isActive = isActive;
    }

    // Načtení uživatele podle ID
    public static UserEntity findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserEntity(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBoolean("isActive")
                );
            }
        }
        return null;
    }

    // Uložení nebo aktualizace uživatele
    public void save(Connection conn) throws SQLException {
        if (this.userId == 0) {
            String sql = "INSERT INTO user (name, email, isActive) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, this.name);
                stmt.setString(2, this.email);
                stmt.setBoolean(3, this.isActive);
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    this.userId = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE user SET name = ?, email = ?, isActive = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, this.name);
                stmt.setString(2, this.email);
                stmt.setBoolean(3, this.isActive);
                stmt.setInt(4, this.userId);
                stmt.executeUpdate();
            }
        }
    }

    // Odstranění uživatele
    public void delete(Connection conn) throws SQLException {
        if (this.userId != 0) {
            String sql = "DELETE FROM user WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, this.userId);
                stmt.executeUpdate();
            }
        }
    }

    // Generování reportu
    public static List<String> generateReport(Connection conn) throws SQLException {
        List<String> report = new ArrayList<>();
        String sql = "SELECT COUNT(*) AS UserCount, isActive FROM user GROUP BY isActive";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                report.add("Active: " + rs.getBoolean("IsActive") + " - Count: " + rs.getInt("UserCount"));
            }
        }
        return report;
    }

    // Gettery a settery
    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
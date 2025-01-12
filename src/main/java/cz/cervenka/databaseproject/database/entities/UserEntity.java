package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserEntity {
    private int id;
    private String name;
    private String email;
    private boolean isActive;

    public UserEntity() {
    }

    public UserEntity(int userId, String name, String email, boolean isActive) {
        this.id = userId;
        this.name = name;
        this.email = email;
        this.isActive = isActive;
    }

    public static List<UserEntity> getAll(Connection conn) throws SQLException {
        List<UserEntity> order = new ArrayList<>();
        String sql = "SELECT * FROM [user]";
        try (Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                order.add(new UserEntity(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("email"),
                        result.getBoolean("isActive")
                ));
            }
        }
        return order;
    }

    public static UserEntity findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM [user] WHERE id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new UserEntity(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("email"),
                        result.getBoolean("isActive")
                );
            }
        }
        return null;
    }

    public void save(Connection conn) throws SQLException {
        if (this.id == 0) {
            String sql = "INSERT INTO [user] (name, email, isActive) VALUES (?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, this.name);
                statement.setString(2, this.email);
                statement.setBoolean(3, this.isActive);
                statement.executeUpdate();
                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE [user] SET name = ?, email = ?, isActive = ? WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, this.name);
                statement.setString(2, this.email);
                statement.setBoolean(3, this.isActive);
                statement.setInt(4, this.id);
                statement.executeUpdate();
            }
        }
    }

    public void delete(Connection conn) throws SQLException {
        if (this.id != 0) {
            String sql = "DELETE FROM [user] WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, this.id);
                statement.executeUpdate();
            }
        }
    }

    public static List<String> generateReport(Connection conn) throws SQLException {
        List<String> report = new ArrayList<>();
        String sql = "SELECT COUNT(*) AS UserCount, isActive FROM [user] GROUP BY isActive";
        try (Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                report.add("Active: " + result.getBoolean("IsActive") + " - Count: " + result.getInt("UserCount"));
            }
        }
        return report;
    }


    public int getId() {
        return id;
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

    public boolean getIsActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
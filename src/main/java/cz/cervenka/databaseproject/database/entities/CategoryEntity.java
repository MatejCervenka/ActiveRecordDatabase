package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryEntity {
    private int id;
    private String name;

    public CategoryEntity() {
    }

    public CategoryEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static CategoryEntity findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM category WHERE id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new CategoryEntity(
                        result.getInt("id"),
                        result.getString("name")
                );
            }
        }
        return null;
    }

    public static List<CategoryEntity> getAll(Connection conn) throws SQLException {
        List<CategoryEntity> categories = new ArrayList<>();
        String sql = "SELECT * FROM category";
        try (Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                categories.add(new CategoryEntity(
                        result.getInt("id"),
                        result.getString("name")
                ));
            }
        }
        return categories;
    }

    public void save(Connection conn) throws SQLException {
        String sql;
        if (this.id == 0) {
            sql = "INSERT INTO category (name) VALUES (?)";
            try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, this.name);
                statement.executeUpdate();
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            }
        } else {
            sql = "UPDATE category SET name =? WHERE id =?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, this.name);
                statement.setInt(2, this.id);
                statement.executeUpdate();
            }
        }
    }

    public void delete(Connection conn) throws SQLException {
        if (this.id!= 0) {
            String sql = "DELETE FROM category WHERE id =?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, this.id);
                statement.executeUpdate();
            }
        }
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
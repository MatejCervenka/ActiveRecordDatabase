package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryEntity {
    private int id;
    private String name;

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
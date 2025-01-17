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

    /**
     * Finds a category by its unique ID.
     *
     * @param id The category ID.
     * @param conn The database connection.
     * @return The `CategoryEntity` object representing the category, or `null` if no category is found.
     * @throws SQLException If a database error occurs.
     */
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

    /**
     * Retrieves all categories from the database.
     *
     * @param conn The database connection.
     * @return A list of all `CategoryEntity` objects.
     * @throws SQLException If a database error occurs.
     */
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

    /**
     * Saves the current category to the database. If the category already exists, it is updated.
     *
     * @param conn The database connection.
     * @throws SQLException If a database error occurs.
     */
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

    /**
     * Deletes the category from the database.
     *
     * @param conn The database connection.
     * @throws SQLException If a database error occurs.
     */
    public void delete(Connection conn) throws SQLException {
        if (this.id != 0) {
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
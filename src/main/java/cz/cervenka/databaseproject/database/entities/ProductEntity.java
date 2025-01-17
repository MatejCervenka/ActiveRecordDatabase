package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductEntity {

    private int id;
    private String name;
    private double price;
    private int stock;
    private int category_id;
    private String category_name;

    public ProductEntity() {}

    public ProductEntity(int id, String name, double price, int stock, int category_id, String category_name) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category_id = category_id;
        this.category_name = category_name;
    }

    /**
     * Finds a product by its ID.
     *
     * @param id the ID of the product
     * @param conn the database connection
     * @return the product with the specified ID or null if not found
     * @throws SQLException if a database error occurs
     */
    public static ProductEntity findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT p.id, p.name, p.price, p.stock, p.category_id, c.name AS category_name " +
                "FROM product p " +
                "JOIN category c ON p.category_id = c.id " +
                "WHERE p.id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new ProductEntity(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getDouble("price"),
                        result.getInt("stock"),
                        result.getInt("category_id"),
                        result.getString("category_name")
                );
            }
        }
        return null;
    }

    /**
     * Retrieves all products from the database.
     *
     * @param conn the database connection
     * @return a list of all products
     * @throws SQLException if a database error occurs
     */
    public static List<ProductEntity> getAll(Connection conn) throws SQLException {
        List<ProductEntity> products = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.price, p.stock, p.category_id, c.name AS category_name " +
                "FROM product p " +
                "JOIN category c ON p.category_id = c.id";
        try (Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                products.add(new ProductEntity(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getDouble("price"),
                        result.getInt("stock"),
                        result.getInt("category_id"),
                        result.getString("category_name")
                ));
            }
        }
        return products;
    }

    /**
     * Finds products by their category ID.
     *
     * @param categoryId the ID of the category
     * @param conn the database connection
     * @return a list of products belonging to the specified category
     * @throws SQLException if a database error occurs
     */
    public static List<ProductEntity> findByCategory(int categoryId, Connection conn) throws SQLException {
        String query = "SELECT * FROM category_products WHERE category_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ProductEntity> products = new ArrayList<>();
                while (rs.next()) {
                    products.add(new ProductEntity(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getInt("category_id"),
                            rs.getString("category_name")
                    ));
                }
                return products;
            }
        }
    }

    /**
     * Saves the current product to the database (either inserts or updates).
     *
     * @param conn the database connection
     * @throws SQLException if a database error occurs
     */
    public void save(Connection conn) throws SQLException {
        String sql;
        if (this.id == 0) {
            sql = "INSERT INTO product (name, price, stock, category_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, this.name);
                statement.setDouble(2, this.price);
                statement.setInt(3, this.stock);
                statement.setInt(4, this.category_id);
                statement.executeUpdate();
                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        } else {
            sql = "UPDATE product SET name = ?, price = ?, stock = ?, category_id = ? WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, this.name);
                statement.setDouble(2, this.price);
                statement.setInt(3, this.stock);
                statement.setInt(4, this.category_id);
                statement.setInt(5, this.id);
                statement.executeUpdate();
            }
        }
    }

    /**
     * Deletes the current product from the database.
     *
     * @param conn the database connection
     * @throws SQLException if a database error occurs
     */
    public void delete(Connection conn) throws SQLException {
        if (this.id != 0) {
            String sql = "DELETE FROM product WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, this.id);
                statement.executeUpdate();
            }
        }
    }

    /**
     * Validates if the requested quantity is within the available stock.
     *
     * @param requestedQuantity the quantity requested by the user
     * @return true if the requested quantity is valid, otherwise false
     */
    public boolean isQuantityValid(int requestedQuantity) {
        return requestedQuantity > 0 && requestedQuantity <= this.stock;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }
}

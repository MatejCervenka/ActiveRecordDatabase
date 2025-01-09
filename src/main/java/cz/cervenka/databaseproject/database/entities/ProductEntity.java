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

    public static ProductEntity findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT p.id, p.name, p.price, p.stock, p.category_id, c.name AS category_name " +
                "FROM product p " +
                "JOIN category c ON p.category_id = c.id " +
                "WHERE p.id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new ProductEntity(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                );
            }
        }
        return null;
    }

    // Get all products with category names
    public static List<ProductEntity> getAllWithCategoryNames(Connection conn) throws SQLException {
        List<ProductEntity> products = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.price, p.stock, p.category_id, c.name AS category_name " +
                "FROM product p " +
                "JOIN category c ON p.category_id = c.id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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
        }
        return products;
    }

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

    public void delete(Connection conn) throws SQLException {
        if (this.id != 0) {
            String sql = "DELETE FROM product WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, this.id);
                statement.executeUpdate();
            }
        }
    }

    public static List<String> generateStockReport(Connection conn) throws SQLException {
        List<String> report = new ArrayList<>();
        String sql = "SELECT Name, SUM(Stock) AS TotalStock FROM product GROUP BY Name";
        try (Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                report.add("Product: " + result.getString("name") + " - Total Stock: " + result.getInt("stock"));
            }
        }
        return report;
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

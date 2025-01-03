package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Product {
    private int id;
    private String name;
    private double price;
    private int stock;
    private String category;


    public Product(int productId, String name, double price, int stock, String category) {
        this.id = productId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    // Načtení produktu podle ID
    public static Product findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM product WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Product(
                        rs.getInt("ProductID"),
                        rs.getString("Name"),
                        rs.getDouble("Price"),
                        rs.getInt("Stock"),
                        rs.getString("Category")
                );
            }
        }
        return null;
    }

    // Uložení nebo aktualizace produktu
    public void save(Connection conn) throws SQLException {
        if (this.id == 0) {
            String sql = "INSERT INTO product (name, price, stock, category) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, this.name);
                stmt.setDouble(2, this.price);
                stmt.setInt(3, this.stock);
                stmt.setString(4, this.category);
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE product SET name = ?, price = ?, stock = ?, category = ? WHERE ProductID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, this.name);
                stmt.setDouble(2, this.price);
                stmt.setInt(3, this.stock);
                stmt.setString(4, this.category);
                stmt.setInt(5, this.id);
                stmt.executeUpdate();
            }
        }
    }

    // Odstranění produktu
    public void delete(Connection conn) throws SQLException {
        if (this.id != 0) {
            String sql = "DELETE FROM product WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, this.id);
                stmt.executeUpdate();
            }
        }
    }

    // Generování reportu o skladových zásobách
    public static List<String> generateStockReport(Connection conn) throws SQLException {
        List<String> report = new ArrayList<>();
        String sql = "SELECT Name, SUM(Stock) AS TotalStock FROM Products GROUP BY Name";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                report.add("Product: " + rs.getString("Name") + " - Total Stock: " + rs.getInt("TotalStock"));
            }
        }
        return report;
    }

    // Gettery a settery
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
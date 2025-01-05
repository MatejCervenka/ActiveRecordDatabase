package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderProductEntity {
    private int id;
    private int product_id;
    private int quantity;

    // Constructor
    public OrderProductEntity(int orderId, int productId, int quantity) {
        this.id = orderId;
        this.product_id = productId;
        this.quantity = quantity;
    }

    // Find by order_id and product_id
    public static OrderProductEntity findById(int orderId, int productId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM orderProducts WHERE order_id = ? AND product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new OrderProductEntity(
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("Quantity")
                );
            }
        }
        return null;
    }

    // Save or update
    public void save(Connection conn) throws SQLException {
        String sql = "MERGE INTO orderProducts AS target " +
                "USING (SELECT ? AS order_id, ? AS product_id) AS source " +
                "ON target.order_id = source.order_id AND target.product_id = source.product_id " +
                "WHEN MATCHED THEN UPDATE SET Quantity = ? " +
                "WHEN NOT MATCHED THEN INSERT (order_id, product_id, Quantity) VALUES (?, ?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.id);
            stmt.setInt(2, this.product_id);
            stmt.setInt(3, this.quantity);
            stmt.setInt(4, this.id);
            stmt.setInt(5, this.product_id);
            stmt.setInt(6, this.quantity);
            stmt.executeUpdate();
        }
    }

    // Delete
    public void delete(Connection conn) throws SQLException {
        String sql = "DELETE FROM orderProducts WHERE order_id = ? AND product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.id);
            stmt.setInt(2, this.product_id);
            stmt.executeUpdate();
        }
    }

    // Get all orderProducts
    public static List<OrderProductEntity> findAll(Connection conn) throws SQLException {
        List<OrderProductEntity> orderProducts = new ArrayList<>();
        String sql = "SELECT * FROM orderProducts";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orderProducts.add(new OrderProductEntity(
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("Quantity")
                ));
            }
        }
        return orderProducts;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

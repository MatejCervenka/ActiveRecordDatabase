package cz.cervenka.databaseproject.database.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OrderProductEntity {
    private int orderId;
    private int productId;
    private int quantity;

    // Constructor
    public OrderProductEntity(int orderId, int productId, int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }


    // Save or Update (MERGE INTO)
    public void save(Connection conn) throws SQLException {
        String sql = "MERGE INTO orderProducts AS target " +
                "USING (SELECT ? AS order_id, ? AS product_id) AS source " +
                "ON target.order_id = source.order_id AND target.product_id = source.product_id " +
                "WHEN MATCHED THEN UPDATE SET quantity = ? " +
                "WHEN NOT MATCHED THEN INSERT (order_id, product_id, quantity) VALUES (?, ?, ?);";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            // For the ON clause
            statement.setInt(1, this.orderId);
            statement.setInt(2, this.productId);

            // For the UPDATE clause
            statement.setInt(3, this.quantity);

            // For the INSERT clause
            statement.setInt(4, this.orderId);
            statement.setInt(5, this.productId);
            statement.setInt(6, this.quantity);

            statement.executeUpdate();
        }
    }

    // Find all by Order ID
    public static List<OrderProductEntity> findByOrderId(int orderId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM orderProducts WHERE order_id = ?";
        List<OrderProductEntity> results = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(new OrderProductEntity(
                            resultSet.getInt("order_id"),
                            resultSet.getInt("product_id"),
                            resultSet.getInt("quantity")
                    ));
                }
            }
        }
        return results;
    }

    // Delete an OrderProductEntity by order_id and product_id
    public static void delete(int orderId, int productId, Connection conn) throws SQLException {
        String sql = "DELETE FROM orderProducts WHERE order_id = ? AND product_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
    }

    // Delete all by Order ID
    public static void deleteByOrderId(int orderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM orderProducts WHERE order_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.executeUpdate();
        }
    }


    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
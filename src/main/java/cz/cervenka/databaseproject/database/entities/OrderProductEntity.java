package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderProductEntity {
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private double total;
    private LocalDate orderDate;
    private String product_name;

    public OrderProductEntity() {
    }

    public OrderProductEntity(int id, int orderId, int productId, int quantity, double total, LocalDate orderDate, String productName) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.total = total;
        this.orderDate = orderDate;
        this.product_name = productName;
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

    public static List<OrderProductEntity> getAll(Connection conn) throws SQLException {
        List<OrderProductEntity> orderProduct = new ArrayList<>();
        String sql = "SELECT oP.order_id, oP.product_id, oP.quantity, o.total as total, o.orderDate AS orderDate, p.name AS product_name " +
                "FROM orderProducts oP " +
                "JOIN [order] o ON oP.order_id = o.id " +
                "JOIN product p ON oP.product_id = p.id ";
        try (Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                orderProduct.add(new OrderProductEntity(
                        result.getInt("id"),
                        result.getInt("order_id"),
                        result.getInt("product_id"),
                        result.getInt("quantity"),
                        result.getDouble("total"),
                        result.getDate("orderDate").toLocalDate(),
                        result.getString("product_name")));
            }
        }
        return orderProduct;
    }

    // Find all by Order ID
    public static List<OrderProductEntity> findByOrderId(int orderId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM orderProducts WHERE order_id = ?";
        List<OrderProductEntity> results = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    results.add(new OrderProductEntity(
                            result.getInt("id"),
                            result.getInt("order_id"),
                            result.getInt("product_id"),
                            result.getInt("quantity"),
                            result.getDouble("total"),
                            result.getDate("orderDate").toLocalDate(),
                            result.getString("product_name")));
                }
            }
        }
        return results;
    }

    // Find by Order Name and Product Name
    public static OrderProductEntity findByOrderTotalAndProductName(double total, String productName, Connection conn) throws SQLException {
        String sql = "SELECT oP.id, oP.order_id, oP.product_id, oP.quantity, o.total as total, o.orderDate AS orderDate, p.name AS product_name " +
                "FROM orderProducts oP " +
                "JOIN [order] o ON oP.order_id = o.id " +
                "JOIN product p ON oP.product_id = p.id " +
                "WHERE total = ? AND name = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setDouble(1, total);
            statement.setString(2, productName);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new OrderProductEntity(
                            result.getInt("id"),
                            result.getInt("order_id"),
                            result.getInt("product_id"),
                            result.getInt("quantity"),
                            result.getDouble("total"),
                            result.getDate("orderDate").toLocalDate(),
                            result.getString("product_name"));
                } else {
                    return null;
                }
            }
        }
    }

    public static void delete(double total, String productName, Connection conn) throws SQLException {
        String sql = "DELETE FROM orderProducts WHERE order_id = ? AND product_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setDouble(1, total);
            statement.setString(2, productName);
            statement.executeUpdate();
        }
    }

    public static void deleteByOrderId(int orderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM orderProducts WHERE order_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.executeUpdate();
        }
    }

    // Getters and setters
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

    public double getOrderTotal() {
        return total;
    }

    public void setOrderTotal(double total) {
        this.total = total;
    }

    public String getProductName() {
        return product_name;
    }

    public void setProductName(String product_name) {
        this.product_name = product_name;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public int getId() {
        return id;
    }
}

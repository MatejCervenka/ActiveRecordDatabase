package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int user_id;
    private Timestamp orderDate;
    private double total;

    // Constructor
    public Order(int id, int userId, Timestamp orderDate, double total) {
        this.id = id;
        this.user_id = userId;
        this.orderDate = orderDate;
        this.total = total;
    }

    // Find by ID
    public static Order findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM order WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Order(
                        rs.getInt("id"),
                        rs.getInt("UserID"),
                        rs.getTimestamp("OrderDate"),
                        rs.getDouble("Total")
                );
            }
        }
        return null;
    }

    // Save or update
    public void save(Connection conn) throws SQLException {
        if (this.id == 0) {
            String sql = "INSERT INTO order (UserID, Total) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, this.user_id);
                stmt.setDouble(2, this.total);
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE order SET UserID = ?, Total = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, this.user_id);
                stmt.setDouble(2, this.total);
                stmt.setInt(3, this.id);
                stmt.executeUpdate();
            }
        }
    }

    // Delete
    public void delete(Connection conn) throws SQLException {
        if (this.id != 0) {
            String sql = "DELETE FROM order WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, this.id);
                stmt.executeUpdate();
            }
        }
    }

    // Get all order
    public static List<Order> findAll(Connection conn) throws SQLException {
        List<Order> order = new ArrayList<>();
        String sql = "SELECT * FROM order";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                order.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("UserID"),
                        rs.getTimestamp("OrderDate"),
                        rs.getDouble("Total")
                ));
            }
        }
        return order;
    }

    // Getters and Setters
    public int getid() {
        return id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}

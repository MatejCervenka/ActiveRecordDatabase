package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderEntity {
    private int id;
    private int user_id;
    private Timestamp orderDate;
    private double total;

    public OrderEntity(int id, int userId, Timestamp orderDate, double total) {
        this.id = id;
        this.user_id = userId;
        this.orderDate = orderDate;
        this.total = total;
    }

    public static OrderEntity findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM [order] WHERE id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new OrderEntity(
                        result.getInt("id"),
                        result.getInt("user_id"),
                        result.getTimestamp("orderDate"),
                        result.getDouble("total")
                );
            }
        }
        return null;
    }

    public void save(Connection conn) throws SQLException {
        if (this.id == 0) {
            String sql = "INSERT INTO [order] (user_id, total) VALUES (?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, this.user_id);
                statement.setDouble(2, this.total);
                statement.executeUpdate();
                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE [order] SET id = ?, total = ? WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, this.user_id);
                statement.setDouble(2, this.total);
                statement.setInt(3, this.id);
                statement.executeUpdate();
            }
        }
    }

    public void delete(Connection conn) throws SQLException {
        if (this.id != 0) {
            String sql = "DELETE FROM [order] WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, this.id);
                statement.executeUpdate();
            }
        }
    }

    public static List<OrderEntity> getAll(Connection conn) throws SQLException {
        List<OrderEntity> order = new ArrayList<>();
        String sql = "SELECT * FROM [order]";
        try (Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                order.add(new OrderEntity(
                        result.getInt("id"),
                        result.getInt("user_id"),
                        result.getTimestamp("orderDate"),
                        result.getDouble("total")
                ));
            }
        }
        return order;
    }

    public int getId() {
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
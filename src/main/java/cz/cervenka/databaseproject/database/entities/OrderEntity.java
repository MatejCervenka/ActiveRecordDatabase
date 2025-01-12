package cz.cervenka.databaseproject.database.entities;

import org.springframework.format.annotation.DateTimeFormat;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderEntity {
    private int id;
    private int user_id;
    private LocalDate orderDate;
    private double total;
    private String user_name;

    public OrderEntity() {
    }

    public OrderEntity(int id, int userId, LocalDate orderDate, double total, String user_name) {
        this.id = id;
        this.user_id = userId;
        this.orderDate = orderDate;
        this.total = total;
        this.user_name = user_name;
    }

    public static OrderEntity findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT o.id, o.user_id, o.orderDate, o.total, u.name AS user_name " +
                "FROM [order] o " +
                "JOIN [user] u ON o.user_id = u.id " +
                "WHERE o.id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new OrderEntity(
                        result.getInt("id"),
                        result.getInt("user_id"),
                        result.getDate("orderDate").toLocalDate(),
                        result.getDouble("total"),
                        result.getString("user_name")
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
        String sql = "SELECT o.id, o.user_id, o.orderDate, o.total, u.name AS user_name " +
                "FROM [order] o " +
                "JOIN [user] u ON o.user_id = u.id";
        try (Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                order.add(new OrderEntity(
                        result.getInt("id"),
                        result.getInt("user_id"),
                        result.getDate("orderDate").toLocalDate(),
                        result.getDouble("total"),
                        result.getString("user_name")
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

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }
}
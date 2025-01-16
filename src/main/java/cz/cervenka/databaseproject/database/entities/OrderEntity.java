package cz.cervenka.databaseproject.database.entities;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderEntity {
    private int id;
    private int customer_id;
    private LocalDate orderDate;
    private String orderNumber;
    private double totalPrice;
    private String customer_name;
    private String customer_surname;

    public OrderEntity() {
    }

    public OrderEntity(int id, int userId, LocalDate orderDate, String orderNumber, double total, String customer_name, String customer_surname) {
        this.id = id;
        this.customer_id = userId;
        this.orderDate = orderDate != null ? orderDate : LocalDate.now();
        this.orderNumber = orderNumber;
        this.totalPrice = total;
        this.customer_name = customer_name;
        this.customer_surname = customer_surname;
    }

    public OrderEntity(CustomerEntity customer, ProductEntity product, int quantity) {
        this.customer_id = customer.getId();
        this.orderDate = LocalDate.now();
        this.totalPrice = product.getPrice() * quantity;
        this.customer_name = customer.getName();
    }


    public static List<OrderEntity> getAll(Connection conn) throws SQLException {
        List<OrderEntity> order = new ArrayList<>();
        String sql = "SELECT o.id, o.customer_id, o.orderNumber, o.orderDate, o.totalPrice, c.name AS customer_name, c.surname AS customer_surname " +
                "FROM [order] o " +
                "JOIN customer c ON o.customer_id = c.id";
        try (Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                order.add(new OrderEntity(
                        result.getInt("id"),
                        result.getInt("customer_id"),
                        result.getDate("orderDate").toLocalDate(),
                        result.getString("orderNumber"),
                        result.getDouble("totalPrice"),
                        result.getString("customer_name"),
                        result.getString("customer_surname")));
            }
        }
        return order;
    }

    public static List<Map<String, Object>> findOrderDetailsByNumber(String orderNumber, Connection conn) throws SQLException {
        String sql = "SELECT * FROM order_list WHERE orderNumber = ?";
        List<Map<String, Object>> orderDetails = new ArrayList<>();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, orderNumber);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                Map<String, Object> order = new HashMap<>();
                order.put("orderNumber", result.getString("orderNumber"));
                order.put("orderDate", result.getDate("orderDate"));
                order.put("totalPrice", result.getDouble("totalPrice"));
                order.put("name", result.getString("name"));
                order.put("surname", result.getString("surname"));
                order.put("email", result.getString("email"));
                order.put("quantity", result.getInt("quantity"));
                orderDetails.add(order);
            }
        }
        return orderDetails;
    }

    public static List<Map<String, Object>> findOrdersByCustomerId(int customerId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM order_list WHERE customer_id = ?";
        List<Map<String, Object>> orders = new ArrayList<>();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                Map<String, Object> order = new HashMap<>();
                order.put("orderNumber", result.getString("orderNumber"));
                order.put("orderDate", result.getDate("orderDate"));
                order.put("totalPrice", result.getDouble("totalPrice"));
                order.put("name", result.getString("name"));
                order.put("surname", result.getString("surname"));
                order.put("email", result.getString("email"));
                order.put("quantity", result.getInt("quantity"));
                orders.add(order);
            }
        }
        return orders;
    }

    public static OrderEntity findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT o.id, o.customer_id, o.orderNumber, o.orderDate, o.totalPrice, c.name AS customer_name, c.surname AS customer_surname " +
                "FROM [order] o " +
                "JOIN customer c ON o.customer_id = c.id " +
                "WHERE o.id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new OrderEntity(
                        result.getInt("id"),
                        result.getInt("customer_id"),
                        result.getDate("orderDate").toLocalDate(),
                        result.getString("orderNumber"),
                        result.getDouble("totalPrice"),
                        result.getString("customer_name"),
                        result.getString("customer_surname"));
            }
        }
        return null;
    }

    public static List<OrderEntity> findByUserId(int userId, Connection conn) throws SQLException {
        List<OrderEntity> orders = new ArrayList<>();
        String sql = "SELECT o.id, o.customer_id, o.orderNumber, o.orderDate, o.totalPrice, c.name AS customer_name, c.surname AS customer_surname " +
                "FROM [order] o " +
                "JOIN customer c ON o.customer_id = c.id " +
                "WHERE o.customer_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                orders.add(new OrderEntity(
                        result.getInt("id"),
                        result.getInt("customer_id"),
                        result.getDate("orderDate").toLocalDate(),
                        result.getString("orderNumber"),
                        result.getDouble("totalPrice"),
                        result.getString("customer_name"),
                        result.getString("customer_surname")));
            }
        }
        return orders;
    }


    public void save(Connection conn) throws SQLException {
        if (this.id == 0) {
            String sql = "INSERT INTO [order] (customer_id, orderNumber, orderDate, totalPrice) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, this.customer_id);
                statement.setString(2, this.orderNumber);

                // Ensure orderDate is set before saving
                if (this.orderDate == null) {
                    this.orderDate = LocalDate.now();
                }
                statement.setDate(3, Date.valueOf(this.orderDate));

                statement.setDouble(4, this.totalPrice);
                statement.executeUpdate();

                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE [order] SET totalPrice = ? WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setDouble(1, this.totalPrice);
                statement.setInt(2, this.id);
                statement.executeUpdate();
            }
        }
    }

    public void deleteWithProducts(Connection conn) throws SQLException {
        String deleteOrderProductSQL = "DELETE FROM orderProduct WHERE order_id = ?";
        String deleteOrderSQL = "DELETE FROM [order] WHERE id = ?";

        try {
            // Start transaction
            conn.setAutoCommit(false);

            // Delete records from orderProduct table
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrderProductSQL)) {
                stmt.setInt(1, this.id);
                stmt.executeUpdate();
            }

            // Delete record from order table
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrderSQL)) {
                stmt.setInt(1, this.id);
                stmt.executeUpdate();
            }

            // Commit transaction
            conn.commit();
        } catch (SQLException e) {
            // Rollback transaction in case of failure
            conn.rollback();
            throw new SQLException("Failed to delete order and associated products", e);
        } finally {
            // Restore default auto-commit behavior
            conn.setAutoCommit(true);
        }
    }



    public int getId() {
        return id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getUser_name() {
        return customer_name;
    }

    public void setUser_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCustomer_surname() {
        return customer_surname;
    }

    public void setCustomer_surname(String customer_surname) {
        this.customer_surname = customer_surname;
    }
}
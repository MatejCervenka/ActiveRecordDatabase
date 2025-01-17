package cz.cervenka.databaseproject.database.entities;

import java.sql.*;

public class CustomerEntity {

    private int id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private boolean subscribe;
    private int user_id;

    public CustomerEntity() {
    }

    public CustomerEntity(int id, String name, String surname, String email, String phone, boolean subscribe, int userId) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.subscribe = subscribe;
        this.user_id = userId;
    }

    public CustomerEntity(String name, String surname, String email, String phone, boolean subscribe) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.subscribe = subscribe;
    }

    /**
     * Saves the current customer to the database. If the customer already exists, it is updated.
     *
     * @param conn The database connection.
     * @throws SQLException If a database error occurs.
     */
    public void save(Connection conn) throws SQLException {
        if (this.id == 0) {
            String sql = "INSERT INTO customer (name, surname, email, phone, subscribe, user_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, this.name);
                statement.setString(2, this.surname);
                statement.setString(3, this.email);
                statement.setString(4, this.phone);
                statement.setBoolean(5, this.subscribe);
                statement.setInt(6, this.user_id);
                statement.executeUpdate();
                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE customer SET name = ?, surname = ?, email = ?, phone = ?, subscribe = ? , user_id = ? WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, this.name);
                statement.setString(2, this.surname);
                statement.setString(3, this.email);
                statement.setString(4, this.phone);
                statement.setBoolean(5, this.subscribe);
                statement.setInt(6, this.user_id);
                statement.setInt(7, this.id);
                statement.executeUpdate();
            }
        }
    }

    /**
     * Finds a customer by their unique ID.
     *
     * @param id The customer ID.
     * @param conn The database connection.
     * @return The `CustomerEntity` object representing the customer, or `null` if no customer is found.
     * @throws SQLException If a database error occurs.
     */
    public static CustomerEntity findById(int id, Connection conn) throws SQLException {
        String query = "SELECT * FROM customer WHERE id = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    CustomerEntity customer = new CustomerEntity();
                    customer.setId(rs.getInt("id"));
                    customer.setName(rs.getString("name"));
                    customer.setSurname(rs.getString("surname"));
                    customer.setEmail(rs.getString("email"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setSubscribe(rs.getBoolean("subscribe"));
                    customer.setUser_id(rs.getInt("user_id"));
                    return customer;
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Finds a customer by their associated user ID.
     *
     * @param userId The user ID.
     * @param conn The database connection.
     * @return The `CustomerEntity` object representing the customer, or `null` if no customer is found.
     * @throws SQLException If a database error occurs.
     */
    public static CustomerEntity findByUserId(int userId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM customer WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new CustomerEntity(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getBoolean("subscribe"),
                            rs.getInt("user_id")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Deletes the customer from the database.
     *
     * @param conn The database connection.
     * @throws SQLException If a database error occurs.
     */
    public void delete(Connection conn) throws SQLException {
        if (this.id != 0) {
            String sql = "DELETE FROM customer WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, this.id);
                statement.executeUpdate();
            }
        }
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isSubscribe() {
        return subscribe;
    }

    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}

package cz.cervenka.databaseproject.database.entities;

import java.sql.*;

public class CustomerEntity {

    private int id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private boolean subscribe;

    public CustomerEntity() {
    }

    public CustomerEntity(int id, String name, String surname, String email, String phone, boolean subscribe) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.subscribe = subscribe;
    }

    public CustomerEntity(String name, String surname, String email, String phone, boolean subscribe) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.subscribe = subscribe;
    }

    public void save(Connection conn) throws SQLException {
        if (this.id == 0) {
            String sql = "INSERT INTO customer (name, surname, email, phone, subscribe) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, this.name);
                statement.setString(2, this.surname);
                statement.setString(3, this.email);
                statement.setString(4, this.phone);
                statement.setBoolean(5, this.subscribe);
                statement.executeUpdate();
                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE customer SET name = ?, surname = ?, email = ?, phone = ?, subscribe = ? WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, this.name);
                statement.setString(2, this.surname);
                statement.setString(3, this.email);
                statement.setString(4, this.phone);
                statement.setBoolean(5, this.subscribe);
                statement.setInt(6, this.id);
                statement.executeUpdate();
            }
        }
    }

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
                    return customer;
                } else {
                    return null;
                }
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
}

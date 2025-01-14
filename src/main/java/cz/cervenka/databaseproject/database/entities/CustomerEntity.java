package cz.cervenka.databaseproject.database.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerEntity {

    private int id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private boolean subscribe;

    // Getters and setters...

    public void save(Connection conn) throws SQLException {
        String query = "INSERT INTO customer (name, surname, email, phone, subscribe) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, this.name);
            stmt.setString(2, this.surname);
            stmt.setString(3, this.email);
            stmt.setString(4, this.phone);
            stmt.setBoolean(5, this.subscribe);
            stmt.executeUpdate();
        }
    }

    public static CustomerEntity findById(int id, Connection conn) throws SQLException {
        String query = "SELECT * FROM customer WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
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

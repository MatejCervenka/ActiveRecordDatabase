package cz.cervenka.databaseproject.database.entities;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserEntity {

    public enum Role {
        ADMIN, USER
    }

    private int id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private Role role;

    public UserEntity() {
    }

    public UserEntity(int userId, String name, String surname, String password, String email, Role role) {
        this.id = userId;
        this.name = name;
        this.surname = surname;
        this.password = hashPassword(password);
        this.email = email;
        this.role = role;
    }

    public static List<UserEntity> getAll(Connection conn) throws SQLException {
        List<UserEntity> user = new ArrayList<>();
        String sql = "SELECT * FROM [user]";
        try (Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                Role role = Role.valueOf(result.getString("role"));
                user.add(new UserEntity(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("surname"),
                        result.getString("password"),
                        result.getString("email"),
                        role
                ));
            }
        }
        return user;
    }

    public static UserEntity findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM [user] WHERE id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                Role role = Role.valueOf(result.getString("role"));
                return new UserEntity(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("surname"),
                        result.getString("password"),
                        result.getString("email"),
                        role
                );
            }
        }
        return null;
    }

    public static UserEntity findByEmail(String email, Connection conn) throws SQLException {
        String sql = "SELECT * FROM [user] WHERE email = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, email);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                Role role = Role.valueOf(result.getString("role"));
                return new UserEntity(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("surname"),
                        result.getString("password"),
                        result.getString("email"),
                        role
                );
            }
        }
        return null;
    }

    public void save(Connection conn) throws SQLException {
        if (this.id == 0) {
            String checkSql = "SELECT COUNT(*) AS count FROM [user]";
            int userCount = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next()) {
                    userCount = rs.getInt("count");
                }
            }

            this.role = (userCount == 0) ? Role.ADMIN : Role.USER;

            String sql = "INSERT INTO [user] (name, surname, password, email, role) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, this.name);
                statement.setString(2, this.surname);
                statement.setString(3, this.password);
                statement.setString(4, this.email);
                statement.setString(5, this.role.name());
                statement.executeUpdate();
                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE [user] SET name = ?, surname = ?, password = ?, email = ? WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, this.name);
                statement.setString(2, this.surname);
                statement.setString(3, this.password);
                statement.setString(4, this.email);
                statement.setString(5, this.role.name());
                statement.setInt(6, this.id);
                statement.executeUpdate();
            }
        }
    }

    public void delete(Connection conn) throws SQLException {
        if (this.id != 0) {
            String sql = "DELETE FROM [user] WHERE id = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, this.id);
                statement.executeUpdate();
            }
        }
    }


    /**
     * Hashes a given password using SHA-256.
     *
     * @param password the plain text password
     * @return the hashed password as a hexadecimal string
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage());
        }
    }

    public boolean isValid(Connection conn) throws SQLException {
        String sql = "SELECT * FROM [user] WHERE email = ? AND password = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, this.email);
            statement.setString(2, this.password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    this.id = resultSet.getInt("id");
                    this.name = resultSet.getString("name");
                    this.surname = resultSet.getString("surname");
                    this.role = Role.valueOf(resultSet.getString("role"));
                    return true;
                }
            }
        }
        return false;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role.name();
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
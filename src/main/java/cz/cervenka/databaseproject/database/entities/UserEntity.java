package cz.cervenka.databaseproject.database.entities;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserEntity {

    /**
     * Enum representing the role of a user.
     */
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

    /**
     * Constructs a new UserEntity with the provided details.
     *
     * @param userId   the user's ID
     * @param name     the user's name
     * @param surname  the user's surname
     * @param password the user's password (will be hashed)
     * @param email    the user's email
     * @param role     the user's role (ADMIN or USER)
     */
    public UserEntity(int userId, String name, String surname, String password, String email, Role role) {
        this.id = userId;
        this.name = name;
        this.surname = surname;
        this.password = hashPassword(password);
        this.email = email;
        this.role = role;
    }

    /**
     * Retrieves all users from the database.
     *
     * @param conn the database connection
     * @return a list of UserEntity objects
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Retrieves a user by their ID from the database.
     *
     * @param id   the user's ID
     * @param conn the database connection
     * @return the UserEntity with the given ID, or null if not found
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Retrieves a user by their email from the database.
     *
     * @param email the user's email
     * @param conn  the database connection
     * @return the UserEntity with the given email, or null if not found
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Saves the current user to the database (inserts or updates).
     *
     * @param conn the database connection
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Deletes the current user from the database.
     *
     * @param conn the database connection
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Verifies if the current user's email and password are valid.
     *
     * @param conn the database connection
     * @return true if the credentials are valid, false otherwise
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Checks if the user registration details are valid.
     *
     * @param user the user to validate
     * @return true if the registration is invalid, false otherwise
     */
    public boolean isInvalidRegistration(UserEntity user) {
        if (user.getName() == null || user.getName().trim().isEmpty() || !isValidName(user.getName())) {
            return true;
        }
        if (user.getSurname() == null || user.getSurname().trim().isEmpty() || !isValidName(user.getSurname())) {
            return true;
        }
        if (user.getEmail() == null || !isValidEmail(user.getEmail())) {
            return true;
        }
        return user.getPassword() == null || user.getPassword().length() < 6;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

    private boolean isValidName(String name) {
        String nameRegex = "^[A-Za-z]{2,50}$";
        return name != null && name.matches(nameRegex);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "^\\+?[0-9]{7,15}$";
        return phoneNumber != null && phoneNumber.matches(phoneRegex);
    }

    // Getters and Setters

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

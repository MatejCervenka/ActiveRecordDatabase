package cz.cervenka.databaseproject.services;

import cz.cervenka.databaseproject.utils.DatabaseConnection;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class ImportService {

    private final DatabaseConnection dbConnection;

    public ImportService(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Imports data from a CSV file into the database.
     * Categories are checked for existence and inserted if necessary,
     * and products are inserted into the database with the appropriate category ID.
     *
     * @param file The CSV file to import.
     * @throws Exception If any errors occur during the import process, including invalid CSV structure.
     */
    public void importCsv(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            Connection conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                for (CSVRecord record : csvParser) {
                    String categoryName = record.get("category_name");
                    String productName = record.get("name");
                    double price = Double.parseDouble(record.get("price"));
                    int stock = Integer.parseInt(record.get("stock"));

                    // Get or insert the category and then insert the product
                    int categoryId = getOrInsertCategory(conn, categoryName);
                    insertProduct(conn, productName, price, stock, categoryId);
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new Exception("Failed to import CSV: " + e.getMessage(), e);
            } finally {
                conn.close();
            }
        }
    }

    /**
     * Retrieves the category ID for an existing category or inserts the category if it does not exist.
     *
     * @param conn The database connection.
     * @param categoryName The name of the category.
     * @return The category ID.
     * @throws SQLException If a database error occurs.
     */
    private int getOrInsertCategory(Connection conn, String categoryName) throws SQLException {
        // Attempt to find the category in the database
        String selectCategorySql = "SELECT id FROM category WHERE name = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectCategorySql)) {
            selectStmt.setString(1, categoryName);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        // Insert the category if not found
        String insertCategorySql = "INSERT INTO category (name) VALUES (?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertCategorySql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, categoryName);
            insertStmt.executeUpdate();
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }

        throw new SQLException("Failed to insert or retrieve category ID for: " + categoryName);
    }

    /**
     * Inserts a product into the database.
     *
     * @param conn The database connection.
     * @param name The product name.
     * @param price The product price.
     * @param stock The product stock quantity.
     * @param categoryId The ID of the category the product belongs to.
     * @throws SQLException If a database error occurs.
     */
    private void insertProduct(Connection conn, String name, double price, int stock, int categoryId) throws SQLException {
        String insertProductSql = "INSERT INTO product (name, price, stock, category_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertProductSql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, stock);
            stmt.setInt(4, categoryId);
            stmt.executeUpdate();
        }
    }

    /**
     * Validates the CSV file to ensure it contains the correct headers and data types.
     * It checks for required columns (`category_name`, `name`, `price`, `stock`) and ensures
     * that the price and stock columns contain valid numeric values.
     *
     * @param file The CSV file to validate.
     * @return `true` if the CSV is valid, `false` otherwise.
     * @throws Exception If an error occurs while reading or parsing the file.
     */
    public boolean validateCsv(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            // Check for required columns
            if (!csvParser.getHeaderMap().containsKey("category_name") ||
                    !csvParser.getHeaderMap().containsKey("name") ||
                    !csvParser.getHeaderMap().containsKey("price") ||
                    !csvParser.getHeaderMap().containsKey("stock")) {
                return false;
            }

            // Validate each record's data types
            for (CSVRecord record : csvParser) {
                String priceStr = record.get("price");
                String stockStr = record.get("stock");

                try {
                    // Try parsing the price and stock values
                    Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    return false;
                }

                try {
                    Integer.parseInt(stockStr);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return true;
    }

}
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

                    // Insert or get category ID
                    int categoryId = getOrInsertCategory(conn, categoryName);

                    // Insert product
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

    private int getOrInsertCategory(Connection conn, String categoryName) throws SQLException {
        // Check if category exists
        String selectCategorySql = "SELECT id FROM category WHERE name = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectCategorySql)) {
            selectStmt.setString(1, categoryName);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        // Insert new category
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
}
package cz.cervenka.databaseproject.services;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class ReportService {

    /**
     * Generates a summary report containing customer order statistics,
     * the most sold product, and the highest-value product.
     *
     * @param conn The database connection.
     * @return A `SummaryReport` object containing the generated report data.
     * @throws SQLException If a database error occurs.
     */
    public SummaryReport generateSummaryReport(Connection conn) throws SQLException {
        SummaryReport report = new SummaryReport();

        // SQL query to get customer order statistics
        String sql1 = """
        SELECT c.name AS customer_name, COUNT(o.id) AS total_orders, SUM(o.totalPrice) AS total_revenue
            FROM customer c
            JOIN [order] o ON c.id = o.customer_id
            GROUP BY c.name
        """;

        // Execute the first query and populate the report with customer data
        try (PreparedStatement stmt = conn.prepareStatement(sql1);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                report.setCustomerName(rs.getString("customer_name"));
                report.setTotalOrders(rs.getInt("total_orders"));
                report.setTotalRevenue(rs.getDouble("total_revenue"));
            }
        }

        // SQL query to get the most sold product
        String sql2 = """
        SELECT TOP 1 p.name AS product_name, SUM(op.quantity) AS total_sold
            FROM product p
            JOIN OrderProduct op ON p.id = op.product_id
            GROUP BY p.name
            ORDER BY total_sold DESC
        """;

        // Execute the second query and populate the report with most sold product data
        try (PreparedStatement stmt = conn.prepareStatement(sql2);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                report.setMostSoldProduct(rs.getString("product_name"));
                report.setTotalSold(rs.getInt("total_sold"));
            }
        }

        // SQL query to get the highest-value product
        String sql3 = """
        SELECT TOP 1 p.name AS product_name, MAX(op.quantity * p.price) AS max_value
            FROM product p
            JOIN OrderProduct op ON p.id = op.product_id
            GROUP BY p.name
            ORDER BY max_value DESC
        """;

        // Execute the third query and populate the report with highest-value product data
        try (PreparedStatement stmt = conn.prepareStatement(sql3);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                report.setHighestValueProduct(rs.getString("product_name"));
                report.setMaxValue(rs.getDouble("max_value"));
            }
        }

        return report;
    }

}
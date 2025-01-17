package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.services.ReportService;
import cz.cervenka.databaseproject.services.SummaryReport;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/report")
public class ReportController {

    private final DatabaseConnection dbConnection;
    private ReportService reportService;

    public ReportController(DatabaseConnection dbConnection, ReportService reportService) {
        this.dbConnection = dbConnection;
        this.reportService = reportService;
    }

    /**
     * Displays the summary report.
     * The method retrieves the summary report using the `ReportService` and adds it to the model for the view.
     *
     * @param model The model to pass the report data to the view.
     * @return The view to display the report.
     */
    @GetMapping
    public String showReport(Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            // Generate the summary report using the report service
            SummaryReport report = reportService.generateSummaryReport(conn);
            model.addAttribute("report", report);
            return "report";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to generate report.");
            return "error";
        }
    }

    /**
     * Exports the summary report as a CSV file.
     * The method retrieves relevant data from the database and writes it to the HTTP response in CSV format.
     *
     * @param response The HTTP response used to send the CSV file to the client.
     * @throws SQLException If an error occurs while retrieving data from the database.
     * @throws IOException If an error occurs while writing the CSV file to the response.
     */
    @GetMapping("/export")
    public void exportReport(HttpServletResponse response) throws SQLException, IOException {
        try (Connection conn = dbConnection.getConnection()) {
            // Set response properties for downloading a CSV file
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=summary_report.csv");

            try (PrintWriter writer = response.getWriter()) {
                // Generate the CSV content for the report

                // SQL query for customer order statistics
                String sql1 = """
                SELECT c.name AS customer_name, COUNT(o.id) AS total_orders, SUM(o.totalPrice) AS total_revenue
                    FROM customer c
                    JOIN [order] o ON c.id = o.customer_id
                    GROUP BY c.name
                """;
                writer.println("Customer Name,Total Orders,Total Revenue");
                try (PreparedStatement stmt = conn.prepareStatement(sql1);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        writer.printf("%s,%d,%.2f%n",
                                rs.getString("customer_name"),
                                rs.getInt("total_orders"),
                                rs.getDouble("total_revenue"));
                    }
                }

                // SQL query for the most sold product
                String sql2 = """
                SELECT TOP 1 p.name AS product_name, SUM(op.quantity) AS total_sold
                    FROM product p
                    JOIN OrderProduct op ON p.id = op.product_id
                    GROUP BY p.name
                    ORDER BY total_sold DESC
                """;
                writer.println("\nMost Sold Product,Quantity Sold");
                try (PreparedStatement stmt = conn.prepareStatement(sql2);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        writer.printf("%s,%d%n",
                                rs.getString("product_name"),
                                rs.getInt("total_sold"));
                    }
                }

                // SQL query for the highest value product
                String sql3 = """
                SELECT TOP 1 p.name AS product_name, MAX(op.quantity * p.price) AS max_value
                    FROM product p
                    JOIN OrderProduct op ON p.id = op.product_id
                    GROUP BY p.name
                    ORDER BY max_value DESC
                """;
                writer.println("\nHighest Value Product,Maximum Value");
                try (PreparedStatement stmt = conn.prepareStatement(sql3);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        writer.printf("%s,%.2f%n",
                                rs.getString("product_name"),
                                rs.getDouble("max_value"));
                    }
                }
            }
        }
    }
}
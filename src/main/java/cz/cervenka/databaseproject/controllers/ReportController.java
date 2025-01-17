package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.services.ReportService;
import cz.cervenka.databaseproject.services.SummaryReport;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.Connection;
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

    @GetMapping
    public String showReport(Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            SummaryReport report = reportService.generateSummaryReport(conn);
            model.addAttribute("report", report);
            return "report";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to generate report.");
            return "error";
        }
    }
}
package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.services.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/import")
public class ImportController {

    private final ImportService csvImportService;

    @Autowired
    public ImportController(ImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @GetMapping
    public String showImportForm(Model model) {
        return "import";
    }

    @PostMapping
    public String importCsv(MultipartFile file, Model model) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".csv")) {
            model.addAttribute("error", "Invalid file. Please upload a CSV file.");
            return "error";
        }

        try {
            csvImportService.importCsv(file);
            model.addAttribute("message", "File imported successfully!");
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to import file: " + e.getMessage());
            return "error";
        }
    }
}
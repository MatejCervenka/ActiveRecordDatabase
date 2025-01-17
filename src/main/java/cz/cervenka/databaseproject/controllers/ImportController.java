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

    /**
     * Displays the import form where users can upload a CSV file.
     *
     * @param model The model to pass attributes to the view.
     * @return The view for the import form.
     */
    @GetMapping
    public String showImportForm(Model model) {
        return "import";
    }

    /**
     * Handles the CSV file upload and import process.
     * Validates the uploaded file, checks its size and format, and then attempts to process it.
     * If the file is valid, it will be imported; otherwise, an error message is displayed.
     *
     * @param file The uploaded CSV file.
     * @param model The model to pass attributes to the view.
     * @return The view to display the result of the import process.
     */
    @PostMapping
    public String importCsv(MultipartFile file, Model model) {
        // Validate the uploaded file
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".csv")) {
            model.addAttribute("error", "Invalid file. Please upload a CSV file.");
            return "import";
        }

        // Check the file size
        if (file.getSize() > 5 * 1024 * 1024) {
            model.addAttribute("error", "File is too large. Maximum size is 5MB.");
            return "import";
        }

        try {
            // Validate the structure of the CSV file
            if (!csvImportService.validateCsv(file)) {
                model.addAttribute("error", "Invalid CSV structure. Please check the columns and data format.");
                return "import";
            }

            // Import the CSV file
            csvImportService.importCsv(file);
            model.addAttribute("message", "File imported successfully!");
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to import file: " + e.getMessage());
            return "import";
        }
    }

}
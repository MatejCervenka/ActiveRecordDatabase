package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.database.entities.CategoryEntity;
import cz.cervenka.databaseproject.database.entities.ProductEntity;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final DatabaseConnection dbConnection;

    public CategoryController(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Displays a list of all products and categories.
     *
     * @param model The model to pass attributes to the view.
     * @return The view for displaying products and categories.
     */
    @GetMapping
    public String listProducts(Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            List<ProductEntity> products = ProductEntity.getAll(conn);
            List<CategoryEntity> categories = CategoryEntity.getAll(conn);
            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
            model.addAttribute("newProduct", new ProductEntity());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "category_products";
    }

    /**
     * Displays products filtered by the selected category.
     *
     * @param categoryId The ID of the category to filter by (optional).
     * @param model The model to pass attributes to the view.
     * @return The view for displaying filtered products by category.
     */
    @PostMapping("/products")
    public String listProductsByCategory(@RequestParam(required = false) Integer categoryId, Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            List<CategoryEntity> categories = CategoryEntity.getAll(conn);
            model.addAttribute("categories", categories);

            if (categoryId != null) {
                List<ProductEntity> products = ProductEntity.findByCategory(categoryId, conn);
                model.addAttribute("products", products);
                CategoryEntity selectedCategory = CategoryEntity.findById(categoryId, conn);
                model.addAttribute("selectedCategory", selectedCategory);
            } else {
                model.addAttribute("products", List.of());
                model.addAttribute("selectedCategory", null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "category_products";
    }
}
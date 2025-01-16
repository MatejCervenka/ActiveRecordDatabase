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

    /*@PostMapping("/products")
    public String getProductsByCategory(@RequestParam("categoryId") int categoryId, Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            List<ProductEntity> products = ProductEntity.findByCategory(categoryId, conn);
            List<CategoryEntity> categories = CategoryEntity.getAll(conn);
            model.addAttribute("categories", categories);
            model.addAttribute("products", products);
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "Error fetching products for the selected category.");
        }
        return "category_products";
    }*/


    /*@PostMapping("/add")
    public String addCategory(@ModelAttribute("newCategory") CategoryEntity user) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            user.save(conn);
        }
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditCategoryForm(@PathVariable int id, Model model) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            CategoryEntity product = CategoryEntity.findById(id, conn);
            List<CategoryEntity> categories = CategoryEntity.getAll(conn);
            model.addAttribute("categories", categories);
            model.addAttribute("editCategory", product);
            model.addAttribute("newCategory", new CategoryEntity());
        }
        return "categories";
    }


    @PostMapping("/edit")
    public String updateCategory(@ModelAttribute("editCategory") CategoryEntity product) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            if (product.getId() > 0) {
                product.save(conn);
            } else {
                throw new IllegalArgumentException("Category ID is missing or invalid.");
            }
        }
        return "redirect:/categories";
    }


    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable int id) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            CategoryEntity product = CategoryEntity.findById(id, conn);
            if (product != null) {
                product.delete(conn);
            }
        }
        return "redirect:/categories";
    }*/
}
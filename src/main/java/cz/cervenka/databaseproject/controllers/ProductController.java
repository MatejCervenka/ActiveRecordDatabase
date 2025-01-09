package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.database.entities.CategoryEntity;
import cz.cervenka.databaseproject.database.entities.ProductEntity;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private DatabaseConnection dbConnection;

    @GetMapping
    public String listProducts(Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            List<ProductEntity> products = ProductEntity.getAllWithCategoryNames(conn);
            List<CategoryEntity> categories = CategoryEntity.getAll(conn);
            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
            model.addAttribute("editProduct", new ProductEntity());
            model.addAttribute("newProduct", new ProductEntity());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "products";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("newProduct") ProductEntity product) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            product.save(conn);
        }
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable int id, Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            ProductEntity product = ProductEntity.findById(id, conn);
            List<CategoryEntity> categories = CategoryEntity.getAll(conn);
            System.out.println("Product: " + product.getId());
            model.addAttribute("editProduct", product);
            model.addAttribute("categories", categories);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "products";
    }

    @PostMapping("/edit")
    public String updateProduct(@ModelAttribute("editProduct") ProductEntity product) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            if (product.getId() != 0) {
                product.save(conn);
            } else {
                throw new IllegalArgumentException("Invalid product ID for editing");
            }
        }
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable int id) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            ProductEntity product = ProductEntity.findById(id, conn);
            if (product != null) {
                product.delete(conn);
            }
        }
        return "redirect:/products";
    }
}
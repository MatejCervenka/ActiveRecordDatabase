package cz.cervenka.databaseproject.controllers;

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
    public String listProducts(Model model) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            List<ProductEntity> products = ProductEntity.getAllWithCategoryNames(conn);
            System.out.println("Fetched Products: " + products.size());
            model.addAttribute("products", products);
        }
        return "products";
    }

    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new ProductEntity());
        return "addProduct";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute ProductEntity product) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            product.save(conn);
        }
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable int id, Model model) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            ProductEntity product = ProductEntity.findById(id, conn);
            model.addAttribute("product", product);
        }
        return "editProduct";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@ModelAttribute ProductEntity product) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            product.save(conn);
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
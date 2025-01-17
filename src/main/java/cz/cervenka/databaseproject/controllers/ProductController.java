package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.database.entities.CategoryEntity;
import cz.cervenka.databaseproject.database.entities.ProductEntity;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final DatabaseConnection dbConnection;

    public ProductController(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Displays a list of all products and categories.
     * If a success message is available in the session, it is passed to the view and then removed from the session.
     *
     * @param model The model to pass attributes to the view.
     * @param session The HTTP session containing user information and messages.
     * @return The view to display the list of products and categories.
     */
    @GetMapping
    public String listProducts(Model model, HttpSession session) {
        try (Connection conn = dbConnection.getConnection()) {
            List<ProductEntity> products = ProductEntity.getAll(conn);
            List<CategoryEntity> categories = CategoryEntity.getAll(conn);

            String successMessage = (String) session.getAttribute("successMessage");
            if (successMessage != null) {
                model.addAttribute("successMessage", successMessage);
                session.removeAttribute("successMessage");
            }

            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "products";
    }

    /**
     * Redirects the user to the home page when the "previous-page" button is clicked.
     *
     * @param response The HTTP response used to perform the redirection.
     * @throws IOException If an error occurs during redirection.
     */
    @GetMapping("/previous-page")
    public void logout(HttpServletResponse response) throws IOException {
        response.sendRedirect("/home");
    }
}
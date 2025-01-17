package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.database.entities.OrderProductEntity;
import cz.cervenka.databaseproject.database.entities.ProductEntity;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final DatabaseConnection dbConnection;

    public CartController(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Adds a product to the cart.
     * If the product is already in the cart, its quantity is increased by 1.
     * Otherwise, the product is added as a new entry in the cart.
     *
     * @param productId The ID of the product to add to the cart.
     * @param session The HTTP session object to store the cart.
     * @return A redirect to the products page.
     * @throws SQLException If a database error occurs.
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam int productId, HttpSession session) throws SQLException {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        try (Connection conn = dbConnection.getConnection()) {
            ProductEntity product = ProductEntity.findById(productId, conn);
            if (product != null) {
                boolean exists = false;
                for (OrderProductEntity item : cart) {
                    if (item.getProductId() == productId) {
                        item.setQuantity(item.getQuantity() + 1);
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    cart.add(new OrderProductEntity(0, 0, product.getId(), 1, product.getPrice(), product.getName(), product.getStock()));
                }
                session.setAttribute("successMessage", "Product added to cart successfully!");
            }
        }

        session.setAttribute("cart", cart);
        return "redirect:/products";
    }

    /**
     * Displays the current contents of the cart.
     *
     * @param model The model to pass attributes to the view.
     * @param session The HTTP session containing the cart data.
     * @return The cart view.
     */
    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");
        model.addAttribute("cart", cart);

        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }

        return "cart";
    }

    /**
     * Updates the quantity of a product in the cart.
     * If the quantity is invalid (less than 1 or greater than the product stock),
     * an error message is displayed.
     *
     * @param productId The ID of the product to update.
     * @param quantity The new quantity for the product.
     * @param session The HTTP session containing the cart data.
     * @param model The model to pass attributes to the view.
     * @return A redirect to the cart page.
     * @throws SQLException If a database error occurs.
     */
    @PostMapping("/update")
    public String updateQuantity(@RequestParam int productId, @RequestParam int quantity, HttpSession session, Model model) throws SQLException {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");
        if (cart != null) {
            try (Connection conn = dbConnection.getConnection()) {
                ProductEntity product = ProductEntity.findById(productId, conn);
                if (product != null && (quantity < 1 || quantity > product.getStock())) {
                    model.addAttribute("error", "Invalid quantity selected.");
                    return "redirect:/cart";
                }

                for (OrderProductEntity item : cart) {
                    if (item.getProductId() == productId) {
                        item.setQuantity(quantity);
                        break;
                    }
                }
            }
        }
        return "redirect:/cart";
    }

    /**
     * Deletes a product from the cart.
     *
     * @param productId The ID of the product to remove from the cart.
     * @param session The HTTP session containing the cart data.
     * @return A redirect to the cart page.
     */
    @PostMapping("/delete")
    public String deleteFromCart(@RequestParam int productId, HttpSession session) {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> item.getProductId() == productId);
        }
        return "redirect:/cart";
    }

    /**
     * Proceeds to the checkout page if the cart is not empty.
     * If the cart is empty, an error message is displayed.
     *
     * @param session The HTTP session containing the cart data.
     * @param model The model to pass attributes to the view.
     * @return A redirect to the checkout page or the cart page.
     */
    @PostMapping("/checkout")
    public String proceedToCheckout(HttpSession session, Model model) {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            model.addAttribute("error", "Your cart is empty. Add items before proceeding to checkout.");
            return "cart";
        }
        return "redirect:/order/checkout";
    }

    /**
     * Redirects the user back to the previous page (home page).
     *
     * @param response The HTTP response for sending the redirect.
     * @throws IOException If an I/O error occurs during the redirection.
     */
    @GetMapping("/previous-page")
    public void logout(HttpServletResponse response) throws IOException {
        response.sendRedirect("/home");
    }
}
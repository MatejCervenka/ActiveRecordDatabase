package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.database.entities.OrderProductEntity;
import cz.cervenka.databaseproject.database.entities.ProductEntity;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/add")
    public String addToCart(@RequestParam int productId, HttpSession session) throws SQLException {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        try (Connection conn = dbConnection.getConnection()) {
            ProductEntity product = ProductEntity.findById(productId, conn); // Fetch product from DB
            if (product != null) {
                // Check if the product is already in the cart
                boolean exists = false;
                for (OrderProductEntity item : cart) {
                    if (item.getProductId() == productId) {
                        item.setQuantity(item.getQuantity() + 1); // Increment quantity
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    // Add new product to cart
                    cart.add(new OrderProductEntity(0, 0, product.getId(), 1, product.getPrice(), product.getName()));
                }
            }
        }

        session.setAttribute("cart", cart);
        return "redirect:/products";
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam int productId, @RequestParam int quantity, HttpSession session) {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");
        if (cart != null) {
            for (OrderProductEntity item : cart) {
                if (item.getProductId() == productId) {
                    item.setQuantity(quantity);
                    break;
                }
            }
        }
        return "redirect:/cart";
    }

    @PostMapping("/delete")
    public String deleteFromCart(@RequestParam int productId, HttpSession session) {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> item.getProductId() == productId);
        }
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String proceedToCheckout() {
        return "redirect:/order/checkout";
    }


    @GetMapping("/previous-page")
    public void logout(HttpServletResponse response) throws IOException {
        response.sendRedirect("/home");
    }
}
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

    @PostMapping("/add")
    public String addToCart(@RequestParam int productId, HttpSession session, Model model) throws SQLException {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        try (Connection conn = dbConnection.getConnection()) {
            ProductEntity product = ProductEntity.findById(productId, conn);
            if (product != null) {
                if (product.getStock() <= 0) {
                    model.addAttribute("error", "This product is out of stock.");
                    return "redirect:/products";
                }

                boolean exists = false;
                for (OrderProductEntity item : cart) {
                    if (item.getProductId() == productId) {
                        if (item.getQuantity() + 1 > product.getStock()) {
                            model.addAttribute("error", "You cannot add more than available stock.");
                            return "redirect:/products";
                        }
                        item.setQuantity(item.getQuantity() + 1);
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    cart.add(new OrderProductEntity(0, 0, product.getId(), 1, product.getPrice(), product.getName(), product.getStock()));
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
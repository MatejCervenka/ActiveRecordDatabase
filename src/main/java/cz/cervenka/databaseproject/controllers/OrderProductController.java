package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.database.entities.CategoryEntity;
import cz.cervenka.databaseproject.database.entities.OrderEntity;
import cz.cervenka.databaseproject.database.entities.OrderProductEntity;
import cz.cervenka.databaseproject.database.entities.ProductEntity;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/order-products")
public class OrderProductController {

    @Autowired
    private DatabaseConnection dbConnection;

    public OrderProductController(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @GetMapping
    public String listAll(Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            List<OrderProductEntity> orderProducts = OrderProductEntity.getAll(conn);
            List<ProductEntity> products = ProductEntity.getAll(conn);
            List<OrderEntity> orders = OrderEntity.getAll(conn);
            model.addAttribute("orderProducts", orderProducts);
            model.addAttribute("products", products);
            model.addAttribute("orders", orders);
            model.addAttribute("newOrderProduct", new OrderProductEntity());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "order_products";
    }


    @GetMapping("/{orderId}")
    public String getProductsByOrderId(@PathVariable int orderId, Model model) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            List<OrderProductEntity> orderProducts = OrderProductEntity.findByOrderId(orderId, conn);
            model.addAttribute("orderProducts", orderProducts);
            model.addAttribute("newOrderProduct", new OrderProductEntity());
            return "order_products";
        }
    }

    // Get product data for editing
    @GetMapping("/edit/{orderTotal}/{productName}")
    public String editProduct(@PathVariable double orderTotal, @PathVariable String productName, Model model) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            OrderProductEntity orderProduct = OrderProductEntity.findByOrderTotalAndProductName(orderTotal, productName, conn);
            model.addAttribute("editOrderProduct", orderProduct);
            return "order_products";
        }
    }

    // Add or update a product in an order
    @PostMapping
    public String addOrUpdateProduct(@ModelAttribute OrderProductEntity orderProduct, @RequestParam int orderId, Model model) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            orderProduct.setOrderId(orderId); // Ensure the correct orderId is set
            orderProduct.save(conn);
            return "redirect:/" + orderId; // Redirect back to the order's products page
        }
    }

    // Delete a product from an order
    @GetMapping("/delete/{orderTotal}/{productName}")
    public String deleteProduct(@PathVariable double orderTotal, @PathVariable String productName, @RequestParam int orderId) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            OrderProductEntity.delete(orderTotal, productName, conn);
            return "redirect:/" + orderId;
        }
    }

    // Delete all products from an order
    @DeleteMapping("/{orderId}")
    public String deleteAllProductsByOrderId(@PathVariable int orderId) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            OrderProductEntity.deleteByOrderId(orderId, conn);
            return "redirect:/" + orderId;
        }
    }
}

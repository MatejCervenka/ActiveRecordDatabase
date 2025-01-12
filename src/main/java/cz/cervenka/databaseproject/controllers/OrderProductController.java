package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.database.entities.OrderProductEntity;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/order-products")
public class OrderProductController {

    @Autowired
    private DatabaseConnection dbConnection;


    public OrderProductController(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }


    // Get all products by order ID
    @GetMapping("/{orderId}")
    public String getProductsByOrderId(@PathVariable int orderId) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            OrderProductEntity.findByOrderId(orderId, conn);
            return "order_products";
        }
    }

    // Add or update a product in an order
    @PostMapping
    public String addOrUpdateProduct(@RequestBody OrderProductEntity orderProduct) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            orderProduct.save(conn);
            return "Product added or updated successfully.";
        }
    }

    // Delete a product from an order
    @DeleteMapping("/{orderId}/{productId}")
    public String deleteProduct(@PathVariable int orderId, @PathVariable int productId) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            OrderProductEntity.delete(orderId, productId, conn);
            return "Product removed from order.";
        }
    }

    // Delete all products from an order
    @DeleteMapping("/{orderId}")
    public String deleteAllProductsByOrderId(@PathVariable int orderId) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            OrderProductEntity.deleteByOrderId(orderId, conn);
            return "All products removed from order.";
        }
    }
}

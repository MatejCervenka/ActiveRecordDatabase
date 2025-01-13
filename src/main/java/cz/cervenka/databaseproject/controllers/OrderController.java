
package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.database.entities.OrderEntity;
import cz.cervenka.databaseproject.database.entities.UserEntity;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final DatabaseConnection dbConnection;

    public OrderController(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @GetMapping
    public String listOrders(Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            List<OrderEntity> orders = OrderEntity.getAll(conn);
            List<UserEntity> users = UserEntity.getAll(conn);
            model.addAttribute("orders", orders);
            model.addAttribute("users", users);
            model.addAttribute("newOrder", new OrderEntity());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "orders";
    }

    @PostMapping("/add")
    public String addOrder(@ModelAttribute("newOrder") OrderEntity user) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            user.save(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "redirect:/orders";
    }

    @GetMapping("/edit/{id}")
    public String showEditOrderForm(@PathVariable int id, Model model) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            OrderEntity order = OrderEntity.findById(id, conn);
            List<OrderEntity> orders = OrderEntity.getAll(conn);
            List<UserEntity> users = UserEntity.getAll(conn);
            model.addAttribute("orders", orders);
            model.addAttribute("users", users);
            model.addAttribute("editOrder", order);
            model.addAttribute("newOrder", new OrderEntity());
        }
        return "orders";
    }


    @PostMapping("/edit")
    public String updateOrder(@ModelAttribute("editOrder") OrderEntity order) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            if (order.getId() > 0) {
                order.save(conn);
            } else {
                throw new IllegalArgumentException("Order ID is missing or invalid.");
            }
        }
        return "redirect:/orders";
    }


    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable int id) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            OrderEntity order = OrderEntity.findById(id, conn);
            if (order != null) {
                order.delete(conn);
            }
        }
        return "redirect:/orders";
    }
}

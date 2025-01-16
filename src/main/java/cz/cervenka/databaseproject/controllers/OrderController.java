
package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.database.entities.*;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final DatabaseConnection dbConnection;

    public OrderController(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /*@GetMapping("/list")
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
    }*/

    @GetMapping
    public String viewMyOrders(HttpSession session, Model model) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            UserEntity loggedUser = (UserEntity) session.getAttribute("loggedUser");
            if (loggedUser == null) {
                return "redirect:/login";
            }

            CustomerEntity customer = CustomerEntity.findByUserId(loggedUser.getId(), conn);
            if (customer == null) {
                model.addAttribute("error", "No customer linked to this user.");
                return "orders";
            }

            List<Map<String, Object>> orders = OrderEntity.findOrdersByUserId(loggedUser.getId(), conn);
            model.addAttribute("orders", orders);
            return "orders";
        }
    }


    @GetMapping("/checkout")
    public String showCheckoutForm(Model model) {
        model.addAttribute("order", new OrderEntity());
        model.addAttribute("customer", new CustomerEntity());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String checkout(@ModelAttribute CustomerEntity customer, @RequestParam int productId, @RequestParam int quantity) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            ProductEntity product = ProductEntity.findById(productId, conn);
            if (product != null && product.getStock() >= quantity) {
                OrderEntity order = new OrderEntity(customer, product, quantity);
                order.save(conn);
            }
        }
        return "redirect:/home";
    }

    @GetMapping("/confirmation/{orderNumber}")
    public String showOrderConfirmation(@PathVariable String orderNumber, Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            List<Map<String, Object>> orderDetails = OrderEntity.findOrderDetailsByNumber(orderNumber, conn);
            model.addAttribute("orderDetails", orderDetails);
            return "confirmation";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to retrieve order details.");
            return "error";
        }
    }

    @PostMapping
    public String placeOrder(@RequestParam String name, @RequestParam String surname,
                             @RequestParam String email, @RequestParam String phone,
                             HttpSession session, Model model) throws SQLException {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            model.addAttribute("error", "Your cart is empty.");
            return "redirect:/cart";
        }

        UserEntity loggedUser = (UserEntity) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return "redirect:/login";
        }

        String generatedOrderNumber = generateOrderNumber();

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                CustomerEntity customer = new CustomerEntity(0, name, surname, email, phone, false, loggedUser.getId());
                customer.save(conn);
                int customerId = customer.getId();
                if (customerId == 0) {
                    throw new SQLException("Failed to save customer.");
                }

                // Create order
                OrderEntity order = new OrderEntity(0, customerId, LocalDate.now(), generatedOrderNumber,
                        calculateTotalPrice(cart), name, surname, 0);
                order.save(conn);

                // Link cart items to order and validate stock
                for (OrderProductEntity item : cart) {
                    ProductEntity product = ProductEntity.findById(item.getProductId(), conn);
                    if (product == null || product.getStock() < item.getQuantity()) {
                        throw new SQLException("Insufficient stock for product: " + item.getProductName());
                    }

                    // Deduct stock
                    product.setStock(product.getStock() - item.getQuantity());
                    product.save(conn);

                    // Save order product
                    OrderProductEntity orderProduct = new OrderProductEntity(0, order.getId(), item.getProductId(),
                            item.getQuantity(), item.getProductPrice(),
                            item.getProductName(), item.getStock());
                    orderProduct.save(conn);
                }

                conn.commit(); // Commit transaction
                session.removeAttribute("cart"); // Clear cart
                return "redirect:/order/confirmation/" + generatedOrderNumber;

            } catch (SQLException e) {
                conn.rollback(); // Rollback transaction in case of error
                model.addAttribute("error", "Failed to place the order: " + e.getMessage());
                return "checkout";
            }
        }
    }


    @GetMapping("/delete/{orderNumber}")
    public String deleteOrder(@PathVariable String orderNumber, Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                OrderEntity order = OrderEntity.findByOrderNumber(orderNumber, conn);
                if (order != null) {
                    List<OrderProductEntity> orderProducts = OrderProductEntity.findByOrderId(order.getId(), conn);

                    for (OrderProductEntity orderProduct : orderProducts) {
                        ProductEntity product = ProductEntity.findById(orderProduct.getProductId(), conn);
                        if (product != null) {
                            // Increase the stock of the product based on the quantity ordered
                            product.setStock(product.getStock() + orderProduct.getQuantity());
                            product.save(conn);  // Save the updated product stock
                        }
                    }

                    order.deleteWithProducts(conn);
                } else {
                    model.addAttribute("error", "Order not found.");
                    return "error";
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                model.addAttribute("error", "Failed to delete the order.");
                return "error";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "Database error occurred.");
            return "error";
        }

        return "redirect:/order";
    }




    /*@PostMapping("/add")
    public String addOrder(@ModelAttribute("newOrder") OrderEntity order) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            order.save(conn);
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
    }*/

    public static double calculateTotalPrice(List<OrderProductEntity> cart) {
        if (cart == null || cart.isEmpty()) {
            return 0.0;
        }

        double totalPrice = 0.0;
        for (OrderProductEntity orderProduct : cart) {
            if (orderProduct.getProductId() != 0) {
                double productPrice = orderProduct.getProductPrice();
                int quantity = orderProduct.getQuantity();
                totalPrice += productPrice * quantity;
            }
        }
        return totalPrice;
    }


    // here make the method
    public static String generateOrderNumber() {
        Random random = new Random();
        StringBuilder orderNumber = new StringBuilder();

        // Prefix "OBJ"
        orderNumber.append("OBJ");

        // Generate random digits/letters (e.g., 12 characters)
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 10; i++) {  // Adjust length as needed
            orderNumber.append(characters.charAt(random.nextInt(characters.length())));
        }

        // Suffix "CZ"
        orderNumber.append("CZ");

        return orderNumber.toString();
    }

}
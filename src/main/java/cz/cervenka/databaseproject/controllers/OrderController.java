
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


    /**
     * Displays a list of orders for the logged-in user.
     * If the user is not logged in or has no linked customer, an appropriate error message is displayed.
     *
     * @param session The HTTP session containing user information.
     * @param model The model to pass attributes to the view.
     * @return The view to display the user's orders.
     * @throws SQLException If a database error occurs while retrieving orders.
     */
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

    /**
     * Displays the checkout form for placing a new order.
     *
     * @param model The model to pass attributes to the view.
     * @return The checkout view.
     */
    @GetMapping("/checkout")
    public String showCheckoutForm(Model model) {
        if (!model.containsAttribute("error")) {
            model.addAttribute("error", "");
        }
        model.addAttribute("order", new OrderEntity());
        model.addAttribute("customer", new CustomerEntity());
        return "checkout";
    }

    /**
     * Processes the checkout form to create a new order.
     * It validates the selected product's stock and creates an order if valid.
     *
     * @param customer The customer information.
     * @param productId The ID of the selected product.
     * @param quantity The quantity of the product to be ordered.
     * @param model The model to pass attributes to the view.
     * @return The result view after processing the checkout.
     * @throws SQLException If a database error occurs.
     */
    @PostMapping("/checkout")
    public String checkout(@ModelAttribute CustomerEntity customer, @RequestParam int productId,
                           @RequestParam int quantity, Model model, HttpSession session) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            UserEntity loggedUser = (UserEntity) session.getAttribute("loggedUser");
            if (loggedUser == null) {
                return "redirect:/login"; // Redirect to login page if not logged in
            }
            ProductEntity product = ProductEntity.findById(productId, conn);
            if (product == null) {
                model.addAttribute("error", "Selected product not found.");
                return "checkout";
            }
            if (product.getStock() < quantity) {
                model.addAttribute("error", "Insufficient stock for the selected product.");
                return "checkout";
            }

            OrderEntity order = new OrderEntity(customer, product, quantity);
            order.save(conn);
            return "redirect:/home";
        }
    }

    /**
     * Displays the order confirmation page with the details of the specified order number.
     *
     * @param orderNumber The order number.
     * @param model The model to pass attributes to the view.
     * @return The order confirmation view.
     */
    @GetMapping("/confirmation/{orderNumber}")
    public String showOrderConfirmation(@PathVariable String orderNumber, Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            List<Map<String, Object>> orderDetails = OrderEntity.findOrderDetailsByNumber(orderNumber, conn);
            model.addAttribute("orderDetails", orderDetails);
            return "confirmation";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to retrieve order details.");
            return "checkout";
        }
    }

    /**
     * Places an order from the user's cart and processes the customer details.
     * If the cart is empty or the customer details are invalid, an error message is displayed.
     *
     * @param name The customer's first name.
     * @param surname The customer's last name.
     * @param email The customer's email address.
     * @param phone The customer's phone number.
     * @param session The HTTP session containing cart and user information.
     * @param model The model to pass attributes to the view.
     * @return The result view after placing the order.
     */
    @PostMapping
    public String placeOrder(@RequestParam String name, @RequestParam String surname,
                             @RequestParam String email, @RequestParam String phone,
                             HttpSession session, Model model) {
        List<OrderProductEntity> cart = (List<OrderProductEntity>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            model.addAttribute("error", "Your cart is empty.");
            return "redirect:/order/checkout";
        }

        UserEntity loggedUser = (UserEntity) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return "redirect:/login";
        }

        if (!isValidCustomer(name, surname, email, phone)) {
            model.addAttribute("error", "Invalid input. Please check your details.");
            return "redirect:/order/checkout";
        }

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            String generatedOrderNumber = generateOrderNumber();

            try {
                CustomerEntity customer = new CustomerEntity(0, name, surname, email, phone, false, loggedUser.getId());
                customer.save(conn);
                OrderEntity order = new OrderEntity(0, customer.getId(), LocalDate.now(), generatedOrderNumber,
                        calculateTotalPrice(cart), name, surname, 0);
                order.save(conn);

                for (OrderProductEntity item : cart) {
                    ProductEntity product = ProductEntity.findById(item.getProductId(), conn);
                    if (product == null || product.getStock() < item.getQuantity()) {
                        throw new SQLException("Insufficient stock for product: " + item.getProductName());
                    }
                    product.setStock(product.getStock() - item.getQuantity());
                    product.save(conn);

                    OrderProductEntity orderProduct = new OrderProductEntity(0, order.getId(), item.getProductId(),
                            item.getQuantity(), item.getProductPrice(), item.getProductName(), item.getStock());
                    orderProduct.save(conn);
                }

                conn.commit();
                session.removeAttribute("cart");
                return "redirect:/order/confirmation/" + generatedOrderNumber;

            } catch (SQLException e) {
                conn.rollback();
                model.addAttribute("error", "Order failed: " + e.getMessage());
                return "redirect:/order/checkout";
            }
        } catch (SQLException e) {
            model.addAttribute("error", "Database error occurred. Please try again.");
            return "redirect:/order/checkout";
        }
    }

    /**
     * Deletes the specified order and restores the stock for the associated products.
     *
     * @param orderNumber The order number.
     * @param model The model to pass attributes to the view.
     * @return The result view after deleting the order.
     */
    @GetMapping("/delete/{orderNumber}")
    public String deleteOrder(@PathVariable String orderNumber, Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                OrderEntity order = OrderEntity.findByOrderNumber(orderNumber, conn);
                if (order != null) {
                    CustomerEntity customer = CustomerEntity.findById(order.getCustomer_id(), conn);

                    List<OrderProductEntity> orderProducts = OrderProductEntity.findByOrderId(order.getId(), conn);
                    for (OrderProductEntity orderProduct : orderProducts) {
                        ProductEntity product = ProductEntity.findById(orderProduct.getProductId(), conn);
                        if (product != null) {
                            product.setStock(product.getStock() + orderProduct.getQuantity());
                            product.save(conn);
                        }
                    }

                    order.deleteWithProducts(conn);
                    if (customer != null) {
                        customer.delete(conn);
                    }
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

    /**
     * Calculates the total price of the products in the cart.
     *
     * @param cart The list of products in the cart.
     * @return The total price.
     */
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

    /**
     * Generates a unique order number.
     *
     * @return A randomly generated order number.
     */
    public static String generateOrderNumber() {
        Random random = new Random();
        StringBuilder orderNumber = new StringBuilder();

        orderNumber.append("OBJ");

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 10; i++) {
            orderNumber.append(characters.charAt(random.nextInt(characters.length())));
        }

        orderNumber.append("CZ");

        return orderNumber.toString();
    }

    /**
     * Validates customer details for the order.
     *
     * @param name The customer's first name.
     * @param surname The customer's last name.
     * @param email The customer's email address.
     * @param phone The customer's phone number.
     * @return True if the customer details are valid, false otherwise.
     */
    private boolean isValidCustomer(String name, String surname, String email, String phone) {
        return isValidName(name) && isValidSurname(surname) && isValidEmail(email) && isValidPhone(phone);
    }

    /**
     * Validates the customer's name.
     *
     * @param name The customer's name.
     * @return True if the name is valid, false otherwise.
     */
    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    /**
     * Validates the customer's surname.
     *
     * @param surname The customer's surname.
     * @return True if the surname is valid, false otherwise.
     */
    private boolean isValidSurname(String surname) {
        return surname != null && !surname.trim().isEmpty();
    }

    /**
     * Validates the customer's email address.
     *
     * @param email The customer's email.
     * @return True if the email is valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Validates the customer's phone number.
     *
     * @param phone The customer's phone number.
     * @return True if the phone number is valid, false otherwise.
     */
    private boolean isValidPhone(String phone) {
        String phoneRegex = "^\\+?[0-9]{1,4}[-\\s]?[0-9]{1,15}$";
        return phone != null && phone.matches(phoneRegex);
    }

}
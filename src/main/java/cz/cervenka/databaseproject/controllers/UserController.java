package cz.cervenka.databaseproject.controllers;

import cz.cervenka.databaseproject.database.entities.CategoryEntity;
import cz.cervenka.databaseproject.database.entities.UserEntity;
import cz.cervenka.databaseproject.database.entities.UserEntity;
import cz.cervenka.databaseproject.utils.DatabaseConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/")
public class UserController {

    private final DatabaseConnection dbConnection;

    public UserController(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Displays the user registration form.
     * This method is called when a user navigates to the registration page.
     *
     * @param model The model used to pass the user object to the view.
     * @return The registration form view.
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserEntity());
        return "register";
    }

    /**
     * Processes the user registration form submission.
     * This method registers a new user by validating the input, hashing the password,
     * and saving the user to the database. It also stores the user in the session if successful.
     *
     * @param user The user entity containing the registration details.
     * @param session The HTTP session used to store the logged-in user.
     * @param model The model used to pass the error message to the view in case of failure.
     * @return The next view, either the home page (on successful registration) or the registration form (on failure).
     * @throws SQLException If an error occurs while interacting with the database.
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserEntity user, HttpSession session, Model model) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            // Validate user registration input
            if (!user.isInvalidRegistration(user)) {
                // Hash the password before saving it to the database
                user.setPassword(UserEntity.hashPassword(user.getPassword()));
                // Save the user to the database
                user.save(conn);
                // Store the logged-in user in the session
                session.setAttribute("loggedUser", user);
                return "redirect:/home";
            }
        }
        // In case of failure, display an error message on the registration page
        model.addAttribute("errorMessage", "Registration failed. Please check your input.");
        return "register";
    }

    /**
     * Displays the user login page.
     * This method is called when a user navigates to the login page.
     *
     * @param model The model used to pass the user object to the view.
     * @return The login form view.
     */
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("user", new UserEntity());
        return "login";
    }

    /**
     * Processes the user login form submission.
     * This method validates the user's credentials (email and hashed password) and,
     * if valid, stores the user in the session. If the credentials are invalid,
     * an error message is displayed.
     *
     * @param user The user entity containing the login details (email and password).
     * @param session The HTTP session used to store the logged-in user.
     * @param model The model used to pass the error message to the view in case of failure.
     * @return The next view, either the home page (on successful login) or the login form (on failure).
     * @throws SQLException If an error occurs while interacting with the database.
     */
    @PostMapping("/login")
    public String loginUser(@ModelAttribute("user") UserEntity user, HttpSession session, Model model) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            // Hash the entered password for validation
            String hashedPassword = UserEntity.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);

            // Validate the user's credentials (email and password)
            if (user.isValid(conn)) {
                // Store the logged-in user in the session
                session.setAttribute("loggedUser", user);
                return "redirect:/home";
            }
        }
        // In case of invalid credentials, display an error message on the login page
        model.addAttribute("errorMessage", "Invalid email or password.");
        return "login";
    }
}
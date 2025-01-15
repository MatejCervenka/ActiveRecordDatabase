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

    /*private final DatabaseConnection dbConnection;

    public UserController(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @GetMapping
    public String listUsers(Model model) {
        try (Connection conn = dbConnection.getConnection()) {
            List<UserEntity> users = UserEntity.getAll(conn);
            model.addAttribute("users", users);
            model.addAttribute("newUser", new UserEntity());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "users";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute("newUser") UserEntity user) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            user.save(conn);
        }
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable int id, Model model) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            UserEntity user = UserEntity.findById(id, conn);
            List<UserEntity> users = UserEntity.getAll(conn);
            model.addAttribute("users", users);
            model.addAttribute("editUser", user);
            model.addAttribute("newUser", new UserEntity());
        }
        return "users";
    }


    @PostMapping("/edit")
    public String updateUser(@ModelAttribute("editUser") UserEntity user) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            if (user.getId() > 0) {
                user.save(conn);
            } else {
                throw new IllegalArgumentException("User ID is missing or invalid.");
            }
        }
        return "redirect:/users";
    }


    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable int id) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            UserEntity user = UserEntity.findById(id, conn);
            if (user != null) {
                user.delete(conn);
            }
        }
        return "redirect:/users";
    }*/

    private final DatabaseConnection dbConnection;

    public UserController(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserEntity());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserEntity user) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            user.setPassword(UserEntity.hashPassword(user.getPassword()));
            user.save(conn);
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("user", new UserEntity());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("user") UserEntity user, HttpSession session) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            String hashedPassword = UserEntity.hashPassword(user.getPassword());

            user.setPassword(hashedPassword);

            if (user.isValid(conn)) {
                session.setAttribute("loggedUser", user);
                return "redirect:/home";
            }
        }
        return "login";
    }
}
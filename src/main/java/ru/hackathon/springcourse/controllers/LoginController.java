package ru.hackathon.springcourse.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.hackathon.springcourse.dao.PeopleDAO;
import ru.hackathon.springcourse.models.Users;
import ru.hackathon.springcourse.services.UserService;
import ru.hackathon.springcourse.validator.ValidForm;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final UserService userService;
    private final ValidForm validForm;

    public LoginController(UserService userService, ValidForm validForm) {
        this.userService = userService;
        this.validForm = validForm;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userForm", new Users());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userForm") Users userForm,
                               BindingResult bindingResult, Model model) {

        logger.info("Attempting to register user with email: {}", userForm.getEmail());
        validForm.valid(bindingResult,userForm);
        if (bindingResult.hasErrors()) {
            logger.warn("Registration validation failed for email: {}. Errors: {}", userForm.getEmail(), bindingResult.getAllErrors());
            return "register"; // Показываем ошибки на той же странице
        }
        try {
            userService.saveUser(userForm);
            logger.info("Successfully called saveUser for email: {}", userForm.getEmail());
        } catch (Exception e) {
            logger.error("Error saving user with email: {}", userForm.getEmail(), e);
            model.addAttribute("registrationError", "An unexpected error occurred during registration.");
            return "register";
        }
        return "redirect:/login";
    }
} 
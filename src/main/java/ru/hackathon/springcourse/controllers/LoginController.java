package ru.hackathon.springcourse.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.hackathon.springcourse.models.Users;
import ru.hackathon.springcourse.services.UserService;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
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
                               BindingResult bindingResult) {

        userService.saveUser(userForm);
        return "redirect:/login";
    }
} 
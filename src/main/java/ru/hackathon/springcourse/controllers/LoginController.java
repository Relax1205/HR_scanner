package ru.hackathon.springcourse.controllers;

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

        validForm.valid(bindingResult,userForm);
        if (bindingResult.hasErrors()) {
            return "register"; // Показываем ошибки на той же странице
        }
        userService.saveUser(userForm);
        return "redirect:/login";
    }
} 
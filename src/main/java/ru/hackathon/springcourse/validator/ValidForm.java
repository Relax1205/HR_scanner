package ru.hackathon.springcourse.validator;

import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.hackathon.springcourse.models.Users;
import ru.hackathon.springcourse.services.UserService;

@Component
public class ValidForm {

    private final UserService userService;

    public ValidForm(UserService userService) {
        this.userService = userService;
    }

    public void valid(BindingResult bindingResult, @Valid Users dto){
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Пароли не совпадают");
        }

        if (userService.findByEmail(dto.getEmail()) != null) {
            bindingResult.rejectValue("email", "error.email", "Пользователь с таким email уже зарегистрирован");
        }
    }

}

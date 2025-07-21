package ru.hackathon.springcourse.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.hackathon.springcourse.dao.PeopleDAO;
import ru.hackathon.springcourse.models.People;

import java.util.List;

@Controller
@RequestMapping("/go")
public class WebController {

    private final PeopleDAO peopleDAO;

    @Autowired
    public WebController(PeopleDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    @GetMapping("")
    public String workers(Model model) {
        List<People> peopleList = peopleDAO.index();
        model.addAttribute("peopleList", peopleList);
        return "index";
    }
}

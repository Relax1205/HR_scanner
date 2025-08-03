package ru.hackathon.springcourse.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.hackathon.springcourse.dao.PeopleDAO;
import ru.hackathon.springcourse.models.People;
import ru.hackathon.springcourse.services.ServiceWork;

import java.util.List;

@Controller
@RequestMapping("/go")
public class WebController {

    private final PeopleDAO peopleDAO;
    private final ServiceWork serviceWork;

    @Autowired
    public WebController(PeopleDAO peopleDAO, ServiceWork serviceWork) {
        this.peopleDAO = peopleDAO;
        this.serviceWork = serviceWork;
    }


    @GetMapping("")
    public String workers(@RequestParam(value = "job", required = false) String job, Model model) {
        serviceWork.work(job, model);
        return "index";
    }


    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") int id){
        peopleDAO.delete(id);
        return  "redirect:/go";

    }
}

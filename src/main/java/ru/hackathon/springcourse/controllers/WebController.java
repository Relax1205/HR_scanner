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
    public String workers(@RequestParam(value = "job", required = false) String job, Model model) {
        if(job != null) {
            List<People> peopleList = peopleDAO.findByJob(job);
            model.addAttribute("peopleList", peopleList);
            model.addAttribute("job", job);

        }
        else {
            List<People> peopleList = peopleDAO.index();
            model.addAttribute("peopleList", peopleList);
            model.addAttribute("job", job);
        }
        return "index";
    }



    @PostMapping("/{id}")
    public String delete(@PathVariable("id") int id){
        peopleDAO.delete(id);
        return  "redirect:/go";

    }
}

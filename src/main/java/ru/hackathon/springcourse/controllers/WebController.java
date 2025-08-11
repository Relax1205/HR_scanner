package ru.hackathon.springcourse.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.hackathon.springcourse.dao.PeopleDAO;
import ru.hackathon.springcourse.models.People;
import ru.hackathon.springcourse.services.ServiceWork;
import ru.hackathon.springcourse.services.UpdateService;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/go")
public class WebController {

    private final PeopleDAO peopleDAO;
    private final ServiceWork serviceWork;
    private final UpdateService updateService;

    @Autowired
    public WebController(PeopleDAO peopleDAO, ServiceWork serviceWork, UpdateService updateService) {
        this.peopleDAO = peopleDAO;
        this.serviceWork = serviceWork;
        this.updateService = updateService;
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
    @GetMapping("/update")
    @ResponseBody
    public String getUpdate() throws IOException {
        People people = updateService.trans_json();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(people);
    }

    @PostMapping("/update")
    public String creatNewResum() throws IOException {
        updateService.v_trans_json();
        return "redirect:/go";
    }
}

package ru.hackathon.springcourse.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hackathon.springcourse.dao.PeopleDAO;
import ru.hackathon.springcourse.models.People;
import ru.hackathon.springcourse.services.ServiceWork;
import ru.hackathon.springcourse.services.UpdateService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

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
    public String creatNewResum(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            File jsonFile = new File("/data/data.json");

            ObjectMapper mapper = new ObjectMapper();
            People person = mapper.readValue(jsonFile, People.class);

            System.out.println("JSON изменился");
            peopleDAO.save(person);

            System.out.println("JSON успешно сохранён в БД: " + person.getName());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка при сохранении JSON в БД");
        }

        return "redirect:/go";
    }

    @PostMapping("/update-json")
    public Object updateFromJson(HttpServletRequest request) {
        try {
            File jsonFile = new File("/data/data.json");

            ObjectMapper mapper = new ObjectMapper();
            People person = mapper.readValue(jsonFile, People.class);

            System.out.println("JSON изменился");
            peopleDAO.save(person);

            System.out.println("JSON успешно сохранён в БД: " + person.getName());

            String accept = request.getHeader("Accept");
            String xrw = request.getHeader("X-Requested-With");
            if ((accept != null && accept.contains("application/json")) ||
                    (xrw != null && xrw.equalsIgnoreCase("XMLHttpRequest"))) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("id", person.getId());
                payload.put("name", person.getName());
                payload.put("job", person.getJob());
                payload.put("persent", person.getPersent());
                return new ResponseEntity<>(payload, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка при сохранении JSON в БД");
            String accept = request.getHeader("Accept");
            String xrw = request.getHeader("X-Requested-With");
            if ((accept != null && accept.contains("application/json")) ||
                    (xrw != null && xrw.equalsIgnoreCase("XMLHttpRequest"))) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Ошибка при сохранении JSON в БД");
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return "redirect:/go";
    }
}

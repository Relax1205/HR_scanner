package ru.hackathon.springcourse.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hackathon.springcourse.dao.PeopleDAO;
import ru.hackathon.springcourse.models.People;

import java.io.File;
import java.io.IOException;

@Service
public class UpdateService {

    private final PeopleDAO peopleDAO;

    public UpdateService(PeopleDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    public People trans_json() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        People people = objectMapper.readValue(new File("src/main/temp_json/data.json"), People.class);
        return people;
    }

    public void v_trans_json() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        People people = objectMapper.readValue(new File("src/main/temp_json/data.json"), People.class);
        System.out.println("Saving person: " + people);
        peopleDAO.save(people);
    }
}

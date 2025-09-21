package ru.hackathon.springcourse.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.hackathon.springcourse.dao.PeopleDAO;
import ru.hackathon.springcourse.models.People;

import java.util.List;

@Service
public class ServiceWork {
    private final PeopleDAO peopleDAO;

    @Autowired
    public ServiceWork(PeopleDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    public void work(String job, Model model){
        if(job != null) {
            List<People> peopleList = peopleDAO.findByJob(job);
            model.addAttribute("peopleList", peopleList);
            model.addAttribute("job", job);

        }else {
            List<People> peopleList = peopleDAO.index();
            model.addAttribute("peopleList", peopleList);
        }
    }
}

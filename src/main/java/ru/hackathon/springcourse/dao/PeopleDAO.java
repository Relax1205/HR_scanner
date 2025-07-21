package ru.hackathon.springcourse.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.springcourse.models.People;

import java.util.List;

@Component
public class PeopleDAO {

    private final SessionFactory sessionFactory;

    @Autowired
    public PeopleDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(People people){
        Session session = sessionFactory.getCurrentSession();
        session.save(people);
    }

    @Transactional(readOnly = true)
    public List<People> index(){
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("select p from People p", People.class).getResultList();
    }

    @Transactional(readOnly = true)
    public People show(int id){
        Session session = sessionFactory.getCurrentSession();
        People people = session.get(People.class, id);

        return people;

    }

    @Transactional
    public void update(int id){
        Session session = sessionFactory.getCurrentSession();
        People peopleUpdate = session.get(People.class, id);

        peopleUpdate.setName(peopleUpdate.getName());
        peopleUpdate.setJob(peopleUpdate.getJob());
        peopleUpdate.setPersent(peopleUpdate.getPersent());
        peopleUpdate.setSkills(peopleUpdate.getSkills());
    }

    @Transactional
    public void delet(int id){
        Session session = sessionFactory.getCurrentSession();
        People peopleDelete = session.get(People.class, id);

        session.remove(peopleDelete);
    }
}

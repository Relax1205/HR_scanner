package ru.hackathon.springcourse.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.springcourse.models.People;

import java.util.List;


@Component
@Repository
public class PeopleDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(People people){
        entityManager.persist(people);
    }

    @Transactional(readOnly = true)
    public List<People> index(){
        return entityManager
                .createQuery("SELECT p FROM People p", People.class)
                .getResultList();
    }

    public List<People> findByJob(String job) {
        return entityManager
                .createQuery("SELECT p FROM People p WHERE p.job = :job", People.class)
                .setParameter("job", job)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public People show(int id){
        return entityManager.find(People.class, id);
    }

    @Transactional
    public void update(int id, People updatedPeople){
        People people = entityManager.find(People.class, id);
        if (people != null) {
            people.setName(updatedPeople.getName());
            people.setJob(updatedPeople.getJob());
            people.setPersent(updatedPeople.getPersent());
            people.setSkills(updatedPeople.getSkills());
        }
    }

    @Transactional
    public void delete(int id){
        People people = entityManager.find(People.class, id);
        if (people != null) {
            entityManager.remove(people);
        }
    }
}
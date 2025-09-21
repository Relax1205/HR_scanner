package ru.hackathon.springcourse.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.springcourse.models.Users;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void saveUser(Users users) {
        logger.info("Inside saveUser for email: {}", users.getEmail());
        users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));
        users.setRole("USER");
        entityManager.persist(users);
        logger.info("User {} persisted to DB.", users.getEmail());
    }

    public Users findByEmail(String email) {
        return entityManager
                .createQuery("SELECT u FROM Users u WHERE u.email = :email", Users.class)
                .setParameter("email", email)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null); // или orElseThrow
    }
}

package ru.hackathon.springcourse.models;

import jakarta.persistence.*;

@Entity
@Table(name="skills")
public class Skills{
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="skill")
    private String skill;

    @ManyToOne
    @JoinColumn(name = "people_id")
    private People people_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public People getPeople_id() {
        return people_id;
    }

    public void setPeople_id(People people_id) {
        this.people_id = people_id;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }
}
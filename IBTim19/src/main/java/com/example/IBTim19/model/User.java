package com.example.IBTim19.model;

import jakarta.persistence.*;

@Entity
@Table
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    @Column
    private String username;


    public User(Integer id, String username) {
        this.id = id;
        this.username = username;
    }

    public User() {

    }
}

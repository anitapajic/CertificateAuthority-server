package com.example.IBTim19.repository;

import com.example.IBTim19.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Integer> {
    public List<Request> findAll();
    public List<Request> findAllBySubjectUsername(String username);


}

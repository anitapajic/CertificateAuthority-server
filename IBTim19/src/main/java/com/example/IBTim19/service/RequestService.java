package com.example.IBTim19.service;

import com.example.IBTim19.model.Request;
import com.example.IBTim19.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestService {
    @Autowired
    private RequestRepository requestRepository;

    public List<Request> findAll(){return this.requestRepository.findAll();}
    public List<Request> findAllBySubjectUsername(String username){return this.requestRepository.findAllBySubjectUsername(username);}
}

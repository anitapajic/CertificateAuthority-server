package com.example.IBTim19.service;

import com.example.IBTim19.repository.CertificateRepository;
import com.example.IBTim19.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CertificateGenerator {

    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private UserRepository userRepository;


}

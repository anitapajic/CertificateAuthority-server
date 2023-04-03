package com.example.IBTim19.service;

import com.example.IBTim19.repository.CertificateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CertificateGenerator {

    @Autowired
    private CertificateRepository certificateRepository;
}

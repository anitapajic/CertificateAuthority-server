package com.example.IBTim19.service;

import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.repository.CertificateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    public Certificate findOneBySerialNumber(String sn) {
        return this.certificateRepository.findOneBySerialNumber(sn);
    }

    public List<Certificate> findAll(){return certificateRepository.findAll();}

}

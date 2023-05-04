package com.example.IBTim19.service;

import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.repository.CertificateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    public Certificate findOneBySerialNumber(String sn) {

        return this.certificateRepository.findOneBySerialNumber(sn);
    }

    public List<Certificate> findAll(){return certificateRepository.findAll();}

    public List<Certificate> findAllByUsername(String username){
        return this.certificateRepository.findAllByUsername(username);
    }
    public Certificate findOneById(Integer id){return this.certificateRepository.findOneById(id);}

    public List<Certificate> findAllByIssuer(String issuer){
        return this.certificateRepository.findAllByIssuer(issuer).orElse(null);
    }

    public Certificate save(Certificate cert){
        return this.certificateRepository.save(cert);
    }
}

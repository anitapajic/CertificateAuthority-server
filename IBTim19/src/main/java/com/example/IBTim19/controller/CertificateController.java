package com.example.IBTim19.controller;

import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.model.IssueCertificateContracts;
import com.example.IBTim19.repository.CertificateRepository;
import com.example.IBTim19.service.CertificateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api")
@CrossOrigin(value="*")
public class CertificateController {

    @Autowired
    private CertificateRepository _certificateRepository;

    @Autowired
    private CertificateGenerator _certificateGenerator;
    public CertificateController(CertificateRepository certificateRepository, CertificateGenerator certificateGenerator) {
        _certificateRepository = certificateRepository;
        _certificateGenerator = certificateGenerator;
    }
    @GetMapping("/certificates")
    public List<Certificate> getAllCertificates() {
        return _certificateRepository.findAll();
    }

    @PostMapping("/certificates")
    public ResponseEntity issueCertificate(@RequestBody IssueCertificateContracts contract) {
        try {
            Certificate certificate = _certificateGenerator.IssueCertificate(contract.getIssuerSN(), contract.getSubjectUsername(), contract.getKeyUsageFlags(), contract.getDate());
            return ResponseEntity.ok(certificate);
        }
        catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

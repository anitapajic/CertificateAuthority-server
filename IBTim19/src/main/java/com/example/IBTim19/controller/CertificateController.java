package com.example.IBTim19.controller;

import com.example.IBTim19.DTO.LoginDTO;
import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.model.IssueCertificateContracts;
import com.example.IBTim19.model.User;
import com.example.IBTim19.service.CertificateGenerator;
import com.example.IBTim19.service.CertificateService;
import com.example.IBTim19.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api")
@CrossOrigin(value="*")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private CertificateGenerator certificateGenerator;
    @Autowired
    private UserService userService;


//    /api/certificates
    @GetMapping("/certificates")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public List<Certificate> getAllCertificates() {
        return certificateService.findAll();
    }

//    /api/certificate
    @PostMapping("/certificate")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity issueCertificate(@RequestBody IssueCertificateContracts contract) {
        try {
            Certificate certificate = certificateGenerator.IssueCertificate(contract.getIssuerSN(), contract.getSubjectUsername(), contract.getKeyUsageFlags(), contract.getDate());
            return new ResponseEntity<>(certificate, HttpStatus.OK);
        }
        catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/myRequests")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity getUserRequests(@AuthenticationPrincipal UserDetails userDetails){
        try {
            List<Certificate> certificates = certificateService.findAllByUsername(userDetails.getUsername());

             return new ResponseEntity<>(certificates, HttpStatus.OK);
        }
        catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

//    @GetMapping(value = "/certificate/{sn}")
//    public ResponseEntity validate(@PathVariable String sn) throws MessagingException, UnsupportedEncodingException {
//
//        if(sn==null){
//            return new ResponseEntity<>("Certificate with this serial number does not exist!", HttpStatus.NOT_FOUND);
//        }
//        Certificate cert = certificateService.findOneBySerialNumber(sn);
//        Certificate issuerCertificate = certificateService.findOneBySerialNumber(cert.issuer);
//        if(cert.validTo.after(new Date()) && cert.getValidTo().before(issuerCertificate.getValidTo())){
//            return new ResponseEntity<>("This certificate is valid!", HttpStatus.OK);
//        }
//
//        return new ResponseEntity(HttpStatus.BAD_REQUEST);
//    }

}

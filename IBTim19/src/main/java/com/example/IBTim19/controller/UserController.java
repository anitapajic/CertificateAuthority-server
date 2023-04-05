package com.example.IBTim19.controller;

import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.service.CertificateService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;

@RestController
@RequestMapping(value = "/api")
@CrossOrigin(value="*")
public class UserController {


    @Autowired
    private CertificateService certificateService;


    @GetMapping(value = "/certificate/{sn}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity validate(@PathVariable String sn) throws MessagingException, UnsupportedEncodingException {

        if(sn==null){
            return new ResponseEntity<>("Certificate with this serial number does not exist!", HttpStatus.NOT_FOUND);
        }
        Certificate cert = certificateService.findOneBySerialNumber(sn);
        Certificate issuerCertificate = certificateService.findOneBySerialNumber(cert.issuer);
        if(cert.validTo.after(new Date()) && cert.getValidTo().before(issuerCertificate.getValidTo())){
            return new ResponseEntity<>("This certificate is valid!", HttpStatus.OK);
        }

        return new ResponseEntity(HttpStatus.BAD_REQUEST);

    }

    }

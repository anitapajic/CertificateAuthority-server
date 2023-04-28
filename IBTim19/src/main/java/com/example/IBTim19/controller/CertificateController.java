package com.example.IBTim19.controller;

import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.model.CertificateStatus;
import com.example.IBTim19.service.CertificateGenerator;
import com.example.IBTim19.service.CertificateService;
import com.example.IBTim19.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/api/certificate")
@CrossOrigin(value="*")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private CertificateGenerator certificateGenerator;
    @Autowired
    private UserService userService;


    @GetMapping(value = "/validate/{sn}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity validate(@PathVariable String sn) {

        if (sn == null) {
            return new ResponseEntity<>("Certificate with this serial number does not exist!", HttpStatus.NOT_FOUND);
        }
        
        Certificate cert = certificateService.findOneBySerialNumber(sn);

        if(cert.getIssuer()==null){
            if(cert.validTo.after(new Date())){
                return new ResponseEntity<>("This is root certificate and it's valid!", HttpStatus.OK);
            }
            return new ResponseEntity("Root certificate is not valid!", HttpStatus.BAD_REQUEST);
        }

        Certificate issuerCertificate = certificateService.findOneBySerialNumber(cert.issuer);
        try {
            X509Certificate certificate = certificateGenerator.readCertificateFromFile(String.format("%s/%s.crt", "crts", sn));
            X509Certificate issCertificate = certificateGenerator.readCertificateFromFile(String.format("%s/%s.crt", "crts", issuerCertificate.getSerialNumber()));
            //certificate.verify(issCertificate.getPublicKey());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (cert.validTo.after(new Date()) && cert.getValidTo().before(issuerCertificate.getValidTo()) && issuerCertificate.getStatus().equals(CertificateStatus.Valid) && cert.getStatus().equals(CertificateStatus.Valid)) {
            return new ResponseEntity<>("This certificate is valid!", HttpStatus.OK);
        }

        return new ResponseEntity<>("This certificate is not valid! ",HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public List<Certificate> getAllCertificates() {
        return certificateService.findAll();
    }


    @GetMapping(value = "/download/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public Resource downloadCertificate(@PathVariable Integer id){
        String serialNumber = certificateService.findOneById(id).getSerialNumber();
        return new FileSystemResource("crts/" + serialNumber + ".crt");
    }


}

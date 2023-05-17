package com.example.IBTim19.controller;

import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.model.CertificateStatus;
import com.example.IBTim19.service.CertificateGenerator;
import com.example.IBTim19.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.cert.X509Certificate;
import java.util.Date;

@RestController
@RequestMapping(value = "/api")
public class UserController {


    @Autowired
    private CertificateService certificateService;
    @Autowired
    private CertificateGenerator certificateGenerator;


    @GetMapping(value = "/certificate/{sn}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity validate(@PathVariable String sn) {

        if (sn == null) {
            return new ResponseEntity<>("Certificate with this serial number does not exist!", HttpStatus.NOT_FOUND);
        }
        try {
            Certificate cert = certificateService.findOneBySerialNumber(sn);
            Certificate issuerCertificate = certificateService.findOneBySerialNumber(cert.getIssuer());

            X509Certificate certificate = certificateGenerator.readCertificateFromFile(String.format("%s/%s.crt", "crts", sn));
            X509Certificate issCertificate = certificateGenerator.readCertificateFromFile(String.format("%s/%s.crt", "crts", issuerCertificate.getSerialNumber()));

//            certificate.verify(issCertificate.getPublicKey());

            if (cert.validTo.after(new Date()) && cert.getValidTo().before(issuerCertificate.getValidTo()) && issuerCertificate.getStatus().equals(CertificateStatus.Valid) && cert.getStatus().equals(CertificateStatus.Valid)) {
                return new ResponseEntity<>("This certificate is valid!", HttpStatus.OK);
            }
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
//        catch (CertificateException | InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
//            return new ResponseEntity<>("This certificate is not valid!", HttpStatus.BAD_REQUEST);
//        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}

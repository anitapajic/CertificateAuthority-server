package com.example.IBTim19.controller;

import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.model.CertificateStatus;
import com.example.IBTim19.model.CertificateType;
import com.example.IBTim19.service.CertificateGenerator;
import com.example.IBTim19.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/api/certificate")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private CertificateGenerator certificateGenerator;


    @GetMapping(value = "/validate/{sn}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity validate(@PathVariable String sn) {

        if (sn == null) {
            return new ResponseEntity<>("Certificate with this serial number does not exist!", HttpStatus.NOT_FOUND);
        }
        System.out.println(sn);
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

        if (cert.validTo.after(new Date()) && cert.getValidTo().before(issuerCertificate.getValidTo()) && issuerCertificate.getStatus().equals(CertificateStatus.Valid) && cert.getStatus().equals(CertificateStatus.Valid) && cert.getIsRevoked().equals(false)) {
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

    @PostMapping(value="/redraw/{sn}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity redrawCertificate(@PathVariable String sn){

        Certificate myCert = certificateService.findOneBySerialNumber(sn);

        if(myCert.certificateType.equals(CertificateType.Root)){
            return new ResponseEntity<>("You can't redraw root certificate!", HttpStatus.BAD_REQUEST);
        }

        myCert.setIsRevoked(true);
        certificateService.save(myCert);

        List<Certificate> issuedCertificates = certificateService.findAllByIssuer(sn);
        certificateService.setRevokedStatus(issuedCertificates);

        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping(value = "/validateByCopy")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity validateByCopy(@RequestParam("file") MultipartFile file) throws IOException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        ///TODO : ko zna da l radi
        byte[] certBytes = file.getBytes();
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
        Certificate issuerCertificate = certificateService.findOneBySerialNumber(String.valueOf(cert.getSerialNumber()));
        X509Certificate issuer = certificateGenerator.readCertificateFromFile(String.format("%s/%s.crt", "crts", issuerCertificate.getSerialNumber()));
        PublicKey publicKey = issuer.getPublicKey();
        cert.verify(publicKey);
        return new ResponseEntity<>("Certificate is valid!", HttpStatus.OK);
    }


}

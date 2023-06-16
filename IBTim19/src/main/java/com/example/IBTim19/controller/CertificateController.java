package com.example.IBTim19.controller;

import com.example.IBTim19.model.*;
import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.service.CertificateGenerator;
import com.example.IBTim19.service.CertificateService;
import com.example.IBTim19.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping(value = "/api/certificate")
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
        System.out.println(sn);
        Certificate cert = certificateService.findOneBySerialNumber(sn);
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaa "+ cert);
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
    public ResponseEntity<?> downloadCertificate(@PathVariable Integer id)  throws IOException{

        Certificate certificate = certificateService.findOneById(id);
        User user = userService.findOneByUsername(certificate.getUsername());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User loggedIn = (User) authentication.getPrincipal();

        String serialNumber = certificate.getSerialNumber();
        String zipFileName = "zipfiles/" + serialNumber + ".zip";

        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(Paths.get(zipFileName)))) {
            // for .crt file
            FileSystemResource crtResource = new FileSystemResource("crts/" + serialNumber + ".crt");
            zipOut.putNextEntry(new ZipEntry(crtResource.getFilename()));
            Files.copy(crtResource.getFile().toPath(), zipOut);
            zipOut.closeEntry();

            if(loggedIn.getUsername().equals(user.getUsername())){
                // for .key file
                FileSystemResource keyResource = new FileSystemResource("keys/" + serialNumber + ".key");
                zipOut.putNextEntry(new ZipEntry(keyResource.getFilename()));
                Files.copy(keyResource.getFile().toPath(), zipOut);
                zipOut.closeEntry();
            }

        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(zipFileName));
    }

    @GetMapping(value="/redraw/{sn}")
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
    @PostMapping(value = "/validateByCopy")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity validateByCopy(@RequestParam("File") MultipartFile file) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            byte[] certBytes = file.getBytes();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(certBytes);
            X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
            BigInteger serialNumberBigInt = cert.getSerialNumber();
            String serialNumberHex = serialNumberBigInt.toString(16);

            validate(serialNumberHex);
            return new ResponseEntity<>("This certificate is valid.",HttpStatus.OK);


        } catch (IOException | CertificateException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


}

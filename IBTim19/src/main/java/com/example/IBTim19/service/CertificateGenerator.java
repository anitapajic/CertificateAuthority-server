package com.example.IBTim19.service;

import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.model.CertificateStatus;
import com.example.IBTim19.model.CertificateType;
import com.example.IBTim19.model.User;
import com.example.IBTim19.repository.CertificateRepository;
import com.example.IBTim19.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Calendar;

@Service
public class CertificateGenerator {

    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private UserRepository userRepository;

    private static String keyDir = "keys";
    private static String crtDir = "crts";

    private Certificate issuer;
    private User subject;
    private boolean isAuthority;
    private X509Certificate issuerCertificate;
    private LocalDateTime validTo;
    private String currentRSA;

    private Certificate ExportGeneratedCertificate(X509Certificate cert){
        Certificate certificate = new Certificate();
        certificate.setIssuer(issuer.getSerialNumber());
        certificate.Status = CertificateStatus.Valid;
        certificate.setCertificateType(isAuthority ?
                (issuerCertificate == null ? CertificateType.Root : CertificateType.Intermediate)
                : CertificateType.End);
        certificate.setSerialNumber(String.valueOf(cert.getSerialNumber()));
        certificate.setSignatureAlgorithm(cert.getSigAlgName());
        certificate.setUsername(subject.getUsername());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(cert.getNotBefore());
        LocalDateTime notBefore = LocalDateTime.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        certificate.setValidFrom(notBefore);

        calendar.setTime(cert.getNotAfter());
        LocalDateTime notAfter = LocalDateTime.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        certificate.setValidTo(notAfter);

        certificateRepository.save(certificate);

        return certificate;

    }

}

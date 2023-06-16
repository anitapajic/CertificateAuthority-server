package com.example.IBTim19.service;

import com.example.IBTim19.model.*;
import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.repository.CertificateRepository;
import com.example.IBTim19.repository.UserRepository;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;


@Service
public class CertificateGenerator {

    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private UserRepository userRepository;

    private static String certDir = "crts";
    private static String keyDir = "keys";

    private Certificate issuer;
    private User subject;
    private boolean isAuthority;
    private X509Certificate issuerCertificate;
    private Date validTo;
    private KeyPair currentKeyPair;
    private KeyUsage flags;
    private CertificateType type;

    @Autowired
    public CertificateGenerator(UserRepository userRepository, CertificateRepository certificateRepository){
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
    }
    public Certificate IssueCertificate(String issuerSN, String subjectUsername, String keyUsageFlags, Date validTo, CertificateType certType) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
            SignatureException, IOException, InvalidKeySpecException, OperatorCreationException, Exception {
       System.out.println("aaaaa");
        validate(issuerSN, subjectUsername, keyUsageFlags, validTo);
        System.out.println("bbbb");
        type = certType;
        X509Certificate cert = generateCertificate();

        return exportGeneratedCertificate(cert);
    }

    private Certificate exportGeneratedCertificate(X509Certificate cert) throws IOException, CertificateEncodingException {
        Certificate certificateForDb = new Certificate();
        certificateForDb.setIssuer(issuer != null ? issuer.getSerialNumber() : null);
        certificateForDb.setStatus(CertificateStatus.Valid);
        
        certificateForDb.setCertificateType(type);

        certificateForDb.setSerialNumber(cert.getSerialNumber().toString(16));
        certificateForDb.setSignatureAlgorithm(cert.getSigAlgName());
        certificateForDb.setUsername(subject.getUsername());
        certificateForDb.setValidFrom(cert.getNotBefore());
        certificateForDb.setValidTo(cert.getNotAfter());
        certificateForDb.setIsRevoked(false);
        certificateRepository.save(certificateForDb);

        Files.write(Paths.get(certDir, certificateForDb.getSerialNumber() + ".crt"),
                cert.getEncoded());
        Files.write(Paths.get(keyDir, certificateForDb.getSerialNumber() + ".key"),
                currentKeyPair.getPrivate().getEncoded());
        isAuthority = false;
        issuer = null;
        return certificateForDb;
    }

    private X509Certificate generateCertificate() throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, IOException {
        X500Name subjectText = new X500Name("CN=" + subject.getUsername());
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096, new SecureRandom());
        currentKeyPair = keyPairGenerator.generateKeyPair();

        X509v3CertificateBuilder certificateBuilder;
        if (issuerCertificate == null) {
            certificateBuilder = new JcaX509v3CertificateBuilder(
                    subjectText,
                    new BigInteger(64, new SecureRandom()),
                    new Date(),
                    validTo,
                    subjectText,
                    currentKeyPair.getPublic());
        } else {
            certificateBuilder = new JcaX509v3CertificateBuilder(
                    new X500Name(issuerCertificate.getSubjectX500Principal().getName()),
                    new BigInteger(64, new SecureRandom()),
                    new Date(),
                    validTo,
                    subjectText,
                    currentKeyPair.getPublic());
        }

        addExtensions(certificateBuilder);

        ContentSigner contentSigner;
        if (issuerCertificate == null) {
            contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(currentKeyPair.getPrivate());
        } else {
            AsymmetricKeyParameter privateKeyParameter = PrivateKeyFactory.createKey(issuerCertificate.getPublicKey().getEncoded());
            contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build((PrivateKey) privateKeyParameter);
        }

        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);

        return new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(certificateHolder);
    }

    private void addExtensions(X509v3CertificateBuilder certificateBuilder) throws CertIOException, NoSuchAlgorithmException, CertIOException {
        certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isAuthority));
        certificateBuilder.addExtension(Extension.keyUsage, true, flags);
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(currentKeyPair.getPublic().getEncoded());
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, new JcaX509ExtensionUtils().createSubjectKeyIdentifier(publicKeyInfo));

        if (issuerCertificate != null) {
            SubjectPublicKeyInfo issuerPublicKeyInfo = SubjectPublicKeyInfo.getInstance(issuerCertificate.getPublicKey().getEncoded());
            certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false, new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(issuerPublicKeyInfo));
        }

    }

    private void validate(String issuerSN, String subjectUsername, String keyUsageFlags, Date validTo) throws Exception {
        if (issuerSN == null) {
            if (!(validTo.after(new Date()))) {
                throw new Exception("The date is not in the accepted range");
            }

        } else {
            issuer = certificateRepository.findOneBySerialNumber(issuerSN);

            X509Certificate issuerCertificate = readCertificateFromFile(String.format("%s/%s.crt", certDir, issuerSN));
            //RSAPrivateKey privateKey = (RSAPrivateKey) getPrivateKeyFromBytes(String.format("%s/%s.key", certDir, issuerSN));
            //issuerCertificate = X509CertificateGeneratorUtils.copyWithPrivateKey(issuerCertificate, privateKey);

            if (!(validTo.after(new Date()) && validTo.before(issuerCertificate.getNotAfter()))) {
                throw new Exception("The date is not in the accepted range");
            }
            if(issuer.getStatus().equals(CertificateStatus.NotValid)){
                throw new Exception("Issuer certificate is not valid!");
            }
        }
        this.validTo = validTo;
        subject = userRepository.findOneUserByUsername(subjectUsername);
        flags = parseFlags(keyUsageFlags);
    }

    private KeyUsage parseFlags(String keyUsageFlags) {
        if (keyUsageFlags == null || keyUsageFlags.isEmpty()) {
            throw new IllegalArgumentException("KeyUsageFlags are mandatory");
        }

        String[] flagArray = keyUsageFlags.split(",");
        int retVal = 0;

        for (String flag : flagArray) {
            try {
                int index = Integer.parseInt(flag);
                retVal |= 1 << index;

                if (index == 5) {
                    isAuthority = true;
                }

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unknown flag: " + flag, e);
            }
        }

        return new KeyUsage(retVal);
    }

    public static PrivateKey getPrivateKeyFromBytes(String path) throws GeneralSecurityException {
        try {
            byte[] keyBytes = Files.readAllBytes(new File(path).toPath());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public X509Certificate readCertificateFromFile(String path) {
        File certificateFile = new File(path);
        try (InputStream inStream = new FileInputStream(certificateFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(inStream);
        } catch (IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }







}

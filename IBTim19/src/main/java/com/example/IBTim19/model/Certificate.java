package com.example.IBTim19.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    @Column
    public String serialNumber;
    @Column
    public String SignatureAlgorithm;
    @Column
    public String Issuer;
    @Column
    public Date ValidFrom;
    @Column
    public Date ValidTo;
    @Column
    public CertificateStatus Status;
    @Column
    public CertificateType CertificateType;
    @Column
    public String username;
}

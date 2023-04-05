package com.example.IBTim19.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    public String signatureAlgorithm;
    @Column
    public String issuer;
    @Column
    public Date validFrom;
    @Column
    public Date validTo;
    @Enumerated(EnumType.STRING)
    public CertificateStatus Status;
    @Enumerated(EnumType.STRING)
    public CertificateType certificateType;
    @Column
    public String username;


}

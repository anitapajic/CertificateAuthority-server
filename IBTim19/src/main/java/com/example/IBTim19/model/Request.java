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
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column
    public String issuer;
    @Column
    public Date validTo;
    @Column
    public CertificateType certificateType;
    @Column
    public RequestStatus state;
    @Column
    public String subjectUsername;


}

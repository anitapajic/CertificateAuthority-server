package com.example.IBTim19.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IssueCertificateContracts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column
    public String subjectUsername;
    @Column
    public String KeyUsageFlags;
    @Column
    public String IssuerSN;
    @Column
    public LocalDateTime date;

}

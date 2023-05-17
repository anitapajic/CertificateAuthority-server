package com.example.IBTim19.repository;

import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Integer> {
    public Certificate findOneBySerialNumber(String sn);

    public List<Certificate> findAll();

    public List<Certificate> findAllByUsername(String username);
    public Certificate findOneById(Integer id);
    public Optional<List<Certificate>> findAllByIssuer(String issuer);
}

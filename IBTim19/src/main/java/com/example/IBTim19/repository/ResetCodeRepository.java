package com.example.IBTim19.repository;

import com.example.IBTim19.model.ResetCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetCodeRepository extends JpaRepository<ResetCode,Integer> {

    public ResetCode findOneById(Integer id);

    public Optional<ResetCode> findOneByUsername(String username);

    public ResetCode findOneByCode(Integer code);

    public void deleteById(Integer id);
}
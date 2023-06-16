package com.example.IBTim19.repository;

import com.example.IBTim19.model.TwoFactor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TwoFactorRepository extends JpaRepository<TwoFactor, Integer> {
    public TwoFactor findOneById(Integer id);
    public Optional<TwoFactor> findOneByUserId(Integer id);

    public void deleteById(Integer id);
}
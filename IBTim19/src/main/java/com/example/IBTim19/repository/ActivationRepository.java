package com.example.IBTim19.repository;

import com.example.IBTim19.model.Activation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivationRepository extends JpaRepository<Activation, Integer> {
    public Activation findOneById(Integer id);
    public Optional<Activation> findOneByUserId(Integer id);

    public void deleteById(Integer id);
}

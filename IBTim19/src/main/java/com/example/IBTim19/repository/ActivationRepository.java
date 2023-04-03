package com.example.IBTim19.repository;

import com.example.IBTim19.model.Activation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivationRepository extends JpaRepository<Activation, Integer> {
    public Activation findOneById(Integer id);
}

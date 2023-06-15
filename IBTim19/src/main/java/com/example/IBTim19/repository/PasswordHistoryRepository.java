package com.example.IBTim19.repository;

import com.example.IBTim19.model.PasswordHistory;
import com.example.IBTim19.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Integer> {
    public List<PasswordHistory> findAllByUsername(String username);

    public PasswordHistory save(PasswordHistory passwordHistory);
}

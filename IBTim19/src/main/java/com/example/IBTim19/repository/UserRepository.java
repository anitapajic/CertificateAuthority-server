package com.example.IBTim19.repository;

import com.example.IBTim19.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    public User findOneById(Integer id);

    public User findOneByUsernameAndPassword(String email, String password);

    Optional<User> findOneByUsername(String username);
    User findOneUserByUsername(String username);

    public Page<User> findAll(Pageable pageable);




}

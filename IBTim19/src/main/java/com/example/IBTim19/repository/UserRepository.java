package com.example.IBTim19.repository;

import com.example.IBTim19.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    public User findOneById(Integer id);

    public User findOneByUsernameAndPassword(String username, String password);

    Optional<User> findOneByUsername(String username);
    Optional<User> findUserByUsername(String username);
    User findOneUserByUsername(String username);
    Optional<User> findOneUserByTelephone(String telephone);

    public Page<User> findAll(Pageable pageable);
    public User save(User user);

    public void deleteById(Integer userId);






}

package com.example.IBTim19.service;


import com.example.IBTim19.exceptions.NotFoundException;
import com.example.IBTim19.model.User;
import com.example.IBTim19.repository.UserRepository;
import com.example.IBTim19.security.UserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = this.userRepository.findOneByUsername(username)
                .orElseThrow(() -> new NotFoundException(String.format("User with username '%s' is not found!", username)));

        return UserFactory.create(user);
    }
}

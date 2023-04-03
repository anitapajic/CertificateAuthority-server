package com.example.IBTim19.controller;

import com.example.IBTim19.DTO.UserDTO;
import com.example.IBTim19.model.Activation;
import com.example.IBTim19.model.User;
import com.example.IBTim19.service.ActivationService;
import com.example.IBTim19.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

@RestController
@RequestMapping(value = "/api/user")
@CrossOrigin(value="*")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ActivationService activationService;

    @PostMapping(consumes = "application/json")
    public ResponseEntity registration(@RequestBody UserDTO userDTO) throws MessagingException, UnsupportedEncodingException {

        // if passenger already exist
        if (userService.findOneByUsername(userDTO.getUsername()) != null) {
            return new ResponseEntity<>("User with that username already exists!", HttpStatus.BAD_REQUEST);
        }


        User user = new User();

        user.setUsername(userDTO.getUsername());
        user.setName(userDTO.getName());
        user.setLastname(userDTO.getLastname());
        //PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        //user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setPassword(userDTO.getPassword());
        user.setAuthorities("USER");

        userService.save(user);
//
//        Activation activation = new Activation();
//        activation.setId(user.getId());
//        activation.setUser(user);
//        activation.setCreationDate(LocalDateTime.now());
//        activation.setExpirationDate(LocalDateTime.now().plusYears(5));
//
//        activationService.save(activation);

        return new ResponseEntity<>(new UserDTO(user), HttpStatus.CREATED);
    }
}

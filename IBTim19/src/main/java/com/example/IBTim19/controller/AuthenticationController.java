package com.example.IBTim19.controller;

import com.example.IBTim19.DTO.AuthDTO;
import com.example.IBTim19.DTO.LoginDTO;
import com.example.IBTim19.DTO.UserDTO;
import com.example.IBTim19.model.User;
import com.example.IBTim19.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping(value = "/api/user")
@CrossOrigin(value="*")
public class AuthenticationController {

    @Autowired
    private UserService userService;
    @PostMapping(
            value = "/login",
            consumes = "application/json")
    public ResponseEntity login(@RequestBody LoginDTO loginDTO) throws MessagingException, UnsupportedEncodingException {
        AuthDTO auth = userService.login(loginDTO.getUsername(), loginDTO.getPassword());

        return new ResponseEntity<>(auth, HttpStatus.OK);
    }


    @PostMapping(
            value = "/register",
            consumes = "application/json")
    public ResponseEntity registration(@RequestBody UserDTO userDTO) throws MessagingException, UnsupportedEncodingException {

        // if passenger already exist
        if (userService.findOneByUsername(userDTO.getUsername()) != null) {
            return new ResponseEntity<>("User with that username already exists!", HttpStatus.BAD_REQUEST);
        }

        User user = userService.createNewUser(userDTO);


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

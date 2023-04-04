package com.example.IBTim19.controller;

import com.example.IBTim19.DTO.UserDTO;
import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.model.User;
import com.example.IBTim19.service.ActivationService;
import com.example.IBTim19.service.CertificateService;
import com.example.IBTim19.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;

@RestController
@RequestMapping(value = "/api/user")
@CrossOrigin(value="*")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ActivationService activationService;
    @Autowired
    private CertificateService certificateService;

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
        user.setTelephone(userDTO.getTelephone());
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

    @PostMapping(
            value = "/login",
            consumes = "application/json")
    public ResponseEntity login(@RequestBody UserDTO userDTO) throws MessagingException, UnsupportedEncodingException {
        User user = userService.findOneLogin(userDTO.getUsername(), userDTO.getPassword());

    return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(value = "/certificate/{sn}")
    public ResponseEntity validate(@PathVariable String sn) throws MessagingException, UnsupportedEncodingException {

        if(sn==null){
            return new ResponseEntity<>("Certificate with this serial number does not exist!", HttpStatus.NOT_FOUND);
        }
        Certificate cert = certificateService.findOneBySerialNumber(sn);
        Certificate issuerCertificate = certificateService.findOneBySerialNumber(cert.issuer);
        if(cert.validTo.after(new Date()) && cert.getValidTo().before(issuerCertificate.getValidTo())){
            return new ResponseEntity<>("This certificate is valid!", HttpStatus.OK);
        }

        return new ResponseEntity(HttpStatus.BAD_REQUEST);

    }

    }

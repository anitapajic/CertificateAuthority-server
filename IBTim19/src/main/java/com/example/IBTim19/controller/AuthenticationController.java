package com.example.IBTim19.controller;

import com.example.IBTim19.DTO.AuthDTO;
import com.example.IBTim19.DTO.LoginDTO;
import com.example.IBTim19.DTO.UserDTO;
import com.example.IBTim19.model.Activation;
import com.example.IBTim19.model.User;
import com.example.IBTim19.service.ActivationService;
import com.example.IBTim19.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

@RestController
@RequestMapping(value = "/api/user")
@CrossOrigin(value="*")
public class AuthenticationController {

    @Autowired
    private UserService userService;
    @Autowired
    private ActivationService activationService;

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
        userService.sendVerificationMail(user.getUsername(), user.getId());


        return new ResponseEntity<>("Check your email", HttpStatus.CREATED);
    }

    //ACTIVATE USER ACCOUNT  /api/user/activate/activationId
    @GetMapping(value = "/activate/{activationId}")
    public ResponseEntity activateAccount(@PathVariable Integer activationId) {


        try{
            Activation activation = activationService.findOne(activationId);

            if(activation.getExpirationDate().isBefore(LocalDateTime.now())){
                return new ResponseEntity<>("Activation expired. Register again!", HttpStatus.BAD_REQUEST);
            }
            User user = activation.getUser();
            user.setActive(true);
            userService.save(user);

            return new ResponseEntity<>("Successful account activation!", HttpStatus.OK);
        }
        catch (NullPointerException ex){
            return new ResponseEntity<>("Activation with entered id does not exist!", HttpStatus.NOT_FOUND);
        }
    }


}

package com.example.IBTim19.controller;

import com.example.IBTim19.DTO.*;
import com.example.IBTim19.model.Activation;
import com.example.IBTim19.model.User;
import com.example.IBTim19.service.ActivationService;
import com.example.IBTim19.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashMap;

@RestController
@RequestMapping(value = "/api/user")
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
        if(auth==null){
            return new ResponseEntity<>("Activate your account", HttpStatus.BAD_REQUEST);

        }

        return new ResponseEntity<>(auth, HttpStatus.OK);
    }


    @PostMapping(
            value = "/register",
            consumes = "application/json")
    public ResponseEntity registration(@RequestBody UserDTO userDTO) throws MessagingException, UnsupportedEncodingException {

        HashMap<String, String> resp = new HashMap();
        if(userService.findOneByTelephone(userDTO.getTelephone()) != null){
            resp.put("response","User with that telephone number already exists!");
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

        // if passenger already exist
        if (userService.findOneByUsername(userDTO.getUsername()) != null) {
            resp.put("response","User with that username already exists!");
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

        User user = userService.createNewUser(userDTO);
        activationService.sendVerificationMail(user.getUsername(), user.getId());

        resp.put("response","Check your email");

        return new ResponseEntity<>(resp, HttpStatus.CREATED);
    }

    //TODO: logout

    @GetMapping(
            value = "/logout")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity logout(){


        return new ResponseEntity<>("Logout", HttpStatus.OK);
    }

    //ACTIVATE USER ACCOUNT  /api/user/activate/activationId
    @GetMapping(value = "/activate/{userId}")
    public ResponseEntity activateAccount(@PathVariable Integer userId) {


        try{
            Activation activation = activationService.findOneByUserId(userId);
            activationService.remove(activation.getId());

            if(activation.getExpirationDate().isBefore(LocalDateTime.now())){
                return new ResponseEntity<>("Activation expired. Register again!", HttpStatus.BAD_REQUEST);
            }
            User user = activation.getUser();
            user.setIsActive(1);
            userService.save(user);


            return new ResponseEntity<>("Successful account activation!", HttpStatus.OK);
        }
        catch (NullPointerException ex){
            return new ResponseEntity<>("Activation with entered id does not exist!", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/verify/{phoneNumber}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity getVerificationCode(@PathVariable String phoneNumber) {

        activationService.sendVerificationCode(phoneNumber);

        return new ResponseEntity<>("Check your phone", HttpStatus.CREATED);
    }

    @PostMapping(
            value = "/verify/{code}",
            consumes = "application/json")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity verifyPhoneNumber(@PathVariable Integer code) {
        Integer response = activationService.verifyPhoneNumber(code);

        if (response == 1) {
            return new ResponseEntity<>(new HashMap<String, String>() {{ put("response", "Verification does not exist!"); }}, HttpStatus.NOT_FOUND);
        }
        else if (response == 2) {
            return new ResponseEntity<>(new HashMap<String, String>() {{ put("response", "Entered code is wrong!"); }}, HttpStatus.BAD_REQUEST);
        }
        else if (response == 3) {
            return new ResponseEntity<>(new HashMap<String, String>() {{ put("response", "Verification expired. Try again!"); }}, HttpStatus.BAD_REQUEST);

        }
        return new ResponseEntity<>(new HashMap<String, String>() {{ put("response", "Successful phone verification!"); }}, HttpStatus.OK);
    }

    @PostMapping(value = "/getResetCode")
    public ResponseEntity getResetCode(@RequestBody ResetDTO reset) {
        Integer res;

        if(reset.getType().equals(ResetType.MAIL)){
            res = activationService.sendResetCode(ResetType.MAIL, reset.getUsername());
        }
        else{
            res = activationService.sendResetCode(ResetType.TELEPHONE, reset.getTelephone());
        }

        if (res == 0){
            return new ResponseEntity<>(new HashMap<String, String>() {{ put("response", "Check your phone"); }}, HttpStatus.CREATED);

        } else if (res == 1) {
            return new ResponseEntity<>(new HashMap<String, String>() {{ put("response", "Check your mail"); }}, HttpStatus.CREATED);

        }
        return new ResponseEntity<>(new HashMap<String, String>() {{ put("response", "User doesn't exist"); }}, HttpStatus.BAD_REQUEST);

    }

    @PostMapping(value = "/resetPassword")
    public ResponseEntity resetPassword(@RequestBody ResetDTO resetDTO) {

        if(!resetDTO.getNewPassword().equals(resetDTO.getNewConfirmed())) {
            return new ResponseEntity<>("New and confirmed passwords do not match", HttpStatus.BAD_REQUEST);
        }

        Integer res =  userService.resetPassword(resetDTO, resetDTO.getUsername());

        if(res == 1){
            return new ResponseEntity<>(new HashMap<String, String>() {{ put("response", "Wrong code"); }}, HttpStatus.BAD_REQUEST);

        } else if (res == 2) {
            return new ResponseEntity<>(new HashMap<String, String>() {{ put("response", "Code expired"); }}, HttpStatus.BAD_REQUEST);

        }

        return new ResponseEntity<>(new HashMap<String, String>() {{ put("response", "Successfully changed password"); }}, HttpStatus.OK);
    }

}

package com.example.IBTim19.controller;

import com.example.IBTim19.DTO.AuthDTO;
import com.example.IBTim19.DTO.LoginDTO;
import com.example.IBTim19.DTO.ResetDTO;
import com.example.IBTim19.DTO.UserDTO;
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

        HashMap<String, String> resp = new HashMap();


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
            user.setActive(true);
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
            return new ResponseEntity<>("Verification does not exist!", HttpStatus.NOT_FOUND);
        }
        else if (response == 2) {
            return new ResponseEntity<>("Entered code is wrong!", HttpStatus.BAD_REQUEST);
        }
        else if (response == 3) {
            return new ResponseEntity<>("Verification expired. Try again!", HttpStatus.BAD_REQUEST);

        }
        return new ResponseEntity<>("Successful phone verification!", HttpStatus.OK);
    }

    @GetMapping(value = "/resetPassword/{type}")
    public ResponseEntity getResetCode(@PathVariable Integer type, @RequestBody UserDTO user) {

        //0 phone
        //1 mail

        if(type == 1){
            activationService.sendResetCode(type, user.getUsername());
            return new ResponseEntity<>("Check your mail", HttpStatus.CREATED);
        }
        activationService.sendResetCode(type, user.getTelephone());

        return new ResponseEntity<>("Check your phone", HttpStatus.CREATED);
    }

    @PostMapping(value = "/resetPassword/{username}")
    public ResponseEntity resetPassword(@RequestBody ResetDTO resetDTO, @PathVariable String username) {

        if(!resetDTO.getNewPassword().equals(resetDTO.getNewConfirmed()))
            return new ResponseEntity<>("New and confirmed passwords do not match", HttpStatus.BAD_REQUEST);

        Integer res =  userService.resetPassword(resetDTO, username);

        if(res == 1){
            return new ResponseEntity<>("Wrong code", HttpStatus.BAD_REQUEST);

        } else if (res == 2) {
            return new ResponseEntity<>("Code expired", HttpStatus.BAD_REQUEST);

        }

        return new ResponseEntity<>("Successfully changed password", HttpStatus.OK);
    }

}

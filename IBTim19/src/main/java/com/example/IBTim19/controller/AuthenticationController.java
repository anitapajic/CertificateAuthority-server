//package com.example.IBTim19.controller;
//
//import com.example.IBTim19.DTO.LoginDTO;
//import com.example.IBTim19.DTO.TokenDTO;
//import com.example.IBTim19.exceptions.BadRequestException;
//import com.example.IBTim19.model.User;
//import com.example.IBTim19.security.TokenUtils;
//import com.example.IBTim19.service.UserDetailsServiceImpl;
//import com.example.IBTim19.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AnonymousAuthenticationToken;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@CrossOrigin(value = "*")
//@RestController
//public class AuthenticationController {
//
//    private AuthenticationManager authenticationManager;
//
//    private UserDetailsServiceImpl userDetailsService;
//
//    private UserService userService;
//
//    private TokenUtils tokenUtils;
//
//    @Autowired
//    public AuthenticationController(
//            AuthenticationManager authenticationManager,
//            UserDetailsServiceImpl userDetailsService,
//            UserService userService,
//            TokenUtils tokenUtils
//
//    ) {
//        this.authenticationManager = authenticationManager;
//        this.userDetailsService = userDetailsService;
//        this.tokenUtils = tokenUtils;
//        this.userService = userService;
//    }
//
//    @PostMapping(
//            value = "api/user/login",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity loginUser(@RequestBody LoginDTO login, BindingResult bindingResult) {
//
//        if (bindingResult.hasErrors()) {
//            return new ResponseEntity<>(bindingResult.getFieldError().getDefaultMessage(), HttpStatus.BAD_REQUEST);
//        }
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (!(auth instanceof AnonymousAuthenticationToken)) {
//            throw new BadRequestException("Unauthorized!");
//        }
//
//        try {
//            TokenDTO token = new TokenDTO();
//            UserDetails userDetails = this.userDetailsService.loadUserByUsername(login.getUsername());
//
//            String tokenValue = this.tokenUtils.generateToken(userDetails);
//            token.setToken(tokenValue);
//
//            User user = this.userService.findIdByUsername(login.getUsername());
//            token.setId(user.getId());
//            token.setRole(user.getAuthorities());
//
//            Authentication authentication = this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            return new ResponseEntity<>(token, HttpStatus.OK);
//        } catch (BadCredentialsException e) {
//            Map<String, String> response = new HashMap<>(){{put("message", e.getMessage());}};
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    @GetMapping(
//            value = "api/user/logout",
//            produces = MediaType.TEXT_PLAIN_VALUE
//    )
//    public ResponseEntity logoutUser() {
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        if (!(auth instanceof AnonymousAuthenticationToken)){
//            SecurityContextHolder.clearContext();
//
//            return new ResponseEntity<>("You successfully logged out!", HttpStatus.OK);
//        }
//        else {
//            throw new BadRequestException("User is not authenticated!");
//        }
//
//    }
//}

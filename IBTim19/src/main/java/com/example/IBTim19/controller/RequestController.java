package com.example.IBTim19.controller;

import com.example.IBTim19.model.Certificate;
import com.example.IBTim19.model.Request;
import com.example.IBTim19.service.CertificateService;
import com.example.IBTim19.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin("*")
public class RequestController {
    @Autowired
    private RequestService requestService;
    @Autowired
    private CertificateService certificateService;

    @GetMapping("/myRequests")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity getUserRequests(@AuthenticationPrincipal UserDetails userDetails){
        try {
            List<Request> requests = requestService.findAllBySubjectUsername(userDetails.getUsername());

            return new ResponseEntity<>(requests, HttpStatus.OK);
        }
        catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity getAllRequests(){
        try {
            List<Request> requests = requestService.findAll();

            return new ResponseEntity<>(requests, HttpStatus.OK);
        }
        catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

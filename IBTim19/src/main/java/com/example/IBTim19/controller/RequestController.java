package com.example.IBTim19.controller;

import com.example.IBTim19.DTO.RequestDTO;
import com.example.IBTim19.model.Request;
import com.example.IBTim19.service.CertificateService;
import com.example.IBTim19.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin("*")
public class RequestController {
    @Autowired
    private RequestService requestService;
    @Autowired
    private CertificateService certificateService;

    @PostMapping(value = "/create",
            consumes = "application/json")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity createRequest(@RequestBody RequestDTO requestDTO){
        try{

            Request request = requestService.processRequest(requestDTO);
            if (request != null){
                return  new ResponseEntity<>(request, HttpStatus.OK);
            }

            return  new ResponseEntity<>("Certificate created", HttpStatus.OK);
        }
        catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/accept/{id}",
            consumes = "application/json")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity acceptRequest(@PathVariable Integer id){
        String s = requestService.acceptRequest(id);
        return  new ResponseEntity<>(s, HttpStatus.OK);

    }
    @PostMapping(value = "/reject/{id}",
            consumes = "application/json")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity rejectRequest(@PathVariable Integer id){
        String s = requestService.rejectRequest(id);
        return  new ResponseEntity<>(s, HttpStatus.OK);

    }

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

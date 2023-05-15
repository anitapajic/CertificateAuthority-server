package com.example.IBTim19.controller;

import com.example.IBTim19.DTO.RequestDTO;
import com.example.IBTim19.model.*;
import com.example.IBTim19.service.CertificateService;
import com.example.IBTim19.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    @Autowired
    private RequestService requestService;
    @Autowired
    private CertificateService certificateService;

    @PostMapping(value = "/create",
            consumes = "application/json")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity createRequest(@RequestBody RequestDTO requestDTO){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User auth = (User) authentication.getPrincipal();
        try{
            if(auth.getRole().equals(Role.USER)){
                if(requestDTO.getCertificateType().equals(String.valueOf(CertificateType.Root))){
                    return  new ResponseEntity<>("You can't request root certificate!", HttpStatus.FORBIDDEN);
                }
            }
            if(requestDTO.getIssuerSN()!=null){
                if(CertificateType.End.equals(certificateService.findOneBySerialNumber(requestDTO.getIssuerSN()).getCertificateType())){
                    return  new ResponseEntity<>("You can't issue certificate based on end certificate!", HttpStatus.BAD_REQUEST);
                }
                if(certificateService.findOneBySerialNumber(requestDTO.getIssuerSN())==null){
                    return  new ResponseEntity<>("Issuer certificate does not exist!", HttpStatus.BAD_REQUEST);
                }
                if(CertificateStatus.NotValid.equals(certificateService.findOneBySerialNumber(requestDTO.getIssuerSN()).getStatus())){
                    return  new ResponseEntity<>("Issuer certificate is invalid!", HttpStatus.BAD_REQUEST);
                }
            }

            Object request = requestService.processRequest(requestDTO);
            if(request == null){
                return  new ResponseEntity<>("Invalide date", HttpStatus.BAD_REQUEST);

            }
            return  new ResponseEntity<>(request, HttpStatus.CREATED);
        }
        catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(value = "/accept/{id}",
            consumes = "application/json")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity acceptRequest(@PathVariable Integer id){

        String s = requestService.acceptRequest(id);
        return  new ResponseEntity<>(s, HttpStatus.OK);

    }
    @PostMapping(value = "/reject/{id}",
            consumes = "application/json")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")

    public ResponseEntity rejectRequest(@PathVariable Integer id, @RequestBody RequestDTO requestDTO){
        String s = requestService.rejectRequest(id, requestDTO.getReason());
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

    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public ResponseEntity getPendingRequests(@AuthenticationPrincipal UserDetails userDetails){
        try {
            List<Request> requests = requestService.findAllByIssuerUsername(userDetails.getUsername());

            return new ResponseEntity<>(requests, HttpStatus.OK);
        }
        catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}

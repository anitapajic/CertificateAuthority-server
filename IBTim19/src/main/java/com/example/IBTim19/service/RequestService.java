package com.example.IBTim19.service;

import com.example.IBTim19.DTO.RequestDTO;
import com.example.IBTim19.model.*;
import com.example.IBTim19.repository.CertificateRepository;
import com.example.IBTim19.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestService {
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private CertificateGenerator certificateGenerator;

    public List<Request> findAll(){return this.requestRepository.findAll();}
    public List<Request> findAllBySubjectUsername(String username){return this.requestRepository.findAllBySubjectUsername(username);}

    public List<Request> findAllByIssuerUsername(String username){return this.requestRepository.findAllByIssuerUsername(username);}

    public Request processRequest(RequestDTO requestDTO){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User auth = (User) authentication.getPrincipal();

        if(auth.getRole().equals(Role.ADMIN)){
            try {
                certificateGenerator.IssueCertificate(requestDTO.getIssuerSN(), auth.getUsername(), "3,4,5", requestDTO.getDate());
                return null;
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }

        Certificate issuerCert = certificateRepository.findOneBySerialNumber(requestDTO.getIssuerSN());

        if(auth.getUsername().equals(issuerCert.getUsername())){
            try {
                certificateGenerator.IssueCertificate(requestDTO.getIssuerSN(), auth.getUsername(), "3,4", requestDTO.getDate());
                return null;
            }
            catch (Exception e) {
                System.out.println(e);
            }

        }

        return createRequest(requestDTO);
    }
    public Request createRequest(RequestDTO requestDTO){
        Request request = new Request();
        if(requestDTO==null){
            return null;
        }
        else{
            request.setIssuerUsername(certificateRepository.findOneBySerialNumber(requestDTO.getIssuerSN()).getUsername());
            request.setState(RequestStatus.PENDING);
            request.setIssuer(requestDTO.getIssuerSN());
            request.setValidTo(requestDTO.getDate());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User auth = (User) authentication.getPrincipal();
            request.setSubjectUsername(auth.getUsername());
            request.setCertificateType(CertificateType.valueOf(requestDTO.getCertificateType()));

            request = requestRepository.save(request);

        }
        return request;
    }

    public String acceptRequest(Integer requestId){
        Request request = requestRepository.findById(requestId).get();


        if(!request.getState().equals(RequestStatus.PENDING)){
            return "Only pending requests can be accepted";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User auth = (User) authentication.getPrincipal();
        Certificate issuerCert = certificateRepository.findOneBySerialNumber(request.getIssuer());
        if (!issuerCert.getUsername().equals(auth.getUsername())){
            return "Only issuer can accept";
        }

        try {
            certificateGenerator.IssueCertificate(request.getIssuer(), request.getSubjectUsername(), "3,4", request.getValidTo());
            request.setState(RequestStatus.ACCEPTED);
            requestRepository.save(request);
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return "Request accepted";
    }

    public String rejectRequest(Integer requestId, String reason){

        Request request = requestRepository.findById(requestId).get();

        if(!request.getState().equals(RequestStatus.PENDING)){
            return "Only pending requests can be rejected";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User auth = (User) authentication.getPrincipal();
        Certificate issuerCert = certificateRepository.findOneBySerialNumber(request.getIssuer());
        if (!issuerCert.getUsername().equals(auth.getUsername())){
            return "Only issuer can reject";
        }

        request.setState(RequestStatus.REJECTED);
        request.setReason(reason);
        requestRepository.save(request);
        return "Request rejected";

    }

}

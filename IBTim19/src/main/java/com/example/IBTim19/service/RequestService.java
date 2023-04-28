package com.example.IBTim19.service;

import com.example.IBTim19.DTO.RequestDTO;
import com.example.IBTim19.model.CertificateType;
import com.example.IBTim19.model.Request;
import com.example.IBTim19.model.RequestStatus;
import com.example.IBTim19.model.User;
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

    public List<Request> findAll(){return this.requestRepository.findAll();}
    public List<Request> findAllBySubjectUsername(String username){return this.requestRepository.findAllBySubjectUsername(username);}

    public Request createRequest(RequestDTO requestDTO){
        Request request = new Request();
        if(requestDTO==null){
            return null;
        }
        else{
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

}

package com.example.IBTim19.controller;

import com.example.IBTim19.service.CertificateService;
import com.example.IBTim19.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestController {
    @Autowired
    private RequestService requestService;
    @Autowired
    private CertificateService certificateService;

}

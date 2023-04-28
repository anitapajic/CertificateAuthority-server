package com.example.IBTim19.DTO;

import java.util.Date;

public class RequestDTO {
    private String certificateType;
    private  Date date;
    private String issuerSN;


    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIssuerSN() {
        return issuerSN;
    }

    public void setIssuerSN(String issuerSN) {
        this.issuerSN = issuerSN;
    }

    @Override
    public String toString() {
        return "RequestDTO{" +
                "certificateType='" + certificateType + '\'' +
                ", date=" + date +
                ", issuerSN='" + issuerSN + '\'' +
                '}';
    }
}

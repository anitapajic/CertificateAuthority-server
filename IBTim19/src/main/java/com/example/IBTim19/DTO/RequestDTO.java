package com.example.IBTim19.DTO;

import java.util.Date;

public class RequestDTO {
    private String keyUsageFlags;
    private String certificateType;
    private  Date date;
    private String issuerSN;

    public String getKeyUsageFlags() {
        return keyUsageFlags;
    }

    public void setKeyUsageFlags(String keyUsageFlags) {
        this.keyUsageFlags = keyUsageFlags;
    }

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
}

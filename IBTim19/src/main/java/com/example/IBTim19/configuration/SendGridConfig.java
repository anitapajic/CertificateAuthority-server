package com.example.IBTim19.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sendgrid.SendGrid;
@Configuration
public class SendGridConfig {
    @Value("${app.sendgrid.key}")
    private String appKey;

    @Bean SendGrid getSentGrid(){
        return new SendGrid(appKey);
    }
}

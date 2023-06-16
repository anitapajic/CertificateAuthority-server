package com.example.IBTim19.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class LoggerConfig{

    private static final Logger logger = LoggerFactory.getLogger(LoggerConfig.class);

    @AfterReturning(pointcut = "execution(* com.example.IBTim19.controller.*.*(..))", returning = "result")
    public void logEndpointEntry(JoinPoint joinPoint, Object result) {
        String endpoint = joinPoint.getSignature().toShortString();
        String method = joinPoint.getSignature().getName();
        String username = getUsername();
        String[] response = result != null ? result.toString().split(" ") : null;
        logger.info("Entered endpoint: {} - Method: {} - User: {} - Response: {}", endpoint, method, username, response[0].substring(1) + " " + response[1]);
    }

    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "Unknown";
    }

}

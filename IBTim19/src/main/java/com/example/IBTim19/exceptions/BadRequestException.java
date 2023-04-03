package com.example.IBTim19.exceptions;

public class BadRequestException extends RuntimeException{

    public BadRequestException() {}

    public BadRequestException(String message) {
        super(message);
    }

}

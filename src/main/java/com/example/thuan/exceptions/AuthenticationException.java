package com.example.thuan.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthenticationException extends RuntimeException {
    private final int errorCode;
    private final HttpStatus status;

    public AuthenticationException(String message, HttpStatus status, int errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
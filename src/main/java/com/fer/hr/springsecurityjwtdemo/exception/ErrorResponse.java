package com.fer.hr.springsecurityjwtdemo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ErrorResponse {
    private final int status;
    private final String message;
    private final Map<String, String> errors;
    private final LocalDateTime timestamp;


    public ErrorResponse(HttpStatus status, String message, Map<String, String> errors) {
        this.status = status.value();
        this.message = message;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(HttpStatus status, String message) {
        this(status, message, new HashMap<>());
    }
}


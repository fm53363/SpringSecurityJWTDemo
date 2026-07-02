package com.fer.hr.springsecurityjwtdemo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
        return ResponseEntity.badRequest().body(response);
    }

    // 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        // Return 401 Unauthorized
        var status = HttpStatus.UNAUTHORIZED;
        ErrorResponse response = new ErrorResponse(status, "Invalid username or password");
        return ResponseEntity
                .status(status)
                .body(response);
    }

    // 409
    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExists(
            UsernameTakenException ex
    ) {
        var status = HttpStatus.CONFLICT;
        ErrorResponse response = new ErrorResponse(status, ex.getMessage());
        return ResponseEntity
                .status(status)
                .body(response);
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse response = new ErrorResponse(status, "Something went wrong", null);

        return ResponseEntity.status(status).body(response);
    }
}


package com.fer.hr.springsecurityjwtdemo.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fer.hr.springsecurityjwtdemo.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthExceptionHandler.class);


    private final ObjectMapper mapper;

    // Handle Authentication failures (401)
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        logger.warn("AuthenticationException: {}", authException.getMessage());
        buildErrorResponse(response, HttpStatus.UNAUTHORIZED, authException.getMessage());
    }

    // Handle Authorization failures (403)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        logger.warn("AccessDeniedException: {}", accessDeniedException.getMessage());
        buildErrorResponse(response, HttpStatus.FORBIDDEN, accessDeniedException.getMessage());

    }

    private void buildErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());
        ErrorResponse body = new ErrorResponse(status, message);
        mapper.writeValue(response.getOutputStream(), body);

    }
}

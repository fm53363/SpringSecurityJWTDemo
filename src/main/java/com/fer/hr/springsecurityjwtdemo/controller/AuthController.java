package com.fer.hr.springsecurityjwtdemo.controller;

import com.fer.hr.springsecurityjwtdemo.auth.CustomUserDetailsService;
import com.fer.hr.springsecurityjwtdemo.auth.JwtService;
import com.fer.hr.springsecurityjwtdemo.dto.LoginRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.RegisterRequestDto;
import com.fer.hr.springsecurityjwtdemo.entity.User;
import com.fer.hr.springsecurityjwtdemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    private final UserService userService;


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(Map.of("token", token));
    }


    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequestDto request) {
        
        User user = userService.register(request);

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }


}


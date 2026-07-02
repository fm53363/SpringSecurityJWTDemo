package com.fer.hr.springsecurityjwtdemo.service.impl;

import com.fer.hr.springsecurityjwtdemo.dto.LoginRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.RegisterRequestDto;
import com.fer.hr.springsecurityjwtdemo.entity.User;
import com.fer.hr.springsecurityjwtdemo.enums.Role;
import com.fer.hr.springsecurityjwtdemo.exception.UsernameTakenException;
import com.fer.hr.springsecurityjwtdemo.repository.UserRepository;
import com.fer.hr.springsecurityjwtdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;


    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    @Override
    public User register(RegisterRequestDto request) {
        logger.info("Register request for username={}", request.username());
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameTakenException();
        }
        User user = User.builder().
                username(request.username()).
                password(passwordEncoder.encode(request.password())).
                role(Role.USER).
                build();

        return userRepository.save(user);
    }

    @Override
    public User login(LoginRequestDto request) {
        logger.info("Login request for username={}", request.username());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        return userRepository.findByUsername(request.username()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }
}

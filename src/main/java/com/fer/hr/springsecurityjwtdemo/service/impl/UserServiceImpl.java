package com.fer.hr.springsecurityjwtdemo.service.impl;

import com.fer.hr.springsecurityjwtdemo.dto.RegisterRequestDto;
import com.fer.hr.springsecurityjwtdemo.entity.User;
import com.fer.hr.springsecurityjwtdemo.enums.Role;
import com.fer.hr.springsecurityjwtdemo.exception.UsernameAlreadyExistsException;
import com.fer.hr.springsecurityjwtdemo.repository.UserRepository;
import com.fer.hr.springsecurityjwtdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public User register(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException();
        }
        User user = User.builder().
                username(request.username()).
                password(passwordEncoder.encode(request.password())).
                role(Role.USER).
                build();

        return userRepository.save(user);
    }
}

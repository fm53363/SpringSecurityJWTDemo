package com.fer.hr.springsecurityjwtdemo.service.impl;

import com.fer.hr.springsecurityjwtdemo.dto.request.CreateUserRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.request.LoginRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.request.RegisterRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.request.UpdateUserRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.response.UserResponseDto;
import com.fer.hr.springsecurityjwtdemo.entity.User;
import com.fer.hr.springsecurityjwtdemo.enums.Role;
import com.fer.hr.springsecurityjwtdemo.exception.UserNotFoundException;
import com.fer.hr.springsecurityjwtdemo.exception.UsernameTakenException;
import com.fer.hr.springsecurityjwtdemo.mapper.UserMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

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

    @Override
    public UserResponseDto getById(Long id) {
        return userRepository
                .findById(id)
                .map(userMapper::toDto)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Override
    public UserResponseDto createUser(CreateUserRequestDto request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameTakenException();
        }
        User user = User.builder()
                .username(request.username()).
                password(passwordEncoder.encode(request.password()))
                .role(request.role().equals(Role.ADMIN) ? Role.ADMIN : Role.USER)
                .build();

        var saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(long id, UpdateUserRequestDto request) {

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        if (request.username() != null) {
            if (userRepository.existsByUsername(request.username())) {
                throw new UsernameTakenException();
            }
            user.setUsername(request.username());
        }
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        return userMapper.toDto(user);
    }

    @Override
    public void deleteUser(long id) {
        userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        userRepository.deleteById(id);
    }

}

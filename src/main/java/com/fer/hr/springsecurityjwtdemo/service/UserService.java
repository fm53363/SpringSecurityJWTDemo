package com.fer.hr.springsecurityjwtdemo.service;

import com.fer.hr.springsecurityjwtdemo.dto.request.CreateUserRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.request.LoginRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.request.RegisterRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.request.UpdateUserRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.response.UserResponseDto;
import com.fer.hr.springsecurityjwtdemo.entity.User;

import java.util.List;

public interface UserService {

    User register(RegisterRequestDto request);

    User login(LoginRequestDto request);

    UserResponseDto getById(Long id);

    List<UserResponseDto> getAllUsers();

    UserResponseDto createUser(CreateUserRequestDto request);

    UserResponseDto updateUser(long id, UpdateUserRequestDto request);

    void deleteUser(long id);
}

package com.fer.hr.springsecurityjwtdemo.service;

import com.fer.hr.springsecurityjwtdemo.dto.LoginRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.RegisterRequestDto;
import com.fer.hr.springsecurityjwtdemo.entity.User;

public interface UserService {

    User register(RegisterRequestDto request);

    User login(LoginRequestDto request);
}

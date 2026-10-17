package com.fer.hr.springsecurityjwtdemo.dto.request;

import com.fer.hr.springsecurityjwtdemo.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CreateUserRequestDto(
        @Size(min = 5, max = 50, message = "Username must be 5-50 characters")
        @NotBlank(message = "Username must not be empty")
        String username,

        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        @NotBlank(message = "Password must not be empty")
        String password,

        Role role
) {
}
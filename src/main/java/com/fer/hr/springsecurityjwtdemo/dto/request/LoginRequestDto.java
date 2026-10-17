package com.fer.hr.springsecurityjwtdemo.dto.request;

import jakarta.validation.constraints.NotBlank;


public record LoginRequestDto(
        @NotBlank(message = "Username must not be empty")
        String username,

        @NotBlank(message = "Password must not be empty")
        String password
) {
}

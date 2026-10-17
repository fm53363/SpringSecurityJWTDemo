package com.fer.hr.springsecurityjwtdemo.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @Size(min = 5, max = 50, message = "Username must be 5-50 characters")
        @NotBlank(message = "Username must not be empty")
        String username,

        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        @NotBlank(message = "Password must not be empty")
        String password,
        String confirmPassword
) {

    @AssertTrue(message = "Passwords do not match")
    private boolean isPasswordMatching() {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
}

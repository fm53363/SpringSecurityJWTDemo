package com.fer.hr.springsecurityjwtdemo.dto.response;

import com.fer.hr.springsecurityjwtdemo.enums.Role;

public record UserResponseDto(
        Long id,
        String username,
        Role role
) {
}

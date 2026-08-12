package com.fer.hr.springsecurityjwtdemo.controller;

import com.fer.hr.springsecurityjwtdemo.dto.request.CreateUserRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.request.UpdateUserRequestDto;
import com.fer.hr.springsecurityjwtdemo.dto.response.UserResponseDto;
import com.fer.hr.springsecurityjwtdemo.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;


    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable("id") long id) {
        UserResponseDto userDto = userService.getById(id);
        return ResponseEntity.ok(userDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsers() {
        var users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody CreateUserRequestDto dto) {
        UserResponseDto savedUser = userService.createUser(dto);
        URI location = URI.create("/api/users/" + savedUser.id());
        return ResponseEntity.created(location).body(savedUser);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody UpdateUserRequestDto dto, @PathVariable("id") long id) {
        var response = userService.updateUser(id, dto);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponseDto> deleteUser(@PathVariable("id") long id) {
        var user = userService.getById(id);
        return ResponseEntity.ok(user);
    }
}

package com.fer.hr.springsecurityjwtdemo.testUtil;

import com.fer.hr.springsecurityjwtdemo.entity.User;
import com.fer.hr.springsecurityjwtdemo.enums.Role;
import com.fer.hr.springsecurityjwtdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class TestUserFactory {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public User createUser(String username, Role role) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .role(role)
                .build();

        return userRepository.save(user);
    }


    public void createUsers(int count, Role role) {
        for (int i = 1; i <= count; i++) {
            createUser("user" + i, role);
        }
    }
}
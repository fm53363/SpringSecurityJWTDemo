package com.fer.hr.springsecurityjwtdemo.controller;

import com.fer.hr.springsecurityjwtdemo.auth.JwtService;
import com.fer.hr.springsecurityjwtdemo.entity.User;
import com.fer.hr.springsecurityjwtdemo.enums.Role;
import com.fer.hr.springsecurityjwtdemo.repository.UserRepository;
import com.fer.hr.springsecurityjwtdemo.testUtil.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtService jwtService;

    private TestUserFactory userFactory;


    private String getToken(User user) {
        return jwtService.generateToken(user);
    }

    @BeforeEach
    void setUp() {
        userFactory = new TestUserFactory(userRepository, passwordEncoder);
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUser {

        @Test
        void shouldReturn200WhenUserIsAdmin() throws Exception {
            User admin = userFactory.createUser("admin", Role.ADMIN);
            User other = userFactory.createUser("other", Role.USER);

            mockMvc.perform(get("/api/users/" + other.getId())
                            .header("Authorization", "Bearer " + getToken(admin))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(other.getId()))
                    .andExpect(jsonPath("$.username").value("other"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void shouldReturn200WhenUserAccessesOwnUser() throws Exception {
            User other = userFactory.createUser("other", Role.USER);

            mockMvc.perform(get("/api/users/" + other.getId())
                            .header("Authorization", "Bearer " + getToken(other))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(other.getId()))
                    .andExpect(jsonPath("$.username").value("other"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void shouldReturn404WhenUserIsAuthorizedButUserDoesNotExist() throws Exception {
            User admin = userFactory.createUser("admin", Role.ADMIN);
            long nonExistingId = 999999L;

            mockMvc.perform(get("/api/users/" + nonExistingId)
                            .header("Authorization", "Bearer " + getToken(admin))
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn403WhenUserIsAuthenticatedButNotAuthorized() throws Exception {
            User first = userFactory.createUser("first", Role.USER);
            User other = userFactory.createUser("other", Role.USER);

            mockMvc.perform(get("/api/users/" + other.getId())
                            .header("Authorization", "Bearer " + getToken(first))
                    )
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenUserIsUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/users/3"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/users")
    class GetUsers {
        @Test
        void shouldReturn200WhenUserIsAdmin() throws Exception {
            User admin = userFactory.createUser("admin", Role.ADMIN);
            userFactory.createUsers(4, Role.USER);

            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + getToken(admin))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)));

            assertThat(userRepository.count()).isEqualTo(5);

        }

        @Test
        void shouldReturn403WhenUserIsAuthenticatedButNotAuthorized() throws Exception {
            User user = userFactory.createUser("user123", Role.USER);
            userFactory.createUsers(4, Role.USER);

            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + getToken(user))
                    )
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenUserIsUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }

    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateUser {
        @Test
        void shouldReturn201WhenUserIsAdmin() throws Exception {
            User admin = userFactory.createUser("admin", Role.ADMIN);

            mockMvc.perform(post("/api/users")
                            .header("Authorization", "Bearer " + getToken(admin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "newuser",
                                        "password": "password123",
                                        "role": "USER"
                                    }
                                    """)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.username").value("newuser"))
                    .andExpect(jsonPath("$.role").value("USER"));
            User created = userRepository.findByUsername("newuser").orElseThrow();
            assertThat(created.getUsername()).isEqualTo("newuser");
            assertThat(created.getRole()).isEqualTo(Role.USER);

        }

        @Test
        void shouldReturn409WhenUserIsAuthorizedButUserWithUsernameAlreadyExists() throws Exception {
            User admin = userFactory.createUser("admin", Role.ADMIN);
            userFactory.createUser("old", Role.USER);

            mockMvc.perform(post("/api/users")
                            .header("Authorization", "Bearer " + getToken(admin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "old",
                                        "password": "password123",
                                        "role": "USER"
                                    }
                                    """)
                    )
                    .andExpect(status().isConflict());
        }

        @Test
        void shouldReturn403WhenUserIsAuthenticatedButNotAuthorized() throws Exception {
            User admin = userFactory.createUser("admin", Role.USER);

            mockMvc.perform(post("/api/users")
                            .header("Authorization", "Bearer " + getToken(admin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "old",
                                        "password": "password123",
                                        "role": "USER"
                                    }
                                    """)
                    )
                    .andExpect(status().isForbidden());

            assertThat(userRepository.findByUsername("old")).isEmpty();
        }

        @Test
        void shouldReturn401WhenUserIsUnauthenticated() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "novi",
                                        "password": "password123",
                                        "role": "USER"
                                    }
                                    """)
                    )
                    .andExpect(status().isUnauthorized());
            assertThat(userRepository.findByUsername("novi")).isEmpty();


        }

    }

    @Nested
    @DisplayName("PATCH /api/users/{id}")
    class UpdateUser {
        @Test
        void shouldReturn200WhenUserIsAdmin() throws Exception {
            User admin = userFactory.createUser("admin", Role.ADMIN);
            User other = userFactory.createUser("oldName", Role.USER);

            mockMvc.perform(patch("/api/users/" + other.getId())
                            .header("Authorization", "Bearer " + getToken(admin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "newName",
                                        "role": "ADMIN"
                                    }
                                    """)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("newName"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));

            assertThat(userRepository.findByUsername("oldName")).isEmpty();
            User updated = userRepository.findByUsername("newName").orElseThrow();
            assertThat(updated.getUsername()).isEqualTo("newName");
            assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        void shouldReturn409WhenUserIsAuthorizedButUserWithUsernameAlreadyExists() throws Exception {
            User admin = userFactory.createUser("admin", Role.ADMIN);
            User user1 = userFactory.createUser("user1", Role.USER);
            User user2 = userFactory.createUser("user2", Role.USER);

            mockMvc.perform(patch("/api/users/" + user1.getId())
                            .header("Authorization", "Bearer " + getToken(admin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "user2"
                                    }
                                    """)
                    )
                    .andExpect(status().isConflict());
            User user1after = userRepository.findById(user1.getId()).orElseThrow();
            assertThat(user1after.getUsername()).isEqualTo("user1");

            User user2after = userRepository.findById(user2.getId()).orElseThrow();
            assertThat(user2after.getUsername()).isEqualTo("user2");
        }

        @Test
        void shouldReturn403WhenUserIsAuthenticatedButNotAuthorized() throws Exception {
            User user = userFactory.createUser("user", Role.USER);
            User old = userFactory.createUser("old", Role.USER);

            mockMvc.perform(patch("/api/users/" + old.getId())
                            .header("Authorization", "Bearer " + getToken(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "new",
                                        "password": "password123",
                                        "role": "USER"
                                    }
                                    """)
                    )
                    .andExpect(status().isForbidden());
            User oldAfter = userRepository.findById(old.getId()).orElseThrow();
            assertThat(oldAfter.getUsername()).isEqualTo("old");
            assertThat(oldAfter.getRole()).isEqualTo(Role.USER);
        }

        @Test
        void shouldReturn401WhenUserIsUnauthenticated() throws Exception {
            User old = userFactory.createUser("old", Role.USER);

            mockMvc.perform(patch("/api/users/" + old.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "old",
                                        "password": "password123",
                                        "role": "USER"
                                    }
                                    """)
                    )
                    .andExpect(status().isUnauthorized());

        }

    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {
        @Test
        void shouldReturn204WhenUserIsAdmin() throws Exception {
            User admin = userFactory.createUser("admin", Role.ADMIN);
            User toBeDeleted = userFactory.createUser("oldName", Role.USER);

            mockMvc.perform(delete("/api/users/" + toBeDeleted.getId())
                            .header("Authorization", "Bearer " + getToken(admin))
                    )
                    .andExpect(status().isNoContent());

            assertThat(userRepository.findByUsername("oldName")).isEmpty();
        }

        @Test
        void shouldReturn204WhenUserDeletesHimself() throws Exception {
            User user1 = userFactory.createUser("user1", Role.USER);

            mockMvc.perform(delete("/api/users/" + user1.getId())
                            .header("Authorization", "Bearer " + getToken(user1))
                    )
                    .andExpect(status().isNoContent());
            assertThat(userRepository.findByUsername("user1")).isEmpty();

        }

        @Test
        void shouldReturn404WhenUserIsAuthorizedButUserByIdDoesntExist() throws Exception {
            User admin = userFactory.createUser("admin", Role.ADMIN);

            mockMvc.perform(delete("/api/users/" + "12312342324")
                            .header("Authorization", "Bearer " + getToken(admin))
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn403WhenUserIsAuthenticatedButNotAuthorized() throws Exception {
            User user = userFactory.createUser("user", Role.USER);
            User other = userFactory.createUser("other", Role.USER);

            mockMvc.perform(delete("/api/users/" + other.getId())
                            .header("Authorization", "Bearer " + getToken(user))
                    )
                    .andExpect(status().isForbidden());


            User otherAfter = userRepository.findById(other.getId()).orElseThrow();
            assertThat(otherAfter.getUsername()).isEqualTo("other");
            assertThat(otherAfter.getRole()).isEqualTo(Role.USER);
        }

        @Test
        void shouldReturn401WhenUserIsUnauthenticated() throws Exception {
            User old = userFactory.createUser("old", Role.USER);

            mockMvc.perform(delete("/api/users/" + old.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "old",
                                        "password": "password123",
                                        "role": "USER"
                                    }
                                    """)
                    )
                    .andExpect(status().isUnauthorized());

        }

    }


}

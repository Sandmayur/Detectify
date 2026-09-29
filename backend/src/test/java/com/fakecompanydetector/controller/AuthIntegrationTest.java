package com.fakecompanydetector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fakecompanydetector.dto.LoginRequest;
import com.fakecompanydetector.dto.RegisterRequest;
import com.fakecompanydetector.entity.User;
import com.fakecompanydetector.entity.enums.Role;
import com.fakecompanydetector.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void register_DuplicateEmail_Returns409() throws Exception {
        User user = User.builder().email("test@example.com").passwordHash("hash").role(Role.USER).build();
        userRepository.save(user);

        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void login_WrongPassword_Returns401() throws Exception {
        User user = User.builder().email("test@example.com").passwordHash(passwordEncoder.encode("correct123")).role(Role.USER).build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest("test@example.com", "wrongpass");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void adminRoute_UserRole_Returns403() throws Exception {
        User user = User.builder().email("user@example.com").passwordHash(passwordEncoder.encode("pass123")).role(Role.USER).build();
        userRepository.save(user);
        
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("user@example.com", "pass123"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // Request admin route
        mockMvc.perform(get("/api/admin/test")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden()); // 403 Forbidden
    }
}

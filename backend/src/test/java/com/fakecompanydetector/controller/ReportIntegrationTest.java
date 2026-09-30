package com.fakecompanydetector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fakecompanydetector.dto.SubmitReportRequest;
import com.fakecompanydetector.dto.VoteRequest;
import com.fakecompanydetector.entity.Company;
import com.fakecompanydetector.entity.Report;
import com.fakecompanydetector.entity.User;
import com.fakecompanydetector.entity.enums.ReportStatus;
import com.fakecompanydetector.entity.enums.Role;
import com.fakecompanydetector.repository.CompanyRepository;
import com.fakecompanydetector.repository.ReportRepository;
import com.fakecompanydetector.repository.UserRepository;
import com.fakecompanydetector.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ReportIntegrationTest {

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
    private CompanyRepository companyRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private JwtService jwtService;

    private User user1;
    private User user2;
    private Company company;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();

        user1 = userRepository.save(User.builder().email("user1@test.com").passwordHash("hash").role(Role.USER).build());
        user2 = userRepository.save(User.builder().email("user2@test.com").passwordHash("hash").role(Role.USER).build());
        company = companyRepository.save(Company.builder().domain("testcompany.com").normalizedName("testcompany").build());
    }

    @Test
    void submitReport_Success() throws Exception {
        String token = jwtService.generateToken(user1.getId(), user1.getEmail(), user1.getRole().name());

        SubmitReportRequest request = SubmitReportRequest.builder()
                .companyDomain("testcompany.com")
                .description("They asked for money")
                .jobUrl("http://example.com/job")
                .build();

        mockMvc.perform(post("/api/reports")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void vote_Success_And_Update() throws Exception {
        Report report = reportRepository.save(Report.builder()
                .company(company)
                .user(user1)
                .status(ReportStatus.APPROVED)
                .description("Test")
                .netUpvotes(0)
                .build());

        String token2 = jwtService.generateToken(user2.getId(), user2.getEmail(), user2.getRole().name());

        // Upvote
        VoteRequest request = new VoteRequest(true);
        mockMvc.perform(post("/api/reports/" + report.getId() + "/vote")
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netUpvotes").value(1));

        // Downvote (change mind) -> should go from +1 to -1
        request.setIsUpvote(false);
        mockMvc.perform(post("/api/reports/" + report.getId() + "/vote")
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netUpvotes").value(-1));
    }

    @Test
    void vote_OwnReport_Fails() throws Exception {
        Report report = reportRepository.save(Report.builder()
                .company(company)
                .user(user1)
                .status(ReportStatus.APPROVED)
                .description("Test")
                .netUpvotes(0)
                .build());

        String token1 = jwtService.generateToken(user1.getId(), user1.getEmail(), user1.getRole().name());

        VoteRequest request = new VoteRequest(true);
        mockMvc.perform(post("/api/reports/" + report.getId() + "/vote")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // Or whatever handles IllegalArgumentException
    }
}

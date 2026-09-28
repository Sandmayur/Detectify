package com.fakecompanydetector.repository;

import com.fakecompanydetector.entity.Company;
import com.fakecompanydetector.entity.Report;
import com.fakecompanydetector.entity.User;
import com.fakecompanydetector.entity.enums.ReportStatus;
import com.fakecompanydetector.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReportRepositoryTest {

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
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company company;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("testuser@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        company = Company.builder()
                .domain("test.com")
                .normalizedName("test company")
                .build();
        companyRepository.save(company);
    }

    @Test
    void testFindByCompanyIdAndStatus() {
        Report report = Report.builder()
                .company(company)
                .user(user)
                .description("Test report")
                .status(ReportStatus.APPROVED)
                .build();
        reportRepository.save(report);

        Page<Report> reports = reportRepository.findByCompanyIdAndStatus(company.getId(), ReportStatus.APPROVED, PageRequest.of(0, 10));

        assertThat(reports.getTotalElements()).isEqualTo(1);
        assertThat(reports.getContent().get(0).getDescription()).isEqualTo("Test report");
    }
}

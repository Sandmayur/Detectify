package com.fakecompanydetector.config;

import com.fakecompanydetector.entity.Company;
import com.fakecompanydetector.entity.User;
import com.fakecompanydetector.entity.enums.Role;
import com.fakecompanydetector.repository.CompanyRepository;
import com.fakecompanydetector.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevDataInitializer {

    @Value("${SEED_ADMIN_EMAIL:admin@example.com}")
    private String adminEmail;

    @Value("${SEED_ADMIN_PASSWORD:adminpassword}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, CompanyRepository companyRepository) {
        return args -> {
            // Seed Admin User
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = User.builder()
                        .email(adminEmail)
                        // In a real app, hash this via PasswordEncoder. For seed raw string is okay if bcrypt is applied in auth phase.
                        .passwordHash(adminPassword)
                        .role(Role.ADMIN)
                        .isSuspended(false)
                        .build();
                userRepository.save(admin);
            }

            // Seed Demo Company
            if (companyRepository.findByDomain("demo-fake-company.com").isEmpty()) {
                Company demo = Company.builder()
                        .domain("demo-fake-company.com")
                        .normalizedName("demo fake company")
                        .originalName("Demo Fake Company Pvt Ltd")
                        .isDemo(true)
                        .build();
                companyRepository.save(demo);
            }
        };
    }
}

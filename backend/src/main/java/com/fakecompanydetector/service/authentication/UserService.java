package com.fakecompanydetector.service.authentication;

import com.fakecompanydetector.entity.User;
import com.fakecompanydetector.exception.UnauthorizedException;
import com.fakecompanydetector.repository.ReportRepository;
import com.fakecompanydetector.repository.UserRepository;
import com.fakecompanydetector.repository.ReportVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ReportVoteRepository reportVoteRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Incorrect old password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        // Anonymize reports
        reportRepository.anonymizeReportsByUserId(userId);
        
        // Delete votes
        reportVoteRepository.deleteByUserId(userId);
        
        // Delete user
        userRepository.deleteById(userId);
    }
}

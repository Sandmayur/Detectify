package com.fakecompanydetector.controller;

import com.fakecompanydetector.dto.PaginatedResponse;
import com.fakecompanydetector.dto.ReportResponse;
import com.fakecompanydetector.dto.SubmitReportRequest;
import com.fakecompanydetector.dto.VoteRequest;
import com.fakecompanydetector.service.report.ReportService;
import com.fakecompanydetector.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ReportResponse> submitReport(@Valid @RequestBody SubmitReportRequest request) {
        UUID userId = getCurrentUserId();
        ReportResponse response = reportService.submitReport(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<ReportResponse> vote(@PathVariable UUID id, @Valid @RequestBody VoteRequest request) {
        UUID userId = getCurrentUserId();
        ReportResponse response = reportService.vote(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ReportResponse>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        UUID currentUserId = getCurrentUserIdSafe();
        PaginatedResponse<ReportResponse> response = reportService.getApprovedReports(PageRequest.of(page, size), currentUserId);
        return ResponseEntity.ok(response);
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }
        
        String email = (String) auth.getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("User not found"))
                .getId();
    }

    private UUID getCurrentUserIdSafe() {
        try {
            return getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}

package com.fakecompanydetector.controller;

import com.fakecompanydetector.dto.PaginatedResponse;
import com.fakecompanydetector.dto.ReportResponse;
import com.fakecompanydetector.dto.ReportStatusUpdateRequest;
import com.fakecompanydetector.entity.enums.ReportStatus;
import com.fakecompanydetector.service.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/reports")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PaginatedResponse<ReportResponse>> getReports(
            @RequestParam(required = false, defaultValue = "PENDING") ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PaginatedResponse<ReportResponse> response = adminService.getReportsByStatus(status, PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/reports/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReportResponse> updateReportStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ReportStatusUpdateRequest request) {
        
        ReportResponse response = adminService.updateReportStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }
}

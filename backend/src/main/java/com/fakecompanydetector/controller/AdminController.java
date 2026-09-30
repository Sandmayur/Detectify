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

    @PatchMapping("/reports/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReportResponse> approveReport(@PathVariable UUID id) {
        ReportResponse response = adminService.updateReportStatus(id, ReportStatus.APPROVED);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/reports/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReportResponse> rejectReport(
            @PathVariable UUID id,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        // We could log the reason from body.get("reason"), but for now just update status
        ReportResponse response = adminService.updateReportStatus(id, ReportStatus.REJECTED);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/disputes")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PaginatedResponse<com.fakecompanydetector.dto.DisputeResponse>> getDisputes(
            @RequestParam(required = false, defaultValue = "PENDING") com.fakecompanydetector.entity.enums.DisputeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PaginatedResponse<com.fakecompanydetector.dto.DisputeResponse> response = adminService.getDisputes(status, PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/disputes/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> resolveDispute(
            @PathVariable UUID id,
            @RequestParam com.fakecompanydetector.entity.enums.DisputeStatus status) {
        
        adminService.resolveDispute(id, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/suspend")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> suspendUser(
            @PathVariable UUID id,
            @RequestParam boolean suspend) {
        
        adminService.suspendUser(id, suspend);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PaginatedResponse<com.fakecompanydetector.dto.AdminUserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PaginatedResponse<com.fakecompanydetector.dto.AdminUserDto> response = adminService.getUsers(PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<com.fakecompanydetector.dto.AdminStatisticsDto> getStatistics() {
        return ResponseEntity.ok(adminService.getStatistics());
    }

    @GetMapping("/keywords")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PaginatedResponse<com.fakecompanydetector.dto.ScamKeywordDto>> getKeywords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PaginatedResponse<com.fakecompanydetector.dto.ScamKeywordDto> response = adminService.getKeywords(PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/keywords")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<com.fakecompanydetector.dto.ScamKeywordDto> addKeyword(
            @Valid @RequestBody com.fakecompanydetector.dto.ScamKeywordRequest request) {
        
        return ResponseEntity.ok(adminService.addKeyword(request));
    }

    @DeleteMapping("/keywords/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteKeyword(@PathVariable UUID id) {
        adminService.deleteKeyword(id);
        return ResponseEntity.ok().build();
    }
}

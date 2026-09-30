package com.fakecompanydetector.service.admin;

import com.fakecompanydetector.dto.PaginatedResponse;
import com.fakecompanydetector.dto.ReportResponse;
import com.fakecompanydetector.entity.Report;
import com.fakecompanydetector.entity.enums.ReportStatus;
import com.fakecompanydetector.repository.ReportRepository;
import com.fakecompanydetector.dto.AdminUserDto;
import com.fakecompanydetector.entity.User;
import com.fakecompanydetector.entity.enums.DisputeStatus;
import com.fakecompanydetector.repository.DisputeRepository;
import com.fakecompanydetector.repository.UserRepository;
import com.fakecompanydetector.repository.CompanyRepository;
import com.fakecompanydetector.repository.AuditLogRepository;
import com.fakecompanydetector.repository.ScamKeywordRepository;
import com.fakecompanydetector.dto.AdminStatisticsDto;
import com.fakecompanydetector.dto.ScamKeywordDto;
import com.fakecompanydetector.dto.ScamKeywordRequest;
import com.fakecompanydetector.entity.AuditLog;
import com.fakecompanydetector.entity.ScamKeyword;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ReportRepository reportRepository;
    private final DisputeRepository disputeRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AuditLogRepository auditLogRepository;
    private final ScamKeywordRepository scamKeywordRepository;

    private void logAdminAction(String action, String targetId, String details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return;
        
        String email = (String) auth.getPrincipal();
        User adminUser = userRepository.findByEmail(email).orElse(null);
        if (adminUser == null) return;

        AuditLog log = AuditLog.builder()
                .adminUser(adminUser)
                .action(action)
                .targetId(targetId)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ReportResponse> getReportsByStatus(ReportStatus status, Pageable pageable) {
        Page<Report> page = reportRepository.findByStatus(status, pageable);
        
        return PaginatedResponse.<ReportResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional
    public ReportResponse updateReportStatus(UUID reportId, ReportStatus newStatus) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        report.setStatus(newStatus);
        report = reportRepository.save(report);
        
        logAdminAction("UPDATE_REPORT_STATUS", reportId.toString(), "Changed status to " + newStatus);
        return mapToResponse(report);
    }

    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .companyId(report.getCompany().getId())
                .companyName(report.getCompany().getOriginalName())
                .companyDomain(report.getCompany().getDomain())
                .description(report.getDescription())
                .jobUrl(report.getJobUrl())
                // Admins see unmasked data in a real app, but for simplicity we reuse the DTO which is currently used by public.
                // We will send unmasked data to admin here since this goes to the Admin view.
                .recruiterEmail(report.getRecruiterEmail())
                .recruiterPhone(report.getRecruiterPhone())
                .evidenceUrl(report.getEvidenceUrl())
                .netUpvotes(report.getNetUpvotes())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<com.fakecompanydetector.dto.DisputeResponse> getDisputes(DisputeStatus status, Pageable pageable) {
        Page<com.fakecompanydetector.entity.Dispute> page = disputeRepository.findByStatus(status, pageable);
        
        return PaginatedResponse.<com.fakecompanydetector.dto.DisputeResponse>builder()
                .content(page.getContent().stream().map(this::mapDisputeToResponse).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<AdminUserDto> getUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        
        return PaginatedResponse.<AdminUserDto>builder()
                .content(page.getContent().stream().map(u -> AdminUserDto.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .role(u.getRole().name())
                        .isSuspended(u.getIsSuspended())
                        .build()).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private com.fakecompanydetector.dto.DisputeResponse mapDisputeToResponse(com.fakecompanydetector.entity.Dispute dispute) {
        return com.fakecompanydetector.dto.DisputeResponse.builder()
                .id(dispute.getId())
                .reportId(dispute.getReport() != null ? dispute.getReport().getId() : null)
                .companyId(dispute.getCompany().getId())
                .companyDomain(dispute.getCompany().getDomain())
                .contactEmail(dispute.getContactEmail())
                .reason(dispute.getReason())
                .status(dispute.getStatus())
                .createdAt(dispute.getCreatedAt())
                .build();
    }

    @Transactional
    public void resolveDispute(UUID disputeId, DisputeStatus status) {
        com.fakecompanydetector.entity.Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));
        dispute.setStatus(status);
        disputeRepository.save(dispute);
        
        logAdminAction("RESOLVE_DISPUTE", disputeId.toString(), "Changed status to " + status);
    }

    @Transactional
    public void suspendUser(UUID userId, boolean suspend) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setIsSuspended(suspend);
        userRepository.save(user);
        
        logAdminAction("SUSPEND_USER", userId.toString(), suspend ? "Suspended user" : "Unsuspended user");
    }

    @Transactional(readOnly = true)
    public AdminStatisticsDto getStatistics() {
        return AdminStatisticsDto.builder()
                .totalCompanies(companyRepository.count())
                .totalReports(reportRepository.count())
                .totalUsers(userRepository.count())
                .pendingReports(reportRepository.countByStatus(ReportStatus.PENDING))
                .pendingDisputes(disputeRepository.countByStatus(DisputeStatus.PENDING))
                .flaggedReports(0) // Mocked for now
                .build();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ScamKeywordDto> getKeywords(Pageable pageable) {
        Page<ScamKeyword> page = scamKeywordRepository.findAll(pageable);
        return PaginatedResponse.<ScamKeywordDto>builder()
                .content(page.getContent().stream().map(k -> ScamKeywordDto.builder()
                        .id(k.getId())
                        .keyword(k.getKeyword())
                        .category(k.getCategory())
                        .weight(k.getWeight())
                        .isActive(k.getIsActive())
                        .build()).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional
    public ScamKeywordDto addKeyword(ScamKeywordRequest request) {
        ScamKeyword keyword = ScamKeyword.builder()
                .keyword(request.getKeyword())
                .category(request.getCategory())
                .weight(request.getWeight())
                .isActive(request.getIsActive())
                .build();
        keyword = scamKeywordRepository.save(keyword);
        
        logAdminAction("ADD_KEYWORD", keyword.getId().toString(), "Added keyword: " + keyword.getKeyword());
        
        return ScamKeywordDto.builder()
                .id(keyword.getId())
                .keyword(keyword.getKeyword())
                .category(keyword.getCategory())
                .weight(keyword.getWeight())
                .isActive(keyword.getIsActive())
                .build();
    }

    @Transactional
    public void deleteKeyword(UUID id) {
        scamKeywordRepository.deleteById(id);
        logAdminAction("DELETE_KEYWORD", id.toString(), "Deleted keyword");
    }
}

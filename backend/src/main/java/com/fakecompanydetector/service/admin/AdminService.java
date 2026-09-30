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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ReportRepository reportRepository;
    private final DisputeRepository disputeRepository;
    private final UserRepository userRepository;

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
    }

    @Transactional
    public void suspendUser(UUID userId, boolean suspend) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setIsSuspended(suspend);
        userRepository.save(user);
    }
}

package com.fakecompanydetector.service.admin;

import com.fakecompanydetector.dto.PaginatedResponse;
import com.fakecompanydetector.dto.ReportResponse;
import com.fakecompanydetector.entity.Report;
import com.fakecompanydetector.entity.enums.ReportStatus;
import com.fakecompanydetector.repository.ReportRepository;
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
}

package com.fakecompanydetector.service.report;

import com.fakecompanydetector.dto.PaginatedResponse;
import com.fakecompanydetector.dto.ReportResponse;
import com.fakecompanydetector.dto.SubmitReportRequest;
import com.fakecompanydetector.dto.VoteRequest;
import com.fakecompanydetector.entity.Company;
import com.fakecompanydetector.entity.Report;
import com.fakecompanydetector.entity.ReportVote;
import com.fakecompanydetector.entity.User;
import com.fakecompanydetector.entity.enums.ReportStatus;
import com.fakecompanydetector.repository.CompanyRepository;
import com.fakecompanydetector.repository.ReportRepository;
import com.fakecompanydetector.repository.ReportVoteRepository;
import com.fakecompanydetector.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportVoteRepository reportVoteRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportResponse submitReport(UUID userId, SubmitReportRequest request) {
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }

        boolean hasEvidence = (request.getJobUrl() != null && !request.getJobUrl().trim().isEmpty())
                || (request.getRecruiterEmail() != null && !request.getRecruiterEmail().trim().isEmpty())
                || (request.getRecruiterPhone() != null && !request.getRecruiterPhone().trim().isEmpty())
                || (request.getEvidenceUrl() != null && !request.getEvidenceUrl().trim().isEmpty());

        if (!hasEvidence) {
            throw new IllegalArgumentException("At least one piece of evidence (URL, email, phone, or screenshot) must be provided");
        }

        if (request.getCompanyDomain() == null && request.getCompanyName() == null) {
            throw new IllegalArgumentException("Company domain or name must be provided");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Company company = findOrCreateCompany(request.getCompanyDomain(), request.getCompanyName());

        Report report = Report.builder()
                .company(company)
                .user(user)
                .status(ReportStatus.PENDING) // Default to pending
                .description(request.getDescription())
                .jobUrl(request.getJobUrl())
                .recruiterEmail(request.getRecruiterEmail())
                .recruiterPhone(request.getRecruiterPhone())
                .evidenceUrl(request.getEvidenceUrl())
                .netUpvotes(0)
                .build();

        report = reportRepository.save(report);

        return mapToResponse(report, userId);
    }

    @Transactional
    public ReportResponse vote(UUID userId, UUID reportId, VoteRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        if (report.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Users cannot vote on their own reports");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<ReportVote> existingVoteOpt = reportVoteRepository.findByReportIdAndUserId(reportId, userId);

        if (existingVoteOpt.isPresent()) {
            ReportVote existingVote = existingVoteOpt.get();
            if (!existingVote.getIsUpvote().equals(request.getIsUpvote())) {
                // User changed their vote
                existingVote.setIsUpvote(request.getIsUpvote());
                reportVoteRepository.save(existingVote);
                
                // Adjust net upvotes: if it was upvote (+1) and becomes downvote (-1), delta is -2.
                // If it was downvote (-1) and becomes upvote (+1), delta is +2.
                report.setNetUpvotes(report.getNetUpvotes() + (request.getIsUpvote() ? 2 : -2));
            }
            // If they cast the same vote, do nothing
        } else {
            ReportVote newVote = ReportVote.builder()
                    .report(report)
                    .user(user)
                    .isUpvote(request.getIsUpvote())
                    .build();
            reportVoteRepository.save(newVote);
            
            report.setNetUpvotes(report.getNetUpvotes() + (request.getIsUpvote() ? 1 : -1));
        }

        report = reportRepository.save(report);
        return mapToResponse(report, userId);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ReportResponse> getApprovedReports(Pageable pageable, UUID currentUserId) {
        Page<Report> reportsPage = reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.APPROVED, pageable);
        
        List<ReportResponse> content = reportsPage.getContent().stream()
                .map(report -> mapToResponse(report, currentUserId))
                .collect(Collectors.toList());

        return PaginatedResponse.<ReportResponse>builder()
                .content(content)
                .pageNumber(reportsPage.getNumber())
                .pageSize(reportsPage.getSize())
                .totalElements(reportsPage.getTotalElements())
                .totalPages(reportsPage.getTotalPages())
                .last(reportsPage.isLast())
                .build();
    }

    private Company findOrCreateCompany(String domain, String name) {
        if (domain != null && !domain.trim().isEmpty()) {
            Optional<Company> existing = companyRepository.findByDomain(domain);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        
        String normalizedName = name != null ? name.toLowerCase().trim() : domain.toLowerCase().trim();
        Optional<Company> existingByName = companyRepository.findByNormalizedName(normalizedName);
        if (existingByName.isPresent()) {
            return existingByName.get();
        }

        Company company = Company.builder()
                .domain(domain)
                .normalizedName(normalizedName)
                .originalName(name != null ? name : domain)
                .isDemo(false)
                .build();
        
        return companyRepository.save(company);
    }

    private ReportResponse mapToResponse(Report report, UUID currentUserId) {
        Boolean userHasVoted = false;
        Boolean userVoteIsUpvote = null;

        if (currentUserId != null) {
            Optional<ReportVote> vote = reportVoteRepository.findByReportIdAndUserId(report.getId(), currentUserId);
            if (vote.isPresent()) {
                userHasVoted = true;
                userVoteIsUpvote = vote.get().getIsUpvote();
            }
        }

        return ReportResponse.builder()
                .id(report.getId())
                .companyId(report.getCompany().getId())
                .companyName(report.getCompany().getOriginalName())
                .companyDomain(report.getCompany().getDomain())
                .description(report.getDescription())
                .jobUrl(report.getJobUrl())
                .recruiterEmail(maskEmail(report.getRecruiterEmail()))
                .recruiterPhone(maskPhone(report.getRecruiterPhone()))
                .evidenceUrl(report.getEvidenceUrl())
                .netUpvotes(report.getNetUpvotes())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .userHasVoted(userHasVoted)
                .userVoteIsUpvote(userVoteIsUpvote)
                .build();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) return "**@" + parts[1];
        return parts[0].substring(0, 2) + "****@" + parts[1];
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }
}

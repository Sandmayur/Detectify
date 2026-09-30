package com.fakecompanydetector.service.dispute;

import com.fakecompanydetector.dto.DisputeResponse;
import com.fakecompanydetector.dto.SubmitDisputeRequest;
import com.fakecompanydetector.entity.Company;
import com.fakecompanydetector.entity.Dispute;
import com.fakecompanydetector.entity.Report;
import com.fakecompanydetector.entity.enums.DisputeStatus;
import com.fakecompanydetector.repository.CompanyRepository;
import com.fakecompanydetector.repository.DisputeRepository;
import com.fakecompanydetector.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final ReportRepository reportRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public DisputeResponse submitDispute(SubmitDisputeRequest request) {
        Company company = companyRepository.findByDomain(request.getCompanyDomain())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        
        Report report = null;
        if (request.getReportId() != null) {
            report = reportRepository.findById(request.getReportId())
                    .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        }

        Dispute dispute = Dispute.builder()
                .company(company)
                .report(report)
                .contactEmail(request.getContactEmail())
                .reason(request.getReason())
                .status(DisputeStatus.PENDING)
                .build();

        dispute = disputeRepository.save(dispute);
        return mapToResponse(dispute);
    }

    private DisputeResponse mapToResponse(Dispute dispute) {
        return DisputeResponse.builder()
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
}

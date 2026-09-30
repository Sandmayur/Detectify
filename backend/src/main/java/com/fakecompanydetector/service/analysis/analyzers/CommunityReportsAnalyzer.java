package com.fakecompanydetector.service.analysis.analyzers;

import com.fakecompanydetector.entity.Report;
import com.fakecompanydetector.entity.enums.ReportStatus;
import com.fakecompanydetector.entity.enums.SignalConfidence;
import com.fakecompanydetector.entity.enums.SignalType;
import com.fakecompanydetector.repository.ReportRepository;
import com.fakecompanydetector.service.analysis.AnalysisContext;
import com.fakecompanydetector.service.analysis.RiskSignalAnalyzer;
import com.fakecompanydetector.service.analysis.SignalResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommunityReportsAnalyzer implements RiskSignalAnalyzer {

    private final ReportRepository reportRepository;

    @Override
    public boolean supports(AnalysisContext ctx) {
        return ctx.getResolvedDomain() != null && !ctx.getResolvedDomain().isBlank();
    }

    @Override
    public SignalResult analyze(AnalysisContext ctx) {
        // Need to find by company domain. We don't have companyId directly in context unless resolved.
        // For simplicity, assuming reportRepository has a way to find by domain or we do it via Company
        List<Report> approvedReports = reportRepository.findByCompanyDomainAndStatus(ctx.getResolvedDomain(), ReportStatus.APPROVED);
        
        if (approvedReports.isEmpty()) {
            return SignalResult.builder()
                    .signalName("Community reports")
                    .signalType(SignalType.NEUTRAL)
                    .points(0)
                    .explanation("No approved community reports found for this company.")
                    .confidence(SignalConfidence.HIGH)
                    .dataSource("Detectify DB")
                    .build();
        }

        int points = 0;
        for (Report r : approvedReports) {
            points += 5; // +5 per report
            if (r.getNetUpvotes() != null && r.getNetUpvotes() > 0) {
                points += (r.getNetUpvotes() / 5) * 2; // +2 per 5 net upvotes
            }
        }
        
        points = Math.min(points, 30); // cap at 30 total

        return SignalResult.builder()
                .signalName("Community reports")
                .signalType(SignalType.RED_FLAG)
                .points(points)
                .explanation("Found " + approvedReports.size() + " approved user-submitted report(s) (unverified allegations).")
                .evidenceJson("{\"reportCount\":" + approvedReports.size() + "}")
                .confidence(SignalConfidence.MEDIUM) // Unverified allegations
                .dataSource("Detectify DB")
                .build();
    }
}

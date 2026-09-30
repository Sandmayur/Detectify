package com.fakecompanydetector.service.analysis;

import com.fakecompanydetector.dto.AnalysisRequest;
import com.fakecompanydetector.dto.AnalysisResponse;
import com.fakecompanydetector.entity.AnalysisSignal;
import com.fakecompanydetector.entity.Company;
import com.fakecompanydetector.entity.CompanyCheck;
import com.fakecompanydetector.entity.enums.RiskCategory;
import com.fakecompanydetector.entity.enums.SignalType;
import com.fakecompanydetector.repository.AnalysisSignalRepository;
import com.fakecompanydetector.repository.CompanyCheckRepository;
import com.fakecompanydetector.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalysisOrchestrator {

    private final List<RiskSignalAnalyzer> analyzers;
    private final CompanyRepository companyRepository;
    private final CompanyCheckRepository companyCheckRepository;
    private final AnalysisSignalRepository analysisSignalRepository;

    private static final String RULES_VERSION = "1.0.0";

    @Transactional
    public AnalysisResponse analyze(AnalysisRequest request) {
        // 1. Resolve Company / Domain
        String rawIdentifier = request.getCompanyIdentifier();
        String domain = rawIdentifier; // Simplified for this phase. In reality, we'd extract domain from URL or name.
        if (rawIdentifier.contains("://")) {
            domain = rawIdentifier.substring(rawIdentifier.indexOf("://") + 3);
            if (domain.contains("/")) {
                domain = domain.substring(0, domain.indexOf("/"));
            }
        }
        domain = domain.toLowerCase().trim();

        Company company = findOrCreateCompany(domain, rawIdentifier);

        // 2. Build Context
        AnalysisContext context = AnalysisContext.builder()
                .request(request)
                .resolvedDomain(domain)
                .build();

        // 3. Run Analyzers
        List<SignalResult> signalResults = new ArrayList<>();
        int finalScore = 0;

        for (RiskSignalAnalyzer analyzer : analyzers) {
            if (analyzer.supports(context)) {
                SignalResult result = analyzer.analyze(context);
                if (result != null) {
                    signalResults.add(result);
                    if (result.getPoints() != null) {
                        finalScore += result.getPoints();
                    }
                }
            }
        }

        // 4. Determine Risk Category
        RiskCategory category = determineCategory(finalScore, signalResults);

        // 5. Save Check and Signals
        CompanyCheck check = CompanyCheck.builder()
                .company(company)
                .rulesVersion(RULES_VERSION)
                .finalScore(finalScore)
                .riskCategory(category)
                // user is null for anonymous checks
                .build();
        check = companyCheckRepository.save(check);

        List<AnalysisResponse.SignalDto> signalDtos = new ArrayList<>();

        for (SignalResult sr : signalResults) {
            AnalysisSignal signal = AnalysisSignal.builder()
                    .check(check)
                    .signalName(sr.getSignalName())
                    .signalType(sr.getSignalType())
                    .points(sr.getPoints())
                    .explanation(sr.getExplanation())
                    .evidenceJson(sr.getEvidenceJson())
                    .confidence(sr.getConfidence())
                    .dataSource(sr.getDataSource())
                    .build();
            analysisSignalRepository.save(signal);

            signalDtos.add(AnalysisResponse.SignalDto.builder()
                    .signalName(sr.getSignalName())
                    .signalType(sr.getSignalType())
                    .points(sr.getPoints())
                    .explanation(sr.getExplanation())
                    .evidenceJson(sr.getEvidenceJson())
                    .confidence(sr.getConfidence())
                    .dataSource(sr.getDataSource())
                    .build());
        }

        // 6. Return Response
        return AnalysisResponse.builder()
                .checkId(check.getId())
                .companyId(company.getId())
                .companyName(company.getOriginalName())
                .rulesVersion(RULES_VERSION)
                .finalScore(finalScore)
                .riskCategory(category)
                .createdAt(check.getCreatedAt())
                .signals(signalDtos)
                .build();
    }

    private RiskCategory determineCategory(int score, List<SignalResult> signals) {
        // Any RED_FLAG guarantees HIGH_RISK, regardless of score.
        boolean hasRedFlag = signals.stream().anyMatch(s -> s.getSignalType() == SignalType.RED_FLAG);
        if (hasRedFlag) {
            return RiskCategory.HIGH;
        }

        if (score >= 40) return RiskCategory.HIGH;
        if (score >= 20) return RiskCategory.MEDIUM;
        return RiskCategory.LOW;
    }

    private Company findOrCreateCompany(String domain, String originalName) {
        Optional<Company> existing = companyRepository.findByDomain(domain);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        Optional<Company> existingByName = companyRepository.findByNormalizedName(domain);
        if (existingByName.isPresent()) {
            return existingByName.get();
        }

        Company company = Company.builder()
                .domain(domain)
                .normalizedName(domain)
                .originalName(originalName)
                .isDemo(false)
                .build();
        
        return companyRepository.save(company);
    }
}

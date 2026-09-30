package com.fakecompanydetector.service.analysis.analyzers;

import com.fakecompanydetector.entity.enums.SignalConfidence;
import com.fakecompanydetector.entity.enums.SignalType;
import com.fakecompanydetector.service.analysis.AnalysisContext;
import com.fakecompanydetector.service.analysis.RiskSignalAnalyzer;
import com.fakecompanydetector.service.analysis.SignalResult;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FreeEmailAnalyzer implements RiskSignalAnalyzer {

    private static final Set<String> FREE_PROVIDERS = Set.of(
            "gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "rediffmail.com", "protonmail.com"
    );

    @Override
    public boolean supports(AnalysisContext ctx) {
        return ctx.getRequest().getRecruiterEmail() != null && !ctx.getRequest().getRecruiterEmail().isBlank();
    }

    @Override
    public SignalResult analyze(AnalysisContext ctx) {
        String email = ctx.getRequest().getRecruiterEmail().trim().toLowerCase();
        String domain = "";
        
        if (email.contains("@")) {
            domain = email.substring(email.indexOf("@") + 1);
        }

        if (FREE_PROVIDERS.contains(domain)) {
            return SignalResult.builder()
                    .signalName("Recruiter email")
                    .signalType(SignalType.RED_FLAG)
                    .points(10)
                    .explanation("The recruiter is using a free email provider, which is highly unusual for legitimate corporate recruiters.")
                    .evidenceJson("{\"domain\":\"" + domain + "\"}")
                    .confidence(SignalConfidence.HIGH)
                    .dataSource("User Input")
                    .build();
        }

        // Email / Website mismatch logic
        String companyDomain = ctx.getResolvedDomain();
        if (companyDomain != null && !companyDomain.isBlank() && !domain.equals(companyDomain)) {
            return SignalResult.builder()
                    .signalName("Email/website mismatch")
                    .signalType(SignalType.WARNING)
                    .points(5)
                    .explanation("The recruiter email domain does not match the company's official domain.")
                    .evidenceJson("{\"emailDomain\":\"" + domain + "\", \"companyDomain\":\"" + companyDomain + "\"}")
                    .confidence(SignalConfidence.HIGH)
                    .dataSource("User Input")
                    .build();
        }

        return SignalResult.builder()
                .signalName("Recruiter email")
                .signalType(SignalType.POSITIVE)
                .points(0)
                .explanation("Recruiter email domain appears professional and matches the company.")
                .confidence(SignalConfidence.HIGH)
                .dataSource("User Input")
                .build();
    }
}

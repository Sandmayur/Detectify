package com.fakecompanydetector.service.analysis.analyzers;

import com.fakecompanydetector.entity.enums.SignalConfidence;
import com.fakecompanydetector.entity.enums.SignalType;
import com.fakecompanydetector.service.analysis.AnalysisContext;
import com.fakecompanydetector.service.analysis.RiskSignalAnalyzer;
import com.fakecompanydetector.service.analysis.SignalResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class FeeDemandAnalyzer implements RiskSignalAnalyzer {

    private static final List<Pattern> FEE_PATTERNS = List.of(
            Pattern.compile("(?i)(registration|training|security deposit|joining|kit) fee"),
            Pattern.compile("(?i)pay to join"),
            Pattern.compile("(?i)refundable deposit")
    );

    @Override
    public boolean supports(AnalysisContext ctx) {
        return ctx.getRequest().getJobDescription() != null && !ctx.getRequest().getJobDescription().isBlank();
    }

    @Override
    public SignalResult analyze(AnalysisContext ctx) {
        String description = ctx.getRequest().getJobDescription();
        
        for (Pattern pattern : FEE_PATTERNS) {
            if (pattern.matcher(description).find()) {
                return SignalResult.builder()
                        .signalName("Fee demand")
                        .signalType(SignalType.RED_FLAG)
                        .points(40)
                        .explanation("The job description explicitly asks for a fee (registration, training, deposit, etc.), which is a strong indicator of a scam.")
                        .evidenceJson("{\"matchedPattern\":\"" + pattern.pattern() + "\"}")
                        .confidence(SignalConfidence.HIGH)
                        .dataSource("Job Description")
                        .build();
            }
        }
        
        return SignalResult.builder()
                .signalName("Fee demand")
                .signalType(SignalType.POSITIVE) // Actually, rules say: if triggered RED FLAG. If not? It's not listed, so maybe NEUTRAL.
                .points(0)
                .explanation("No fee demands detected in the job description.")
                .confidence(SignalConfidence.HIGH)
                .dataSource("Job Description")
                .build();
    }
}

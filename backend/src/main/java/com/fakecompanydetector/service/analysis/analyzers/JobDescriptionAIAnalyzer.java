package com.fakecompanydetector.service.analysis.analyzers;

import com.fakecompanydetector.service.analysis.SignalResult;
import com.fakecompanydetector.entity.enums.SignalType;
import com.fakecompanydetector.entity.enums.SignalConfidence;
import com.fakecompanydetector.service.analysis.AnalysisContext;
import com.fakecompanydetector.service.analysis.RiskSignalAnalyzer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class JobDescriptionAIAnalyzer implements RiskSignalAnalyzer {

    @Value("${analysis.ai.enabled:false}")
    private boolean isAiEnabled;

    @Value("${analysis.ai.max-points:15}")
    private int maxAiPoints;

    @Override
    public boolean supports(AnalysisContext ctx) {
        return ctx.getRequest() != null && ctx.getRequest().getJobDescription() != null && !ctx.getRequest().getJobDescription().isBlank();
    }

    @Override
    public SignalResult analyze(AnalysisContext ctx) {
        if (!isAiEnabled) {
            return buildUnknownResult("AI analysis is disabled.");
        }

        try {
            // Mock AI Call
            // In a real implementation, this would call an LLM API and parse the result
            // treating the text as untrusted.
            String jobDesc = ctx.getRequest().getJobDescription().toLowerCase();
            
            // Dummy logic to simulate AI
            boolean hasSuspiciousTone = jobDesc.contains("urgent") || jobDesc.contains("guaranteed");

            if (hasSuspiciousTone) {
                return SignalResult.builder()
                        .signalName("AI Text Analysis")
                        .signalType(SignalType.WARNING)
                        .points(Math.min(10, maxAiPoints))
                        .explanation("AI analysis detected a highly urgent or unrealistic tone commonly used in fraudulent offers.")
                        .evidenceJson("{\"detail\": \"Detected suspicious semantics in job description\"}")
                        .confidence(SignalConfidence.MEDIUM)
                        .dataSource("AI Analyzer")
                        .build();
            } else {
                return SignalResult.builder()
                        .signalName("AI Text Analysis")
                        .signalType(SignalType.NEUTRAL)
                        .points(0)
                        .explanation("AI analysis found no overtly suspicious semantics.")
                        .evidenceJson("{\"detail\": \"Job description semantics appear normal\"}")
                        .confidence(SignalConfidence.MEDIUM)
                        .dataSource("AI Analyzer")
                        .build();
            }

        } catch (Exception e) {
            return buildUnknownResult("AI analysis failed: " + e.getMessage());
        }
    }

    private SignalResult buildUnknownResult(String reason) {
        return SignalResult.builder()
                .signalName("AI Text Analysis")
                .signalType(SignalType.UNKNOWN)
                .points(0)
                .explanation(reason)
                .confidence(SignalConfidence.LOW)
                .dataSource("AI Analyzer")
                .build();
    }
}

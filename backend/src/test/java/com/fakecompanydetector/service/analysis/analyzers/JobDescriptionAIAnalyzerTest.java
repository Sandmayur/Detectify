package com.fakecompanydetector.service.analysis.analyzers;

import com.fakecompanydetector.service.analysis.SignalResult;
import com.fakecompanydetector.entity.enums.SignalType;
import com.fakecompanydetector.dto.AnalysisRequest;
import com.fakecompanydetector.service.analysis.AnalysisContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JobDescriptionAIAnalyzerTest {

    private JobDescriptionAIAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new JobDescriptionAIAnalyzer();
        ReflectionTestUtils.setField(analyzer, "maxAiPoints", 15);
    }

    @Test
    void supports_ShouldReturnTrue_WhenJobDescriptionExists() {
        AnalysisContext ctx = AnalysisContext.builder()
                .request(AnalysisRequest.builder().jobDescription("Some desc").build())
                .build();
        assertTrue(analyzer.supports(ctx));
    }

    @Test
    void supports_ShouldReturnFalse_WhenJobDescriptionIsMissing() {
        AnalysisContext ctx = AnalysisContext.builder().request(AnalysisRequest.builder().build()).build();
        assertFalse(analyzer.supports(ctx));
    }

    @Test
    void analyze_ShouldReturnUnknown_WhenAiDisabled() {
        ReflectionTestUtils.setField(analyzer, "isAiEnabled", false);
        AnalysisContext ctx = AnalysisContext.builder()
                .request(AnalysisRequest.builder().jobDescription("Some desc").build())
                .build();

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.UNKNOWN, result.getSignalType());
        assertEquals(0, result.getPoints());
        assertTrue(result.getExplanation().contains("disabled"));
    }

    @Test
    void analyze_ShouldReturnWarning_WhenSuspiciousToneDetected() {
        ReflectionTestUtils.setField(analyzer, "isAiEnabled", true);
        AnalysisContext ctx = AnalysisContext.builder()
                .request(AnalysisRequest.builder().jobDescription("This is an urgent hiring requirement with guaranteed placement.").build())
                .build();

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.WARNING, result.getSignalType());
        assertEquals(10, result.getPoints());
    }

    @Test
    void analyze_ShouldReturnNeutral_WhenNoSuspiciousToneDetected() {
        ReflectionTestUtils.setField(analyzer, "isAiEnabled", true);
        AnalysisContext ctx = AnalysisContext.builder()
                .request(AnalysisRequest.builder().jobDescription("We are looking for a software engineer to join our team.").build())
                .build();

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.NEUTRAL, result.getSignalType());
        assertEquals(0, result.getPoints());
    }
}

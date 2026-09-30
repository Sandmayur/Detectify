package com.fakecompanydetector.service.analysis.analyzers;

import com.fakecompanydetector.dto.AnalysisRequest;
import com.fakecompanydetector.entity.enums.SignalType;
import com.fakecompanydetector.service.analysis.AnalysisContext;
import com.fakecompanydetector.service.analysis.SignalResult;
import com.fakecompanydetector.service.analysis.provider.RegistrationDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class RegistrationAnalyzerTest {

    @Mock
    private ObjectProvider<RegistrationDataProvider> providers;

    @InjectMocks
    private RegistrationAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNoProviderConfigured_ReturnsUnknown() {
        AnalysisRequest req = AnalysisRequest.builder().companyIdentifier("TestCorp").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).build();
        
        when(providers.stream()).thenReturn(Stream.empty());

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.UNKNOWN, result.getSignalType());
        assertEquals("No registration data source is configured.", result.getExplanation());
    }

    @Test
    void testProviderReturnsActive_ReturnsPositive() {
        AnalysisRequest req = AnalysisRequest.builder().companyIdentifier("TestCorp").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).build();
        
        RegistrationDataProvider provider = mock(RegistrationDataProvider.class);
        when(provider.isConfigured()).thenReturn(true);
        when(provider.query("TestCorp")).thenReturn(new RegistrationDataProvider.RegistrationResult(true, "U12345", "ACTIVE", "MCA", LocalDateTime.now()));
        
        when(providers.stream()).thenReturn(Stream.of(provider));

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.POSITIVE, result.getSignalType());
        assertEquals(-15, result.getPoints());
        assertEquals("Company is officially registered and active.", result.getExplanation());
    }

    @Test
    void testProviderReturnsStruckOff_ReturnsWarning() {
        AnalysisRequest req = AnalysisRequest.builder().companyIdentifier("TestCorp").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).build();
        
        RegistrationDataProvider provider = mock(RegistrationDataProvider.class);
        when(provider.isConfigured()).thenReturn(true);
        when(provider.query("TestCorp")).thenReturn(new RegistrationDataProvider.RegistrationResult(true, "U12345", "STRUCK_OFF", "MCA", LocalDateTime.now()));
        
        when(providers.stream()).thenReturn(Stream.of(provider));

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.WARNING, result.getSignalType());
        assertEquals(0, result.getPoints());
        assertEquals("Company is registered but its status is: STRUCK_OFF.", result.getExplanation());
    }

    @Test
    void testProviderReturnsNotFound_ReturnsRedFlag() {
        AnalysisRequest req = AnalysisRequest.builder().companyIdentifier("TestCorp").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).build();
        
        RegistrationDataProvider provider = mock(RegistrationDataProvider.class);
        when(provider.isConfigured()).thenReturn(true);
        when(provider.query("TestCorp")).thenReturn(new RegistrationDataProvider.RegistrationResult(false, null, null, "MCA", LocalDateTime.now()));
        
        when(providers.stream()).thenReturn(Stream.of(provider));

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.RED_FLAG, result.getSignalType());
        assertEquals(15, result.getPoints());
        assertEquals("No matching registration record found in official sources.", result.getExplanation());
    }
}

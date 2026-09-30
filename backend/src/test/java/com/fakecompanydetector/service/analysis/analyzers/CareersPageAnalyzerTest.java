package com.fakecompanydetector.service.analysis.analyzers;

import com.fakecompanydetector.entity.CareersCache;
import com.fakecompanydetector.entity.enums.CacheStatus;
import com.fakecompanydetector.entity.enums.SignalType;
import com.fakecompanydetector.repository.CareersCacheRepository;
import com.fakecompanydetector.service.analysis.AnalysisContext;
import com.fakecompanydetector.service.analysis.SignalResult;
import com.fakecompanydetector.dto.AnalysisRequest;
import com.fakecompanydetector.util.SafeHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

class CareersPageAnalyzerTest {

    @Mock
    private SafeHttpClient httpClient;

    @Mock
    private CareersCacheRepository cacheRepository;

    @InjectMocks
    private CareersPageAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(cacheRepository.findById(any())).thenReturn(Optional.empty());
    }

    @Test
    void testBlockedByRobotsTxt_ReturnsUnknown() throws Exception {
        AnalysisRequest req = AnalysisRequest.builder().jobTitle("Developer").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).resolvedDomain("example.com").build();
        
        when(httpClient.safeFetch("https://example.com/robots.txt")).thenReturn("User-agent: *\nDisallow: /");

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.UNKNOWN, result.getSignalType());
        assertEquals(0, result.getPoints());
        assertEquals("Careers search blocked by robots.txt.", result.getExplanation());
        
        // Ensure we saved to cache
        verify(cacheRepository).save(any(CareersCache.class));
    }

    @Test
    void testProviderUnavailable_NetworkError_ReturnsUnknown() throws Exception {
        AnalysisRequest req = AnalysisRequest.builder().jobTitle("Developer").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).resolvedDomain("example.com").build();
        
        when(httpClient.safeFetch("https://example.com/robots.txt")).thenReturn("");
        when(httpClient.safeFetch("https://example.com")).thenThrow(new IOException("Connection refused"));

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.UNKNOWN, result.getSignalType());
        assertEquals("Failed to verify careers page due to network or timeout issues.", result.getExplanation());
    }

    @Test
    void testJsOnlyPage_ReturnsUnknown() throws Exception {
        AnalysisRequest req = AnalysisRequest.builder().jobTitle("Developer").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).resolvedDomain("example.com").build();
        
        when(httpClient.safeFetch("https://example.com/robots.txt")).thenReturn("");
        when(httpClient.safeFetch("https://example.com")).thenReturn("<html><body><a href=\"/careers\">Careers</a></body></html>");
        when(httpClient.safeFetch("https://example.com/careers")).thenReturn("<html><head><script src='app.js'></script></head><body><div id='root'></div><script>loadApp();</script></body></html>");

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.UNKNOWN, result.getSignalType());
        assertEquals("Careers page appears to require JavaScript to render.", result.getExplanation());
    }

    @Test
    void testJobTitleMatch_ReturnsPositive() throws Exception {
        AnalysisRequest req = AnalysisRequest.builder().jobTitle("Senior Developer").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).resolvedDomain("example.com").build();
        
        when(httpClient.safeFetch("https://example.com/robots.txt")).thenReturn("");
        when(httpClient.safeFetch("https://example.com")).thenReturn("<html><body><a href=\"/careers\">Careers</a></body></html>");
        when(httpClient.safeFetch("https://example.com/careers")).thenReturn("<html><body>We are hiring a Senior Developer!</body></html>");

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.POSITIVE, result.getSignalType());
        assertEquals(-10, result.getPoints());
    }

    @Test
    void testJobTitleMissing_ReturnsWarning() throws Exception {
        AnalysisRequest req = AnalysisRequest.builder().jobTitle("Senior Developer").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).resolvedDomain("example.com").build();
        
        when(httpClient.safeFetch("https://example.com/robots.txt")).thenReturn("");
        when(httpClient.safeFetch("https://example.com")).thenReturn("<html><body><a href=\"/careers\">Careers</a></body></html>");
        when(httpClient.safeFetch("https://example.com/careers")).thenReturn("<html><body>We are hiring a Sales Manager and a Junior Dev!</body></html>");

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.WARNING, result.getSignalType());
        assertEquals(10, result.getPoints());
    }
}

package com.fakecompanydetector.service.analysis.analyzers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fakecompanydetector.dto.AnalysisRequest;
import com.fakecompanydetector.entity.enums.SignalType;
import com.fakecompanydetector.service.analysis.AnalysisContext;
import com.fakecompanydetector.service.analysis.SignalResult;
import com.fakecompanydetector.util.SafeHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebPresenceAnalyzerTest {

    private SafeHttpClient httpClient;
    private WebPresenceAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        httpClient = mock(SafeHttpClient.class);
        analyzer = new WebPresenceAnalyzer(httpClient, new ObjectMapper());

        // Set the configurable properties that would normally be injected by @Value
        ReflectionTestUtils.setField(analyzer, "maxWarningPoints", 20);
        ReflectionTestUtils.setField(analyzer, "maxPositivePoints", -15);
        ReflectionTestUtils.setField(analyzer, "minWordCount", 50);
        
        ReflectionTestUtils.setField(analyzer, "aboutUsMissing", 3);
        ReflectionTestUtils.setField(analyzer, "aboutUsPresent", -3);
        ReflectionTestUtils.setField(analyzer, "contactMissing", 5);
        ReflectionTestUtils.setField(analyzer, "contactPresent", -3);
        ReflectionTestUtils.setField(analyzer, "addressMissing", 5);
        ReflectionTestUtils.setField(analyzer, "addressPresent", -3);
        ReflectionTestUtils.setField(analyzer, "httpsMissing", 10);
        ReflectionTestUtils.setField(analyzer, "socialMissing", 5);
        ReflectionTestUtils.setField(analyzer, "socialPresent", -3);
        ReflectionTestUtils.setField(analyzer, "thinContent", 10);
    }

    @Test
    void testBlockedByRobotsTxt_ReturnsUnknown() throws Exception {
        AnalysisRequest req = AnalysisRequest.builder().companyIdentifier("https://example.com").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).resolvedDomain("example.com").build();
        
        when(httpClient.safeFetch("https://example.com/robots.txt")).thenReturn("User-agent: *\nDisallow: /");

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.UNKNOWN, result.getSignalType());
        assertEquals(0, result.getPoints());
        assertEquals("Web presence check blocked by robots.txt.", result.getExplanation());
    }

    @Test
    void testProviderUnavailable_NetworkError_ReturnsUnknown() throws Exception {
        AnalysisRequest req = AnalysisRequest.builder().companyIdentifier("https://example.com").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).resolvedDomain("example.com").build();
        
        when(httpClient.safeFetch("https://example.com/robots.txt")).thenReturn("");
        when(httpClient.safeFetch("https://example.com")).thenThrow(new IOException("Connection refused"));

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.UNKNOWN, result.getSignalType());
        assertEquals("Failed to analyze web presence due to network or timeout issues.", result.getExplanation());
    }

    @Test
    void testIdealSite_ReturnsMaxPositivePoints() throws Exception {
        AnalysisRequest req = AnalysisRequest.builder().companyIdentifier("https://example.com").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).resolvedDomain("example.com").build();
        
        String dummyText = " word".repeat(50); // to avoid thin content
        String html = "<html><body>" + dummyText + 
                      "<a href='/about'>About Us</a>" +
                      "<a href='/contact'>Contact Us</a>" +
                      "<p>123456 New York Avenue, NY</p>" +
                      "<a href='https://linkedin.com/company/example'>LinkedIn</a>" +
                      "</body></html>";
                      
        when(httpClient.safeFetch("https://example.com/robots.txt")).thenReturn("");
        when(httpClient.safeFetch("https://example.com")).thenReturn(html);

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.POSITIVE, result.getSignalType());
        // -3 (about) + -3 (contact) + -3 (address) + 0 (https) + -3 (social) + 0 (content) = -12
        assertEquals(-12, result.getPoints());
    }

    @Test
    void testBareBonesSite_ReturnsMaxWarningPointsCapped() throws Exception {
        AnalysisRequest req = AnalysisRequest.builder().companyIdentifier("http://example.com").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).resolvedDomain("example.com").build();
        
        String html = "<html><body>Placeholder</body></html>"; // thin content, no links, http
                      
        when(httpClient.safeFetch("http://example.com/robots.txt")).thenReturn("");
        when(httpClient.safeFetch("http://example.com")).thenReturn(html);

        SignalResult result = analyzer.analyze(ctx);

        assertEquals(SignalType.WARNING, result.getSignalType());
        // 3 (about) + 5 (contact) + 5 (address) + 10 (https) + 5 (social) + 10 (thin) = 38 > cap (20)
        assertEquals(20, result.getPoints());
    }

    @Test
    void testFakeSocialLinks_TreatedAsMissing() throws Exception {
        AnalysisRequest req = AnalysisRequest.builder().companyIdentifier("https://example.com").build();
        AnalysisContext ctx = AnalysisContext.builder().request(req).resolvedDomain("example.com").build();
        
        String dummyText = " word".repeat(50);
        String html = "<html><body>" + dummyText + 
                      "<a href='#'>Facebook</a>" +
                      "<a href='#'>LinkedIn</a>" +
                      "</body></html>";
                      
        when(httpClient.safeFetch("https://example.com/robots.txt")).thenReturn("");
        when(httpClient.safeFetch("https://example.com")).thenReturn(html);

        SignalResult result = analyzer.analyze(ctx);

        assertTrue(result.getExplanation().contains("No valid social media links found."));
    }
}

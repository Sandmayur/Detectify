package com.fakecompanydetector.service.analysis;

import com.fakecompanydetector.dto.AnalysisRequest;
import lombok.Builder;
import lombok.Data;
import org.jsoup.nodes.Document;

@Data
@Builder
public class AnalysisContext {
    private AnalysisRequest request;
    // Add additional contextual data if necessary (e.g., cached WHOIS info, parsed domain)
    private String resolvedDomain;
    
    // Cached fetch results to avoid duplicate network calls
    private String homepageHtml;
    private Document homepageDoc;
    private Boolean isRobotsTxtBlocked;
}

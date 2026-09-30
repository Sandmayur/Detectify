package com.fakecompanydetector.service.analysis;

import com.fakecompanydetector.dto.AnalysisRequest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalysisContext {
    private AnalysisRequest request;
    // Add additional contextual data if necessary (e.g., cached WHOIS info, parsed domain)
    private String resolvedDomain;
}

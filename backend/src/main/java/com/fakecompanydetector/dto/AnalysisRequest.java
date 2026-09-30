package com.fakecompanydetector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    @NotBlank(message = "Company name or domain is required")
    private String companyIdentifier;
    
    private String jobUrl;
    private String jobTitle;
    private String jobDescription;
    private String recruiterEmail;
}

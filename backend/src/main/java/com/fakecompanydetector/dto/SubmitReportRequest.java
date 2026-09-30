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
public class SubmitReportRequest {
    
    private String companyDomain;
    private String companyName;

    @NotBlank(message = "Description is required")
    private String description;

    private String jobUrl;
    private String recruiterEmail;
    private String recruiterPhone;
    private String evidenceUrl;
}

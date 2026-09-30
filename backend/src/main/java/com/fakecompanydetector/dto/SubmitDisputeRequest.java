package com.fakecompanydetector.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitDisputeRequest {
    
    private UUID reportId; // optional, can dispute a general score
    
    @NotBlank(message = "Company domain is required")
    private String companyDomain;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @NotBlank(message = "Reason is required")
    private String reason;
}

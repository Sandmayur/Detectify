package com.fakecompanydetector.dto;

import com.fakecompanydetector.entity.enums.DisputeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeResponse {
    private UUID id;
    private UUID reportId;
    private UUID companyId;
    private String companyDomain;
    private String contactEmail;
    private String reason;
    private DisputeStatus status;
    private LocalDateTime createdAt;
}

package com.fakecompanydetector.dto;

import com.fakecompanydetector.entity.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private ReportStatus status;
}

package com.fakecompanydetector.dto;

import com.fakecompanydetector.entity.enums.RiskCategory;
import com.fakecompanydetector.entity.enums.SignalConfidence;
import com.fakecompanydetector.entity.enums.SignalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private UUID checkId;
    private UUID companyId;
    private String companyName;
    private String rulesVersion;
    private Integer finalScore;
    private RiskCategory riskCategory;
    private LocalDateTime createdAt;
    private List<SignalDto> signals;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignalDto {
        private String signalName;
        private SignalType signalType;
        private Integer points;
        private String explanation;
        private String evidenceJson;
        private SignalConfidence confidence;
        private String dataSource;
    }
}

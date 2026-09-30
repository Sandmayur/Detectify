package com.fakecompanydetector.dto;

import com.fakecompanydetector.entity.enums.ReportStatus;
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
public class ReportResponse {
    private UUID id;
    private UUID companyId;
    private String companyName;
    private String companyDomain;
    private String description;
    private String jobUrl;
    private String recruiterEmail; // masked in frontend or here
    private String recruiterPhone; // masked in frontend or here
    private String evidenceUrl;
    private Integer netUpvotes;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private Boolean userHasVoted;
    private Boolean userVoteIsUpvote;
}

package com.fakecompanydetector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatisticsDto {
    private long totalCompanies;
    private long totalReports;
    private long totalUsers;
    private long pendingReports;
    private long pendingDisputes;
    private long flaggedReports;
}

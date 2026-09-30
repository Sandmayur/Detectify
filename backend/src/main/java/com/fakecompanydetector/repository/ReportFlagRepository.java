package com.fakecompanydetector.repository;

import com.fakecompanydetector.entity.ReportFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportFlagRepository extends JpaRepository<ReportFlag, UUID> {
    boolean existsByReportIdAndUserId(UUID reportId, UUID userId);
    long countByReportId(UUID reportId);
}

package com.fakecompanydetector.repository;

import com.fakecompanydetector.entity.Report;
import com.fakecompanydetector.entity.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    Page<Report> findByCompanyIdAndStatus(UUID companyId, ReportStatus status, Pageable pageable);
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);
    java.util.List<Report> findByCompanyDomainAndStatus(String domain, ReportStatus status);
    
    java.util.List<Report> findByUserIdAndCompanyIdAndCreatedAtAfter(UUID userId, UUID companyId, java.time.LocalDateTime date);
    
    long countByUserIdAndCreatedAtAfter(UUID userId, java.time.LocalDateTime date);
    
    long countByStatus(ReportStatus status);
}

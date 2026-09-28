package com.fakecompanydetector.repository;

import com.fakecompanydetector.entity.ReportVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportVoteRepository extends JpaRepository<ReportVote, UUID> {
    Optional<ReportVote> findByReportIdAndUserId(UUID reportId, UUID userId);
    boolean existsByReportIdAndUserId(UUID reportId, UUID userId);
}

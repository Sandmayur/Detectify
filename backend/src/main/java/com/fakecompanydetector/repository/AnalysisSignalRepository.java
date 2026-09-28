package com.fakecompanydetector.repository;

import com.fakecompanydetector.entity.AnalysisSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisSignalRepository extends JpaRepository<AnalysisSignal, UUID> {
    List<AnalysisSignal> findByCheckId(UUID checkId);
}

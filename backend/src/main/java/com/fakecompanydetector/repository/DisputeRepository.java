package com.fakecompanydetector.repository;

import com.fakecompanydetector.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fakecompanydetector.entity.enums.DisputeStatus;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);
}

package com.fakecompanydetector.repository;

import com.fakecompanydetector.entity.CompanyCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyCheckRepository extends JpaRepository<CompanyCheck, UUID> {
    Page<CompanyCheck> findByUserId(UUID userId, Pageable pageable);
}

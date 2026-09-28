package com.fakecompanydetector.repository;

import com.fakecompanydetector.entity.ScamKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScamKeywordRepository extends JpaRepository<ScamKeyword, UUID> {
    List<ScamKeyword> findByIsActiveTrue();
}

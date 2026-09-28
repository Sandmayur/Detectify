package com.fakecompanydetector.repository;

import com.fakecompanydetector.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findByDomain(String domain);
    Optional<Company> findByNormalizedName(String normalizedName);
    List<Company> findByNormalizedNameContainingIgnoreCase(String query);
}

package com.fakecompanydetector.repository;

import com.fakecompanydetector.entity.RegistrationCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationCacheRepository extends JpaRepository<RegistrationCache, String> {
}

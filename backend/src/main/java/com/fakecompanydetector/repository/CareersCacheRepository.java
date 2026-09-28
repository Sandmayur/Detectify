package com.fakecompanydetector.repository;

import com.fakecompanydetector.entity.CareersCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareersCacheRepository extends JpaRepository<CareersCache, CareersCache.CareersCacheId> {
}

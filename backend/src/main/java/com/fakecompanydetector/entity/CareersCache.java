package com.fakecompanydetector.entity;

import com.fakecompanydetector.entity.enums.CacheStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "careers_cache")
@IdClass(CareersCache.CareersCacheId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareersCache {

    @Id
    private String domain;

    @Id
    @Column(name = "job_title")
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CacheStatus status;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CareersCacheId implements Serializable {
        private String domain;
        private String jobTitle;
    }
}

package com.fakecompanydetector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "domain_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainCache {
    @Id
    private String domain;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "is_https_valid")
    private Boolean isHttpsValid;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;
}

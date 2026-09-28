package com.fakecompanydetector.entity;

import com.fakecompanydetector.entity.enums.KeywordCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "scam_keywords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScamKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KeywordCategory category;

    @Column(nullable = false)
    private Integer weight;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}

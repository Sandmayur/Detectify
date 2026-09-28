package com.fakecompanydetector.entity;

import com.fakecompanydetector.entity.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "job_url", length = 1024)
    private String jobUrl;

    @Column(name = "recruiter_email")
    private String recruiterEmail;

    @Column(name = "recruiter_phone")
    private String recruiterPhone;

    @Column(name = "evidence_url", length = 1024)
    private String evidenceUrl;

    @Column(name = "net_upvotes")
    @Builder.Default
    private Integer netUpvotes = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

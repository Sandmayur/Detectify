package com.fakecompanydetector.entity;

import com.fakecompanydetector.entity.enums.SignalConfidence;
import com.fakecompanydetector.entity.enums.SignalType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analysis_signals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisSignal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_id", nullable = false)
    private CompanyCheck check;

    @Column(name = "signal_name", nullable = false)
    private String signalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false)
    private SignalType signalType;

    @Column(nullable = false)
    private Integer points;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence_json")
    private String evidenceJson;

    @Enumerated(EnumType.STRING)
    private SignalConfidence confidence;

    @Column(name = "data_source")
    private String dataSource;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

package com.medtrack.model;

import com.medtrack.model.enums.Severity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_a_id", nullable = false)
    private Medication medicationA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_b_id", nullable = false)
    private Medication medicationB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    private String description;

    @Column(nullable = false)
    @Builder.Default
    private String source = "LOCAL";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

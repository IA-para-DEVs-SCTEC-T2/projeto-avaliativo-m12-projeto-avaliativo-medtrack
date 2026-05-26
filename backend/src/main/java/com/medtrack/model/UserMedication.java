package com.medtrack.model;

import com.medtrack.model.enums.FrequencyUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "user_medications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMedication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    private String dosage;

    @Column(name = "frequency_value")
    private Integer frequencyValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_unit")
    private FrequencyUnit frequencyUnit;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "reminder_time")
    private LocalTime reminderTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

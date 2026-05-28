package com.medtrack.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UserMedicationRequest(
        @NotNull(message = "ID do medicamento é obrigatório")
        Long medicationId,

        String dosage,

        Integer frequencyValue,

        String frequencyUnit,

        String reminderTime,

        LocalDate startDate,

        LocalDate endDate
) {}

package com.medtrack.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserMedicationRequest(
        @NotNull(message = "ID do medicamento é obrigatório")
        Long medicationId,

        String dosage,

        Integer frequencyValue,

        String frequencyUnit,

        String reminderTime
) {}

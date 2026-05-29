package com.medtrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InteractionRequest(
        @NotNull(message = "ID do medicamento A é obrigatório")
        Long medicationAId,

        @NotNull(message = "ID do medicamento B é obrigatório")
        Long medicationBId,

        @NotBlank(message = "Severidade é obrigatória")
        String severity,

        String description
) {}

package com.medtrack.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MedicationRequest(
        @NotBlank(message = "Nome é obrigatório")
        String name,

        String activeIngredient,

        String description
) {}

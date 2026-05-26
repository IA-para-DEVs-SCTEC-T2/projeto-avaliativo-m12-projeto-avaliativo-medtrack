package com.medtrack.dto.response;

import com.medtrack.model.Medication;

public record MedicationResponse(
        Long id,
        String name,
        String activeIngredient,
        String description
) {
    public static MedicationResponse from(Medication medication) {
        return new MedicationResponse(
                medication.getId(),
                medication.getName(),
                medication.getActiveIngredient(),
                medication.getDescription()
        );
    }
}

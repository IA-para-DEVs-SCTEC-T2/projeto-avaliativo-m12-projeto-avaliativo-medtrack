package com.medtrack.dto.response;

import com.medtrack.model.Interaction;

public record InteractionResponse(
        Long id,
        String medicationAName,
        String medicationBName,
        String severity,
        String description,
        String source
) {
    public static InteractionResponse from(Interaction interaction) {
        return new InteractionResponse(
                interaction.getId(),
                interaction.getMedicationA().getName(),
                interaction.getMedicationB().getName(),
                interaction.getSeverity().name(),
                interaction.getDescription(),
                interaction.getSource()
        );
    }
}

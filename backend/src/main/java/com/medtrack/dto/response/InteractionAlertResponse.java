package com.medtrack.dto.response;

import java.util.List;

public record InteractionAlertResponse(
        UserMedicationResponse medication,
        List<InteractionResponse> interactions,
        boolean hasInteractions
) {
    public static InteractionAlertResponse of(UserMedicationResponse medication, List<InteractionResponse> interactions) {
        return new InteractionAlertResponse(medication, interactions, !interactions.isEmpty());
    }
}

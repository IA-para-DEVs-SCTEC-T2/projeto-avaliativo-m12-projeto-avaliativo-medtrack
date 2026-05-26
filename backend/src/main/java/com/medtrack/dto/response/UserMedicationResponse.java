package com.medtrack.dto.response;

import com.medtrack.model.UserMedication;

import java.time.LocalTime;

public record UserMedicationResponse(
        Long id,
        Long medicationId,
        String medicationName,
        String dosage,
        Integer frequencyValue,
        String frequencyUnit,
        LocalTime reminderTime
) {
    public static UserMedicationResponse from(UserMedication um) {
        return new UserMedicationResponse(
                um.getId(),
                um.getMedication().getId(),
                um.getMedication().getName(),
                um.getDosage(),
                um.getFrequencyValue(),
                um.getFrequencyUnit() != null ? um.getFrequencyUnit().name() : null,
                um.getReminderTime()
        );
    }
}

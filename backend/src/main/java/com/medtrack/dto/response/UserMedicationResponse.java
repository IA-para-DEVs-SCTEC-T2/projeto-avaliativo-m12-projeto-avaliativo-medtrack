package com.medtrack.dto.response;

import com.medtrack.model.UserMedication;

import java.time.LocalDate;
import java.time.LocalTime;

public record UserMedicationResponse(
        Long id,
        Long medicationId,
        String medicationName,
        String dosage,
        Integer frequencyValue,
        String frequencyUnit,
        LocalTime reminderTime,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
    public static UserMedicationResponse from(UserMedication um) {
        LocalDate start = um.getStartDate() != null ? um.getStartDate().toLocalDate() : null;
        LocalDate end = um.getEndDate() != null ? um.getEndDate().toLocalDate() : null;
        boolean isActive = end == null || !LocalDate.now().isAfter(end);

        return new UserMedicationResponse(
                um.getId(),
                um.getMedication().getId(),
                um.getMedication().getName(),
                um.getDosage(),
                um.getFrequencyValue(),
                um.getFrequencyUnit() != null ? um.getFrequencyUnit().name() : null,
                um.getReminderTime(),
                start,
                end,
                isActive
        );
    }
}

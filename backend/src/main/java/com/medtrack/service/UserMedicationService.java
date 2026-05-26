package com.medtrack.service;

import com.medtrack.dto.request.UserMedicationRequest;
import com.medtrack.dto.response.InteractionAlertResponse;
import com.medtrack.dto.response.InteractionResponse;
import com.medtrack.dto.response.UserMedicationResponse;
import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.model.Interaction;
import com.medtrack.model.Medication;
import com.medtrack.model.User;
import com.medtrack.model.UserMedication;
import com.medtrack.model.enums.FrequencyUnit;
import com.medtrack.repository.MedicationRepository;
import com.medtrack.repository.UserMedicationRepository;
import com.medtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserMedicationService {

    private final UserMedicationRepository userMedicationRepository;
    private final UserRepository userRepository;
    private final MedicationRepository medicationRepository;
    private final InteractionService interactionService;

    public List<UserMedicationResponse> findByUser(String email) {
        User user = findUserByEmail(email);
        return userMedicationRepository.findByUserId(user.getId()).stream()
                .map(UserMedicationResponse::from)
                .toList();
    }

    @Transactional
    public InteractionAlertResponse addMedication(String email, UserMedicationRequest request) {
        User user = findUserByEmail(email);
        Medication medication = medicationRepository.findById(request.medicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento não encontrado: " + request.medicationId()));

        UserMedication userMedication = UserMedication.builder()
                .user(user)
                .medication(medication)
                .dosage(request.dosage())
                .frequencyValue(request.frequencyValue())
                .frequencyUnit(request.frequencyUnit() != null ? FrequencyUnit.valueOf(request.frequencyUnit()) : null)
                .reminderTime(request.reminderTime() != null ? LocalTime.parse(request.reminderTime()) : null)
                .build();

        UserMedication saved = userMedicationRepository.save(userMedication);

        // Verificar interações com medicamentos já cadastrados pelo usuário
        List<InteractionResponse> interactions = checkInteractionsForUser(user.getId(), medication.getId());

        return InteractionAlertResponse.of(UserMedicationResponse.from(saved), interactions);
    }

    @Transactional
    public void removeMedication(String email, Long userMedicationId) {
        User user = findUserByEmail(email);
        UserMedication um = userMedicationRepository.findById(userMedicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + userMedicationId));

        if (!um.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Acesso negado");
        }

        userMedicationRepository.delete(um);
    }

    private List<InteractionResponse> checkInteractionsForUser(UUID userId, Long newMedicationId) {
        // Buscar todos os medicamentos do usuário
        List<Long> userMedicationIds = userMedicationRepository.findByUserId(userId).stream()
                .map(um -> um.getMedication().getId())
                .filter(id -> !id.equals(newMedicationId))
                .toList();

        // Buscar interações do novo medicamento
        List<Interaction> allInteractions = interactionService.findByMedicationId(newMedicationId);

        // Filtrar apenas interações com medicamentos que o usuário já usa
        return allInteractions.stream()
                .filter(interaction -> {
                    Long otherMedId = interaction.getMedicationA().getId().equals(newMedicationId)
                            ? interaction.getMedicationB().getId()
                            : interaction.getMedicationA().getId();
                    return userMedicationIds.contains(otherMedId);
                })
                .map(InteractionResponse::from)
                .toList();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}

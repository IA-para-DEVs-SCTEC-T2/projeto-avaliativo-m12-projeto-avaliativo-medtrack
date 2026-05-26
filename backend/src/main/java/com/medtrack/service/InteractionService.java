package com.medtrack.service;

import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.model.Interaction;
import com.medtrack.model.Medication;
import com.medtrack.repository.InteractionRepository;
import com.medtrack.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final MedicationRepository medicationRepository;

    public List<Interaction> findAll() {
        return interactionRepository.findAll();
    }

    public List<Interaction> findByMedicationId(Long medicationId) {
        return interactionRepository.findByMedicationId(medicationId);
    }

    @Transactional
    public Interaction create(Long medicationAId, Long medicationBId, Interaction interaction) {
        Medication medA = medicationRepository.findById(medicationAId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento A não encontrado: " + medicationAId));
        Medication medB = medicationRepository.findById(medicationBId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento B não encontrado: " + medicationBId));

        interaction.setMedicationA(medA);
        interaction.setMedicationB(medB);
        return interactionRepository.save(interaction);
    }

    @Transactional
    public void delete(Long id) {
        if (!interactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Interação não encontrada: " + id);
        }
        interactionRepository.deleteById(id);
    }
}

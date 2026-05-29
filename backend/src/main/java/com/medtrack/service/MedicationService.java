package com.medtrack.service;

import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.model.Medication;
import com.medtrack.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public List<Medication> findAll() {
        return medicationRepository.findAll();
    }

    public Medication findById(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento não encontrado: " + id));
    }

    @Transactional
    public Medication create(Medication medication) {
        return medicationRepository.save(medication);
    }

    @Transactional
    public Medication update(Long id, Medication updated) {
        Medication existing = findById(id);
        existing.setName(updated.getName());
        existing.setActiveIngredient(updated.getActiveIngredient());
        existing.setDescription(updated.getDescription());
        return medicationRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!medicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medicamento não encontrado: " + id);
        }
        medicationRepository.deleteById(id);
    }
}

package com.medtrack.service;

import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.model.Interaction;
import com.medtrack.model.Medication;
import com.medtrack.repository.InteractionRepository;
import com.medtrack.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service refatorado seguindo SOLID:
 * - SRP: responsável apenas por orquestrar verificações de interação
 * - OCP: novas fontes de interação (FDA, etc.) são adicionadas via InteractionChecker
 * - DIP: depende da abstração InteractionChecker, não de implementações concretas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final MedicationRepository medicationRepository;
    private final List<InteractionChecker> interactionCheckers;

    /**
     * Busca interações usando todas as fontes disponíveis (local + FDA + futuras).
     * Cada InteractionChecker é consultado e os resultados são agregados.
     */
    public List<Interaction> findByMedicationId(Long medicationId) {
        List<Interaction> allInteractions = new ArrayList<>();

        for (InteractionChecker checker : interactionCheckers) {
            try {
                List<Interaction> found = checker.findInteractions(medicationId);
                allInteractions.addAll(found);
                log.debug("Fonte '{}' retornou {} interações para medicamento {}",
                        checker.getSource(), found.size(), medicationId);
            } catch (Exception e) {
                log.warn("Erro ao consultar fonte '{}': {}", checker.getSource(), e.getMessage());
                // Fallback: continua com as outras fontes
            }
        }

        return allInteractions;
    }

    public List<Interaction> findAll() {
        return interactionRepository.findAll();
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

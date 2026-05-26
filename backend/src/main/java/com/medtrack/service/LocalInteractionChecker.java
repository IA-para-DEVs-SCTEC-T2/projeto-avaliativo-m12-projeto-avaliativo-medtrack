package com.medtrack.service;

import com.medtrack.model.Interaction;
import com.medtrack.repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementação local — busca interações na base de dados PostgreSQL.
 * Sempre disponível, sem dependência de serviços externos.
 */
@Component
@RequiredArgsConstructor
public class LocalInteractionChecker implements InteractionChecker {

    private final InteractionRepository interactionRepository;

    @Override
    public List<Interaction> findInteractions(Long medicationId) {
        return interactionRepository.findByMedicationId(medicationId);
    }

    @Override
    public String getSource() {
        return "LOCAL";
    }
}

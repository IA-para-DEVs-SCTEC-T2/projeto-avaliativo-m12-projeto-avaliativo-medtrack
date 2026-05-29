package com.medtrack.service;

import com.medtrack.model.Interaction;

import java.util.List;

/**
 * Interface para verificação de interações medicamentosas.
 * Permite múltiplas implementações (local, FDA, etc.) seguindo OCP e DIP.
 */
public interface InteractionChecker {

    /**
     * Busca interações para um medicamento específico.
     *
     * @param medicationId ID do medicamento
     * @return lista de interações encontradas
     */
    List<Interaction> findInteractions(Long medicationId);

    /**
     * Identifica a fonte desta implementação.
     */
    String getSource();
}

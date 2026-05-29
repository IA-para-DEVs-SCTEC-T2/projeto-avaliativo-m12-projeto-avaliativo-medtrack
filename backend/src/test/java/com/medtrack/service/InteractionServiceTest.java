package com.medtrack.service;

import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.model.Interaction;
import com.medtrack.model.Medication;
import com.medtrack.model.enums.Severity;
import com.medtrack.repository.InteractionRepository;
import com.medtrack.repository.MedicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InteractionService — Testes Unitários")
class InteractionServiceTest {

    @Mock
    private InteractionRepository interactionRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private InteractionChecker localChecker;

    private InteractionService interactionService;

    @BeforeEach
    void setUp() {
        interactionService = new InteractionService(
                interactionRepository,
                medicationRepository,
                List.of(localChecker)
        );
    }

    @Test
    @DisplayName("findByMedicationId deve agregar resultados de todos os checkers")
    void findByMedicationId_shouldAggregateResults() {
        Interaction interaction = buildInteraction(1L, "Varfarina", "Aspirina", Severity.SEVERE);
        when(localChecker.findInteractions(1L)).thenReturn(List.of(interaction));

        List<Interaction> result = interactionService.findByMedicationId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeverity()).isEqualTo(Severity.SEVERE);
        verify(localChecker).findInteractions(1L);
    }

    @Test
    @DisplayName("findByMedicationId deve continuar se um checker falhar")
    void findByMedicationId_shouldContinueOnCheckerFailure() {
        when(localChecker.findInteractions(1L)).thenThrow(new RuntimeException("DB error"));

        List<Interaction> result = interactionService.findByMedicationId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("create deve associar medicamentos e salvar")
    void create_shouldAssociateMedicationsAndSave() {
        Medication medA = Medication.builder().id(1L).name("Varfarina").build();
        Medication medB = Medication.builder().id(2L).name("Aspirina").build();
        Interaction interaction = Interaction.builder().severity(Severity.SEVERE).description("Risco").build();

        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medA));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medB));
        when(interactionRepository.save(any())).thenReturn(interaction);

        Interaction result = interactionService.create(1L, 2L, interaction);

        assertThat(result.getMedicationA()).isEqualTo(medA);
        assertThat(result.getMedicationB()).isEqualTo(medB);
        verify(interactionRepository).save(interaction);
    }

    @Test
    @DisplayName("create deve lançar exceção se medicamento A não existir")
    void create_shouldThrowIfMedicationANotFound() {
        when(medicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactionService.create(99L, 2L, new Interaction()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Medicamento A");
    }

    @Test
    @DisplayName("delete deve lançar exceção se interação não existir")
    void delete_shouldThrowIfNotFound() {
        when(interactionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> interactionService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete deve remover interação existente")
    void delete_shouldRemoveExisting() {
        when(interactionRepository.existsById(1L)).thenReturn(true);

        interactionService.delete(1L);

        verify(interactionRepository).deleteById(1L);
    }

    private Interaction buildInteraction(Long id, String medA, String medB, Severity severity) {
        return Interaction.builder()
                .id(id)
                .medicationA(Medication.builder().id(1L).name(medA).build())
                .medicationB(Medication.builder().id(2L).name(medB).build())
                .severity(severity)
                .description("Interação de teste")
                .source("LOCAL")
                .build();
    }
}

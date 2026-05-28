package com.medtrack.service;

import com.medtrack.integration.fda.FdaApiClient;
import com.medtrack.integration.fda.dto.FdaSearchResponse;
import com.medtrack.model.Interaction;
import com.medtrack.model.Medication;
import com.medtrack.model.enums.Severity;
import com.medtrack.repository.MedicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FdaInteractionChecker — Testes Unitários")
class FdaInteractionCheckerTest {

    @Mock
    private FdaApiClient fdaApiClient;

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private FdaInteractionChecker checker;

    private Medication varfarina;
    private Medication aspirina;
    private Medication paracetamol;

    @BeforeEach
    void setUp() {
        varfarina = Medication.builder().id(1L).name("Varfarina").activeIngredient("Warfarin").build();
        aspirina = Medication.builder().id(2L).name("Aspirina").activeIngredient("Aspirin").build();
        paracetamol = Medication.builder().id(3L).name("Paracetamol").activeIngredient("Acetaminophen").build();
    }

    @Test
    @DisplayName("getSource deve retornar FDA")
    void getSource_shouldReturnFda() {
        assertThat(checker.getSource()).isEqualTo("FDA");
    }

    @Test
    @DisplayName("Deve retornar vazio quando o medicamento não existe")
    void shouldReturnEmptyWhenMedicationNotFound() {
        when(medicationRepository.findById(99L)).thenReturn(Optional.empty());

        List<Interaction> result = checker.findInteractions(99L);

        assertThat(result).isEmpty();
        verify(fdaApiClient, never()).searchDrugLabel(anyString());
    }

    @Test
    @DisplayName("Deve retornar vazio quando FdaApiClient retorna Optional.empty (flag desligada / erro)")
    void shouldReturnEmptyWhenFdaClientReturnsEmpty() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(varfarina));
        when(fdaApiClient.searchDrugLabel("Warfarin")).thenReturn(Optional.empty());

        List<Interaction> result = checker.findInteractions(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve mapear interação quando FDA descreve match com outro medicamento do catálogo")
    void shouldMapInteractionWhenFdaMentionsKnownMedication() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(varfarina));
        when(fdaApiClient.searchDrugLabel("Warfarin"))
                .thenReturn(Optional.of(buildFdaResponse(List.of(
                        "Concomitant use of Aspirina increases bleeding risk."
                ))));
        when(medicationRepository.findAll()).thenReturn(List.of(varfarina, aspirina, paracetamol));

        List<Interaction> result = checker.findInteractions(1L);

        assertThat(result).hasSize(1);
        Interaction interaction = result.get(0);
        assertThat(interaction.getMedicationA()).isEqualTo(varfarina);
        assertThat(interaction.getMedicationB()).isEqualTo(aspirina);
        assertThat(interaction.getSource()).isEqualTo("FDA");
        assertThat(interaction.getSeverity()).isEqualTo(Severity.MODERATE);
        assertThat(interaction.getDescription()).contains("Aspirina");
    }

    @Test
    @DisplayName("Deve fazer matching também pelo princípio ativo")
    void shouldMatchByActiveIngredient() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(varfarina));
        when(fdaApiClient.searchDrugLabel("Warfarin"))
                .thenReturn(Optional.of(buildFdaResponse(List.of(
                        "Avoid combination with aspirin (acetylsalicylic acid)."
                ))));
        when(medicationRepository.findAll()).thenReturn(List.of(varfarina, aspirina));

        List<Interaction> result = checker.findInteractions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMedicationB()).isEqualTo(aspirina);
    }

    @Test
    @DisplayName("Não deve incluir o próprio medicamento como par")
    void shouldNotMatchSelf() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(varfarina));
        when(fdaApiClient.searchDrugLabel("Warfarin"))
                .thenReturn(Optional.of(buildFdaResponse(List.of("Varfarina interactions described here."))));
        when(medicationRepository.findAll()).thenReturn(List.of(varfarina));

        List<Interaction> result = checker.findInteractions(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar vazio quando FDA não cita nenhum medicamento conhecido do catálogo")
    void shouldReturnEmptyWhenNoMatchInCatalog() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(varfarina));
        when(fdaApiClient.searchDrugLabel("Warfarin"))
                .thenReturn(Optional.of(buildFdaResponse(List.of("Caution with NSAIDs and SSRIs."))));
        when(medicationRepository.findAll()).thenReturn(List.of(varfarina, paracetamol));

        List<Interaction> result = checker.findInteractions(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve truncar descrições muito longas (>500 chars)")
    void shouldTruncateLongDescriptions() {
        String hugeDescription = "Aspirina " + "x".repeat(800);
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(varfarina));
        when(fdaApiClient.searchDrugLabel("Warfarin"))
                .thenReturn(Optional.of(buildFdaResponse(List.of(hugeDescription))));
        when(medicationRepository.findAll()).thenReturn(List.of(varfarina, aspirina));

        List<Interaction> result = checker.findInteractions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).hasSize(500);
        assertThat(result.get(0).getDescription()).endsWith("...");
    }

    private FdaSearchResponse buildFdaResponse(List<String> drugInteractions) {
        FdaSearchResponse.FdaDrugResult result = new FdaSearchResponse.FdaDrugResult(
                null,
                drugInteractions,
                List.of()
        );
        return new FdaSearchResponse(null, List.of(result));
    }
}

package com.medtrack.service;

import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.model.Medication;
import com.medtrack.repository.MedicationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicationService — Testes Unitários")
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private MedicationService medicationService;

    @Test
    @DisplayName("findAll deve retornar lista de medicamentos")
    void findAll_shouldReturnList() {
        Medication med = Medication.builder().id(1L).name("Aspirina").build();
        when(medicationRepository.findAll()).thenReturn(List.of(med));

        List<Medication> result = medicationService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Aspirina");
    }

    @Test
    @DisplayName("findById deve retornar medicamento existente")
    void findById_shouldReturnMedication() {
        Medication med = Medication.builder().id(1L).name("Varfarina").build();
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(med));

        Medication result = medicationService.findById(1L);

        assertThat(result.getName()).isEqualTo("Varfarina");
    }

    @Test
    @DisplayName("findById deve lançar exceção para ID inexistente")
    void findById_shouldThrowWhenNotFound() {
        when(medicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicationService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create deve salvar e retornar medicamento")
    void create_shouldSaveAndReturn() {
        Medication med = Medication.builder().name("Ibuprofeno").activeIngredient("Ibuprofen").build();
        when(medicationRepository.save(any())).thenReturn(med);

        Medication result = medicationService.create(med);

        assertThat(result.getName()).isEqualTo("Ibuprofeno");
        verify(medicationRepository).save(med);
    }

    @Test
    @DisplayName("update deve atualizar campos e salvar")
    void update_shouldModifyAndSave() {
        Medication existing = Medication.builder().id(1L).name("Aspirina").activeIngredient("ASA").description("old").build();
        Medication updated = Medication.builder().name("Aspirina 500").activeIngredient("Ácido Acetilsalicílico").description("new").build();

        when(medicationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(medicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Medication result = medicationService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("Aspirina 500");
        assertThat(result.getActiveIngredient()).isEqualTo("Ácido Acetilsalicílico");
        assertThat(result.getDescription()).isEqualTo("new");
    }

    @Test
    @DisplayName("delete deve remover medicamento existente")
    void delete_shouldRemoveExisting() {
        when(medicationRepository.existsById(1L)).thenReturn(true);

        medicationService.delete(1L);

        verify(medicationRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete deve lançar exceção para ID inexistente")
    void delete_shouldThrowWhenNotFound() {
        when(medicationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> medicationService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

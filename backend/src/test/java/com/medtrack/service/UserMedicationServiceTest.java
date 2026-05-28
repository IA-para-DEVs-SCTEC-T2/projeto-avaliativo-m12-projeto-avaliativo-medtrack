package com.medtrack.service;

import com.medtrack.dto.request.UserMedicationRequest;
import com.medtrack.dto.response.InteractionAlertResponse;
import com.medtrack.dto.response.UserMedicationResponse;
import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.model.Medication;
import com.medtrack.model.User;
import com.medtrack.model.UserMedication;
import com.medtrack.model.enums.FrequencyUnit;
import com.medtrack.model.enums.Role;
import com.medtrack.repository.MedicationRepository;
import com.medtrack.repository.UserMedicationRepository;
import com.medtrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserMedicationService — Testes Unitários")
class UserMedicationServiceTest {

    @Mock
    private UserMedicationRepository userMedicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private InteractionService interactionService;

    @InjectMocks
    private UserMedicationService service;

    private User user;
    private Medication medication;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@medtrack.local")
                .role(Role.USER)
                .active(true)
                .build();

        medication = Medication.builder()
                .id(1L)
                .name("Aspirina")
                .activeIngredient("Ácido Acetilsalicílico")
                .build();
    }

    @Test
    @DisplayName("findByUser deve retornar medicamentos do usuário")
    void findByUser_shouldReturnUserMedications() {
        UserMedication um = UserMedication.builder()
                .id(1L).user(user).medication(medication)
                .dosage("500mg").frequencyValue(2).frequencyUnit(FrequencyUnit.DAILY)
                .reminderTime(LocalTime.of(8, 0))
                .build();

        when(userRepository.findByEmail("test@medtrack.local")).thenReturn(Optional.of(user));
        when(userMedicationRepository.findByUserId(user.getId())).thenReturn(List.of(um));

        List<UserMedicationResponse> result = service.findByUser("test@medtrack.local");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).medicationName()).isEqualTo("Aspirina");
        assertThat(result.get(0).dosage()).isEqualTo("500mg");
    }

    @Test
    @DisplayName("findByUser deve lançar exceção para usuário inexistente")
    void findByUser_shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("nope@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByUser("nope@x.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("addMedication deve salvar e retornar alerta de interação")
    void addMedication_shouldSaveAndReturnAlert() {
        UserMedicationRequest request = new UserMedicationRequest(1L, "500mg", 2, "DAILY", "08:00", null, null);

        UserMedication saved = UserMedication.builder()
                .id(10L).user(user).medication(medication)
                .dosage("500mg").frequencyValue(2).frequencyUnit(FrequencyUnit.DAILY)
                .reminderTime(LocalTime.of(8, 0))
                .build();

        when(userRepository.findByEmail("test@medtrack.local")).thenReturn(Optional.of(user));
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
        when(userMedicationRepository.save(any())).thenReturn(saved);
        when(userMedicationRepository.findByUserId(user.getId())).thenReturn(List.of());
        when(interactionService.findByMedicationId(1L)).thenReturn(List.of());

        InteractionAlertResponse result = service.addMedication("test@medtrack.local", request);

        assertThat(result.hasInteractions()).isFalse();
        assertThat(result.medication().medicationName()).isEqualTo("Aspirina");
        verify(userMedicationRepository).save(any());
    }

    @Test
    @DisplayName("addMedication deve lançar exceção para medicamento inexistente")
    void addMedication_shouldThrowWhenMedicationNotFound() {
        UserMedicationRequest request = new UserMedicationRequest(99L, null, null, null, null, null, null);

        when(userRepository.findByEmail("test@medtrack.local")).thenReturn(Optional.of(user));
        when(medicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addMedication("test@medtrack.local", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("removeMedication deve remover registro do próprio usuário")
    void removeMedication_shouldDeleteOwnRecord() {
        UserMedication um = UserMedication.builder().id(1L).user(user).medication(medication).build();

        when(userRepository.findByEmail("test@medtrack.local")).thenReturn(Optional.of(user));
        when(userMedicationRepository.findById(1L)).thenReturn(Optional.of(um));

        service.removeMedication("test@medtrack.local", 1L);

        verify(userMedicationRepository).delete(um);
    }

    @Test
    @DisplayName("removeMedication deve negar acesso a registro de outro usuário")
    void removeMedication_shouldDenyAccessToOtherUser() {
        User otherUser = User.builder().id(UUID.randomUUID()).email("other@x.com").build();
        UserMedication um = UserMedication.builder().id(1L).user(otherUser).medication(medication).build();

        when(userRepository.findByEmail("test@medtrack.local")).thenReturn(Optional.of(user));
        when(userMedicationRepository.findById(1L)).thenReturn(Optional.of(um));

        assertThatThrownBy(() -> service.removeMedication("test@medtrack.local", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Acesso negado");
    }

    @Test
    @DisplayName("deactivateMedication deve setar endDate como ontem")
    void deactivateMedication_shouldSetEndDateToYesterday() {
        UserMedication um = UserMedication.builder().id(1L).user(user).medication(medication).build();

        when(userRepository.findByEmail("test@medtrack.local")).thenReturn(Optional.of(user));
        when(userMedicationRepository.findById(1L)).thenReturn(Optional.of(um));
        when(userMedicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserMedicationResponse result = service.deactivateMedication("test@medtrack.local", 1L);

        assertThat(result.active()).isFalse();
        assertThat(result.endDate()).isNotNull();
    }
}

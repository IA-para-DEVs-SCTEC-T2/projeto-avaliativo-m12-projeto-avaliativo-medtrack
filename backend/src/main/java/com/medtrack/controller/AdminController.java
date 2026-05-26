package com.medtrack.controller;

import com.medtrack.dto.request.InteractionRequest;
import com.medtrack.dto.request.MedicationRequest;
import com.medtrack.dto.response.InteractionResponse;
import com.medtrack.dto.response.MedicationResponse;
import com.medtrack.dto.response.UserResponse;
import com.medtrack.model.Interaction;
import com.medtrack.model.Medication;
import com.medtrack.model.enums.Severity;
import com.medtrack.service.AdminService;
import com.medtrack.service.InteractionService;
import com.medtrack.service.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Painel administrativo — requer role ADMIN")
public class AdminController {

    private final MedicationService medicationService;
    private final InteractionService interactionService;
    private final AdminService adminService;

    // === Medicamentos ===

    @GetMapping("/medications")
    @Operation(summary = "Listar todos os medicamentos")
    public ResponseEntity<List<MedicationResponse>> listMedications() {
        List<MedicationResponse> medications = medicationService.findAll().stream()
                .map(MedicationResponse::from)
                .toList();
        return ResponseEntity.ok(medications);
    }

    @PostMapping("/medications")
    @Operation(summary = "Cadastrar novo medicamento")
    public ResponseEntity<MedicationResponse> createMedication(@Valid @RequestBody MedicationRequest request) {
        Medication medication = Medication.builder()
                .name(request.name())
                .activeIngredient(request.activeIngredient())
                .description(request.description())
                .build();
        Medication saved = medicationService.create(medication);
        return ResponseEntity.status(HttpStatus.CREATED).body(MedicationResponse.from(saved));
    }

    @PutMapping("/medications/{id}")
    @Operation(summary = "Atualizar medicamento")
    public ResponseEntity<MedicationResponse> updateMedication(@PathVariable Long id,
                                                                @Valid @RequestBody MedicationRequest request) {
        Medication medication = Medication.builder()
                .name(request.name())
                .activeIngredient(request.activeIngredient())
                .description(request.description())
                .build();
        Medication updated = medicationService.update(id, medication);
        return ResponseEntity.ok(MedicationResponse.from(updated));
    }

    @DeleteMapping("/medications/{id}")
    @Operation(summary = "Remover medicamento")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        medicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // === Interações ===

    @GetMapping("/interactions")
    @Operation(summary = "Listar todas as interações")
    public ResponseEntity<List<InteractionResponse>> listInteractions() {
        List<InteractionResponse> interactions = interactionService.findAll().stream()
                .map(InteractionResponse::from)
                .toList();
        return ResponseEntity.ok(interactions);
    }

    @PostMapping("/interactions")
    @Operation(summary = "Cadastrar nova interação")
    public ResponseEntity<InteractionResponse> createInteraction(@Valid @RequestBody InteractionRequest request) {
        Interaction interaction = Interaction.builder()
                .severity(Severity.valueOf(request.severity()))
                .description(request.description())
                .source("LOCAL")
                .build();
        Interaction saved = interactionService.create(request.medicationAId(), request.medicationBId(), interaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(InteractionResponse.from(saved));
    }

    @DeleteMapping("/interactions/{id}")
    @Operation(summary = "Remover interação")
    public ResponseEntity<Void> deleteInteraction(@PathVariable Long id) {
        interactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // === Usuários ===

    @GetMapping("/users")
    @Operation(summary = "Listar todos os usuários")
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = adminService.findAllUsers().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{id}/toggle-active")
    @Operation(summary = "Ativar/desativar usuário")
    public ResponseEntity<UserResponse> toggleUserActive(@PathVariable UUID id) {
        return ResponseEntity.ok(UserResponse.from(adminService.toggleUserActive(id)));
    }
}

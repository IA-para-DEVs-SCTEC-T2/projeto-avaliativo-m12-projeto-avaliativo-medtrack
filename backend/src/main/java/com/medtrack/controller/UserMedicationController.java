package com.medtrack.controller;

import com.medtrack.dto.request.UserMedicationRequest;
import com.medtrack.dto.response.InteractionAlertResponse;
import com.medtrack.dto.response.UserMedicationResponse;
import com.medtrack.service.UserMedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-medications")
@RequiredArgsConstructor
@Tag(name = "Medicamentos do Usuário", description = "Gerenciar medicamentos pessoais e verificar interações")
public class UserMedicationController {

    private final UserMedicationService userMedicationService;

    @GetMapping
    @Operation(summary = "Listar meus medicamentos")
    public ResponseEntity<List<UserMedicationResponse>> getMyMedications(Authentication auth) {
        return ResponseEntity.ok(userMedicationService.findByUser(auth.getName()));
    }

    @PostMapping
    @Operation(summary = "Adicionar medicamento", description = "Cadastra medicamento e retorna alertas de interação")
    public ResponseEntity<InteractionAlertResponse> addMedication(
            Authentication auth,
            @Valid @RequestBody UserMedicationRequest request) {
        InteractionAlertResponse response = userMedicationService.addMedication(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover medicamento da minha lista")
    public ResponseEntity<Void> removeMedication(Authentication auth, @PathVariable Long id) {
        userMedicationService.removeMedication(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}

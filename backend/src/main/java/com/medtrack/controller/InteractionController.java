package com.medtrack.controller;

import com.medtrack.dto.response.InteractionResponse;
import com.medtrack.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interactions")
@RequiredArgsConstructor
@Tag(name = "Interações", description = "Consultar interações medicamentosas")
public class InteractionController {

    private final InteractionService interactionService;

    @GetMapping("/medication/{medicationId}")
    @Operation(summary = "Buscar interações de um medicamento")
    public ResponseEntity<List<InteractionResponse>> getByMedication(@PathVariable Long medicationId) {
        List<InteractionResponse> interactions = interactionService.findByMedicationId(medicationId).stream()
                .map(InteractionResponse::from)
                .toList();
        return ResponseEntity.ok(interactions);
    }
}

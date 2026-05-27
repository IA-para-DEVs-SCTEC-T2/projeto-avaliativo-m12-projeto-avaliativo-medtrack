package com.medtrack.controller;

import com.medtrack.dto.response.MedicationResponse;
import com.medtrack.service.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/medications")
@RequiredArgsConstructor
@Tag(name = "Medicamentos", description = "Consulta pública de medicamentos cadastrados")
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping
    @Operation(summary = "Listar todos os medicamentos disponíveis",
            description = "Retorna a lista completa de medicamentos cadastrados no sistema. Requer autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido")
    })
    public ResponseEntity<List<MedicationResponse>> listAll() {
        List<MedicationResponse> medications = medicationService.findAll().stream()
                .map(MedicationResponse::from)
                .toList();
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar medicamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Medicamento não encontrado")
    })
    public ResponseEntity<MedicationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(MedicationResponse.from(medicationService.findById(id)));
    }
}

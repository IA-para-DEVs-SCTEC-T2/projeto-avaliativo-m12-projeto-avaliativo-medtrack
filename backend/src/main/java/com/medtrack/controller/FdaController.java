package com.medtrack.controller;

import com.medtrack.integration.fda.FdaApiClient;
import com.medtrack.model.SystemConfig;
import com.medtrack.repository.SystemConfigRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/admin/fda")
@RequiredArgsConstructor
@Tag(name = "FDA Integration", description = "Gerenciar integração com openFDA — requer ADMIN")
public class FdaController {

    private final FdaApiClient fdaApiClient;
    private final SystemConfigRepository systemConfigRepository;

    @GetMapping("/test-connection")
    @Operation(summary = "Testar conexão com a API FDA")
    public ResponseEntity<Map<String, Object>> testConnection() {
        boolean success = fdaApiClient.testConnection();
        return ResponseEntity.ok(Map.of(
                "connected", success,
                "message", success ? "Conexão com FDA OK" : "Falha na conexão com FDA",
                "timestamp", LocalDateTime.now()
        ));
    }

    @PostMapping("/toggle")
    @Operation(summary = "Ativar/desativar integração FDA")
    public ResponseEntity<Map<String, Object>> toggleFda(@RequestBody Map<String, Boolean> body) {
        boolean enabled = body.getOrDefault("enabled", false);

        SystemConfig config = systemConfigRepository.findByKey("FDA_INTEGRATION_ENABLED")
                .orElse(SystemConfig.builder().key("FDA_INTEGRATION_ENABLED").build());

        config.setValue(String.valueOf(enabled));
        config.setUpdatedAt(LocalDateTime.now());
        systemConfigRepository.save(config);

        return ResponseEntity.ok(Map.of(
                "fdaEnabled", enabled,
                "message", enabled ? "Integração FDA ativada" : "Integração FDA desativada"
        ));
    }

    @GetMapping("/status")
    @Operation(summary = "Verificar status da integração FDA")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean enabled = systemConfigRepository.findByKey("FDA_INTEGRATION_ENABLED")
                .map(c -> "true".equalsIgnoreCase(c.getValue()))
                .orElse(false);

        return ResponseEntity.ok(Map.of("fdaEnabled", enabled));
    }
}

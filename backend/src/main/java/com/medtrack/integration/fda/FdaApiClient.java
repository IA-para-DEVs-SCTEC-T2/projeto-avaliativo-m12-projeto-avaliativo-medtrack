package com.medtrack.integration.fda;

import com.medtrack.integration.fda.dto.FdaSearchResponse;
import com.medtrack.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FdaApiClient {

    private final SystemConfigRepository systemConfigRepository;

    @Value("${fda.api.base-url:https://api.fda.gov}")
    private String baseUrl;

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    /**
     * Busca informações de um medicamento na API openFDA.
     * Retorna Optional.empty() se a integração estiver desabilitada,
     * se houver timeout, ou se ocorrer qualquer erro.
     */
    public Optional<FdaSearchResponse> searchDrugLabel(String drugName) {
        if (!isFdaEnabled()) {
            log.debug("Integração FDA desabilitada");
            return Optional.empty();
        }

        try {
            RestClient client = RestClient.builder()
                    .baseUrl(baseUrl)
                    .build();

            String json = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/drug/label.json")
                            .queryParam("search", "openfda.substance_name:\"" + drugName + "\"+openfda.generic_name:\"" + drugName + "\"")
                            .queryParam("limit", "1")
                            .build())
                    .retrieve()
                    .body(String.class);

            if (json == null || json.isBlank()) {
                return Optional.empty();
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            FdaSearchResponse response = mapper.readValue(json, FdaSearchResponse.class);

            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.warn("Erro ao consultar FDA para '{}': {} — usando apenas base local", drugName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Testa a conexão com a API da FDA.
     * Usado pelo admin para verificar se a integração está funcionando.
     */
    public boolean testConnection() {
        try {
            RestClient client = RestClient.builder()
                    .baseUrl(baseUrl)
                    .build();

            client.get()
                    .uri("/drug/label.json?limit=1")
                    .retrieve()
                    .body(String.class);

            return true;
        } catch (Exception e) {
            log.error("Teste de conexão FDA falhou: {}", e.getMessage());
            return false;
        }
    }

    private boolean isFdaEnabled() {
        return systemConfigRepository.findByKey("FDA_INTEGRATION_ENABLED")
                .map(config -> "true".equalsIgnoreCase(config.getValue()))
                .orElse(false);
    }
}

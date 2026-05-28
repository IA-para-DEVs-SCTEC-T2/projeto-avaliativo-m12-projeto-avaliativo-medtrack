package com.medtrack.service;

import com.medtrack.integration.fda.FdaApiClient;
import com.medtrack.integration.fda.dto.FdaSearchResponse;
import com.medtrack.model.Interaction;
import com.medtrack.model.Medication;
import com.medtrack.model.enums.Severity;
import com.medtrack.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Implementação openFDA — busca interações na Drug Label API quando a feature flag
 * está ativada. Retorna interações com {@code source="FDA"} sem persisti-las.
 *
 * <p>Limitação conhecida: a Drug Label API não classifica severidade. Adotamos
 * {@link Severity#MODERATE} como default e registramos a fonte no campo {@code source}
 * para o usuário poder distinguir das interações curadas localmente.</p>
 *
 * <p>Matching: para cada string em {@code drug_interactions} retornada pela FDA,
 * verificamos quais medicamentos do catálogo local têm o nome (ou princípio ativo)
 * mencionado na descrição. É um matching por substring case-insensitive — adequado
 * ao MVP, embora possa ter falsos positivos com nomes muito curtos ou genéricos.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FdaInteractionChecker implements InteractionChecker {

    private final FdaApiClient fdaApiClient;
    private final MedicationRepository medicationRepository;

    @Override
    public List<Interaction> findInteractions(Long medicationId) {
        Optional<Medication> source = medicationRepository.findById(medicationId);
        if (source.isEmpty()) {
            return List.of();
        }
        Medication medication = source.get();

        Optional<FdaSearchResponse> fdaResponse = fdaApiClient.searchDrugLabel(medication.getName());
        if (fdaResponse.isEmpty()) {
            return List.of();
        }

        List<String> drugInteractions = extractDrugInteractions(fdaResponse.get());
        if (drugInteractions.isEmpty()) {
            return List.of();
        }

        List<Medication> catalog = medicationRepository.findAll();
        List<Interaction> matches = new ArrayList<>();

        for (String description : drugInteractions) {
            String haystack = description.toLowerCase(Locale.ROOT);
            for (Medication candidate : catalog) {
                if (candidate.getId().equals(medicationId)) {
                    continue;
                }
                if (mentions(haystack, candidate)) {
                    matches.add(buildFdaInteraction(medication, candidate, description));
                }
            }
        }

        log.debug("FDA encontrou {} interações para '{}'", matches.size(), medication.getName());
        return matches;
    }

    @Override
    public String getSource() {
        return "FDA";
    }

    private boolean mentions(String haystackLower, Medication candidate) {
        if (candidate.getName() != null && haystackLower.contains(candidate.getName().toLowerCase(Locale.ROOT))) {
            return true;
        }
        return candidate.getActiveIngredient() != null
                && !candidate.getActiveIngredient().isBlank()
                && haystackLower.contains(candidate.getActiveIngredient().toLowerCase(Locale.ROOT));
    }

    private List<String> extractDrugInteractions(FdaSearchResponse response) {
        if (response.results() == null || response.results().isEmpty()) {
            return List.of();
        }
        return response.results().stream()
                .map(FdaSearchResponse.FdaDrugResult::drug_interactions)
                .filter(list -> list != null && !list.isEmpty())
                .flatMap(List::stream)
                .toList();
    }

    private Interaction buildFdaInteraction(Medication source, Medication target, String description) {
        return Interaction.builder()
                .medicationA(source)
                .medicationB(target)
                .severity(Severity.MODERATE)
                .description(truncate(description, 500))
                .source("FDA")
                .build();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}

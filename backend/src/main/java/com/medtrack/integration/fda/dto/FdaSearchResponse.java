package com.medtrack.integration.fda.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FdaSearchResponse(
        Meta meta,
        List<FdaDrugResult> results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            String disclaimer,
            Results results
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Results(int skip, int limit, int total) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FdaDrugResult(
            OpenFda openfda,
            List<String> drug_interactions,
            List<String> warnings
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenFda(
            List<String> brand_name,
            List<String> generic_name,
            List<String> substance_name
    ) {}
}

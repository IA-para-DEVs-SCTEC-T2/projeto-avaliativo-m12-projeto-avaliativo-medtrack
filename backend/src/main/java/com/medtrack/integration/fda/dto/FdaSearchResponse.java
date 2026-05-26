package com.medtrack.integration.fda.dto;

import java.util.List;

public record FdaSearchResponse(
        Meta meta,
        List<FdaDrugResult> results
) {
    public record Meta(
            Disclaimer disclaimer,
            Results results
    ) {
        public record Disclaimer(String text) {}
        public record Results(int skip, int limit, int total) {}
    }

    public record FdaDrugResult(
            OpenFda openfda,
            List<String> drug_interactions,
            List<String> warnings
    ) {}

    public record OpenFda(
            List<String> brand_name,
            List<String> generic_name,
            List<String> substance_name
    ) {}
}

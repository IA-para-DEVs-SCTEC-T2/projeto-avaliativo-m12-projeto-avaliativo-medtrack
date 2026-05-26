package com.medtrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI medTrackOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MedTrack API")
                .description("API de rastreamento de medicamentos com privacidade por design")
                .version("1.0.0")
                .license(new License().name("Projeto Acadêmico")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .schemaRequirement("Bearer Authentication",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .bearerFormat("JWT")
                    .scheme("bearer"));
    }
}

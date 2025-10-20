package com.clinic.physioclinic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi apiGroup() {
        return GroupedOpenApi.builder()
                .group("physio-api")
                .pathsToMatch("/api/**")
                .packagesToScan(
                        "com.clinic.physioclinic.controller",
                        "com.clinic.physioclinic.web"
                )
                .build();
    }

    @Bean
    public OpenAPI appOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Physio Clinic API")
                .version("v1")
                .description("Appointments, auth, and clinic endpoints"));
    }
}

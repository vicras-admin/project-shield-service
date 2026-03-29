package com.vicras.projectshield.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI projectShieldOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Project Shield API")
                        .description("REST API for Project Shield - Portfolio Management and Capacity Planning")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Project Shield Team")
                                .email("team@projectshield.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://projectshield.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Clerk JWT token")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}

package com.vicras.projectshield.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClerkConfig {

    @Value("${clerk.api.base-url}")
    private String clerkApiBaseUrl;

    @Bean
    public RestClient clerkRestClient() {
        String secretKey = System.getenv("CLERK_SECRET_KEY");
        if (secretKey == null || secretKey.isBlank()) {
            secretKey = "placeholder";
        }

        return RestClient.builder()
                .baseUrl(clerkApiBaseUrl)
                .defaultHeader("Authorization", "Bearer " + secretKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

package com.vicras.projectshield.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ResendConfig {

    @Value("${resend.from-email:noreply@projectshield.app}")
    private String fromEmail;

    @Bean
    public RestClient resendRestClient() {
        String apiKey = System.getenv("RESEND_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = "placeholder";
        }

        return RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String getFromEmail() {
        return fromEmail;
    }
}

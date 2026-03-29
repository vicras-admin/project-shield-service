package com.vicras.projectshield;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

@SpringBootApplication
public class ProjectShieldApplication {

    private static final Logger log = LoggerFactory.getLogger(ProjectShieldApplication.class);

    private final ConfigurableEnvironment environment;

    public ProjectShieldApplication(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProjectShieldApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupConfig() {
        log.debug("========== Active Profiles ==========");
        log.debug("Profiles: {}", Arrays.toString(environment.getActiveProfiles()));

        log.debug("========== Application Settings ==========");
        Set<String> propertyNames = new TreeSet<>();
        for (var propertySource : environment.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource<?> enumerable) {
                for (String name : enumerable.getPropertyNames()) {
                    propertyNames.add(name);
                }
            }
        }
        for (String name : propertyNames) {
            if (name.startsWith("spring.") || name.startsWith("server.") ||
                name.startsWith("logging.") || name.startsWith("clerk.") ||
                name.startsWith("resend.") || name.startsWith("app.")) {
                String value = isSensitive(name)
                        ? "******"
                        : environment.getProperty(name);
                log.debug("  {} = {}", name, value);
            }
        }

        log.debug("========== Environment Variables ==========");
        Set.of("PS_DB_USERNAME", "PS_DB_PASSWORD", "PS_DB_URL",
               "CLERK_ISSUER_URI", "CLERK_SECRET_KEY",
               "RESEND_API_KEY", "FRONTEND_URL",
               "SENTRY_DSN", "SENTRY_AUTH_TOKEN"
        ).stream().sorted().forEach(envVar -> {
            String value = System.getenv(envVar);
            if (value != null) {
                log.debug("  {} = {}", envVar, isSensitiveEnv(envVar) ? "******" : value);
            } else {
                log.debug("  {} = <not set>", envVar);
            }
        });

        log.debug("==========================================");
    }

    private boolean isSensitive(String name) {
        String lower = name.toLowerCase();
        return lower.contains("password") || lower.contains("secret") || lower.contains("api-key")
                || lower.contains("api_key") || lower.contains("token") || lower.contains("dsn");
    }

    private boolean isSensitiveEnv(String name) {
        String lower = name.toLowerCase();
        return lower.contains("password") || lower.contains("secret") || lower.contains("api_key")
                || lower.contains("token") || lower.contains("dsn");
    }
}

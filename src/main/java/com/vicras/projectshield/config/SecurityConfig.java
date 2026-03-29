package com.vicras.projectshield.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/health", "/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/register").permitAll()
                .requestMatchers("/api/invitations/*/validate").permitAll()
                .requestMatchers("/api/invitations/accept").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(clerkGrantedAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }

    /**
     * Custom converter for Clerk JWT claims.
     * Extracts roles from Clerk's JWT structure:
     * 1. 'o.rol' - organization role in nested 'o' object (Clerk v2 JWT format)
     * 2. 'org_role' - legacy organization role claim
     * 3. 'roles' - custom roles claim if configured
     * 4. Falls back to 'member' role for authenticated users without explicit roles
     */
    @SuppressWarnings("unchecked")
    private Converter<Jwt, Collection<GrantedAuthority>> clerkGrantedAuthoritiesConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Check for Clerk v2 organization object with nested role (o.rol)
            // Clerk uses "org:admin", "org:member" format — strip the "org:" prefix
            Map<String, Object> orgClaim = jwt.getClaim("o");
            if (orgClaim != null) {
                Object role = orgClaim.get("rol");
                if (role != null && !role.toString().isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + stripOrgPrefix(role.toString())));
                }
            }

            // Check for legacy org_role claim
            String orgRole = jwt.getClaimAsString("org_role");
            if (orgRole != null && !orgRole.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + stripOrgPrefix(orgRole)));
            }

            // Check for custom roles claim
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }

            // Grant default 'member' role if no roles found (for JIT provisioned users)
            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_member"));
            }

            return authorities;
        };
    }

    /**
     * Strips the "org:" prefix from Clerk organization roles.
     * e.g., "org:admin" becomes "admin", "org:member" becomes "member".
     */
    private static String stripOrgPrefix(String role) {
        return role.startsWith("org:") ? role.substring(4) : role;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173", "http://localhost:5174"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

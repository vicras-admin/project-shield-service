package com.vicras.projectshield.integration;

import com.vicras.projectshield.dto.clerk.ClerkOrganizationResponse;
import com.vicras.projectshield.dto.clerk.ClerkUserResponse;
import com.vicras.projectshield.repository.MemberRepository;
import com.vicras.projectshield.repository.OrganizationRepository;
import com.vicras.projectshield.service.ClerkService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import(RegistrationApiIntegrationTest.TestConfig.class)
class RegistrationApiIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ClerkService mockClerkService() {
            ClerkService clerkService = mock(ClerkService.class);

            ClerkUserResponse userResponse = new ClerkUserResponse(
                    "user_123",
                    List.of(new ClerkUserResponse.EmailAddress("email_789", "admin@acme.com")),
                    "John",
                    "Doe"
            );
            when(clerkService.createUser(any(), any(), any(), any())).thenReturn(userResponse);

            ClerkOrganizationResponse orgResponse = new ClerkOrganizationResponse(
                    "org_456",
                    "Acme Corp",
                    "acme-corp",
                    System.currentTimeMillis()
            );
            when(clerkService.createOrganization(any(), any(), any())).thenReturn(orgResponse);

            // Note: addUserToOrganization and verifyEmail are not called
            // - created_by handles membership
            // - email verification is handled by the frontend
            return clerkService;
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
        memberRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void register_Success_Returns201() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "organizationName": "Acme Corp",
                        "organizationSlug": "acme-corp",
                        "email": "admin@acme.com",
                        "password": "password123",
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                    """)
        .when()
                .post("/api/register")
        .then()
                .statusCode(201)
                .body("organization.clerkOrganizationId", equalTo("org_456"))
                .body("organization.name", equalTo("Acme Corp"))
                .body("organization.slug", equalTo("acme-corp"))
                .body("organization.id", notNullValue())
                .body("organization.createdAt", notNullValue())
                .body("user.clerkUserId", equalTo("user_123"))
                .body("user.email", equalTo("admin@acme.com"))
                .body("user.firstName", equalTo("John"))
                .body("user.lastName", equalTo("Doe"))
                .body("user.role", equalTo("admin"));
    }

    @Test
    void register_MissingRequiredFields_Returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "organizationName": "",
                        "organizationSlug": "",
                        "email": "",
                        "password": "",
                        "firstName": "",
                        "lastName": ""
                    }
                    """)
        .when()
                .post("/api/register")
        .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", equalTo("Validation failed"))
                .body("errors", hasSize(greaterThan(0)));
    }

    @Test
    void register_InvalidEmail_Returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "organizationName": "Acme Corp",
                        "organizationSlug": "acme-corp",
                        "email": "not-an-email",
                        "password": "password123",
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                    """)
        .when()
                .post("/api/register")
        .then()
                .statusCode(400)
                .body("errors", hasItem("Email must be valid"));
    }

    @Test
    void register_InvalidSlug_Returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "organizationName": "Acme Corp",
                        "organizationSlug": "Invalid Slug!",
                        "email": "admin@acme.com",
                        "password": "password123",
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                    """)
        .when()
                .post("/api/register")
        .then()
                .statusCode(400)
                .body("errors", hasItem("Slug must be lowercase alphanumeric with hyphens"));
    }

    @Test
    void register_PasswordTooShort_Returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "organizationName": "Acme Corp",
                        "organizationSlug": "acme-corp",
                        "email": "admin@acme.com",
                        "password": "short",
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                    """)
        .when()
                .post("/api/register")
        .then()
                .statusCode(400)
                .body("errors", hasItem("Password must be at least 8 characters"));
    }

    @Test
    void register_NoAuthRequired() {
        // This test verifies that the registration endpoint does not require authentication
        // No Authorization header - should still succeed
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "organizationName": "Test Org",
                        "organizationSlug": "register-test-org",
                        "email": "test@example.com",
                        "password": "password123",
                        "firstName": "Test",
                        "lastName": "User"
                    }
                    """)
        .when()
                .post("/api/register")
        .then()
                .statusCode(201);
    }
}

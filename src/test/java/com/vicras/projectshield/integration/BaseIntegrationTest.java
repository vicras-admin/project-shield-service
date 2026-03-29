package com.vicras.projectshield.integration;

import com.vicras.projectshield.entity.Organization;
import com.vicras.projectshield.repository.OrganizationRepository;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests that provides common setup and authentication helpers.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected OrganizationRepository organizationRepository;

    protected Organization testOrganization;

    /**
     * Test token format: "test-token-{role}"
     * The TestSecurityConfig JwtDecoder parses this and creates appropriate authorities.
     * All test tokens include org_id: "org_test_default"
     */
    protected static final String ADMIN_TOKEN = "test-token-admin";
    protected static final String PROJECT_MANAGER_TOKEN = "test-token-project_manager";
    protected static final String TEAM_LEAD_TOKEN = "test-token-team_lead";
    protected static final String MEMBER_TOKEN = "test-token-member";
    protected static final String VIEWER_TOKEN = "test-token-viewer";

    protected static final String TEST_ORG_CLERK_ID = "org_test_default";

    @BeforeEach
    void baseSetUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";

        // Create or get the test organization that matches the JWT org_id claim
        testOrganization = organizationRepository.findByClerkOrganizationId(TEST_ORG_CLERK_ID)
                .orElseGet(() -> {
                    Organization org = new Organization();
                    org.setClerkOrganizationId(TEST_ORG_CLERK_ID);
                    org.setName("Test Organization");
                    org.setSlug("test-org");
                    return organizationRepository.save(org);
                });
    }

    /**
     * Creates a request specification with admin authorization.
     * Use this for most tests that don't specifically test role restrictions.
     */
    protected RequestSpecification asAdmin() {
        return RestAssured.given()
                .header("Authorization", "Bearer " + ADMIN_TOKEN);
    }

    /**
     * Creates a request specification with project manager authorization.
     */
    protected RequestSpecification asProjectManager() {
        return RestAssured.given()
                .header("Authorization", "Bearer " + PROJECT_MANAGER_TOKEN);
    }

    /**
     * Creates a request specification with team lead authorization.
     */
    protected RequestSpecification asTeamLead() {
        return RestAssured.given()
                .header("Authorization", "Bearer " + TEAM_LEAD_TOKEN);
    }

    /**
     * Creates a request specification with member authorization.
     */
    protected RequestSpecification asMember() {
        return RestAssured.given()
                .header("Authorization", "Bearer " + MEMBER_TOKEN);
    }

    /**
     * Creates a request specification with viewer authorization (read-only).
     */
    protected RequestSpecification asViewer() {
        return RestAssured.given()
                .header("Authorization", "Bearer " + VIEWER_TOKEN);
    }
}

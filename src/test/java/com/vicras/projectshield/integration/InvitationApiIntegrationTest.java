package com.vicras.projectshield.integration;

import com.vicras.projectshield.entity.Invitation;
import com.vicras.projectshield.entity.InvitationStatus;
import com.vicras.projectshield.entity.Member;
import com.vicras.projectshield.entity.RoleEntity;
import com.vicras.projectshield.entity.Seniority;
import com.vicras.projectshield.repository.InvitationRepository;
import com.vicras.projectshield.repository.MemberRepository;
import com.vicras.projectshield.repository.RoleRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

class InvitationApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Member adminMember;

    @BeforeEach
    void setUp() {
        // Create a member that matches the test JWT user
        adminMember = memberRepository.findByClerkUserId("test-user")
                .orElseGet(() -> {
                    RoleEntity adminRole = roleRepository.findByName("admin").orElseThrow();
                    Member member = new Member();
                    member.setClerkUserId("test-user");
                    member.setOrganization(testOrganization);
                    member.setFirstName("Test");
                    member.setLastName("Admin");
                    member.setEmail("admin@test.com");
                    member.setRole(adminRole);
                    member.setSeniority(Seniority.lead);
                    member.setHoursPerDay(8);
                    return memberRepository.save(member);
                });
    }

    @Test
    void createInvitations_asAdmin_returnsCreated() {
        String requestBody = """
            {
                "invitations": [
                    { "email": "newuser@example.com", "role": "member" }
                ]
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/invitations")
        .then()
                .statusCode(201)
                .body("$", hasSize(1))
                .body("[0].email", equalTo("newuser@example.com"))
                .body("[0].role", equalTo("member"))
                .body("[0].status", equalTo("PENDING"));
    }

    @Test
    void createInvitations_asViewer_returnsForbidden() {
        String requestBody = """
            {
                "invitations": [
                    { "email": "user@example.com", "role": "member" }
                ]
            }
            """;

        asViewer()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/invitations")
        .then()
                .statusCode(403);
    }

    @Test
    void createInvitations_asMember_returnsForbidden() {
        String requestBody = """
            {
                "invitations": [
                    { "email": "user@example.com", "role": "member" }
                ]
            }
            """;

        asMember()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/invitations")
        .then()
                .statusCode(403);
    }

    @Test
    void getInvitations_asAdmin_returnsOk() {
        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/invitations")
        .then()
                .statusCode(200)
                .body("$", is(notNullValue()));
    }

    @Test
    void getInvitations_asViewer_returnsForbidden() {
        asViewer()
                .contentType(ContentType.JSON)
        .when()
                .get("/invitations")
        .then()
                .statusCode(403);
    }

    @Test
    void revokeInvitation_withPendingInvitation_returnsNoContent() {
        Invitation invitation = createTestInvitation("revoke-test@example.com", InvitationStatus.PENDING);

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .put("/invitations/{id}/revoke", invitation.getId().toString())
        .then()
                .statusCode(204);
    }

    @Test
    void revokeInvitation_asViewer_returnsForbidden() {
        Invitation invitation = createTestInvitation("revoke-viewer@example.com", InvitationStatus.PENDING);

        asViewer()
                .contentType(ContentType.JSON)
        .when()
                .put("/invitations/{id}/revoke", invitation.getId().toString())
        .then()
                .statusCode(403);
    }

    @Test
    void resendInvitation_withPendingInvitation_returnsOk() {
        Invitation invitation = createTestInvitation("resend-test@example.com", InvitationStatus.PENDING);

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .put("/invitations/{id}/resend", invitation.getId().toString())
        .then()
                .statusCode(200)
                .body("email", equalTo("resend-test@example.com"));
    }

    @Test
    void validateInvitation_withValidToken_returnsValidation() {
        Invitation invitation = createTestInvitation("validate@example.com", InvitationStatus.PENDING);

        // Public endpoint - use admin token since test security requires JWT parsing
        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/invitations/{token}/validate", invitation.getToken())
        .then()
                .statusCode(200)
                .body("valid", equalTo(true))
                .body("email", equalTo("validate@example.com"))
                .body("organizationName", equalTo("Test Organization"))
                .body("role", equalTo("member"));
    }

    @Test
    void validateInvitation_withInvalidToken_returnsInvalid() {
        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/invitations/{token}/validate", "nonexistent-token")
        .then()
                .statusCode(200)
                .body("valid", equalTo(false));
    }

    @Test
    void createInvitations_withInvalidEmail_returnsBadRequest() {
        String requestBody = """
            {
                "invitations": [
                    { "email": "not-an-email", "role": "member" }
                ]
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/invitations")
        .then()
                .statusCode(400);
    }

    @Test
    void createInvitations_withEmptyList_returnsBadRequest() {
        String requestBody = """
            {
                "invitations": []
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/invitations")
        .then()
                .statusCode(400);
    }

    private Invitation createTestInvitation(String email, InvitationStatus status) {
        Invitation invitation = new Invitation();
        invitation.setOrganization(testOrganization);
        invitation.setInvitedBy(adminMember);
        invitation.setEmail(email);
        invitation.setRole("member");
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setStatus(status);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        return invitationRepository.save(invitation);
    }
}

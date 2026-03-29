package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.clerk.*;
import com.vicras.projectshield.exception.ClerkApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ClerkService {

    private static final Logger logger = LoggerFactory.getLogger(ClerkService.class);

    private final RestClient clerkRestClient;

    public ClerkService(RestClient clerkRestClient) {
        this.clerkRestClient = clerkRestClient;
    }

    public ClerkUserResponse createUser(String email, String password, String firstName, String lastName) {
        ClerkCreateUserRequest request = ClerkCreateUserRequest.of(email, password, firstName, lastName);

        try {
            return clerkRestClient.post()
                    .uri("/users")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes());
                        logger.error("Clerk API error creating user: {} - {}", res.getStatusCode(), body);
                        throw new ClerkApiException("Failed to create user in Clerk: " + body);
                    })
                    .body(ClerkUserResponse.class);
        } catch (ClerkApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating user in Clerk", e);
            throw new ClerkApiException("Failed to create user in Clerk: " + e.getMessage(), e);
        }
    }

    public ClerkOrganizationResponse createOrganization(String name, String slug, String createdBy) {
        ClerkCreateOrganizationRequest request = ClerkCreateOrganizationRequest.of(name, slug, createdBy);

        try {
            return clerkRestClient.post()
                    .uri("/organizations")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes());
                        logger.error("Clerk API error creating organization: {} - {}", res.getStatusCode(), body);
                        throw new ClerkApiException("Failed to create organization in Clerk: " + body);
                    })
                    .body(ClerkOrganizationResponse.class);
        } catch (ClerkApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating organization in Clerk", e);
            throw new ClerkApiException("Failed to create organization in Clerk: " + e.getMessage(), e);
        }
    }

    public ClerkMembershipResponse addUserToOrganization(String organizationId, String userId, String role) {
        ClerkAddMembershipRequest request = new ClerkAddMembershipRequest(userId, role);

        try {
            return clerkRestClient.post()
                    .uri("/organizations/{orgId}/memberships", organizationId)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes());
                        logger.error("Clerk API error adding membership: {} - {}", res.getStatusCode(), body);
                        throw new ClerkApiException("Failed to add user to organization in Clerk: " + body);
                    })
                    .body(ClerkMembershipResponse.class);
        } catch (ClerkApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error adding user to organization in Clerk", e);
            throw new ClerkApiException("Failed to add user to organization in Clerk: " + e.getMessage(), e);
        }
    }

    public void verifyEmail(String emailAddressId) {
        try {
            clerkRestClient.patch()
                    .uri("/email_addresses/{emailId}", emailAddressId)
                    .body(java.util.Map.of("verified", true))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes());
                        logger.error("Clerk API error verifying email: {} - {}", res.getStatusCode(), body);
                        throw new ClerkApiException("Failed to verify email in Clerk: " + body);
                    })
                    .toBodilessEntity();
            logger.info("Successfully verified email {} in Clerk", emailAddressId);
        } catch (ClerkApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying email in Clerk", e);
            throw new ClerkApiException("Failed to verify email in Clerk: " + e.getMessage(), e);
        }
    }

    public void deleteUser(String userId) {
        try {
            clerkRestClient.delete()
                    .uri("/users/{userId}", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes());
                        logger.error("Clerk API error deleting user: {} - {}", res.getStatusCode(), body);
                        // Don't throw on delete failures during rollback - just log
                    })
                    .toBodilessEntity();
            logger.info("Successfully deleted user {} from Clerk", userId);
        } catch (Exception e) {
            logger.error("Error deleting user {} from Clerk: {}", userId, e.getMessage());
            // Don't throw on delete failures during rollback
        }
    }

    public void deleteOrganization(String organizationId) {
        try {
            clerkRestClient.delete()
                    .uri("/organizations/{orgId}", organizationId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes());
                        logger.error("Clerk API error deleting organization: {} - {}", res.getStatusCode(), body);
                        // Don't throw on delete failures during rollback - just log
                    })
                    .toBodilessEntity();
            logger.info("Successfully deleted organization {} from Clerk", organizationId);
        } catch (Exception e) {
            logger.error("Error deleting organization {} from Clerk: {}", organizationId, e.getMessage());
            // Don't throw on delete failures during rollback
        }
    }
}

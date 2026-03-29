package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.clerk.*;
import com.vicras.projectshield.exception.ClerkApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClerkServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestBodyUriSpec requestPatchUriSpec;

    private ClerkService clerkService;

    @BeforeEach
    void setUp() {
        clerkService = new ClerkService(restClient);
    }

    @Test
    void createUser_Success() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/users")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ClerkCreateUserRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        ClerkUserResponse expectedResponse = new ClerkUserResponse(
                "user_123",
                List.of(new ClerkUserResponse.EmailAddress("email_001", "test@example.com")),
                "John",
                "Doe"
        );
        when(responseSpec.body(ClerkUserResponse.class)).thenReturn(expectedResponse);

        ClerkUserResponse result = clerkService.createUser("test@example.com", "password", "John", "Doe");

        assertNotNull(result);
        assertEquals("user_123", result.id());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("test@example.com", result.getPrimaryEmail());

        verify(restClient).post();
        verify(requestBodyUriSpec).uri("/users");
    }

    @Test
    void createOrganization_Success() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/organizations")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ClerkCreateOrganizationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        ClerkOrganizationResponse expectedResponse = new ClerkOrganizationResponse(
                "org_456",
                "Acme Corp",
                "acme-corp",
                System.currentTimeMillis()
        );
        when(responseSpec.body(ClerkOrganizationResponse.class)).thenReturn(expectedResponse);

        ClerkOrganizationResponse result = clerkService.createOrganization("Acme Corp", "acme-corp", "user_123");

        assertNotNull(result);
        assertEquals("org_456", result.id());
        assertEquals("Acme Corp", result.name());
        assertEquals("acme-corp", result.slug());

        verify(restClient).post();
        verify(requestBodyUriSpec).uri("/organizations");
    }

    @Test
    void addUserToOrganization_Success() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(eq("/organizations/{orgId}/memberships"), eq("org_456")))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ClerkAddMembershipRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        ClerkMembershipResponse expectedResponse = new ClerkMembershipResponse(
                "mem_789",
                new ClerkMembershipResponse.OrganizationInfo("org_456", "Acme Corp"),
                new ClerkMembershipResponse.PublicUserData("user_123"),
                "org:admin"
        );
        when(responseSpec.body(ClerkMembershipResponse.class)).thenReturn(expectedResponse);

        ClerkMembershipResponse result = clerkService.addUserToOrganization("org_456", "user_123", "org:admin");

        assertNotNull(result);
        assertEquals("mem_789", result.id());
        assertEquals("org:admin", result.role());

        verify(restClient).post();
    }

    @Test
    void createUser_ApiError_ThrowsClerkApiException() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/users")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ClerkCreateUserRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new ClerkApiException("Email already exists"));

        ClerkApiException exception = assertThrows(ClerkApiException.class, () ->
                clerkService.createUser("test@example.com", "password", "John", "Doe")
        );

        assertTrue(exception.getMessage().contains("Email already exists"));
    }

    @Test
    void createOrganization_ApiError_ThrowsClerkApiException() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/organizations")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ClerkCreateOrganizationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new ClerkApiException("Slug taken"));

        ClerkApiException exception = assertThrows(ClerkApiException.class, () ->
                clerkService.createOrganization("Acme Corp", "acme-corp", "user_123")
        );

        assertTrue(exception.getMessage().contains("Slug taken"));
    }

    @Test
    void clerkUserResponse_getPrimaryEmail_ReturnsFirstEmail() {
        ClerkUserResponse response = new ClerkUserResponse(
                "user_123",
                List.of(
                        new ClerkUserResponse.EmailAddress("email_001", "first@example.com"),
                        new ClerkUserResponse.EmailAddress("email_002", "second@example.com")
                ),
                "John",
                "Doe"
        );

        assertEquals("first@example.com", response.getPrimaryEmail());
    }

    @Test
    void clerkUserResponse_getPrimaryEmail_ReturnsNullWhenEmpty() {
        ClerkUserResponse response = new ClerkUserResponse(
                "user_123",
                List.of(),
                "John",
                "Doe"
        );

        assertNull(response.getPrimaryEmail());
    }

    @Test
    void clerkUserResponse_getPrimaryEmail_ReturnsNullWhenNull() {
        ClerkUserResponse response = new ClerkUserResponse(
                "user_123",
                null,
                "John",
                "Doe"
        );

        assertNull(response.getPrimaryEmail());
    }

    @Test
    void clerkAddMembershipRequest_adminMembership_SetsCorrectRole() {
        ClerkAddMembershipRequest request = ClerkAddMembershipRequest.adminMembership("user_123");

        assertEquals("user_123", request.userId());
        assertEquals("org:admin", request.role());
    }

    @SuppressWarnings("unchecked")
    @Test
    void verifyEmail_Success() {
        when(restClient.patch()).thenReturn(requestPatchUriSpec);
        when(requestPatchUriSpec.uri(eq("/email_addresses/{emailId}"), eq("email_001")))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(java.util.Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> clerkService.verifyEmail("email_001"));

        verify(restClient).patch();
    }

    @SuppressWarnings("unchecked")
    @Test
    void verifyEmail_ApiError_ThrowsClerkApiException() {
        when(restClient.patch()).thenReturn(requestPatchUriSpec);
        when(requestPatchUriSpec.uri(eq("/email_addresses/{emailId}"), eq("email_001")))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(java.util.Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new ClerkApiException("Email not found"));

        ClerkApiException exception = assertThrows(ClerkApiException.class, () ->
                clerkService.verifyEmail("email_001")
        );

        assertTrue(exception.getMessage().contains("Email not found"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void verifyEmail_GenericError_ThrowsClerkApiException() {
        when(restClient.patch()).thenReturn(requestPatchUriSpec);
        when(requestPatchUriSpec.uri(eq("/email_addresses/{emailId}"), eq("email_001")))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(java.util.Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        ClerkApiException exception = assertThrows(ClerkApiException.class, () ->
                clerkService.verifyEmail("email_001")
        );

        assertTrue(exception.getMessage().contains("Failed to verify email in Clerk"));
    }

    @Test
    void deleteUser_Success() {
        when(restClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("/users/{userId}"), eq("user_123")))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> clerkService.deleteUser("user_123"));

        verify(restClient).delete();
    }

    @Test
    void deleteUser_Error_DoesNotThrow() {
        when(restClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("/users/{userId}"), eq("user_123")))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        assertDoesNotThrow(() -> clerkService.deleteUser("user_123"));
    }

    @Test
    void deleteOrganization_Success() {
        when(restClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("/organizations/{orgId}"), eq("org_456")))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> clerkService.deleteOrganization("org_456"));

        verify(restClient).delete();
    }

    @Test
    void deleteOrganization_Error_DoesNotThrow() {
        when(restClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("/organizations/{orgId}"), eq("org_456")))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        assertDoesNotThrow(() -> clerkService.deleteOrganization("org_456"));
    }

    @Test
    void createUser_GenericError_ThrowsClerkApiException() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/users")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ClerkCreateUserRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        ClerkApiException exception = assertThrows(ClerkApiException.class, () ->
                clerkService.createUser("test@example.com", "password", "John", "Doe")
        );

        assertTrue(exception.getMessage().contains("Failed to create user in Clerk"));
    }

    @Test
    void createOrganization_GenericError_ThrowsClerkApiException() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/organizations")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ClerkCreateOrganizationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        ClerkApiException exception = assertThrows(ClerkApiException.class, () ->
                clerkService.createOrganization("Acme Corp", "acme-corp", "user_123")
        );

        assertTrue(exception.getMessage().contains("Failed to create organization in Clerk"));
    }

    @Test
    void addUserToOrganization_GenericError_ThrowsClerkApiException() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(eq("/organizations/{orgId}/memberships"), eq("org_456")))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ClerkAddMembershipRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        ClerkApiException exception = assertThrows(ClerkApiException.class, () ->
                clerkService.addUserToOrganization("org_456", "user_123", "org:admin")
        );

        assertTrue(exception.getMessage().contains("Failed to add user to organization in Clerk"));
    }
}

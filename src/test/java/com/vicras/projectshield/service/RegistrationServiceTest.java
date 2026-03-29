package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.clerk.ClerkOrganizationResponse;
import com.vicras.projectshield.dto.clerk.ClerkUserResponse;
import com.vicras.projectshield.dto.request.RegistrationRequest;
import com.vicras.projectshield.dto.response.RegistrationResponse;
import com.vicras.projectshield.entity.Member;
import com.vicras.projectshield.entity.Organization;
import com.vicras.projectshield.entity.RoleEntity;
import com.vicras.projectshield.entity.Seniority;
import com.vicras.projectshield.exception.ClerkApiException;
import com.vicras.projectshield.exception.RegistrationException;
import com.vicras.projectshield.repository.MemberRepository;
import com.vicras.projectshield.repository.OrganizationRepository;
import com.vicras.projectshield.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private ClerkService clerkService;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RoleRepository roleRepository;

    private RegistrationService registrationService;

    private RoleEntity adminRole;

    @BeforeEach
    void setUp() {
        adminRole = new RoleEntity();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName("admin");

        registrationService = new RegistrationService(clerkService, organizationRepository, memberRepository, roleRepository);
    }

    @Test
    void register_Success() {
        RegistrationRequest request = new RegistrationRequest(
                "Acme Corp",
                "acme-corp",
                "admin@acme.com",
                "password123",
                "John",
                "Doe"
        );

        when(organizationRepository.existsBySlug("acme-corp")).thenReturn(false);

        ClerkUserResponse userResponse = new ClerkUserResponse(
                "user_123",
                List.of(new ClerkUserResponse.EmailAddress("email_789", "admin@acme.com")),
                "John",
                "Doe"
        );
        when(clerkService.createUser("admin@acme.com", "password123", "John", "Doe"))
                .thenReturn(userResponse);

        ClerkOrganizationResponse orgResponse = new ClerkOrganizationResponse(
                "org_456",
                "Acme Corp",
                "acme-corp",
                System.currentTimeMillis()
        );
        when(clerkService.createOrganization("Acme Corp", "acme-corp", "user_123"))
                .thenReturn(orgResponse);

        Organization savedOrg = new Organization();
        savedOrg.setId(UUID.randomUUID());
        savedOrg.setClerkOrganizationId("org_456");
        savedOrg.setName("Acme Corp");
        savedOrg.setSlug("acme-corp");
        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrg);

        when(roleRepository.findByName("admin")).thenReturn(java.util.Optional.of(adminRole));

        Member savedMember = new Member();
        savedMember.setId(UUID.randomUUID());
        savedMember.setClerkUserId("user_123");
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        RegistrationResponse response = registrationService.register(request);

        assertNotNull(response);
        assertEquals("org_456", response.organization().clerkOrganizationId());
        assertEquals("Acme Corp", response.organization().name());
        assertEquals("acme-corp", response.organization().slug());
        assertEquals("user_123", response.user().clerkUserId());
        assertEquals("admin@acme.com", response.user().email());
        assertEquals("John", response.user().firstName());
        assertEquals("Doe", response.user().lastName());
        assertEquals("admin", response.user().role());

        verify(organizationRepository).existsBySlug("acme-corp");
        verify(clerkService).createUser("admin@acme.com", "password123", "John", "Doe");
        verify(clerkService).createOrganization("Acme Corp", "acme-corp", "user_123");
        // Note: addUserToOrganization is not called because created_by handles membership
        verify(clerkService, never()).addUserToOrganization(any(), any(), any());
        // Note: verifyEmail is not called - email verification is handled by the frontend
        verify(clerkService, never()).verifyEmail(any());
        verify(organizationRepository).save(any(Organization.class));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void register_SlugAlreadyExists_ThrowsRegistrationException() {
        RegistrationRequest request = new RegistrationRequest(
                "Acme Corp",
                "acme-corp",
                "admin@acme.com",
                "password123",
                "John",
                "Doe"
        );

        when(organizationRepository.existsBySlug("acme-corp")).thenReturn(true);

        RegistrationException exception = assertThrows(RegistrationException.class, () ->
                registrationService.register(request)
        );

        assertEquals("Organization slug already exists: acme-corp", exception.getMessage());

        verify(organizationRepository).existsBySlug("acme-corp");
        verifyNoInteractions(clerkService);
        verify(organizationRepository, never()).save(any());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void register_ClerkUserCreationFails_ThrowsException() {
        RegistrationRequest request = new RegistrationRequest(
                "Acme Corp",
                "acme-corp",
                "admin@acme.com",
                "password123",
                "John",
                "Doe"
        );

        when(organizationRepository.existsBySlug("acme-corp")).thenReturn(false);
        when(clerkService.createUser("admin@acme.com", "password123", "John", "Doe"))
                .thenThrow(new ClerkApiException("Email already exists"));

        assertThrows(RegistrationException.class, () -> registrationService.register(request));

        verify(clerkService, never()).createOrganization(any(), any(), any());
        verify(organizationRepository, never()).save(any());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void register_ClerkOrgCreationFails_RollsBackUser() {
        RegistrationRequest request = new RegistrationRequest(
                "Acme Corp",
                "acme-corp",
                "admin@acme.com",
                "password123",
                "John",
                "Doe"
        );

        when(organizationRepository.existsBySlug("acme-corp")).thenReturn(false);

        ClerkUserResponse userResponse = new ClerkUserResponse(
                "user_123",
                List.of(new ClerkUserResponse.EmailAddress("email_789", "admin@acme.com")),
                "John",
                "Doe"
        );
        when(clerkService.createUser("admin@acme.com", "password123", "John", "Doe"))
                .thenReturn(userResponse);

        when(clerkService.createOrganization("Acme Corp", "acme-corp", "user_123"))
                .thenThrow(new ClerkApiException("Slug already exists in Clerk"));

        assertThrows(RegistrationException.class, () -> registrationService.register(request));

        verify(clerkService).deleteUser("user_123");
        verify(clerkService, never()).deleteOrganization(any());
        verify(organizationRepository, never()).save(any());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void register_DatabaseSaveFails_RollsBackClerkResources() {
        RegistrationRequest request = new RegistrationRequest(
                "Acme Corp",
                "acme-corp",
                "admin@acme.com",
                "password123",
                "John",
                "Doe"
        );

        when(organizationRepository.existsBySlug("acme-corp")).thenReturn(false);

        ClerkUserResponse userResponse = new ClerkUserResponse(
                "user_123",
                List.of(new ClerkUserResponse.EmailAddress("email_789", "admin@acme.com")),
                "John",
                "Doe"
        );
        when(clerkService.createUser("admin@acme.com", "password123", "John", "Doe"))
                .thenReturn(userResponse);

        ClerkOrganizationResponse orgResponse = new ClerkOrganizationResponse(
                "org_456",
                "Acme Corp",
                "acme-corp",
                System.currentTimeMillis()
        );
        when(clerkService.createOrganization("Acme Corp", "acme-corp", "user_123"))
                .thenReturn(orgResponse);

        when(organizationRepository.save(any(Organization.class)))
                .thenThrow(new RuntimeException("Database error"));

        // roleRepository.findByName is not reached because the org save fails first
        assertThrows(RegistrationException.class, () -> registrationService.register(request));

        verify(clerkService).deleteOrganization("org_456");
        verify(clerkService).deleteUser("user_123");
    }

    @Test
    void register_SavesCorrectOrganizationData() {
        RegistrationRequest request = new RegistrationRequest(
                "Acme Corp",
                "acme-corp",
                "admin@acme.com",
                "password123",
                "John",
                "Doe"
        );

        when(organizationRepository.existsBySlug("acme-corp")).thenReturn(false);

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

        ArgumentCaptor<Organization> orgCaptor = ArgumentCaptor.forClass(Organization.class);
        Organization savedOrg = new Organization();
        savedOrg.setId(UUID.randomUUID());
        savedOrg.setClerkOrganizationId("org_456");
        savedOrg.setName("Acme Corp");
        savedOrg.setSlug("acme-corp");
        when(organizationRepository.save(orgCaptor.capture())).thenReturn(savedOrg);

        when(roleRepository.findByName("admin")).thenReturn(java.util.Optional.of(adminRole));

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        Member savedMember = new Member();
        savedMember.setId(UUID.randomUUID());
        when(memberRepository.save(memberCaptor.capture())).thenReturn(savedMember);

        registrationService.register(request);

        Organization capturedOrg = orgCaptor.getValue();
        assertEquals("org_456", capturedOrg.getClerkOrganizationId());
        assertEquals("Acme Corp", capturedOrg.getName());
        assertEquals("acme-corp", capturedOrg.getSlug());

        Member capturedMember = memberCaptor.getValue();
        assertEquals("user_123", capturedMember.getClerkUserId());
        assertEquals("John", capturedMember.getFirstName());
        assertEquals("Doe", capturedMember.getLastName());
        assertEquals("admin@acme.com", capturedMember.getEmail());
        assertEquals("admin", capturedMember.getRole().getName());
        assertEquals(Seniority.lead, capturedMember.getSeniority());
        assertEquals(8, capturedMember.getHoursPerDay());
    }
}

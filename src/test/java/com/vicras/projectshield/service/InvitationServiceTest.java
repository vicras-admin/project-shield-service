package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.clerk.ClerkUserResponse;
import com.vicras.projectshield.dto.request.AcceptInvitationRequest;
import com.vicras.projectshield.dto.request.BulkInviteRequest;
import com.vicras.projectshield.dto.request.InviteRequest;
import com.vicras.projectshield.dto.response.InvitationResponse;
import com.vicras.projectshield.dto.response.InviteValidationResponse;
import com.vicras.projectshield.entity.*;
import com.vicras.projectshield.exception.InvitationException;
import com.vicras.projectshield.repository.InvitationRepository;
import com.vicras.projectshield.repository.MemberRepository;
import com.vicras.projectshield.repository.RoleRepository;
import com.vicras.projectshield.security.OrganizationContext;
import com.vicras.projectshield.security.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ClerkService clerkService;

    @Mock
    private EmailService emailService;

    @Mock
    private OrganizationContext organizationContext;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private InvitationService invitationService;

    private Organization testOrganization;
    private Member testAdmin;

    @BeforeEach
    void setUp() {
        testOrganization = new Organization();
        testOrganization.setId(UUID.randomUUID());
        testOrganization.setClerkOrganizationId("org_test");
        testOrganization.setName("Test Org");
        testOrganization.setSlug("test-org");

        RoleEntity adminRole = new RoleEntity();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName("admin");

        testAdmin = new Member();
        testAdmin.setId(UUID.randomUUID());
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("User");
        testAdmin.setEmail("admin@test.com");
        testAdmin.setRole(adminRole);
        testAdmin.setOrganization(testOrganization);
        testAdmin.setSeniority(Seniority.lead);
        testAdmin.setHoursPerDay(8);
    }

    @Test
    void createInvitations_withValidData_createsAndSendsEmail() {
        when(organizationContext.getCurrentOrganization()).thenReturn(testOrganization);
        when(userContext.getCurrentMember()).thenReturn(Optional.of(testAdmin));
        when(invitationRepository.existsByEmailAndOrganizationIdAndStatus(
                anyString(), any(UUID.class), any(InvitationStatus.class))).thenReturn(false);
        when(memberRepository.findByRoleName(anyString())).thenReturn(List.of());
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> {
            Invitation inv = invocation.getArgument(0);
            inv.setId(UUID.randomUUID());
            return inv;
        });

        BulkInviteRequest request = new BulkInviteRequest(List.of(
                new InviteRequest("user@example.com", "member")
        ));

        List<InvitationResponse> responses = invitationService.createInvitations(request);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).email()).isEqualTo("user@example.com");
        assertThat(responses.get(0).role()).isEqualTo("member");

        verify(emailService).sendInvitationEmail(
                eq("user@example.com"), eq("Admin User"), eq("Test Org"), anyString(), eq("member"));
        verify(invitationRepository).save(any(Invitation.class));
    }

    @Test
    void createInvitations_withDuplicatePending_skipsInvite() {
        when(organizationContext.getCurrentOrganization()).thenReturn(testOrganization);
        when(userContext.getCurrentMember()).thenReturn(Optional.of(testAdmin));
        when(memberRepository.findByRoleName(anyString())).thenReturn(List.of());
        when(invitationRepository.existsByEmailAndOrganizationIdAndStatus(
                eq("existing@example.com"), any(UUID.class), eq(InvitationStatus.PENDING))).thenReturn(true);

        BulkInviteRequest request = new BulkInviteRequest(List.of(
                new InviteRequest("existing@example.com", "member")
        ));

        List<InvitationResponse> responses = invitationService.createInvitations(request);

        assertThat(responses).isEmpty();
        verify(emailService, never()).sendInvitationEmail(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createInvitations_withCurrentUserNotFound_throwsException() {
        when(organizationContext.getCurrentOrganization()).thenReturn(testOrganization);
        when(userContext.getCurrentMember()).thenReturn(Optional.empty());

        BulkInviteRequest request = new BulkInviteRequest(List.of(
                new InviteRequest("user@example.com", "member")
        ));

        assertThatThrownBy(() -> invitationService.createInvitations(request))
                .isInstanceOf(InvitationException.class)
                .hasMessageContaining("Current user not found");
    }

    @Test
    void revokeInvitation_withPendingInvitation_revokes() {
        when(organizationContext.getCurrentOrganization()).thenReturn(testOrganization);

        Invitation invitation = createTestInvitation(InvitationStatus.PENDING);
        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));

        invitationService.revokeInvitation(invitation.getId());

        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.REVOKED);
    }

    @Test
    void revokeInvitation_withNonPendingStatus_throwsException() {
        when(organizationContext.getCurrentOrganization()).thenReturn(testOrganization);

        Invitation invitation = createTestInvitation(InvitationStatus.ACCEPTED);
        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.revokeInvitation(invitation.getId()))
                .isInstanceOf(InvitationException.class)
                .hasMessageContaining("Only pending invitations can be revoked");
    }

    @Test
    void resendInvitation_refreshesTokenAndExpiry() {
        when(organizationContext.getCurrentOrganization()).thenReturn(testOrganization);
        when(userContext.getCurrentMember()).thenReturn(Optional.of(testAdmin));

        Invitation invitation = createTestInvitation(InvitationStatus.PENDING);
        String originalToken = invitation.getToken();
        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

        InvitationResponse response = invitationService.resendInvitation(invitation.getId());

        assertThat(response.token()).isNotEqualTo(originalToken);
        verify(emailService).sendInvitationEmail(
                eq("invited@example.com"), eq("Admin User"), eq("Test Org"), anyString(), eq("member"));
    }

    @Test
    void validateInvitation_withValidToken_returnsValid() {
        Invitation invitation = createTestInvitation(InvitationStatus.PENDING);
        when(invitationRepository.findByToken("valid-token")).thenReturn(Optional.of(invitation));

        InviteValidationResponse response = invitationService.validateInvitation("valid-token");

        assertThat(response.valid()).isTrue();
        assertThat(response.email()).isEqualTo("invited@example.com");
        assertThat(response.organizationName()).isEqualTo("Test Org");
        assertThat(response.role()).isEqualTo("member");
        assertThat(response.expired()).isFalse();
    }

    @Test
    void validateInvitation_withInvalidToken_returnsInvalid() {
        when(invitationRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        InviteValidationResponse response = invitationService.validateInvitation("invalid-token");

        assertThat(response.valid()).isFalse();
    }

    @Test
    void validateInvitation_withExpiredInvitation_returnsExpired() {
        Invitation invitation = createTestInvitation(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(invitationRepository.findByToken("expired-token")).thenReturn(Optional.of(invitation));

        InviteValidationResponse response = invitationService.validateInvitation("expired-token");

        assertThat(response.valid()).isFalse();
        assertThat(response.expired()).isTrue();
    }

    @Test
    void acceptInvitation_withValidToken_createsUserAndMember() {
        Invitation invitation = createTestInvitation(InvitationStatus.PENDING);
        when(invitationRepository.findByToken("accept-token")).thenReturn(Optional.of(invitation));

        RoleEntity memberRoleEntity = new RoleEntity();
        memberRoleEntity.setId(UUID.randomUUID());
        memberRoleEntity.setName("member");
        when(roleRepository.findByName("member")).thenReturn(Optional.of(memberRoleEntity));

        ClerkUserResponse clerkUser = new ClerkUserResponse("clerk_user_123", null, "John", "Doe");
        when(clerkService.createUser(anyString(), anyString(), anyString(), anyString())).thenReturn(clerkUser);
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> {
            Member m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

        AcceptInvitationRequest request = new AcceptInvitationRequest(
                "accept-token", "John", "Doe", "password123"
        );

        invitationService.acceptInvitation(request);

        verify(clerkService).createUser("invited@example.com", "password123", "John", "Doe");
        verify(clerkService).addUserToOrganization("org_test", "clerk_user_123", "member");

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getFirstName()).isEqualTo("John");
        assertThat(memberCaptor.getValue().getLastName()).isEqualTo("Doe");
        assertThat(memberCaptor.getValue().getEmail()).isEqualTo("invited@example.com");

        ArgumentCaptor<Invitation> invCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository, atLeastOnce()).save(invCaptor.capture());
        Invitation savedInv = invCaptor.getAllValues().stream()
                .filter(i -> i.getStatus() == InvitationStatus.ACCEPTED)
                .findFirst().orElseThrow();
        assertThat(savedInv.getAcceptedAt()).isNotNull();
    }

    @Test
    void acceptInvitation_withExpiredToken_throwsException() {
        Invitation invitation = createTestInvitation(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(invitationRepository.findByToken("expired-token")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

        AcceptInvitationRequest request = new AcceptInvitationRequest(
                "expired-token", "John", "Doe", "password123"
        );

        assertThatThrownBy(() -> invitationService.acceptInvitation(request))
                .isInstanceOf(InvitationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void acceptInvitation_withRevokedStatus_throwsException() {
        Invitation invitation = createTestInvitation(InvitationStatus.REVOKED);
        when(invitationRepository.findByToken("revoked-token")).thenReturn(Optional.of(invitation));

        AcceptInvitationRequest request = new AcceptInvitationRequest(
                "revoked-token", "John", "Doe", "password123"
        );

        assertThatThrownBy(() -> invitationService.acceptInvitation(request))
                .isInstanceOf(InvitationException.class)
                .hasMessageContaining("no longer valid");
    }

    @Test
    void acceptInvitation_whenClerkFails_rollsBackUser() {
        Invitation invitation = createTestInvitation(InvitationStatus.PENDING);
        when(invitationRepository.findByToken("fail-token")).thenReturn(Optional.of(invitation));

        ClerkUserResponse clerkUser = new ClerkUserResponse("clerk_user_fail", null, "John", "Doe");
        when(clerkService.createUser(anyString(), anyString(), anyString(), anyString())).thenReturn(clerkUser);
        doThrow(new RuntimeException("Clerk org error")).when(clerkService)
                .addUserToOrganization(anyString(), anyString(), anyString());

        AcceptInvitationRequest request = new AcceptInvitationRequest(
                "fail-token", "John", "Doe", "password123"
        );

        assertThatThrownBy(() -> invitationService.acceptInvitation(request))
                .isInstanceOf(InvitationException.class);

        verify(clerkService).deleteUser("clerk_user_fail");
    }

    private Invitation createTestInvitation(InvitationStatus status) {
        Invitation invitation = new Invitation();
        invitation.setId(UUID.randomUUID());
        invitation.setOrganization(testOrganization);
        invitation.setInvitedBy(testAdmin);
        invitation.setEmail("invited@example.com");
        invitation.setRole("member");
        invitation.setToken("valid-token");
        invitation.setStatus(status);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        return invitation;
    }
}

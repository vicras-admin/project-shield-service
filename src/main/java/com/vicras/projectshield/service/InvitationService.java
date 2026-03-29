package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.clerk.ClerkUserResponse;
import com.vicras.projectshield.dto.request.AcceptInvitationRequest;
import com.vicras.projectshield.dto.request.BulkInviteRequest;
import com.vicras.projectshield.dto.request.InviteRequest;
import com.vicras.projectshield.dto.response.InvitationResponse;
import com.vicras.projectshield.dto.response.InviteValidationResponse;
import com.vicras.projectshield.entity.Invitation;
import com.vicras.projectshield.entity.InvitationStatus;
import com.vicras.projectshield.entity.Member;
import com.vicras.projectshield.entity.Organization;
import com.vicras.projectshield.entity.RoleEntity;
import com.vicras.projectshield.entity.Seniority;
import com.vicras.projectshield.exception.InvitationException;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.InvitationRepository;
import com.vicras.projectshield.repository.MemberRepository;
import com.vicras.projectshield.repository.RoleRepository;
import com.vicras.projectshield.security.OrganizationContext;
import com.vicras.projectshield.security.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InvitationService {

    private static final Logger logger = LoggerFactory.getLogger(InvitationService.class);
    private static final int INVITATION_EXPIRY_DAYS = 7;

    private final InvitationRepository invitationRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final ClerkService clerkService;
    private final EmailService emailService;
    private final OrganizationContext organizationContext;
    private final UserContext userContext;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public InvitationService(InvitationRepository invitationRepository,
                             MemberRepository memberRepository,
                             RoleRepository roleRepository,
                             ClerkService clerkService,
                             EmailService emailService,
                             OrganizationContext organizationContext,
                             UserContext userContext) {
        this.invitationRepository = invitationRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.clerkService = clerkService;
        this.emailService = emailService;
        this.organizationContext = organizationContext;
        this.userContext = userContext;
    }

    @Transactional
    public List<InvitationResponse> createInvitations(BulkInviteRequest request) {
        Organization organization = organizationContext.getCurrentOrganization();
        Member inviter = userContext.getCurrentMember()
                .orElseThrow(() -> new InvitationException("Current user not found"));

        List<InvitationResponse> responses = new ArrayList<>();

        for (InviteRequest invite : request.invitations()) {
            String email = invite.email().trim().toLowerCase();

            // Check for existing member
            List<Member> existingMembers = memberRepository.findByRoleName(invite.role());
            boolean alreadyMember = existingMembers.stream()
                    .anyMatch(m -> email.equals(m.getEmail()) &&
                            m.getOrganization().getId().equals(organization.getId()));
            if (alreadyMember) {
                logger.warn("Skipping invitation for {} - already a member of the organization", email);
                continue;
            }

            // Check for duplicate pending invitation
            if (invitationRepository.existsByEmailAndOrganizationIdAndStatus(
                    email, organization.getId(), InvitationStatus.PENDING)) {
                logger.warn("Skipping invitation for {} - pending invitation already exists", email);
                continue;
            }

            Invitation invitation = new Invitation();
            invitation.setOrganization(organization);
            invitation.setInvitedBy(inviter);
            invitation.setEmail(email);
            invitation.setRole(invite.role());
            invitation.setToken(UUID.randomUUID().toString());
            invitation.setStatus(InvitationStatus.PENDING);
            invitation.setExpiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRY_DAYS));

            invitation = invitationRepository.save(invitation);

            String inviteLink = frontendUrl + "/#accept-invite?token=" + invitation.getToken();
            emailService.sendInvitationEmail(
                    email,
                    inviter.getFullName(),
                    organization.getName(),
                    inviteLink,
                    invite.role()
            );

            responses.add(InvitationResponse.from(invitation));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getInvitations() {
        Organization organization = organizationContext.getCurrentOrganization();

        List<Invitation> invitations = invitationRepository
                .findByOrganizationIdOrderByCreatedAtDesc(organization.getId());

        return invitations.stream()
                .map(inv -> {
                    // Auto-mark expired invitations
                    if (inv.getStatus() == InvitationStatus.PENDING &&
                            inv.getExpiresAt().isBefore(LocalDateTime.now())) {
                        inv.setStatus(InvitationStatus.EXPIRED);
                        invitationRepository.save(inv);
                    }
                    return InvitationResponse.from(inv);
                })
                .toList();
    }

    @Transactional
    public void revokeInvitation(UUID id) {
        Organization organization = organizationContext.getCurrentOrganization();
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + id));

        if (!invitation.getOrganization().getId().equals(organization.getId())) {
            throw new InvitationException("Invitation does not belong to your organization");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvitationException("Only pending invitations can be revoked");
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepository.save(invitation);
        logger.info("Invitation {} revoked", id);
    }

    @Transactional
    public InvitationResponse resendInvitation(UUID id) {
        Organization organization = organizationContext.getCurrentOrganization();
        Member inviter = userContext.getCurrentMember()
                .orElseThrow(() -> new InvitationException("Current user not found"));

        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + id));

        if (!invitation.getOrganization().getId().equals(organization.getId())) {
            throw new InvitationException("Invitation does not belong to your organization");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvitationException("Only pending invitations can be resent");
        }

        // Refresh token and expiry
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRY_DAYS));
        invitation = invitationRepository.save(invitation);

        String inviteLink = frontendUrl + "/#accept-invite?token=" + invitation.getToken();
        emailService.sendInvitationEmail(
                invitation.getEmail(),
                inviter.getFullName(),
                organization.getName(),
                inviteLink,
                invitation.getRole()
        );

        logger.info("Invitation {} resent to {}", id, invitation.getEmail());
        return InvitationResponse.from(invitation);
    }

    @Transactional(readOnly = true)
    public InviteValidationResponse validateInvitation(String token) {
        Invitation invitation = invitationRepository.findByToken(token).orElse(null);

        if (invitation == null) {
            return new InviteValidationResponse(false, null, null, null, false);
        }

        boolean expired = invitation.getExpiresAt().isBefore(LocalDateTime.now());
        boolean valid = invitation.getStatus() == InvitationStatus.PENDING && !expired;

        return new InviteValidationResponse(
                valid,
                invitation.getEmail(),
                invitation.getOrganization().getName(),
                invitation.getRole(),
                expired
        );
    }

    @Transactional
    public void acceptInvitation(AcceptInvitationRequest request) {
        Invitation invitation = invitationRepository.findByToken(request.token())
                .orElseThrow(() -> new InvitationException("Invalid invitation token"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvitationException("This invitation is no longer valid (status: " + invitation.getStatus() + ")");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new InvitationException("This invitation has expired");
        }

        Organization organization = invitation.getOrganization();
        String clerkUserId = null;

        try {
            // Create user in Clerk
            ClerkUserResponse clerkUser = clerkService.createUser(
                    invitation.getEmail(),
                    request.password(),
                    request.firstName(),
                    request.lastName()
            );
            clerkUserId = clerkUser.id();
            logger.info("Created Clerk user {} for invitation acceptance", clerkUserId);

            // Add user to Clerk organization with the invited role
            clerkService.addUserToOrganization(
                    organization.getClerkOrganizationId(),
                    clerkUserId,
                    invitation.getRole()
            );
            logger.info("Added user {} to Clerk org {} with role {}",
                    clerkUserId, organization.getClerkOrganizationId(), invitation.getRole());

            // Create local member record
            Member member = new Member();
            member.setClerkUserId(clerkUserId);
            member.setOrganization(organization);
            member.setFirstName(request.firstName());
            member.setLastName(request.lastName());
            member.setEmail(invitation.getEmail());
            RoleEntity roleEntity = roleRepository.findByName(invitation.getRole())
                    .orElseThrow(() -> new InvitationException("Role not found: " + invitation.getRole()));
            member.setRole(roleEntity);
            member.setSeniority(Seniority.mid);
            member.setHoursPerDay(8);
            memberRepository.save(member);
            logger.info("Created local member for invitation acceptance: {}", member.getId());

            // Mark invitation as accepted
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setAcceptedAt(LocalDateTime.now());
            invitationRepository.save(invitation);

        } catch (Exception e) {
            logger.error("Invitation acceptance failed, initiating rollback", e);

            if (clerkUserId != null) {
                logger.info("Deleting Clerk user: {}", clerkUserId);
                clerkService.deleteUser(clerkUserId);
            }

            if (e instanceof InvitationException) {
                throw e;
            }
            throw new InvitationException("Failed to accept invitation: " + e.getMessage(), e);
        }
    }

}

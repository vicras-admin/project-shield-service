package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.clerk.ClerkOrganizationResponse;
import com.vicras.projectshield.dto.clerk.ClerkUserResponse;
import com.vicras.projectshield.dto.request.RegistrationRequest;
import com.vicras.projectshield.dto.response.RegistrationResponse;
import com.vicras.projectshield.entity.Member;
import com.vicras.projectshield.entity.Organization;
import com.vicras.projectshield.entity.RoleEntity;
import com.vicras.projectshield.entity.Seniority;
import com.vicras.projectshield.exception.RegistrationException;
import com.vicras.projectshield.repository.MemberRepository;
import com.vicras.projectshield.repository.OrganizationRepository;
import com.vicras.projectshield.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    private final ClerkService clerkService;
    private final OrganizationRepository organizationRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

    public RegistrationService(ClerkService clerkService, OrganizationRepository organizationRepository,
                               MemberRepository memberRepository, RoleRepository roleRepository) {
        this.clerkService = clerkService;
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        // Step 1: Validate slug doesn't exist locally
        if (organizationRepository.existsBySlug(request.organizationSlug())) {
            throw new RegistrationException("Organization slug already exists: " + request.organizationSlug());
        }

        String clerkUserId = null;
        String clerkOrgId = null;

        try {
            // Step 2: Create user in Clerk
            ClerkUserResponse clerkUser = clerkService.createUser(
                    request.email(),
                    request.password(),
                    request.firstName(),
                    request.lastName()
            );
            clerkUserId = clerkUser.id();
            logger.info("Created user in Clerk: {}", clerkUserId);

            // Step 3: Create organization in Clerk (with createdBy=clerkUserId)
            // Note: created_by automatically adds the user as admin, no need to call addUserToOrganization
            ClerkOrganizationResponse clerkOrg = clerkService.createOrganization(
                    request.organizationName(),
                    request.organizationSlug(),
                    clerkUserId
            );
            clerkOrgId = clerkOrg.id();
            logger.info("Created organization in Clerk: {} (user {} added as admin via created_by)", clerkOrgId, clerkUserId);

            // Step 4: Save organization to local DB
            Organization organization = new Organization();
            organization.setClerkOrganizationId(clerkOrgId);
            organization.setName(request.organizationName());
            organization.setSlug(request.organizationSlug());
            organization = organizationRepository.save(organization);
            logger.info("Created organization in local DB: {}", organization.getId());

            // Step 5: Create member record linked to Clerk user
            Member member = new Member();
            member.setClerkUserId(clerkUserId);
            member.setOrganization(organization);
            member.setFirstName(request.firstName());
            member.setLastName(request.lastName());
            member.setEmail(request.email());
            RoleEntity adminRole = roleRepository.findByName("admin")
                    .orElseThrow(() -> new RegistrationException("Admin role not found in database"));
            member.setRole(adminRole);
            member.setSeniority(Seniority.lead);
            member.setHoursPerDay(8);
            member = memberRepository.save(member);
            logger.info("Created member in local DB: {} for user {}", member.getId(), clerkUserId);

            // Step 6: Return RegistrationResponse
            return RegistrationResponse.of(
                    organization,
                    clerkUserId,
                    request.email(),
                    request.firstName(),
                    request.lastName(),
                    "admin"
            );

        } catch (Exception e) {
            // Rollback Clerk resources on failure
            logger.error("Registration failed, initiating rollback", e);

            if (clerkOrgId != null) {
                logger.info("Deleting Clerk organization: {}", clerkOrgId);
                clerkService.deleteOrganization(clerkOrgId);
            }

            if (clerkUserId != null) {
                logger.info("Deleting Clerk user: {}", clerkUserId);
                clerkService.deleteUser(clerkUserId);
            }

            if (e instanceof RegistrationException) {
                throw e;
            }
            throw new RegistrationException("Registration failed: " + e.getMessage(), e);
        }
    }
}

package com.vicras.projectshield.security;

import com.vicras.projectshield.entity.Organization;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.OrganizationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrganizationContext {

    private final OrganizationRepository organizationRepository;

    public OrganizationContext(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organization getCurrentOrganization() {
        String clerkOrgId = getCurrentClerkOrganizationId();
        if (clerkOrgId == null) {
            throw new IllegalStateException("No organization context available");
        }
        return organizationRepository.findByClerkOrganizationId(clerkOrgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found for Clerk ID: " + clerkOrgId));
    }

    /**
     * Extracts the Clerk organization ID from the JWT.
     * Supports both Clerk v2 format (o.id) and legacy format (org_id).
     */
    public String getCurrentClerkOrganizationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        // Check for Clerk v2 organization object (o.id)
        Map<String, Object> orgClaim = jwt.getClaim("o");
        if (orgClaim != null) {
            Object orgId = orgClaim.get("id");
            if (orgId != null) {
                return orgId.toString();
            }
        }

        // Fall back to legacy org_id claim
        return jwt.getClaimAsString("org_id");
    }

    public boolean hasOrganizationContext() {
        return getCurrentClerkOrganizationId() != null;
    }
}

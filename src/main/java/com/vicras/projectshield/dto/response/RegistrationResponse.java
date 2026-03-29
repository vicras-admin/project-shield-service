package com.vicras.projectshield.dto.response;

import com.vicras.projectshield.entity.Organization;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistrationResponse(
        OrganizationInfo organization,
        UserInfo user
) {
    public record OrganizationInfo(
            UUID id,
            String clerkOrganizationId,
            String name,
            String slug,
            LocalDateTime createdAt
    ) {
        public static OrganizationInfo from(Organization org) {
            return new OrganizationInfo(
                    org.getId(),
                    org.getClerkOrganizationId(),
                    org.getName(),
                    org.getSlug(),
                    org.getCreatedAt()
            );
        }
    }

    public record UserInfo(
            String clerkUserId,
            String email,
            String firstName,
            String lastName,
            String role
    ) {}

    public static RegistrationResponse of(Organization org, String clerkUserId, String email,
                                          String firstName, String lastName, String role) {
        return new RegistrationResponse(
                OrganizationInfo.from(org),
                new UserInfo(clerkUserId, email, firstName, lastName, role)
        );
    }
}

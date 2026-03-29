package com.vicras.projectshield.dto.clerk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClerkMembershipResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("organization")
        OrganizationInfo organization,

        @JsonProperty("public_user_data")
        PublicUserData publicUserData,

        @JsonProperty("role")
        String role
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrganizationInfo(
            @JsonProperty("id")
            String id,

            @JsonProperty("name")
            String name
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PublicUserData(
            @JsonProperty("user_id")
            String userId
    ) {}
}

package com.vicras.projectshield.dto.clerk;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClerkAddMembershipRequest(
        @JsonProperty("user_id")
        String userId,

        @JsonProperty("role")
        String role
) {
    public static ClerkAddMembershipRequest adminMembership(String userId) {
        return new ClerkAddMembershipRequest(userId, "org:admin");
    }
}

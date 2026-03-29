package com.vicras.projectshield.dto.clerk;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClerkCreateOrganizationRequest(
        @JsonProperty("name")
        String name,

        @JsonProperty("slug")
        String slug,

        @JsonProperty("created_by")
        String createdBy
) {
    public static ClerkCreateOrganizationRequest of(String name, String slug, String createdBy) {
        return new ClerkCreateOrganizationRequest(name, slug, createdBy);
    }
}

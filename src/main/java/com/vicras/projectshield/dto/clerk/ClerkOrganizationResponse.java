package com.vicras.projectshield.dto.clerk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClerkOrganizationResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("name")
        String name,

        @JsonProperty("slug")
        String slug,

        @JsonProperty("created_at")
        Long createdAt
) {}

package com.vicras.projectshield.dto.clerk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClerkUserResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("email_addresses")
        List<EmailAddress> emailAddresses,

        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("last_name")
        String lastName
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmailAddress(
            @JsonProperty("id")
            String id,

            @JsonProperty("email_address")
            String emailAddress
    ) {}

    public String getPrimaryEmail() {
        if (emailAddresses != null && !emailAddresses.isEmpty()) {
            return emailAddresses.get(0).emailAddress();
        }
        return null;
    }
}

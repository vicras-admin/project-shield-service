package com.vicras.projectshield.dto.clerk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClerkCreateUserRequest(
        @JsonProperty("email_address")
        List<String> emailAddress,

        @JsonProperty("password")
        String password,

        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("last_name")
        String lastName,

        @JsonProperty("skip_password_checks")
        boolean skipPasswordChecks,

        @JsonProperty("skip_email_verification")
        boolean skipEmailVerification
) {
    public static ClerkCreateUserRequest of(String email, String password, String firstName, String lastName) {
        return new ClerkCreateUserRequest(List.of(email), password, firstName, lastName, false, true);
    }
}

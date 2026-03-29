package com.vicras.projectshield.dto.response;

public record InviteValidationResponse(
        boolean valid,
        String email,
        String organizationName,
        String role,
        boolean expired
) {
}

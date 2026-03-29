package com.vicras.projectshield.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkInviteRequest(
        @NotEmpty(message = "At least one invitation is required")
        @Valid
        List<InviteRequest> invitations
) {
}

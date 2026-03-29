package com.vicras.projectshield.dto.response;

import com.vicras.projectshield.entity.Invitation;

import java.time.LocalDateTime;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        String email,
        String role,
        String status,
        String invitedByName,
        String token,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static InvitationResponse from(Invitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus().name(),
                invitation.getInvitedBy().getFullName(),
                invitation.getToken(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt()
        );
    }
}

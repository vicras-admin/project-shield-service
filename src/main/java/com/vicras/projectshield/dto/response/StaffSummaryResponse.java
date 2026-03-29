package com.vicras.projectshield.dto.response;

import com.vicras.projectshield.entity.Member;
import com.vicras.projectshield.entity.Seniority;

import java.util.UUID;

public record StaffSummaryResponse(
        UUID id,
        String firstName,
        String lastName,
        String middleInitial,
        String avatar,
        String role,
        Seniority seniority
) {
    public static StaffSummaryResponse from(Member member) {
        return new StaffSummaryResponse(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                member.getMiddleInitial(),
                member.getAvatar(),
                member.getRole().getName(),
                member.getSeniority()
        );
    }
}

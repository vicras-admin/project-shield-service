package com.vicras.projectshield.dto.response;

import com.vicras.projectshield.entity.Member;
import com.vicras.projectshield.entity.Seniority;

import java.util.List;
import java.util.UUID;

public record StaffResponse(
        UUID id,
        String firstName,
        String lastName,
        String middleInitial,
        String avatar,
        String email,
        String phone,
        String role,
        Seniority seniority,
        UUID teamId,
        String teamName,
        Integer hoursPerDay,
        List<DomainResponse> domains,
        List<SkillResponse> skills
) {
    public static StaffResponse from(Member member) {
        return new StaffResponse(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                member.getMiddleInitial(),
                member.getAvatar(),
                member.getEmail(),
                member.getPhone(),
                member.getRole().getName(),
                member.getSeniority(),
                member.getTeam() != null ? member.getTeam().getId() : null,
                member.getTeam() != null ? member.getTeam().getName() : null,
                member.getHoursPerDay(),
                member.getDomains().stream().map(DomainResponse::from).toList(),
                member.getSkills().stream().map(SkillResponse::from).toList()
        );
    }
}

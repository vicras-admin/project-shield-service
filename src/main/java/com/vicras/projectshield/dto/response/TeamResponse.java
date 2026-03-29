package com.vicras.projectshield.dto.response;

import com.vicras.projectshield.entity.Team;

import java.util.List;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        String description,
        List<StaffSummaryResponse> members
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getMembers().stream()
                        .map(StaffSummaryResponse::from)
                        .toList()
        );
    }

    public static TeamResponse fromWithoutMembers(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                List.of()
        );
    }
}

package com.vicras.projectshield.dto.response;

import com.vicras.projectshield.entity.Phase;
import com.vicras.projectshield.entity.PhaseType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PhaseResponse(
        UUID id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        PhaseType type,
        List<ProjectResponse> projects
) {
    public static PhaseResponse from(Phase phase) {
        return new PhaseResponse(
                phase.getId(),
                phase.getName(),
                phase.getDescription(),
                phase.getStartDate(),
                phase.getEndDate(),
                phase.getType(),
                phase.getProjects().stream()
                        .map(ProjectResponse::from)
                        .toList()
        );
    }

    public static PhaseResponse fromWithoutProjects(Phase phase) {
        return new PhaseResponse(
                phase.getId(),
                phase.getName(),
                phase.getDescription(),
                phase.getStartDate(),
                phase.getEndDate(),
                phase.getType(),
                List.of()
        );
    }
}

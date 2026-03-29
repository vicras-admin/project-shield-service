package com.vicras.projectshield.dto.response;

import java.util.List;
import java.util.UUID;

public record GapAnalysisResponse(
        UUID phaseId,
        List<RoleGap> gaps,
        Integer totalGapCount
) {
    public record RoleGap(
            String role,
            Integer required,
            Integer available,
            Integer gap,
            List<ProjectNeed> projectsNeedingRole
    ) {
    }

    public record ProjectNeed(
            UUID projectId,
            String projectName,
            Integer countNeeded
    ) {
    }
}

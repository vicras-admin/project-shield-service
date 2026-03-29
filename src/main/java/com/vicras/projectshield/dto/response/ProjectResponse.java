package com.vicras.projectshield.dto.response;

import com.vicras.projectshield.dto.ProjectRatingsDto;
import com.vicras.projectshield.dto.StaffingNeedDto;
import com.vicras.projectshield.entity.Project;
import com.vicras.projectshield.entity.ProjectStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID phaseId,
        String name,
        String description,
        String justification,
        String sponsor,
        BigDecimal estimatedBudget,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal overallScore,
        Integer stackRank,
        ProjectStatus status,
        ProjectRatingsDto ratings,
        List<StaffingNeedDto> staffingNeeds
) {
    public static ProjectResponse from(Project project) {
        ProjectRatingsDto ratingsDto = null;
        if (project.getRatings() != null) {
            ratingsDto = new ProjectRatingsDto(
                    project.getRatings().getStrategicAlignment(),
                    project.getRatings().getFinancialBenefit(),
                    project.getRatings().getRiskProfile(),
                    project.getRatings().getFeasibility(),
                    project.getRatings().getRegulatoryCompliance()
            );
        }

        List<StaffingNeedDto> staffingNeedDtos = project.getStaffingNeeds().stream()
                .map(sn -> new StaffingNeedDto(
                        sn.getId(),
                        sn.getRole(),
                        sn.getCount(),
                        sn.getDurationWeeks()
                ))
                .toList();

        return new ProjectResponse(
                project.getId(),
                project.getPhase().getId(),
                project.getName(),
                project.getDescription(),
                project.getJustification(),
                project.getSponsor(),
                project.getEstimatedBudget(),
                project.getStartDate(),
                project.getEndDate(),
                project.getOverallScore(),
                project.getStackRank(),
                project.getStatus(),
                ratingsDto,
                staffingNeedDtos
        );
    }
}

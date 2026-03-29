package com.vicras.projectshield.dto.request;

import com.vicras.projectshield.dto.ProjectRatingsDto;
import com.vicras.projectshield.dto.StaffingNeedDto;
import com.vicras.projectshield.entity.ProjectStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProjectRequest(
        @NotBlank(message = "Name is required")
        String name,

        String description,

        String justification,

        String sponsor,

        BigDecimal estimatedBudget,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        BigDecimal overallScore,

        @Min(value = 1, message = "Stack rank must be positive")
        Integer stackRank,

        ProjectStatus status,

        @Valid
        ProjectRatingsDto ratings,

        @Valid
        List<StaffingNeedDto> staffingNeeds
) {
}

package com.vicras.projectshield.dto.request;

import com.vicras.projectshield.entity.PhaseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PhaseRequest(
        @NotBlank(message = "Name is required")
        String name,

        String description,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotNull(message = "Type is required")
        PhaseType type
) {
}

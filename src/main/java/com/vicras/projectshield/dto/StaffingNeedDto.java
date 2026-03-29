package com.vicras.projectshield.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StaffingNeedDto(
        UUID id,

        @NotBlank(message = "Role is required")
        String role,

        @NotNull(message = "Count is required")
        @Min(value = 1, message = "Count must be at least 1")
        Integer count,

        @NotNull(message = "Duration weeks is required")
        @Min(value = 1, message = "Duration weeks must be at least 1")
        Integer durationWeeks
) {
}

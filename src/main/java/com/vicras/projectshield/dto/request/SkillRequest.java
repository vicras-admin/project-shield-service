package com.vicras.projectshield.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SkillRequest(
        @NotBlank(message = "Name is required")
        String name
) {
}

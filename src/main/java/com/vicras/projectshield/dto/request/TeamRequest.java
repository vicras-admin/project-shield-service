package com.vicras.projectshield.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TeamRequest(
        @NotBlank(message = "Name is required")
        String name,

        String description
) {
}

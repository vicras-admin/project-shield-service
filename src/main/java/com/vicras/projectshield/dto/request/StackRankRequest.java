package com.vicras.projectshield.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StackRankRequest(
        @NotNull(message = "Stack rank is required")
        @Min(value = 1, message = "Stack rank must be positive")
        Integer stackRank
) {
}

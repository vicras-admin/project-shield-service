package com.vicras.projectshield.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ProjectRatingsDto(
        @Min(value = 1, message = "Strategic alignment must be between 1 and 5")
        @Max(value = 5, message = "Strategic alignment must be between 1 and 5")
        Integer strategicAlignment,

        @Min(value = 1, message = "Financial benefit must be between 1 and 5")
        @Max(value = 5, message = "Financial benefit must be between 1 and 5")
        Integer financialBenefit,

        @Min(value = 1, message = "Risk profile must be between 1 and 5")
        @Max(value = 5, message = "Risk profile must be between 1 and 5")
        Integer riskProfile,

        @Min(value = 1, message = "Feasibility must be between 1 and 5")
        @Max(value = 5, message = "Feasibility must be between 1 and 5")
        Integer feasibility,

        @Min(value = 1, message = "Regulatory compliance must be between 1 and 5")
        @Max(value = 5, message = "Regulatory compliance must be between 1 and 5")
        Integer regulatoryCompliance
) {
}

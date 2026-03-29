package com.vicras.projectshield.dto.request;

import com.vicras.projectshield.entity.Seniority;
import jakarta.validation.constraints.*;

import java.util.Set;
import java.util.UUID;

public record StaffRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String middleInitial,

        String avatar,

        @Email(message = "Invalid email format")
        String email,

        String phone,

        @NotBlank(message = "Role is required")
        String role,

        @NotNull(message = "Seniority is required")
        Seniority seniority,

        UUID teamId,

        @Min(value = 1, message = "Hours per day must be between 1 and 8")
        @Max(value = 8, message = "Hours per day must be between 1 and 8")
        Integer hoursPerDay,

        Set<UUID> domainIds,

        Set<UUID> skillIds
) {
}

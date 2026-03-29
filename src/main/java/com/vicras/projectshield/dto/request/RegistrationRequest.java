package com.vicras.projectshield.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank(message = "Organization name is required")
        @Size(max = 255, message = "Organization name must be at most 255 characters")
        String organizationName,

        @NotBlank(message = "Organization slug is required")
        @Size(max = 100, message = "Organization slug must be at most 100 characters")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens")
        String organizationSlug,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 255, message = "First name must be at most 255 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 255, message = "Last name must be at most 255 characters")
        String lastName
) {}

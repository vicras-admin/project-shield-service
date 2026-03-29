package com.vicras.projectshield.controller;

import com.vicras.projectshield.dto.request.RegistrationRequest;
import com.vicras.projectshield.dto.response.RegistrationResponse;
import com.vicras.projectshield.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/register")
@Tag(name = "Registration", description = "User and organization registration endpoints")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    @Operation(
            summary = "Register user and organization",
            description = "Creates a new user and organization in Clerk, then saves the organization to the local database"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Validation error or slug already exists"),
            @ApiResponse(responseCode = "502", description = "Clerk API error")
    })
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

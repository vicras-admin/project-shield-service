package com.vicras.projectshield.controller;

import com.vicras.projectshield.dto.request.PhaseRequest;
import com.vicras.projectshield.dto.response.PhaseResponse;
import com.vicras.projectshield.service.PhaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.vicras.projectshield.security.Roles.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/phases")
@Tag(name = "Phases", description = "Planning phase management endpoints")
public class PhaseController {

    private static final Logger log = LoggerFactory.getLogger(PhaseController.class);

    private final PhaseService phaseService;

    public PhaseController(PhaseService phaseService) {
        this.phaseService = phaseService;
    }

    @GetMapping
    @PreAuthorize(CAN_VIEW_PHASES)
    @Operation(summary = "Get all phases", description = "Retrieves a list of all planning phases")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved phases")
    public ResponseEntity<List<PhaseResponse>> getAllPhases() {
        log.debug("GET /api/phases - fetching all phases");
        List<PhaseResponse> phases = phaseService.getAllPhases();
        log.debug("GET /api/phases - returning {} phases", phases.size());
        return ResponseEntity.ok(phases);
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_VIEW_PHASES)
    @Operation(summary = "Get phase by ID", description = "Retrieves a specific phase with its projects")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved phase"),
            @ApiResponse(responseCode = "404", description = "Phase not found")
    })
    public ResponseEntity<PhaseResponse> getPhaseById(
            @Parameter(description = "Phase ID") @PathVariable UUID id) {
        log.debug("GET /api/phases/{} - fetching phase", id);
        PhaseResponse phase = phaseService.getPhaseById(id);
        log.debug("GET /api/phases/{} - returning phase '{}'", id, phase.name());
        return ResponseEntity.ok(phase);
    }

    @PostMapping
    @PreAuthorize(CAN_MANAGE_PHASES)
    @Operation(summary = "Create phase", description = "Creates a new planning phase")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Phase created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<PhaseResponse> createPhase(@Valid @RequestBody PhaseRequest request) {
        PhaseResponse created = phaseService.createPhase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_PHASES)
    @Operation(summary = "Update phase", description = "Updates an existing planning phase")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Phase updated successfully"),
            @ApiResponse(responseCode = "404", description = "Phase not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<PhaseResponse> updatePhase(
            @Parameter(description = "Phase ID") @PathVariable UUID id,
            @Valid @RequestBody PhaseRequest request) {
        return ResponseEntity.ok(phaseService.updatePhase(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_PHASES)
    @Operation(summary = "Delete phase", description = "Deletes a planning phase and all its projects")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Phase deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Phase not found")
    })
    public ResponseEntity<Void> deletePhase(
            @Parameter(description = "Phase ID") @PathVariable UUID id) {
        phaseService.deletePhase(id);
        return ResponseEntity.noContent().build();
    }
}

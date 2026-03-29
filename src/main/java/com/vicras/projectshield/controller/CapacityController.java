package com.vicras.projectshield.controller;

import com.vicras.projectshield.dto.response.CapacityAllocationResponse;
import com.vicras.projectshield.dto.response.GapAnalysisResponse;
import com.vicras.projectshield.service.CapacityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.vicras.projectshield.security.Roles.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/capacity")
@Tag(name = "Capacity Planning", description = "Capacity allocation and gap analysis endpoints")
public class CapacityController {

    private final CapacityService capacityService;

    public CapacityController(CapacityService capacityService) {
        this.capacityService = capacityService;
    }

    @GetMapping("/strategic")
    @PreAuthorize(CAN_VIEW_CAPACITY)
    @Operation(summary = "Calculate capacity allocation",
            description = "Calculates staff allocation to projects based on priority and role matching")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully calculated capacity allocation"),
            @ApiResponse(responseCode = "404", description = "Phase not found")
    })
    public ResponseEntity<CapacityAllocationResponse> getCapacityAllocation(
            @Parameter(description = "Phase ID to calculate allocation for") @RequestParam UUID phaseId) {
        return ResponseEntity.ok(capacityService.calculateCapacityAllocation(phaseId));
    }

    @GetMapping("/gaps")
    @PreAuthorize(CAN_VIEW_CAPACITY)
    @Operation(summary = "Calculate staffing gaps",
            description = "Analyzes staffing gaps between required and available resources by role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully calculated staffing gaps"),
            @ApiResponse(responseCode = "404", description = "Phase not found")
    })
    public ResponseEntity<GapAnalysisResponse> getGapAnalysis(
            @Parameter(description = "Phase ID to analyze gaps for") @RequestParam UUID phaseId) {
        return ResponseEntity.ok(capacityService.calculateGaps(phaseId));
    }
}

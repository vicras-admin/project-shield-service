package com.vicras.projectshield.controller;

import com.vicras.projectshield.dto.request.StaffRequest;
import com.vicras.projectshield.dto.response.StaffResponse;
import com.vicras.projectshield.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.vicras.projectshield.security.Roles.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff")
@Tag(name = "Staff", description = "Staff member management endpoints")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    @PreAuthorize(CAN_VIEW_STAFF)
    @Operation(summary = "Get all staff", description = "Retrieves a list of all staff members")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved staff members")
    public ResponseEntity<List<StaffResponse>> getAllStaff() {
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_VIEW_STAFF)
    @Operation(summary = "Get staff by ID", description = "Retrieves a specific staff member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved staff member"),
            @ApiResponse(responseCode = "404", description = "Staff member not found")
    })
    public ResponseEntity<StaffResponse> getStaffById(
            @Parameter(description = "Staff member ID") @PathVariable UUID id) {
        return ResponseEntity.ok(staffService.getStaffById(id));
    }

    @PostMapping
    @PreAuthorize(CAN_MANAGE_STAFF)
    @Operation(summary = "Create staff member", description = "Creates a new staff member")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Staff member created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<StaffResponse> createStaff(@Valid @RequestBody StaffRequest request) {
        StaffResponse created = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_STAFF)
    @Operation(summary = "Update staff member", description = "Updates an existing staff member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff member updated successfully"),
            @ApiResponse(responseCode = "404", description = "Staff member not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<StaffResponse> updateStaff(
            @Parameter(description = "Staff member ID") @PathVariable UUID id,
            @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(staffService.updateStaff(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_STAFF)
    @Operation(summary = "Delete staff member", description = "Deletes a staff member")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Staff member deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Staff member not found")
    })
    public ResponseEntity<Void> deleteStaff(
            @Parameter(description = "Staff member ID") @PathVariable UUID id) {
        staffService.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }
}

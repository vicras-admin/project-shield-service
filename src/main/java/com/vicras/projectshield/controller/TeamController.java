package com.vicras.projectshield.controller;

import com.vicras.projectshield.dto.request.TeamRequest;
import com.vicras.projectshield.dto.response.TeamResponse;
import com.vicras.projectshield.service.TeamService;
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
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Team management endpoints")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    @PreAuthorize(CAN_VIEW_TEAMS)
    @Operation(summary = "Get all teams", description = "Retrieves a list of all teams with their members")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved teams")
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_VIEW_TEAMS)
    @Operation(summary = "Get team by ID", description = "Retrieves a specific team with its members")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved team"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<TeamResponse> getTeamById(
            @Parameter(description = "Team ID") @PathVariable UUID id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @PostMapping
    @PreAuthorize(CAN_MANAGE_TEAMS)
    @Operation(summary = "Create team", description = "Creates a new team")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Team created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest request) {
        TeamResponse created = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_TEAMS)
    @Operation(summary = "Update team", description = "Updates an existing team")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Team updated successfully"),
            @ApiResponse(responseCode = "404", description = "Team not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<TeamResponse> updateTeam(
            @Parameter(description = "Team ID") @PathVariable UUID id,
            @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(teamService.updateTeam(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_TEAMS)
    @Operation(summary = "Delete team", description = "Deletes a team (members are unassigned, not deleted)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Team deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Team not found")
    })
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "Team ID") @PathVariable UUID id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
}

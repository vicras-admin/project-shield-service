package com.vicras.projectshield.controller;

import com.vicras.projectshield.dto.request.StackRankRequest;
import com.vicras.projectshield.dto.request.ProjectRequest;
import com.vicras.projectshield.dto.response.ProjectResponse;
import com.vicras.projectshield.service.ProjectService;
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
@RequestMapping("/api")
@Tag(name = "Projects", description = "Project management endpoints")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/phases/{phaseId}/projects")
    @PreAuthorize(CAN_VIEW_PROJECTS)
    @Operation(summary = "Get projects by phase", description = "Retrieves all projects in a specific phase")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved projects"),
            @ApiResponse(responseCode = "404", description = "Phase not found")
    })
    public ResponseEntity<List<ProjectResponse>> getProjectsByPhase(
            @Parameter(description = "Phase ID") @PathVariable UUID phaseId) {
        return ResponseEntity.ok(projectService.getProjectsByPhase(phaseId));
    }

    @GetMapping("/projects/{id}")
    @PreAuthorize(CAN_VIEW_PROJECTS)
    @Operation(summary = "Get project by ID", description = "Retrieves a specific project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved project"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ProjectResponse> getProjectById(
            @Parameter(description = "Project ID") @PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping("/phases/{phaseId}/projects")
    @PreAuthorize(CAN_MANAGE_PROJECTS)
    @Operation(summary = "Create project", description = "Creates a new project in a phase")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or dates outside phase bounds"),
            @ApiResponse(responseCode = "404", description = "Phase not found")
    })
    public ResponseEntity<ProjectResponse> createProject(
            @Parameter(description = "Phase ID") @PathVariable UUID phaseId,
            @Valid @RequestBody ProjectRequest request) {
        ProjectResponse created = projectService.createProject(phaseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/projects/{id}")
    @PreAuthorize(CAN_MANAGE_PROJECTS)
    @Operation(summary = "Update project", description = "Updates an existing project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or dates outside phase bounds")
    })
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "Project ID") @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @PutMapping("/projects/{id}/stack-rank")
    @PreAuthorize(CAN_MANAGE_PROJECTS)
    @Operation(summary = "Update stack rank", description = "Updates the priority/stack rank of a project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stack rank updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid stack rank value")
    })
    public ResponseEntity<ProjectResponse> updateStackRank(
            @Parameter(description = "Project ID") @PathVariable UUID id,
            @Valid @RequestBody StackRankRequest request) {
        return ResponseEntity.ok(projectService.updateStackRank(id, request.stackRank()));
    }

    @DeleteMapping("/projects/{id}")
    @PreAuthorize(CAN_MANAGE_PROJECTS)
    @Operation(summary = "Delete project", description = "Deletes a project")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project ID") @PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}

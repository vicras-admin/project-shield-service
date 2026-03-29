package com.vicras.projectshield.controller;

import com.vicras.projectshield.dto.request.SkillRequest;
import com.vicras.projectshield.dto.response.SkillResponse;
import com.vicras.projectshield.service.SkillService;
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
@RequestMapping("/api/skills")
@Tag(name = "Skills", description = "Skill management endpoints")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    @PreAuthorize(CAN_VIEW_SKILLS)
    @Operation(summary = "Get all skills", description = "Retrieves a list of all skills")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved skills")
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_VIEW_SKILLS)
    @Operation(summary = "Get skill by ID", description = "Retrieves a specific skill")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved skill"),
            @ApiResponse(responseCode = "404", description = "Skill not found")
    })
    public ResponseEntity<SkillResponse> getSkillById(
            @Parameter(description = "Skill ID") @PathVariable UUID id) {
        return ResponseEntity.ok(skillService.getSkillById(id));
    }

    @PostMapping
    @PreAuthorize(CAN_MANAGE_SKILLS)
    @Operation(summary = "Create skill", description = "Creates a new skill")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Skill created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<SkillResponse> createSkill(@Valid @RequestBody SkillRequest request) {
        SkillResponse created = skillService.createSkill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_SKILLS)
    @Operation(summary = "Update skill", description = "Updates an existing skill")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skill updated successfully"),
            @ApiResponse(responseCode = "404", description = "Skill not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<SkillResponse> updateSkill(
            @Parameter(description = "Skill ID") @PathVariable UUID id,
            @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(skillService.updateSkill(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_SKILLS)
    @Operation(summary = "Delete skill", description = "Deletes a skill")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Skill deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Skill not found")
    })
    public ResponseEntity<Void> deleteSkill(
            @Parameter(description = "Skill ID") @PathVariable UUID id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }
}

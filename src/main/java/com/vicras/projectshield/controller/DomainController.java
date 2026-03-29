package com.vicras.projectshield.controller;

import com.vicras.projectshield.dto.request.DomainRequest;
import com.vicras.projectshield.dto.response.DomainResponse;
import com.vicras.projectshield.service.DomainService;
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
@RequestMapping("/api/domains")
@Tag(name = "Domains", description = "Domain management endpoints")
public class DomainController {

    private final DomainService domainService;

    public DomainController(DomainService domainService) {
        this.domainService = domainService;
    }

    @GetMapping
    @PreAuthorize(CAN_VIEW_DOMAINS)
    @Operation(summary = "Get all domains", description = "Retrieves a list of all domains")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved domains")
    public ResponseEntity<List<DomainResponse>> getAllDomains() {
        return ResponseEntity.ok(domainService.getAllDomains());
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_VIEW_DOMAINS)
    @Operation(summary = "Get domain by ID", description = "Retrieves a specific domain")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved domain"),
            @ApiResponse(responseCode = "404", description = "Domain not found")
    })
    public ResponseEntity<DomainResponse> getDomainById(
            @Parameter(description = "Domain ID") @PathVariable UUID id) {
        return ResponseEntity.ok(domainService.getDomainById(id));
    }

    @PostMapping
    @PreAuthorize(CAN_MANAGE_DOMAINS)
    @Operation(summary = "Create domain", description = "Creates a new domain")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Domain created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<DomainResponse> createDomain(@Valid @RequestBody DomainRequest request) {
        DomainResponse created = domainService.createDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_DOMAINS)
    @Operation(summary = "Update domain", description = "Updates an existing domain")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Domain updated successfully"),
            @ApiResponse(responseCode = "404", description = "Domain not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<DomainResponse> updateDomain(
            @Parameter(description = "Domain ID") @PathVariable UUID id,
            @Valid @RequestBody DomainRequest request) {
        return ResponseEntity.ok(domainService.updateDomain(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_DOMAINS)
    @Operation(summary = "Delete domain", description = "Deletes a domain")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Domain deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Domain not found")
    })
    public ResponseEntity<Void> deleteDomain(
            @Parameter(description = "Domain ID") @PathVariable UUID id) {
        domainService.deleteDomain(id);
        return ResponseEntity.noContent().build();
    }
}

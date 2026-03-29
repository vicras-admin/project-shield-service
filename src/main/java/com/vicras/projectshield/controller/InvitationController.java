package com.vicras.projectshield.controller;

import com.vicras.projectshield.dto.request.AcceptInvitationRequest;
import com.vicras.projectshield.dto.request.BulkInviteRequest;
import com.vicras.projectshield.dto.response.InvitationResponse;
import com.vicras.projectshield.dto.response.InviteValidationResponse;
import com.vicras.projectshield.service.InvitationService;
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
@RequestMapping("/api/invitations")
@Tag(name = "Invitations", description = "Member invitation management endpoints")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @PreAuthorize(CAN_MANAGE_INVITATIONS)
    @Operation(summary = "Send invitations", description = "Send one or more invitations to join the organization")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invitations sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<List<InvitationResponse>> createInvitations(
            @Valid @RequestBody BulkInviteRequest request) {
        List<InvitationResponse> responses = invitationService.createInvitations(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping
    @PreAuthorize(CAN_MANAGE_INVITATIONS)
    @Operation(summary = "Get all invitations", description = "Retrieves all invitations for the current organization")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved invitations")
    public ResponseEntity<List<InvitationResponse>> getInvitations() {
        return ResponseEntity.ok(invitationService.getInvitations());
    }

    @PutMapping("/{id}/revoke")
    @PreAuthorize(CAN_MANAGE_INVITATIONS)
    @Operation(summary = "Revoke invitation", description = "Revokes a pending invitation")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Invitation revoked successfully"),
            @ApiResponse(responseCode = "404", description = "Invitation not found"),
            @ApiResponse(responseCode = "400", description = "Invitation cannot be revoked")
    })
    public ResponseEntity<Void> revokeInvitation(
            @Parameter(description = "Invitation ID") @PathVariable UUID id) {
        invitationService.revokeInvitation(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/resend")
    @PreAuthorize(CAN_MANAGE_INVITATIONS)
    @Operation(summary = "Resend invitation", description = "Resends a pending invitation with a refreshed token and expiry")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation resent successfully"),
            @ApiResponse(responseCode = "404", description = "Invitation not found"),
            @ApiResponse(responseCode = "400", description = "Invitation cannot be resent")
    })
    public ResponseEntity<InvitationResponse> resendInvitation(
            @Parameter(description = "Invitation ID") @PathVariable UUID id) {
        return ResponseEntity.ok(invitationService.resendInvitation(id));
    }

    @GetMapping("/{token}/validate")
    @Operation(summary = "Validate invitation token", description = "Validates an invitation token (public endpoint)")
    @ApiResponse(responseCode = "200", description = "Validation result")
    public ResponseEntity<InviteValidationResponse> validateInvitation(
            @Parameter(description = "Invitation token") @PathVariable String token) {
        return ResponseEntity.ok(invitationService.validateInvitation(token));
    }

    @PostMapping("/accept")
    @Operation(summary = "Accept invitation", description = "Accepts an invitation and creates a new user account (public endpoint)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation accepted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired invitation")
    })
    public ResponseEntity<Void> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        invitationService.acceptInvitation(request);
        return ResponseEntity.ok().build();
    }
}

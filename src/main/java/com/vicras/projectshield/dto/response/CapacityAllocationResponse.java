package com.vicras.projectshield.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CapacityAllocationResponse(
        UUID phaseId,
        List<ProjectAllocation> allocations,
        Map<String, Integer> totalCapacityByRole,
        Map<String, Integer> allocatedCapacityByRole
) {
    public record ProjectAllocation(
            UUID projectId,
            String projectName,
            Integer stackRank,
            List<RoleAllocation> roleAllocations
    ) {
    }

    public record RoleAllocation(
            String role,
            Integer required,
            Integer assigned,
            List<StaffAssignment> assignments
    ) {
    }

    public record StaffAssignment(
            UUID staffId,
            String staffName,
            Integer hoursPerDay
    ) {
    }
}

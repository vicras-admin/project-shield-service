package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.response.CapacityAllocationResponse;
import com.vicras.projectshield.dto.response.CapacityAllocationResponse.ProjectAllocation;
import com.vicras.projectshield.dto.response.CapacityAllocationResponse.RoleAllocation;
import com.vicras.projectshield.dto.response.CapacityAllocationResponse.StaffAssignment;
import com.vicras.projectshield.dto.response.GapAnalysisResponse;
import com.vicras.projectshield.dto.response.GapAnalysisResponse.ProjectNeed;
import com.vicras.projectshield.dto.response.GapAnalysisResponse.RoleGap;
import com.vicras.projectshield.entity.Member;
import com.vicras.projectshield.entity.StaffingNeed;
import com.vicras.projectshield.entity.Project;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.MemberRepository;
import com.vicras.projectshield.repository.PhaseRepository;
import com.vicras.projectshield.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CapacityService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final PhaseRepository phaseRepository;

    public CapacityService(ProjectRepository projectRepository,
                          MemberRepository memberRepository,
                          PhaseRepository phaseRepository) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.phaseRepository = phaseRepository;
    }

    public CapacityAllocationResponse calculateCapacityAllocation(UUID phaseId) {
        if (!phaseRepository.existsById(phaseId)) {
            throw new ResourceNotFoundException("Phase", phaseId);
        }

        List<Project> projects = projectRepository.findByPhaseIdOrderByOverallScoreDesc(phaseId);
        List<Member> allMembers = memberRepository.findAll();

        Map<String, List<Member>> membersByRole = allMembers.stream()
                .collect(Collectors.groupingBy(m -> m.getRole().getName()));

        Map<UUID, Boolean> memberAssigned = new HashMap<>();
        List<ProjectAllocation> allocations = new ArrayList<>();

        Map<String, Integer> totalCapacityByRole = new HashMap<>();
        Map<String, Integer> allocatedCapacityByRole = new HashMap<>();

        for (Member member : allMembers) {
            totalCapacityByRole.merge(member.getRole().getName(), member.getHoursPerDay(), Integer::sum);
        }

        for (Project project : projects) {
            List<RoleAllocation> roleAllocations = new ArrayList<>();

            for (StaffingNeed need : project.getStaffingNeeds()) {
                List<Member> availableMembers = membersByRole.getOrDefault(need.getRole(), List.of())
                        .stream()
                        .filter(m -> !memberAssigned.getOrDefault(m.getId(), false))
                        .toList();

                List<StaffAssignment> assignments = new ArrayList<>();
                int assigned = 0;

                for (Member member : availableMembers) {
                    if (assigned >= need.getCount()) {
                        break;
                    }
                    memberAssigned.put(member.getId(), true);
                    assignments.add(new StaffAssignment(
                            member.getId(),
                            member.getFullName(),
                            member.getHoursPerDay()
                    ));
                    allocatedCapacityByRole.merge(member.getRole().getName(), member.getHoursPerDay(), Integer::sum);
                    assigned++;
                }

                roleAllocations.add(new RoleAllocation(
                        need.getRole(),
                        need.getCount(),
                        assigned,
                        assignments
                ));
            }

            allocations.add(new ProjectAllocation(
                    project.getId(),
                    project.getName(),
                    project.getStackRank(),
                    roleAllocations
            ));
        }

        return new CapacityAllocationResponse(
                phaseId,
                allocations,
                totalCapacityByRole,
                allocatedCapacityByRole
        );
    }

    public GapAnalysisResponse calculateGaps(UUID phaseId) {
        if (!phaseRepository.existsById(phaseId)) {
            throw new ResourceNotFoundException("Phase", phaseId);
        }

        List<Project> projects = projectRepository.findByPhaseIdOrderByOverallScoreDesc(phaseId);
        List<Member> allMembers = memberRepository.findAll();

        Map<String, Integer> availableByRole = allMembers.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getRole().getName(),
                        Collectors.summingInt(m -> 1)
                ));

        Map<String, Integer> requiredByRole = new HashMap<>();
        Map<String, List<ProjectNeed>> projectNeedsByRole = new HashMap<>();

        for (Project project : projects) {
            for (StaffingNeed need : project.getStaffingNeeds()) {
                requiredByRole.merge(need.getRole(), need.getCount(), Integer::sum);

                projectNeedsByRole.computeIfAbsent(need.getRole(), k -> new ArrayList<>())
                        .add(new ProjectNeed(
                                project.getId(),
                                project.getName(),
                                need.getCount()
                        ));
            }
        }

        Set<String> allRoles = new HashSet<>();
        allRoles.addAll(availableByRole.keySet());
        allRoles.addAll(requiredByRole.keySet());

        List<RoleGap> gaps = new ArrayList<>();
        int totalGapCount = 0;

        for (String role : allRoles) {
            int required = requiredByRole.getOrDefault(role, 0);
            int available = availableByRole.getOrDefault(role, 0);
            int gap = required - available;

            if (gap > 0) {
                totalGapCount += gap;
                gaps.add(new RoleGap(
                        role,
                        required,
                        available,
                        gap,
                        projectNeedsByRole.getOrDefault(role, List.of())
                ));
            }
        }

        gaps.sort((a, b) -> Integer.compare(b.gap(), a.gap()));

        return new GapAnalysisResponse(phaseId, gaps, totalGapCount);
    }
}

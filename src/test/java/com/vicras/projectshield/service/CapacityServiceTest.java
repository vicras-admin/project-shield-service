package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.response.CapacityAllocationResponse;
import com.vicras.projectshield.dto.response.GapAnalysisResponse;
import com.vicras.projectshield.entity.*;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.MemberRepository;
import com.vicras.projectshield.repository.PhaseRepository;
import com.vicras.projectshield.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapacityServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PhaseRepository phaseRepository;

    @InjectMocks
    private CapacityService capacityService;

    private UUID phaseId;
    private Phase phase;

    @BeforeEach
    void setUp() {
        phaseId = UUID.randomUUID();
        phase = new Phase();
        phase.setId(phaseId);
        phase.setName("Q1 2025");
        phase.setStartDate(LocalDate.of(2025, 1, 1));
        phase.setEndDate(LocalDate.of(2025, 3, 31));
        phase.setType(PhaseType.QUARTER);
    }

    @Test
    void calculateCapacityAllocation_withNonExistentPhase_throwsException() {
        when(phaseRepository.existsById(phaseId)).thenReturn(false);

        assertThatThrownBy(() -> capacityService.calculateCapacityAllocation(phaseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Phase");
    }

    @Test
    void calculateCapacityAllocation_withNoProjects_returnsEmptyAllocations() {
        when(phaseRepository.existsById(phaseId)).thenReturn(true);
        when(projectRepository.findByPhaseIdOrderByOverallScoreDesc(phaseId)).thenReturn(List.of());
        when(memberRepository.findAll()).thenReturn(List.of());

        CapacityAllocationResponse result = capacityService.calculateCapacityAllocation(phaseId);

        assertThat(result.phaseId()).isEqualTo(phaseId);
        assertThat(result.allocations()).isEmpty();
        assertThat(result.totalCapacityByRole()).isEmpty();
    }

    @Test
    void calculateCapacityAllocation_assignsStaffByPriority() {
        when(phaseRepository.existsById(phaseId)).thenReturn(true);

        Project project1 = createProject("High Priority", new BigDecimal("4.5"));
        StaffingNeed need1 = createStaffingNeed(project1, "Backend Developer", 2);
        project1.getStaffingNeeds().add(need1);

        Project project2 = createProject("Low Priority", new BigDecimal("3.0"));
        StaffingNeed need2 = createStaffingNeed(project2, "Backend Developer", 1);
        project2.getStaffingNeeds().add(need2);

        when(projectRepository.findByPhaseIdOrderByOverallScoreDesc(phaseId))
                .thenReturn(List.of(project1, project2));

        Member dev1 = createMember("Dev 1", "Backend Developer");
        Member dev2 = createMember("Dev 2", "Backend Developer");

        when(memberRepository.findAll()).thenReturn(List.of(dev1, dev2));

        CapacityAllocationResponse result = capacityService.calculateCapacityAllocation(phaseId);

        assertThat(result.allocations()).hasSize(2);

        var highPriorityAllocation = result.allocations().get(0);
        assertThat(highPriorityAllocation.projectName()).isEqualTo("High Priority");
        assertThat(highPriorityAllocation.roleAllocations().get(0).assigned()).isEqualTo(2);

        var lowPriorityAllocation = result.allocations().get(1);
        assertThat(lowPriorityAllocation.projectName()).isEqualTo("Low Priority");
        assertThat(lowPriorityAllocation.roleAllocations().get(0).assigned()).isEqualTo(0);
    }

    @Test
    void calculateGaps_withNonExistentPhase_throwsException() {
        when(phaseRepository.existsById(phaseId)).thenReturn(false);

        assertThatThrownBy(() -> capacityService.calculateGaps(phaseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Phase");
    }

    @Test
    void calculateGaps_computesCorrectGaps() {
        when(phaseRepository.existsById(phaseId)).thenReturn(true);

        Project project = createProject("Project A", new BigDecimal("4.0"));
        StaffingNeed need = createStaffingNeed(project, "Frontend Developer", 3);
        project.getStaffingNeeds().add(need);

        when(projectRepository.findByPhaseIdOrderByOverallScoreDesc(phaseId))
                .thenReturn(List.of(project));

        Member dev = createMember("Dev 1", "Frontend Developer");
        when(memberRepository.findAll()).thenReturn(List.of(dev));

        GapAnalysisResponse result = capacityService.calculateGaps(phaseId);

        assertThat(result.phaseId()).isEqualTo(phaseId);
        assertThat(result.totalGapCount()).isEqualTo(2);
        assertThat(result.gaps()).hasSize(1);

        var gap = result.gaps().get(0);
        assertThat(gap.role()).isEqualTo("Frontend Developer");
        assertThat(gap.required()).isEqualTo(3);
        assertThat(gap.available()).isEqualTo(1);
        assertThat(gap.gap()).isEqualTo(2);
    }

    @Test
    void calculateGaps_withSufficientStaff_returnsNoGaps() {
        when(phaseRepository.existsById(phaseId)).thenReturn(true);

        Project project = createProject("Project A", new BigDecimal("4.0"));
        StaffingNeed need = createStaffingNeed(project, "Backend Developer", 1);
        project.getStaffingNeeds().add(need);

        when(projectRepository.findByPhaseIdOrderByOverallScoreDesc(phaseId))
                .thenReturn(List.of(project));

        Member dev1 = createMember("Dev 1", "Backend Developer");
        Member dev2 = createMember("Dev 2", "Backend Developer");
        when(memberRepository.findAll()).thenReturn(List.of(dev1, dev2));

        GapAnalysisResponse result = capacityService.calculateGaps(phaseId);

        assertThat(result.gaps()).isEmpty();
        assertThat(result.totalGapCount()).isEqualTo(0);
    }

    private Project createProject(String name, BigDecimal score) {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setPhase(phase);
        project.setName(name);
        project.setOverallScore(score);
        project.setStartDate(LocalDate.of(2025, 1, 1));
        project.setEndDate(LocalDate.of(2025, 3, 31));
        project.setStatus(ProjectStatus.accepted);
        return project;
    }

    private StaffingNeed createStaffingNeed(Project project, String role, int count) {
        StaffingNeed need = new StaffingNeed();
        need.setId(UUID.randomUUID());
        need.setProject(project);
        need.setRole(role);
        need.setCount(count);
        need.setDurationWeeks(12);
        return need;
    }

    private Member createMember(String name, String roleName) {
        RoleEntity role = new RoleEntity();
        role.setId(UUID.randomUUID());
        role.setName(roleName);

        String[] parts = name.split(" ", 2);
        Member member = new Member();
        member.setId(UUID.randomUUID());
        member.setFirstName(parts[0]);
        member.setLastName(parts.length > 1 ? parts[1] : "");
        member.setRole(role);
        member.setSeniority(Seniority.mid);
        member.setHoursPerDay(8);
        return member;
    }
}

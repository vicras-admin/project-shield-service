package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.ProjectRatingsDto;
import com.vicras.projectshield.dto.StaffingNeedDto;
import com.vicras.projectshield.dto.request.ProjectRequest;
import com.vicras.projectshield.dto.response.ProjectResponse;
import com.vicras.projectshield.entity.*;
import com.vicras.projectshield.exception.DateRangeException;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.PhaseRepository;
import com.vicras.projectshield.repository.ProjectRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private PhaseRepository phaseRepository;

    @Mock
    private OrganizationContext organizationContext;

    @InjectMocks
    private ProjectService projectService;

    private Phase phase;
    private Project project;
    private UUID phaseId;
    private UUID projectId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        phaseId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("Test Org");
        organization.setSlug("test-org");

        phase = new Phase();
        phase.setId(phaseId);
        phase.setOrganization(organization);
        phase.setName("Q1 2025");
        phase.setStartDate(LocalDate.of(2025, 1, 1));
        phase.setEndDate(LocalDate.of(2025, 3, 31));
        phase.setType(PhaseType.QUARTER);

        project = new Project();
        project.setId(projectId);
        project.setOrganization(organization);
        project.setPhase(phase);
        project.setName("Project Alpha");
        project.setDescription("Test project");
        project.setStartDate(LocalDate.of(2025, 1, 15));
        project.setEndDate(LocalDate.of(2025, 3, 15));
        project.setStatus(ProjectStatus.ACCEPTED);
        project.setOverallScore(new BigDecimal("4.2"));
        project.setStackRank(1);
    }

    @Test
    void getProjectsByPhase_returnsList() {
        when(phaseRepository.existsById(phaseId)).thenReturn(true);
        when(projectRepository.findByPhaseIdOrderByStackRankAsc(phaseId)).thenReturn(List.of(project));

        List<ProjectResponse> result = projectService.getProjectsByPhase(phaseId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Project Alpha");
    }

    @Test
    void getProjectsByPhase_withInvalidPhaseId_throwsException() {
        when(phaseRepository.existsById(phaseId)).thenReturn(false);

        assertThatThrownBy(() -> projectService.getProjectsByPhase(phaseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Phase");
    }

    @Test
    void getProjectById_withValidId_returnsProject() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        ProjectResponse result = projectService.getProjectById(projectId);

        assertThat(result.id()).isEqualTo(projectId);
        assertThat(result.name()).isEqualTo("Project Alpha");
    }

    @Test
    void getProjectById_withInvalidId_throwsException() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(projectId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createProject_withValidRequest_createsProject() {
        ProjectRequest request = new ProjectRequest(
                "New Project", "Description", "Business need", "John Sponsor",
                new BigDecimal("100000"),
                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 3, 15),
                new BigDecimal("4.0"), 2, ProjectStatus.STRATEGIC,
                new ProjectRatingsDto(4, 5, 3, 4, 5),
                List.of(new StaffingNeedDto(null, "Backend Developer", 2, 8))
        );

        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(phaseRepository.findById(phaseId)).thenReturn(Optional.of(phase));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        ProjectResponse result = projectService.createProject(phaseId, request);

        assertThat(result.name()).isEqualTo("New Project");
        assertThat(result.status()).isEqualTo(ProjectStatus.STRATEGIC);
    }

    @Test
    void createProject_withInvalidPhaseId_throwsException() {
        ProjectRequest request = new ProjectRequest(
                "New Project", null, null, null, null,
                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 3, 15),
                null, null, null, null, null
        );

        when(phaseRepository.findById(phaseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(phaseId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createProject_withDatesOutsidePhase_throwsException() {
        ProjectRequest request = new ProjectRequest(
                "New Project", null, null, null, null,
                LocalDate.of(2024, 12, 1), LocalDate.of(2025, 2, 15),
                null, null, null, null, null
        );

        when(phaseRepository.findById(phaseId)).thenReturn(Optional.of(phase));

        assertThatThrownBy(() -> projectService.createProject(phaseId, request))
                .isInstanceOf(DateRangeException.class)
                .hasMessageContaining("phase dates");
    }

    @Test
    void createProject_withEndBeforeStart_throwsException() {
        ProjectRequest request = new ProjectRequest(
                "New Project", null, null, null, null,
                LocalDate.of(2025, 3, 15), LocalDate.of(2025, 2, 1),
                null, null, null, null, null
        );

        when(phaseRepository.findById(phaseId)).thenReturn(Optional.of(phase));

        assertThatThrownBy(() -> projectService.createProject(phaseId, request))
                .isInstanceOf(DateRangeException.class)
                .hasMessageContaining("end date must be on or after start date");
    }

    @Test
    void updateProject_withValidRequest_updatesProject() {
        ProjectRequest request = new ProjectRequest(
                "Updated Project", "Updated description", null, null, null,
                LocalDate.of(2025, 1, 20), LocalDate.of(2025, 3, 20),
                new BigDecimal("4.5"), 1, ProjectStatus.ACCEPTED,
                null, null
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse result = projectService.updateProject(projectId, request);

        verify(projectRepository).save(project);
        assertThat(project.getName()).isEqualTo("Updated Project");
    }

    @Test
    void updateProject_withInvalidId_throwsException() {
        ProjectRequest request = new ProjectRequest(
                "Updated", null, null, null, null,
                LocalDate.of(2025, 1, 15), LocalDate.of(2025, 3, 15),
                null, null, null, null, null
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(projectId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStackRank_withValidId_updatesRank() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse result = projectService.updateStackRank(projectId, 5);

        assertThat(project.getStackRank()).isEqualTo(5);
        verify(projectRepository).save(project);
    }

    @Test
    void updateStackRank_withInvalidId_throwsException() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateStackRank(projectId, 5))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProject_withValidId_deletesProject() {
        when(projectRepository.existsById(projectId)).thenReturn(true);

        projectService.deleteProject(projectId);

        verify(projectRepository).deleteById(projectId);
    }

    @Test
    void deleteProject_withInvalidId_throwsException() {
        when(projectRepository.existsById(projectId)).thenReturn(false);

        assertThatThrownBy(() -> projectService.deleteProject(projectId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

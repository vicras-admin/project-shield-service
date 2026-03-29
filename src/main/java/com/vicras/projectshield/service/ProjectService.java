package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.StaffingNeedDto;
import com.vicras.projectshield.dto.request.ProjectRequest;
import com.vicras.projectshield.dto.response.ProjectResponse;
import com.vicras.projectshield.entity.*;
import com.vicras.projectshield.exception.DateRangeException;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.PhaseRepository;
import com.vicras.projectshield.repository.ProjectRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PhaseRepository phaseRepository;
    private final OrganizationContext organizationContext;

    public ProjectService(ProjectRepository projectRepository,
                          PhaseRepository phaseRepository,
                          OrganizationContext organizationContext) {
        this.projectRepository = projectRepository;
        this.phaseRepository = phaseRepository;
        this.organizationContext = organizationContext;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByPhase(UUID phaseId) {
        if (!phaseRepository.existsById(phaseId)) {
            throw new ResourceNotFoundException("Phase", phaseId);
        }
        return projectRepository.findByPhaseIdOrderByStackRankAsc(phaseId).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        return ProjectResponse.from(project);
    }

    public ProjectResponse createProject(UUID phaseId, ProjectRequest request) {
        Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Phase", phaseId));

        validateProjectDates(request, phase);

        Project project = new Project();
        project.setOrganization(organizationContext.getCurrentOrganization());
        project.setPhase(phase);
        mapRequestToEntity(request, project);

        Project saved = projectRepository.save(project);
        return ProjectResponse.from(saved);
    }

    public ProjectResponse updateProject(UUID id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        validateProjectDates(request, project.getPhase());
        mapRequestToEntity(request, project);

        Project saved = projectRepository.save(project);
        return ProjectResponse.from(saved);
    }

    public ProjectResponse updateStackRank(UUID id, Integer stackRank) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        project.setStackRank(stackRank);

        Project saved = projectRepository.save(project);
        return ProjectResponse.from(saved);
    }

    public void deleteProject(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", id);
        }
        projectRepository.deleteById(id);
    }

    private void validateProjectDates(ProjectRequest request, Phase phase) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new DateRangeException("Project end date must be on or after start date");
        }

        if (request.startDate().isBefore(phase.getStartDate()) ||
                request.endDate().isAfter(phase.getEndDate())) {
            throw new DateRangeException(
                    String.format("Project dates must fall within phase dates (%s to %s)",
                            phase.getStartDate(), phase.getEndDate())
            );
        }
    }

    private void mapRequestToEntity(ProjectRequest request, Project project) {
        project.setName(request.name());
        project.setDescription(request.description());
        project.setJustification(request.justification());
        project.setSponsor(request.sponsor());
        project.setEstimatedBudget(request.estimatedBudget());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setOverallScore(request.overallScore());
        project.setStackRank(request.stackRank());
        project.setStatus(request.status() != null ? request.status() : ProjectStatus.accepted);

        if (request.ratings() != null) {
            ProjectRatings ratings = project.getRatings();
            if (ratings == null) {
                ratings = new ProjectRatings();
                project.setRatings(ratings);
            }
            ratings.setStrategicAlignment(request.ratings().strategicAlignment());
            ratings.setFinancialBenefit(request.ratings().financialBenefit());
            ratings.setRiskProfile(request.ratings().riskProfile());
            ratings.setFeasibility(request.ratings().feasibility());
            ratings.setRegulatoryCompliance(request.ratings().regulatoryCompliance());
        }

        if (request.staffingNeeds() != null) {
            project.getStaffingNeeds().clear();
            for (StaffingNeedDto needDto : request.staffingNeeds()) {
                StaffingNeed need = new StaffingNeed();
                need.setRole(needDto.role());
                need.setCount(needDto.count());
                need.setDurationWeeks(needDto.durationWeeks());
                project.addStaffingNeed(need);
            }
        }
    }
}

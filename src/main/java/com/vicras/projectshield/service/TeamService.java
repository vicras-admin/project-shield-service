package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.request.TeamRequest;
import com.vicras.projectshield.dto.response.TeamResponse;
import com.vicras.projectshield.entity.Team;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.TeamRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final OrganizationContext organizationContext;

    public TeamService(TeamRepository teamRepository, OrganizationContext organizationContext) {
        this.teamRepository = teamRepository;
        this.organizationContext = organizationContext;
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        UUID orgId = organizationContext.getCurrentOrganization().getId();
        return teamRepository.findByOrganizationId(orgId).stream()
                .map(TeamResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeamById(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
        return TeamResponse.from(team);
    }

    public TeamResponse createTeam(TeamRequest request) {
        Team team = new Team();
        team.setOrganization(organizationContext.getCurrentOrganization());
        team.setName(request.name());
        team.setDescription(request.description());

        Team saved = teamRepository.save(team);
        return TeamResponse.from(saved);
    }

    public TeamResponse updateTeam(UUID id, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));

        team.setName(request.name());
        team.setDescription(request.description());

        Team saved = teamRepository.save(team);
        return TeamResponse.from(saved);
    }

    public void deleteTeam(UUID id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team", id);
        }
        teamRepository.deleteById(id);
    }
}

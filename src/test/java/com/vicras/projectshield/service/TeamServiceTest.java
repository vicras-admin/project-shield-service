package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.request.TeamRequest;
import com.vicras.projectshield.dto.response.TeamResponse;
import com.vicras.projectshield.entity.Organization;
import com.vicras.projectshield.entity.Team;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.TeamRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private OrganizationContext organizationContext;

    @InjectMocks
    private TeamService teamService;

    private Team team;
    private UUID teamId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();
        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("Test Org");
        organization.setSlug("test-org");

        team = new Team();
        team.setId(teamId);
        team.setName("Platform Team");
        team.setDescription("Core platform development");
        team.setOrganization(organization);
    }

    @Test
    void getAllTeams_returnsList() {
        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(teamRepository.findByOrganizationId(organization.getId())).thenReturn(List.of(team));

        List<TeamResponse> result = teamService.getAllTeams();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Platform Team");
    }

    @Test
    void getAllTeams_returnsEmptyList() {
        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(teamRepository.findByOrganizationId(organization.getId())).thenReturn(List.of());

        List<TeamResponse> result = teamService.getAllTeams();

        assertThat(result).isEmpty();
    }

    @Test
    void getTeamById_withValidId_returnsTeam() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        TeamResponse result = teamService.getTeamById(teamId);

        assertThat(result.id()).isEqualTo(teamId);
        assertThat(result.name()).isEqualTo("Platform Team");
        assertThat(result.description()).isEqualTo("Core platform development");
    }

    @Test
    void getTeamById_withInvalidId_throwsException() {
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamById(teamId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Team");
    }

    @Test
    void createTeam_withValidRequest_createsTeam() {
        TeamRequest request = new TeamRequest("New Team", "New description");

        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        TeamResponse result = teamService.createTeam(request);

        assertThat(result.name()).isEqualTo("New Team");
        assertThat(result.description()).isEqualTo("New description");

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("New Team");
        assertThat(captor.getValue().getOrganization()).isEqualTo(organization);
    }

    @Test
    void updateTeam_withValidRequest_updatesTeam() {
        TeamRequest request = new TeamRequest("Updated Team", "Updated description");

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        TeamResponse result = teamService.updateTeam(teamId, request);

        verify(teamRepository).save(team);
        assertThat(team.getName()).isEqualTo("Updated Team");
        assertThat(team.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void updateTeam_withInvalidId_throwsException() {
        TeamRequest request = new TeamRequest("Updated Team", "Updated description");
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.updateTeam(teamId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTeam_withValidId_deletesTeam() {
        when(teamRepository.existsById(teamId)).thenReturn(true);

        teamService.deleteTeam(teamId);

        verify(teamRepository).deleteById(teamId);
    }

    @Test
    void deleteTeam_withInvalidId_throwsException() {
        when(teamRepository.existsById(teamId)).thenReturn(false);

        assertThatThrownBy(() -> teamService.deleteTeam(teamId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

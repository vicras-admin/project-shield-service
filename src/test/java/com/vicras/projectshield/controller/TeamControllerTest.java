package com.vicras.projectshield.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vicras.projectshield.dto.request.TeamRequest;
import com.vicras.projectshield.dto.response.TeamResponse;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
@ActiveProfiles("test")
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamService teamService;

    @Test
    void getAllTeams_returnsTeamList() throws Exception {
        UUID teamId = UUID.randomUUID();
        TeamResponse team = new TeamResponse(teamId, "Platform Team", "Core platform", List.of());

        when(teamService.getAllTeams()).thenReturn(List.of(team));

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(teamId.toString()))
                .andExpect(jsonPath("$[0].name").value("Platform Team"));
    }

    @Test
    void getTeamById_withValidId_returnsTeam() throws Exception {
        UUID teamId = UUID.randomUUID();
        TeamResponse team = new TeamResponse(teamId, "Platform Team", "Core platform", List.of());

        when(teamService.getTeamById(teamId)).thenReturn(team);

        mockMvc.perform(get("/api/teams/{id}", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Platform Team"));
    }

    @Test
    void getTeamById_withInvalidId_returns404() throws Exception {
        UUID teamId = UUID.randomUUID();

        when(teamService.getTeamById(teamId)).thenThrow(new ResourceNotFoundException("Team", teamId));

        mockMvc.perform(get("/api/teams/{id}", teamId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTeam_withValidRequest_returnsCreated() throws Exception {
        UUID teamId = UUID.randomUUID();
        TeamRequest request = new TeamRequest("Platform Team", "Core platform");
        TeamResponse response = new TeamResponse(teamId, "Platform Team", "Core platform", List.of());

        when(teamService.createTeam(any(TeamRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.name").value("Platform Team"));
    }

    @Test
    void createTeam_withInvalidRequest_returnsBadRequest() throws Exception {
        TeamRequest request = new TeamRequest("", null);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTeam_withValidRequest_returnsUpdated() throws Exception {
        UUID teamId = UUID.randomUUID();
        TeamRequest request = new TeamRequest("Updated Team", "Updated description");
        TeamResponse response = new TeamResponse(teamId, "Updated Team", "Updated description", List.of());

        when(teamService.updateTeam(eq(teamId), any(TeamRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/teams/{id}", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Team"));
    }

    @Test
    void deleteTeam_withValidId_returnsNoContent() throws Exception {
        UUID teamId = UUID.randomUUID();

        mockMvc.perform(delete("/api/teams/{id}", teamId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTeam_withInvalidId_returns404() throws Exception {
        UUID teamId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("Team", teamId)).when(teamService).deleteTeam(teamId);

        mockMvc.perform(delete("/api/teams/{id}", teamId))
                .andExpect(status().isNotFound());
    }
}

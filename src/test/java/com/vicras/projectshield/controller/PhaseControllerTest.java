package com.vicras.projectshield.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vicras.projectshield.dto.request.PhaseRequest;
import com.vicras.projectshield.dto.response.PhaseResponse;
import com.vicras.projectshield.entity.PhaseType;
import com.vicras.projectshield.exception.DateRangeException;
import com.vicras.projectshield.service.PhaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhaseController.class)
@ActiveProfiles("test")
class PhaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private PhaseService phaseService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getAllPhases_returnsList() throws Exception {
        UUID phaseId = UUID.randomUUID();
        PhaseResponse phase = new PhaseResponse(
                phaseId, "Q1 2025", "First quarter",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31),
                PhaseType.QUARTER, List.of()
        );

        when(phaseService.getAllPhases()).thenReturn(List.of(phase));

        mockMvc.perform(get("/api/phases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Q1 2025"))
                .andExpect(jsonPath("$[0].type").value("quarter"));
    }

    @Test
    void createPhase_withValidRequest_returnsCreated() throws Exception {
        UUID phaseId = UUID.randomUUID();
        PhaseRequest request = new PhaseRequest(
                "Q1 2025", "First quarter",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31),
                PhaseType.QUARTER
        );
        PhaseResponse response = new PhaseResponse(
                phaseId, "Q1 2025", "First quarter",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31),
                PhaseType.QUARTER, List.of()
        );

        when(phaseService.createPhase(any(PhaseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/phases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Q1 2025"));
    }

    @Test
    void createPhase_withInvalidDateRange_returnsBadRequest() throws Exception {
        PhaseRequest request = new PhaseRequest(
                "Invalid Phase", "Description",
                LocalDate.of(2025, 6, 30), LocalDate.of(2025, 4, 1),
                PhaseType.QUARTER
        );

        when(phaseService.createPhase(any(PhaseRequest.class)))
                .thenThrow(new DateRangeException("End date must be on or after start date"));

        mockMvc.perform(post("/api/phases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("End date must be on or after start date"));
    }

    @Test
    void createPhase_withMissingName_returnsBadRequest() throws Exception {
        String requestJson = """
            {
                "startDate": "2025-01-01",
                "endDate": "2025-03-31",
                "type": "quarter"
            }
            """;

        mockMvc.perform(post("/api/phases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
}

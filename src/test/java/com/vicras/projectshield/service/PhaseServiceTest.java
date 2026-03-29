package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.request.PhaseRequest;
import com.vicras.projectshield.dto.response.PhaseResponse;
import com.vicras.projectshield.entity.Organization;
import com.vicras.projectshield.entity.Phase;
import com.vicras.projectshield.entity.PhaseType;
import com.vicras.projectshield.exception.DateRangeException;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.PhaseRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhaseServiceTest {

    @Mock
    private PhaseRepository phaseRepository;

    @Mock
    private OrganizationContext organizationContext;

    @InjectMocks
    private PhaseService phaseService;

    private Phase phase;
    private UUID phaseId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        phaseId = UUID.randomUUID();
        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("Test Org");
        organization.setSlug("test-org");

        phase = new Phase();
        phase.setId(phaseId);
        phase.setOrganization(organization);
        phase.setName("Q1 2025");
        phase.setDescription("First quarter");
        phase.setStartDate(LocalDate.of(2025, 1, 1));
        phase.setEndDate(LocalDate.of(2025, 3, 31));
        phase.setType(PhaseType.QUARTER);
    }

    @Test
    void getAllPhases_returnsList() {
        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(phaseRepository.findByOrganizationId(organization.getId())).thenReturn(List.of(phase));

        List<PhaseResponse> result = phaseService.getAllPhases();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Q1 2025");
    }

    @Test
    void getPhaseById_withValidId_returnsPhase() {
        when(phaseRepository.findById(phaseId)).thenReturn(Optional.of(phase));

        PhaseResponse result = phaseService.getPhaseById(phaseId);

        assertThat(result.id()).isEqualTo(phaseId);
        assertThat(result.name()).isEqualTo("Q1 2025");
    }

    @Test
    void getPhaseById_withInvalidId_throwsException() {
        when(phaseRepository.findById(phaseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> phaseService.getPhaseById(phaseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Phase");
    }

    @Test
    void createPhase_withValidRequest_createsPhase() {
        PhaseRequest request = new PhaseRequest(
                "Q2 2025",
                "Second quarter",
                LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 6, 30),
                PhaseType.QUARTER
        );

        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(phaseRepository.save(any(Phase.class))).thenAnswer(invocation -> {
            Phase saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        PhaseResponse result = phaseService.createPhase(request);

        assertThat(result.name()).isEqualTo("Q2 2025");

        ArgumentCaptor<Phase> captor = ArgumentCaptor.forClass(Phase.class);
        verify(phaseRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(PhaseType.QUARTER);
        assertThat(captor.getValue().getOrganization()).isEqualTo(organization);
    }

    @Test
    void createPhase_withInvalidDateRange_throwsException() {
        PhaseRequest request = new PhaseRequest(
                "Invalid Phase",
                "Description",
                LocalDate.of(2025, 6, 30),
                LocalDate.of(2025, 4, 1),
                PhaseType.QUARTER
        );

        assertThatThrownBy(() -> phaseService.createPhase(request))
                .isInstanceOf(DateRangeException.class)
                .hasMessageContaining("End date must be on or after start date");
    }

    @Test
    void updatePhase_withValidRequest_updatesPhase() {
        PhaseRequest request = new PhaseRequest(
                "Q1 2025 Updated",
                "Updated description",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 3, 31),
                PhaseType.QUARTER
        );

        when(phaseRepository.findById(phaseId)).thenReturn(Optional.of(phase));
        when(phaseRepository.save(any(Phase.class))).thenReturn(phase);

        PhaseResponse result = phaseService.updatePhase(phaseId, request);

        verify(phaseRepository).save(phase);
        assertThat(phase.getName()).isEqualTo("Q1 2025 Updated");
    }

    @Test
    void deletePhase_withValidId_deletesPhase() {
        when(phaseRepository.existsById(phaseId)).thenReturn(true);

        phaseService.deletePhase(phaseId);

        verify(phaseRepository).deleteById(phaseId);
    }

    @Test
    void deletePhase_withInvalidId_throwsException() {
        when(phaseRepository.existsById(phaseId)).thenReturn(false);

        assertThatThrownBy(() -> phaseService.deletePhase(phaseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.request.PhaseRequest;
import com.vicras.projectshield.dto.response.PhaseResponse;
import com.vicras.projectshield.entity.Phase;
import com.vicras.projectshield.exception.DateRangeException;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.PhaseRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PhaseService {

    private static final Logger log = LoggerFactory.getLogger(PhaseService.class);

    private final PhaseRepository phaseRepository;
    private final OrganizationContext organizationContext;

    public PhaseService(PhaseRepository phaseRepository, OrganizationContext organizationContext) {
        this.phaseRepository = phaseRepository;
        this.organizationContext = organizationContext;
    }

    @Transactional(readOnly = true)
    public List<PhaseResponse> getAllPhases() {
        UUID orgId = organizationContext.getCurrentOrganization().getId();
        log.debug("Fetching phases for organization {}", orgId);
        List<Phase> phases = phaseRepository.findByOrganizationId(orgId);
        log.debug("Found {} phases, mapping to response DTOs", phases.size());
        return phases.stream()
                .map(PhaseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PhaseResponse getPhaseById(UUID id) {
        log.debug("Fetching phase by id: {}", id);
        Phase phase = phaseRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug("Phase not found with id: {}", id);
                    return new ResourceNotFoundException("Phase", id);
                });
        log.debug("Found phase '{}', mapping to response DTO with projects", phase.getName());
        return PhaseResponse.from(phase);
    }

    public PhaseResponse createPhase(PhaseRequest request) {
        validateDateRange(request);

        Phase phase = new Phase();
        phase.setOrganization(organizationContext.getCurrentOrganization());
        mapRequestToEntity(request, phase);

        Phase saved = phaseRepository.save(phase);
        return PhaseResponse.fromWithoutProjects(saved);
    }

    public PhaseResponse updatePhase(UUID id, PhaseRequest request) {
        validateDateRange(request);

        Phase phase = phaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phase", id));

        mapRequestToEntity(request, phase);

        Phase saved = phaseRepository.save(phase);
        return PhaseResponse.from(saved);
    }

    public void deletePhase(UUID id) {
        if (!phaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Phase", id);
        }
        phaseRepository.deleteById(id);
    }

    private void validateDateRange(PhaseRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new DateRangeException("End date must be on or after start date");
        }
    }

    private void mapRequestToEntity(PhaseRequest request, Phase phase) {
        phase.setName(request.name());
        phase.setDescription(request.description());
        phase.setStartDate(request.startDate());
        phase.setEndDate(request.endDate());
        phase.setType(request.type());
    }
}

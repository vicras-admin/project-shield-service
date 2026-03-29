package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.request.DomainRequest;
import com.vicras.projectshield.dto.response.DomainResponse;
import com.vicras.projectshield.entity.Domain;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.DomainRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DomainService {

    private final DomainRepository domainRepository;
    private final OrganizationContext organizationContext;

    public DomainService(DomainRepository domainRepository, OrganizationContext organizationContext) {
        this.domainRepository = domainRepository;
        this.organizationContext = organizationContext;
    }

    @Transactional(readOnly = true)
    public List<DomainResponse> getAllDomains() {
        UUID orgId = organizationContext.getCurrentOrganization().getId();
        return domainRepository.findByOrganizationId(orgId).stream()
                .map(DomainResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DomainResponse getDomainById(UUID id) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domain", id));
        return DomainResponse.from(domain);
    }

    public DomainResponse createDomain(DomainRequest request) {
        Domain domain = new Domain();
        domain.setOrganization(organizationContext.getCurrentOrganization());
        domain.setName(request.name());

        Domain saved = domainRepository.save(domain);
        return DomainResponse.from(saved);
    }

    public DomainResponse updateDomain(UUID id, DomainRequest request) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domain", id));

        domain.setName(request.name());

        Domain saved = domainRepository.save(domain);
        return DomainResponse.from(saved);
    }

    public void deleteDomain(UUID id) {
        if (!domainRepository.existsById(id)) {
            throw new ResourceNotFoundException("Domain", id);
        }
        domainRepository.deleteById(id);
    }
}

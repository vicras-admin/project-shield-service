package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.request.DomainRequest;
import com.vicras.projectshield.dto.response.DomainResponse;
import com.vicras.projectshield.entity.Domain;
import com.vicras.projectshield.entity.Organization;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.DomainRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class DomainServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private OrganizationContext organizationContext;

    @InjectMocks
    private DomainService domainService;

    private Domain domain;
    private UUID domainId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();

        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("Test Org");
        organization.setSlug("test-org");

        domain = new Domain();
        domain.setId(domainId);
        domain.setOrganization(organization);
        domain.setName("Claims Processing");
    }

    @Test
    void getAllDomains_returnsList() {
        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(domainRepository.findByOrganizationId(organization.getId())).thenReturn(List.of(domain));

        List<DomainResponse> result = domainService.getAllDomains();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Claims Processing");
    }

    @Test
    void getDomainById_withValidId_returnsDomain() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));

        DomainResponse result = domainService.getDomainById(domainId);

        assertThat(result.id()).isEqualTo(domainId);
        assertThat(result.name()).isEqualTo("Claims Processing");
    }

    @Test
    void getDomainById_withInvalidId_throwsException() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> domainService.getDomainById(domainId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createDomain_withValidRequest_createsDomain() {
        DomainRequest request = new DomainRequest("Payment & Billing");

        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(domainRepository.save(any(Domain.class))).thenAnswer(invocation -> {
            Domain saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        DomainResponse result = domainService.createDomain(request);

        assertThat(result.name()).isEqualTo("Payment & Billing");
        verify(domainRepository).save(any(Domain.class));
    }

    @Test
    void updateDomain_withValidRequest_updatesDomain() {
        DomainRequest request = new DomainRequest("Updated Domain");

        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(domainRepository.save(any(Domain.class))).thenReturn(domain);

        DomainResponse result = domainService.updateDomain(domainId, request);

        verify(domainRepository).save(domain);
        assertThat(domain.getName()).isEqualTo("Updated Domain");
    }

    @Test
    void updateDomain_withInvalidId_throwsException() {
        DomainRequest request = new DomainRequest("Updated Domain");

        when(domainRepository.findById(domainId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> domainService.updateDomain(domainId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteDomain_withValidId_deletesDomain() {
        when(domainRepository.existsById(domainId)).thenReturn(true);

        domainService.deleteDomain(domainId);

        verify(domainRepository).deleteById(domainId);
    }

    @Test
    void deleteDomain_withInvalidId_throwsException() {
        when(domainRepository.existsById(domainId)).thenReturn(false);

        assertThatThrownBy(() -> domainService.deleteDomain(domainId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

package com.vicras.projectshield.repository;

import com.vicras.projectshield.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DomainRepository extends JpaRepository<Domain, UUID> {

    List<Domain> findByOrganizationId(UUID organizationId);

    Optional<Domain> findByOrganizationIdAndName(UUID organizationId, String name);
}

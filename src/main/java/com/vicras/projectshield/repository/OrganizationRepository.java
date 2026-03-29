package com.vicras.projectshield.repository;

import com.vicras.projectshield.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    boolean existsBySlug(String slug);

    Optional<Organization> findBySlug(String slug);

    Optional<Organization> findByClerkOrganizationId(String clerkOrganizationId);
}

package com.vicras.projectshield.repository;

import com.vicras.projectshield.entity.Phase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhaseRepository extends JpaRepository<Phase, UUID> {

    List<Phase> findByOrganizationId(UUID organizationId);
}

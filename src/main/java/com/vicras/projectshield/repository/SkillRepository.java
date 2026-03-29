package com.vicras.projectshield.repository;

import com.vicras.projectshield.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {

    List<Skill> findByOrganizationId(UUID organizationId);

    Optional<Skill> findByOrganizationIdAndName(UUID organizationId, String name);
}

package com.vicras.projectshield.repository;

import com.vicras.projectshield.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByPhaseIdOrderByStackRankAsc(UUID phaseId);

    @Query("SELECT p FROM Project p WHERE p.phase.id = :phaseId ORDER BY p.overallScore DESC NULLS LAST")
    List<Project> findByPhaseIdOrderByOverallScoreDesc(@Param("phaseId") UUID phaseId);
}

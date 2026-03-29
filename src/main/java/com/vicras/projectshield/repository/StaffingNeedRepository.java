package com.vicras.projectshield.repository;

import com.vicras.projectshield.entity.StaffingNeed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StaffingNeedRepository extends JpaRepository<StaffingNeed, UUID> {

    List<StaffingNeed> findByProjectId(UUID projectId);
}

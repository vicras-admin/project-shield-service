package com.vicras.projectshield.repository;

import com.vicras.projectshield.entity.Invitation;
import com.vicras.projectshield.entity.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    Optional<Invitation> findByToken(String token);

    List<Invitation> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    boolean existsByEmailAndOrganizationIdAndStatus(String email, UUID organizationId, InvitationStatus status);

    List<Invitation> findAllByStatusAndExpiresAtBefore(InvitationStatus status, LocalDateTime dateTime);
}

package com.vicras.projectshield.repository;

import com.vicras.projectshield.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {

    List<Member> findByOrganizationId(UUID organizationId);

    List<Member> findByTeamId(UUID teamId);

    List<Member> findByRoleName(String roleName);

    Optional<Member> findByClerkUserId(String clerkUserId);
}

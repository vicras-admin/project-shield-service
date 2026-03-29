package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.request.StaffRequest;
import com.vicras.projectshield.dto.response.StaffResponse;
import com.vicras.projectshield.entity.Domain;
import com.vicras.projectshield.entity.Member;
import com.vicras.projectshield.entity.RoleEntity;
import com.vicras.projectshield.entity.Skill;
import com.vicras.projectshield.entity.Team;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.DomainRepository;
import com.vicras.projectshield.repository.MemberRepository;
import com.vicras.projectshield.repository.RoleRepository;
import com.vicras.projectshield.repository.SkillRepository;
import com.vicras.projectshield.repository.TeamRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class StaffService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final RoleRepository roleRepository;
    private final DomainRepository domainRepository;
    private final SkillRepository skillRepository;
    private final OrganizationContext organizationContext;

    public StaffService(MemberRepository memberRepository, TeamRepository teamRepository,
                        RoleRepository roleRepository,
                        DomainRepository domainRepository, SkillRepository skillRepository,
                        OrganizationContext organizationContext) {
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
        this.roleRepository = roleRepository;
        this.domainRepository = domainRepository;
        this.skillRepository = skillRepository;
        this.organizationContext = organizationContext;
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        UUID orgId = organizationContext.getCurrentOrganization().getId();
        return memberRepository.findByOrganizationId(orgId).stream()
                .map(StaffResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StaffResponse getStaffById(UUID id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));
        return StaffResponse.from(member);
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffByRole(String role) {
        return memberRepository.findByRoleName(role).stream()
                .map(StaffResponse::from)
                .toList();
    }

    public StaffResponse createStaff(StaffRequest request) {
        Member member = new Member();
        member.setOrganization(organizationContext.getCurrentOrganization());
        mapRequestToEntity(request, member);

        Member saved = memberRepository.save(member);
        return StaffResponse.from(saved);
    }

    public StaffResponse updateStaff(UUID id, StaffRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));

        mapRequestToEntity(request, member);

        Member saved = memberRepository.save(member);
        return StaffResponse.from(saved);
    }

    public void deleteStaff(UUID id) {
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Member", id);
        }
        memberRepository.deleteById(id);
    }

    private void mapRequestToEntity(StaffRequest request, Member member) {
        member.setFirstName(request.firstName());
        member.setLastName(request.lastName());
        member.setMiddleInitial(request.middleInitial());
        member.setAvatar(request.avatar());
        member.setEmail(request.email());
        member.setPhone(request.phone());

        RoleEntity role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.role()));
        member.setRole(role);

        member.setSeniority(request.seniority());
        member.setHoursPerDay(request.hoursPerDay() != null ? request.hoursPerDay() : 8);

        if (request.teamId() != null) {
            Team team = teamRepository.findById(request.teamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team", request.teamId()));
            member.setTeam(team);
        } else {
            member.setTeam(null);
        }

        if (request.domainIds() != null) {
            Set<Domain> domains = new HashSet<>();
            for (UUID domainId : request.domainIds()) {
                Domain domain = domainRepository.findById(domainId)
                        .orElseThrow(() -> new ResourceNotFoundException("Domain", domainId));
                domains.add(domain);
            }
            member.setDomains(domains);
        }

        if (request.skillIds() != null) {
            Set<Skill> skills = new HashSet<>();
            for (UUID skillId : request.skillIds()) {
                Skill skill = skillRepository.findById(skillId)
                        .orElseThrow(() -> new ResourceNotFoundException("Skill", skillId));
                skills.add(skill);
            }
            member.setSkills(skills);
        }
    }
}

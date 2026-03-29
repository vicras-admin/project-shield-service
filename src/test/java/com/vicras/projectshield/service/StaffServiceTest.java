package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.request.StaffRequest;
import com.vicras.projectshield.dto.response.StaffResponse;
import com.vicras.projectshield.entity.*;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.DomainRepository;
import com.vicras.projectshield.repository.MemberRepository;
import com.vicras.projectshield.repository.RoleRepository;
import com.vicras.projectshield.repository.SkillRepository;
import com.vicras.projectshield.repository.TeamRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private OrganizationContext organizationContext;

    @InjectMocks
    private StaffService staffService;

    private Member member;
    private Team team;
    private UUID memberId;
    private UUID teamId;
    private Organization organization;
    private RoleEntity memberRole;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        teamId = UUID.randomUUID();

        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("Test Org");
        organization.setSlug("test-org");

        team = new Team();
        team.setId(teamId);
        team.setName("Platform Team");
        team.setOrganization(organization);

        memberRole = new RoleEntity();
        memberRole.setId(UUID.randomUUID());
        memberRole.setName("member");

        member = new Member();
        member.setId(memberId);
        member.setOrganization(organization);
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john@example.com");
        member.setRole(memberRole);
        member.setSeniority(Seniority.senior);
        member.setHoursPerDay(8);
        member.setTeam(team);
    }

    @Test
    void getAllStaff_returnsList() {
        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(memberRepository.findByOrganizationId(organization.getId())).thenReturn(List.of(member));

        List<StaffResponse> result = staffService.getAllStaff();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).firstName()).isEqualTo("John");
        assertThat(result.get(0).lastName()).isEqualTo("Doe");
    }

    @Test
    void getStaffById_withValidId_returnsStaff() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        StaffResponse result = staffService.getStaffById(memberId);

        assertThat(result.id()).isEqualTo(memberId);
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.role()).isEqualTo("member");
    }

    @Test
    void getStaffById_withInvalidId_throwsException() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffService.getStaffById(memberId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getStaffByRole_returnsFilteredList() {
        when(memberRepository.findByRoleName("member")).thenReturn(List.of(member));

        List<StaffResponse> result = staffService.getStaffByRole("member");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).role()).isEqualTo("member");
    }

    @Test
    void createStaff_withValidRequest_createsStaff() {
        UUID domainId = UUID.randomUUID();
        UUID skillId1 = UUID.randomUUID();
        UUID skillId2 = UUID.randomUUID();

        Domain domain = new Domain();
        domain.setId(domainId);
        domain.setName("Claims Processing");

        Skill skill1 = new Skill();
        skill1.setId(skillId1);
        skill1.setName("React");

        Skill skill2 = new Skill();
        skill2.setId(skillId2);
        skill2.setName("TypeScript");

        StaffRequest request = new StaffRequest(
                "Jane", "Doe", null, null, "jane@example.com", "555-1234",
                "member", Seniority.mid, teamId, 6,
                Set.of(domainId), Set.of(skillId1, skillId2)
        );

        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(roleRepository.findByName("member")).thenReturn(Optional.of(memberRole));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(skillRepository.findById(skillId1)).thenReturn(Optional.of(skill1));
        when(skillRepository.findById(skillId2)).thenReturn(Optional.of(skill2));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        StaffResponse result = staffService.createStaff(request);

        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.role()).isEqualTo("member");
        assertThat(result.hoursPerDay()).isEqualTo(6);
    }

    @Test
    void createStaff_withoutTeam_createsStaff() {
        StaffRequest request = new StaffRequest(
                "Jane", "Doe", null, null, "jane@example.com", null,
                "member", Seniority.mid, null, null,
                null, null
        );

        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(roleRepository.findByName("member")).thenReturn(Optional.of(memberRole));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        StaffResponse result = staffService.createStaff(request);

        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.teamId()).isNull();
        assertThat(result.hoursPerDay()).isEqualTo(8);
    }

    @Test
    void createStaff_withInvalidTeamId_throwsException() {
        StaffRequest request = new StaffRequest(
                "Jane", "Doe", null, null, "jane@example.com", null,
                "member", Seniority.mid, teamId, 8,
                null, null
        );

        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(roleRepository.findByName("member")).thenReturn(Optional.of(memberRole));
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffService.createStaff(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Team");
    }

    @Test
    void createStaff_withInvalidDomainId_throwsException() {
        UUID invalidDomainId = UUID.randomUUID();

        StaffRequest request = new StaffRequest(
                "Jane", "Doe", null, null, "jane@example.com", null,
                "member", Seniority.mid, null, 8,
                Set.of(invalidDomainId), null
        );

        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(roleRepository.findByName("member")).thenReturn(Optional.of(memberRole));
        when(domainRepository.findById(invalidDomainId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffService.createStaff(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Domain");
    }

    @Test
    void createStaff_withInvalidSkillId_throwsException() {
        UUID invalidSkillId = UUID.randomUUID();

        StaffRequest request = new StaffRequest(
                "Jane", "Doe", null, null, "jane@example.com", null,
                "member", Seniority.mid, null, 8,
                null, Set.of(invalidSkillId)
        );

        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(roleRepository.findByName("member")).thenReturn(Optional.of(memberRole));
        when(skillRepository.findById(invalidSkillId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffService.createStaff(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Skill");
    }

    @Test
    void updateStaff_withValidRequest_updatesStaff() {
        UUID domainId = UUID.randomUUID();
        UUID skillId1 = UUID.randomUUID();
        UUID skillId2 = UUID.randomUUID();

        Domain domain = new Domain();
        domain.setId(domainId);
        domain.setName("Analytics");

        Skill skill1 = new Skill();
        skill1.setId(skillId1);
        skill1.setName("Java");

        Skill skill2 = new Skill();
        skill2.setId(skillId2);
        skill2.setName("React");

        RoleEntity teamLeadRole = new RoleEntity();
        teamLeadRole.setId(UUID.randomUUID());
        teamLeadRole.setName("team_lead");

        StaffRequest request = new StaffRequest(
                "John", "Updated", null, null, "john.updated@example.com", null,
                "team_lead", Seniority.lead, null, 7,
                Set.of(domainId), Set.of(skillId1, skillId2)
        );

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(roleRepository.findByName("team_lead")).thenReturn(Optional.of(teamLeadRole));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(skillRepository.findById(skillId1)).thenReturn(Optional.of(skill1));
        when(skillRepository.findById(skillId2)).thenReturn(Optional.of(skill2));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        StaffResponse result = staffService.updateStaff(memberId, request);

        verify(memberRepository).save(member);
        assertThat(member.getFirstName()).isEqualTo("John");
        assertThat(member.getLastName()).isEqualTo("Updated");
        assertThat(member.getRole().getName()).isEqualTo("team_lead");
    }

    @Test
    void updateStaff_withInvalidId_throwsException() {
        StaffRequest request = new StaffRequest(
                "John", "Updated", null, null, null, null,
                "member", Seniority.mid, null, 8, null, null
        );

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffService.updateStaff(memberId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteStaff_withValidId_deletesStaff() {
        when(memberRepository.existsById(memberId)).thenReturn(true);

        staffService.deleteStaff(memberId);

        verify(memberRepository).deleteById(memberId);
    }

    @Test
    void deleteStaff_withInvalidId_throwsException() {
        when(memberRepository.existsById(memberId)).thenReturn(false);

        assertThatThrownBy(() -> staffService.deleteStaff(memberId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

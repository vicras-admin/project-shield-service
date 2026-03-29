package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.request.SkillRequest;
import com.vicras.projectshield.dto.response.SkillResponse;
import com.vicras.projectshield.entity.Organization;
import com.vicras.projectshield.entity.Skill;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.SkillRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private OrganizationContext organizationContext;

    @InjectMocks
    private SkillService skillService;

    private Skill skill;
    private UUID skillId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        skillId = UUID.randomUUID();

        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("Test Org");
        organization.setSlug("test-org");

        skill = new Skill();
        skill.setId(skillId);
        skill.setOrganization(organization);
        skill.setName("Java");
    }

    @Test
    void getAllSkills_returnsList() {
        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(skillRepository.findByOrganizationId(organization.getId())).thenReturn(List.of(skill));

        List<SkillResponse> result = skillService.getAllSkills();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Java");
    }

    @Test
    void getSkillById_withValidId_returnsSkill() {
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));

        SkillResponse result = skillService.getSkillById(skillId);

        assertThat(result.id()).isEqualTo(skillId);
        assertThat(result.name()).isEqualTo("Java");
    }

    @Test
    void getSkillById_withInvalidId_throwsException() {
        when(skillRepository.findById(skillId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.getSkillById(skillId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSkill_withValidRequest_createsSkill() {
        SkillRequest request = new SkillRequest("Spring Boot");

        when(organizationContext.getCurrentOrganization()).thenReturn(organization);
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
            Skill saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        SkillResponse result = skillService.createSkill(request);

        assertThat(result.name()).isEqualTo("Spring Boot");
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    void updateSkill_withValidRequest_updatesSkill() {
        SkillRequest request = new SkillRequest("Updated Skill");

        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));
        when(skillRepository.save(any(Skill.class))).thenReturn(skill);

        SkillResponse result = skillService.updateSkill(skillId, request);

        verify(skillRepository).save(skill);
        assertThat(skill.getName()).isEqualTo("Updated Skill");
    }

    @Test
    void updateSkill_withInvalidId_throwsException() {
        SkillRequest request = new SkillRequest("Updated Skill");

        when(skillRepository.findById(skillId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.updateSkill(skillId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteSkill_withValidId_deletesSkill() {
        when(skillRepository.existsById(skillId)).thenReturn(true);

        skillService.deleteSkill(skillId);

        verify(skillRepository).deleteById(skillId);
    }

    @Test
    void deleteSkill_withInvalidId_throwsException() {
        when(skillRepository.existsById(skillId)).thenReturn(false);

        assertThatThrownBy(() -> skillService.deleteSkill(skillId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

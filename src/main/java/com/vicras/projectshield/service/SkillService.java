package com.vicras.projectshield.service;

import com.vicras.projectshield.dto.request.SkillRequest;
import com.vicras.projectshield.dto.response.SkillResponse;
import com.vicras.projectshield.entity.Skill;
import com.vicras.projectshield.exception.ResourceNotFoundException;
import com.vicras.projectshield.repository.SkillRepository;
import com.vicras.projectshield.security.OrganizationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;
    private final OrganizationContext organizationContext;

    public SkillService(SkillRepository skillRepository, OrganizationContext organizationContext) {
        this.skillRepository = skillRepository;
        this.organizationContext = organizationContext;
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> getAllSkills() {
        UUID orgId = organizationContext.getCurrentOrganization().getId();
        return skillRepository.findByOrganizationId(orgId).stream()
                .map(SkillResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SkillResponse getSkillById(UUID id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", id));
        return SkillResponse.from(skill);
    }

    public SkillResponse createSkill(SkillRequest request) {
        Skill skill = new Skill();
        skill.setOrganization(organizationContext.getCurrentOrganization());
        skill.setName(request.name());

        Skill saved = skillRepository.save(skill);
        return SkillResponse.from(saved);
    }

    public SkillResponse updateSkill(UUID id, SkillRequest request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", id));

        skill.setName(request.name());

        Skill saved = skillRepository.save(skill);
        return SkillResponse.from(saved);
    }

    public void deleteSkill(UUID id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill", id);
        }
        skillRepository.deleteById(id);
    }
}

package com.vicras.projectshield.dto.response;

import com.vicras.projectshield.entity.Skill;

import java.time.LocalDateTime;
import java.util.UUID;

public record SkillResponse(
        UUID id,
        String name,
        LocalDateTime createdAt
) {
    public static SkillResponse from(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCreatedAt()
        );
    }
}

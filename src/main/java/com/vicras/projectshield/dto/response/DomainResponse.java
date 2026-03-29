package com.vicras.projectshield.dto.response;

import com.vicras.projectshield.entity.Domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record DomainResponse(
        UUID id,
        String name,
        LocalDateTime createdAt
) {
    public static DomainResponse from(Domain domain) {
        return new DomainResponse(
                domain.getId(),
                domain.getName(),
                domain.getCreatedAt()
        );
    }
}

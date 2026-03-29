package com.vicras.projectshield.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PhaseType {
    QUARTER,
    HALF,
    ANNUAL,
    CUSTOM;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}

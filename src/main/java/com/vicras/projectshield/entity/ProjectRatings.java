package com.vicras.projectshield.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProjectRatings {

    @Column(name = "rating_strategic_alignment")
    private Integer strategicAlignment;

    @Column(name = "rating_financial_benefit")
    private Integer financialBenefit;

    @Column(name = "rating_risk_profile")
    private Integer riskProfile;

    @Column(name = "rating_feasibility")
    private Integer feasibility;

    @Column(name = "rating_regulatory_compliance")
    private Integer regulatoryCompliance;

    public Integer getStrategicAlignment() {
        return strategicAlignment;
    }

    public void setStrategicAlignment(Integer strategicAlignment) {
        this.strategicAlignment = strategicAlignment;
    }

    public Integer getFinancialBenefit() {
        return financialBenefit;
    }

    public void setFinancialBenefit(Integer financialBenefit) {
        this.financialBenefit = financialBenefit;
    }

    public Integer getRiskProfile() {
        return riskProfile;
    }

    public void setRiskProfile(Integer riskProfile) {
        this.riskProfile = riskProfile;
    }

    public Integer getFeasibility() {
        return feasibility;
    }

    public void setFeasibility(Integer feasibility) {
        this.feasibility = feasibility;
    }

    public Integer getRegulatoryCompliance() {
        return regulatoryCompliance;
    }

    public void setRegulatoryCompliance(Integer regulatoryCompliance) {
        this.regulatoryCompliance = regulatoryCompliance;
    }
}

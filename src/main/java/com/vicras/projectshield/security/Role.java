package com.vicras.projectshield.security;

/**
 * User roles for ProjectShield authorization.
 * These roles must match the role names configured in Clerk.
 *
 * Role hierarchy (highest to lowest):
 * ADMIN > PROJECT_MANAGER > TEAM_LEAD > MEMBER > VIEWER
 */
public enum Role {
    /**
     * Full system access - manage users, system settings, all data
     */
    ADMIN("admin"),

    /**
     * Create/edit projects, assign resources, manage planning phases
     */
    PROJECT_MANAGER("project_manager"),

    /**
     * Manage their team's capacity, view/assign team members to projects
     */
    TEAM_LEAD("team_lead"),

    /**
     * View own assignments, update personal availability
     */
    MEMBER("member"),

    /**
     * Read-only access to dashboards and reports
     */
    VIEWER("viewer");

    private final String clerkRoleName;

    Role(String clerkRoleName) {
        this.clerkRoleName = clerkRoleName;
    }

    /**
     * Returns the role name as it appears in Clerk JWT claims.
     */
    public String getClerkRoleName() {
        return clerkRoleName;
    }

    /**
     * Returns the Spring Security authority string (ROLE_ prefix).
     */
    public String getAuthority() {
        return "ROLE_" + clerkRoleName;
    }
}

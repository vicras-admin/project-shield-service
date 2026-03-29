package com.vicras.projectshield.security;

/**
 * Constants for role-based authorization expressions.
 * Use these in @PreAuthorize annotations for consistency.
 */
public final class Roles {

    private Roles() {
        // Utility class - prevent instantiation
    }

    // Individual role checks
    public static final String IS_ADMIN = "hasRole('admin')";
    public static final String IS_PROJECT_MANAGER = "hasRole('project_manager')";
    public static final String IS_TEAM_LEAD = "hasRole('team_lead')";
    public static final String IS_MEMBER = "hasRole('member')";
    public static final String IS_VIEWER = "hasRole('viewer')";

    // Combined role checks (hierarchical access)
    /**
     * Admin only
     */
    public static final String ADMIN_ONLY = IS_ADMIN;

    /**
     * Admin or Project Manager
     */
    public static final String MANAGER_OR_ABOVE = "hasAnyRole('admin', 'project_manager')";

    /**
     * Admin, Project Manager, or Team Lead
     */
    public static final String LEAD_OR_ABOVE = "hasAnyRole('admin', 'project_manager', 'team_lead')";

    /**
     * Admin, Project Manager, Team Lead, or Member
     */
    public static final String MEMBER_OR_ABOVE = "hasAnyRole('admin', 'project_manager', 'team_lead', 'member')";

    /**
     * Any authenticated user with a valid role
     */
    public static final String ANY_ROLE = "hasAnyRole('admin', 'project_manager', 'team_lead', 'member', 'viewer')";

    /**
     * Can manage staff records (create, update, delete)
     */
    public static final String CAN_MANAGE_STAFF = MANAGER_OR_ABOVE;

    /**
     * Can view staff records
     */
    public static final String CAN_VIEW_STAFF = LEAD_OR_ABOVE;

    /**
     * Can manage teams (create, update, delete)
     */
    public static final String CAN_MANAGE_TEAMS = MANAGER_OR_ABOVE;

    /**
     * Can view teams
     */
    public static final String CAN_VIEW_TEAMS = ANY_ROLE;

    /**
     * Can manage phases (create, update, delete)
     */
    public static final String CAN_MANAGE_PHASES = MANAGER_OR_ABOVE;

    /**
     * Can view phases
     */
    public static final String CAN_VIEW_PHASES = ANY_ROLE;

    /**
     * Can manage projects (create, update, delete)
     */
    public static final String CAN_MANAGE_PROJECTS = MANAGER_OR_ABOVE;

    /**
     * Can view projects
     */
    public static final String CAN_VIEW_PROJECTS = ANY_ROLE;

    /**
     * Can view capacity and gap analysis
     */
    public static final String CAN_VIEW_CAPACITY = ANY_ROLE;

    /**
     * Can manage domains (create, update, delete)
     */
    public static final String CAN_MANAGE_DOMAINS = MANAGER_OR_ABOVE;

    /**
     * Can view domains
     */
    public static final String CAN_VIEW_DOMAINS = ANY_ROLE;

    /**
     * Can manage skills (create, update, delete)
     */
    public static final String CAN_MANAGE_SKILLS = MANAGER_OR_ABOVE;

    /**
     * Can view skills
     */
    public static final String CAN_VIEW_SKILLS = ANY_ROLE;

    /**
     * Can manage invitations (send, revoke, resend)
     */
    public static final String CAN_MANAGE_INVITATIONS = ADMIN_ONLY;
}

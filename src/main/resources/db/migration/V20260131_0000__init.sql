--======================================================================================================================
-- Organizations
--======================================================================================================================
CREATE TABLE organizations
(
    id                    UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    clerk_organization_id VARCHAR(255),
    name                  VARCHAR(255) NOT NULL,
    slug                  VARCHAR(100) NOT NULL UNIQUE,
    description           TEXT,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);


--======================================================================================================================
-- Teams
--======================================================================================================================
CREATE TABLE teams
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    organization_id UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_teams_organization_id ON teams (organization_id);


--======================================================================================================================
-- Roles
--======================================================================================================================
CREATE TABLE roles
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO roles (id, name) VALUES
    ('00000000-0000-4000-8000-000000000001', 'admin'),
    ('00000000-0000-4000-8000-000000000002', 'project_manager'),
    ('00000000-0000-4000-8000-000000000003', 'team_lead'),
    ('00000000-0000-4000-8000-000000000004', 'member'),
    ('00000000-0000-4000-8000-000000000005', 'viewer');


--======================================================================================================================
-- Members
--======================================================================================================================
CREATE TABLE members
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    team_id         UUID         REFERENCES teams (id) ON DELETE SET NULL,
    clerk_user_id   VARCHAR(255) UNIQUE,
    first_name      VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    middle_initial  VARCHAR(1),
    avatar          VARCHAR(500),
    email           VARCHAR(255),
    phone           VARCHAR(50),
    role_id         UUID         NOT NULL REFERENCES roles (id),
    seniority       VARCHAR(20)  NOT NULL CHECK (seniority IN ('junior', 'mid', 'senior', 'lead')),
    hours_per_day   INTEGER      NOT NULL DEFAULT 8 CHECK (hours_per_day >= 1 AND hours_per_day <= 8),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_members_clerk_user_id ON members (clerk_user_id);
CREATE INDEX idx_members_organization_id ON members (organization_id);
CREATE INDEX idx_members_team_id ON members (team_id);
CREATE INDEX idx_members_role_id ON members (role_id);


--======================================================================================================================
-- Domains
--======================================================================================================================
CREATE TABLE domains
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    organization_id UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (organization_id, name)
);

CREATE INDEX idx_domains_organization_id ON domains (organization_id);


--======================================================================================================================
-- Member domains
--======================================================================================================================
CREATE TABLE member_domains
(
    member_id UUID NOT NULL REFERENCES members (id) ON DELETE CASCADE,
    domain_id UUID NOT NULL REFERENCES domains (id) ON DELETE CASCADE,
    PRIMARY KEY (member_id, domain_id)
);


--======================================================================================================================
-- Skills
--======================================================================================================================
CREATE TABLE skills
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    organization_id UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (organization_id, name)
);

CREATE INDEX idx_skills_organization_id ON skills (organization_id);


--======================================================================================================================
-- Member skills
--======================================================================================================================
CREATE TABLE member_skills
(
    member_id UUID NOT NULL REFERENCES members (id) ON DELETE CASCADE,
    skill_id  UUID NOT NULL REFERENCES skills (id) ON DELETE CASCADE,
    PRIMARY KEY (member_id, skill_id)
);


--======================================================================================================================
-- Phases
--======================================================================================================================
CREATE TABLE phases
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    organization_id UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    type            VARCHAR(20)  NOT NULL CHECK (type IN ('QUARTER', 'HALF', 'ANNUAL', 'CUSTOM')),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT phases_date_check CHECK (end_date >= start_date)
);

CREATE INDEX idx_phases_organization_id ON phases (organization_id);


--======================================================================================================================
-- Projects
--======================================================================================================================
CREATE TABLE projects
(
    id                           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    organization_id              UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    phase_id                     UUID         NOT NULL REFERENCES phases (id) ON DELETE CASCADE,
    name                         VARCHAR(255) NOT NULL,
    description                  TEXT,
    justification                TEXT,
    sponsor                      VARCHAR(255),
    estimated_budget             DECIMAL(15, 2),
    start_date                   DATE         NOT NULL,
    end_date                     DATE         NOT NULL,
    overall_score                DECIMAL(3, 2) CHECK (overall_score >= 1 AND overall_score <= 5),
    stack_rank                   INTEGER CHECK (stack_rank >= 0),
    status                       VARCHAR(20)  NOT NULL DEFAULT 'ACCEPTED' CHECK (status IN ('ACCEPTED', 'STRATEGIC', 'REJECTED')),
    rating_strategic_alignment   INTEGER CHECK (rating_strategic_alignment >= 1 AND rating_strategic_alignment <= 5),
    rating_financial_benefit     INTEGER CHECK (rating_financial_benefit >= 1 AND rating_financial_benefit <= 5),
    rating_risk_profile          INTEGER CHECK (rating_risk_profile >= 1 AND rating_risk_profile <= 5),
    rating_feasibility           INTEGER CHECK (rating_feasibility >= 1 AND rating_feasibility <= 5),
    rating_regulatory_compliance INTEGER CHECK (rating_regulatory_compliance >= 1 AND rating_regulatory_compliance <= 5),
    created_at                   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT projects_date_check CHECK (end_date >= start_date)
);

CREATE INDEX idx_projects_organization_id ON projects (organization_id);
CREATE INDEX idx_projects_phase_id ON projects (phase_id);
CREATE INDEX idx_projects_status ON projects (status);
CREATE INDEX idx_projects_stack_rank ON projects (stack_rank);


--======================================================================================================================
-- Staffing needs
--======================================================================================================================
CREATE TABLE staffing_needs
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    project_id     UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    role           VARCHAR(100) NOT NULL,
    count          INTEGER      NOT NULL CHECK (count > 0),
    duration_weeks INTEGER      NOT NULL CHECK (duration_weeks > 0),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_staffing_needs_project_id ON staffing_needs (project_id);
CREATE INDEX idx_staffing_needs_role ON staffing_needs (role);


--======================================================================================================================
-- Invitations
--======================================================================================================================
CREATE TABLE invitations (
                             id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             organization_id UUID         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
                             invited_by_id   UUID         NOT NULL REFERENCES members(id) ON DELETE CASCADE,
                             email           VARCHAR(255) NOT NULL,
                             role            VARCHAR(50)  NOT NULL CHECK (role IN ('admin','project_manager','team_lead','member','viewer')),
                             token           VARCHAR(255) NOT NULL UNIQUE,
                             status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','ACCEPTED','REVOKED','EXPIRED')),
                             expires_at      TIMESTAMP    NOT NULL,
                             accepted_at     TIMESTAMP,
                             created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invitations_organization_id ON invitations(organization_id);
CREATE INDEX idx_invitations_token ON invitations(token);
CREATE INDEX idx_invitations_email_org_status ON invitations(email, organization_id, status);
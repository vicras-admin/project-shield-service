-- ==============================================================================================================
-- Test Data for Local Development
-- ==============================================================================================================
-- Usage:  psql -U postgres -d local -f db/sql/seed_test_data.sql
--
-- This script is idempotent: it clears all test data and re-inserts it.
-- It auto-detects the existing organization and admin member (created via registration),
-- so it works regardless of which Clerk org/user IDs were assigned.
--
-- Prerequisites:
--   1. Flyway migrations must have been applied first (./gradlew flywayMigrate).
--   2. You must have registered at least one organization via the app.
-- ==============================================================================================================

SET search_path TO projectshield;

-- ==========================================
-- 0. Auto-detect organization and admin member
-- ==========================================
-- Grab the first (and typically only) organization
SELECT id AS org_id FROM projectshield.organizations LIMIT 1 \gset

-- Fail fast if no org exists
\if :{?org_id}
\else
  \echo ''
  \echo 'ERROR: No organization found in the database.'
  \echo '       Register an organization via the app first, then re-run this script.'
  \echo ''
  \q
\endif

-- Grab the admin member (the one with a clerk_user_id, created during registration)
SELECT id AS admin_member_id FROM projectshield.members WHERE organization_id = :'org_id' AND clerk_user_id IS NOT NULL LIMIT 1 \gset

\echo ''
\echo 'Seeding test data for organization: ' :org_id
\echo ''

BEGIN;

-- ==========================================
-- 1. Clear existing test data (order matters for FK constraints)
--    Preserves the organization and admin member.
-- ==========================================
DELETE FROM projectshield.staffing_needs WHERE project_id IN (SELECT id FROM projectshield.projects WHERE organization_id = :'org_id');
DELETE FROM projectshield.projects       WHERE organization_id = :'org_id';
DELETE FROM projectshield.phases         WHERE organization_id = :'org_id';
DELETE FROM projectshield.invitations    WHERE organization_id = :'org_id';
DELETE FROM projectshield.member_skills  WHERE member_id IN (SELECT id FROM projectshield.members WHERE organization_id = :'org_id');
DELETE FROM projectshield.member_domains WHERE member_id IN (SELECT id FROM projectshield.members WHERE organization_id = :'org_id');
DELETE FROM projectshield.members        WHERE organization_id = :'org_id' AND clerk_user_id IS NULL;
DELETE FROM projectshield.skills         WHERE organization_id = :'org_id';
DELETE FROM projectshield.domains        WHERE organization_id = :'org_id';
DELETE FROM projectshield.teams          WHERE organization_id = :'org_id';


-- ==========================================
-- 2. Teams
-- ==========================================
INSERT INTO projectshield.teams (id, organization_id, name, description) VALUES
    ('a1b2c3d4-0001-4000-8000-000000000001', :'org_id', 'Claims Processing Team', 'Builds and maintains claims adjudication systems, handles auto-adjudication rules and provider payment workflows.'),
    ('a1b2c3d4-0001-4000-8000-000000000002', :'org_id', 'Member Portal Team', 'Develops member-facing web and mobile applications including benefits lookup, ID cards, and self-service tools.'),
    ('a1b2c3d4-0001-4000-8000-000000000003', :'org_id', 'Analytics Team', 'Data analytics, reporting solutions, and business intelligence dashboards for operational and executive stakeholders.'),
    ('a1b2c3d4-0001-4000-8000-000000000004', :'org_id', 'Platform Engineering Team', 'Infrastructure, CI/CD pipelines, cloud operations, and developer tooling. Owns shared services and deployment automation.'),
    ('a1b2c3d4-0001-4000-8000-000000000005', :'org_id', 'QA & Automation Team', 'Quality assurance, test automation frameworks, regression suites, and release validation across all product teams.'),
    ('a1b2c3d4-0001-4000-8000-000000000006', :'org_id', 'Provider Network Team', 'Systems for provider onboarding, credentialing, network adequacy reporting, and provider directory management.'),
    ('a1b2c3d4-0001-4000-8000-000000000007', :'org_id', 'Compliance & Security Team', 'Regulatory compliance tooling, audit trail systems, HIPAA controls, and security monitoring infrastructure.');


-- ==========================================
-- 3. Domains
-- ==========================================
INSERT INTO projectshield.domains (id, organization_id, name) VALUES
    ('f0000000-0001-4000-8000-000000000001', :'org_id', 'Claims Processing'),
    ('f0000000-0001-4000-8000-000000000002', :'org_id', 'Member Services'),
    ('f0000000-0001-4000-8000-000000000003', :'org_id', 'Eligibility & Enrollment'),
    ('f0000000-0001-4000-8000-000000000004', :'org_id', 'Benefits Administration'),
    ('f0000000-0001-4000-8000-000000000005', :'org_id', 'Customer Portal'),
    ('f0000000-0001-4000-8000-000000000006', :'org_id', 'Payment & Billing'),
    ('f0000000-0001-4000-8000-000000000007', :'org_id', 'Analytics & Reporting'),
    ('f0000000-0001-4000-8000-000000000008', :'org_id', 'Compliance & Regulatory'),
    ('f0000000-0001-4000-8000-000000000009', :'org_id', 'Provider Network'),
    ('f0000000-0001-4000-8000-000000000010', :'org_id', 'Care Management');


-- ==========================================
-- 4. Skills
-- ==========================================
INSERT INTO projectshield.skills (id, organization_id, name) VALUES
    ('f1000000-0001-4000-8000-000000000001', :'org_id', 'React'),
    ('f1000000-0001-4000-8000-000000000002', :'org_id', 'TypeScript'),
    ('f1000000-0001-4000-8000-000000000003', :'org_id', 'CSS'),
    ('f1000000-0001-4000-8000-000000000004', :'org_id', 'Tailwind'),
    ('f1000000-0001-4000-8000-000000000005', :'org_id', 'Node.js'),
    ('f1000000-0001-4000-8000-000000000006', :'org_id', 'Python'),
    ('f1000000-0001-4000-8000-000000000007', :'org_id', 'PostgreSQL'),
    ('f1000000-0001-4000-8000-000000000008', :'org_id', 'AWS'),
    ('f1000000-0001-4000-8000-000000000009', :'org_id', 'Figma'),
    ('f1000000-0001-4000-8000-000000000010', :'org_id', 'Sketch'),
    ('f1000000-0001-4000-8000-000000000011', :'org_id', 'User Research'),
    ('f1000000-0001-4000-8000-000000000012', :'org_id', 'MongoDB'),
    ('f1000000-0001-4000-8000-000000000013', :'org_id', 'Docker'),
    ('f1000000-0001-4000-8000-000000000014', :'org_id', 'SQL'),
    ('f1000000-0001-4000-8000-000000000015', :'org_id', 'Tableau'),
    ('f1000000-0001-4000-8000-000000000016', :'org_id', 'Excel'),
    ('f1000000-0001-4000-8000-000000000017', :'org_id', 'Java'),
    ('f1000000-0001-4000-8000-000000000018', :'org_id', 'Spring Boot'),
    ('f1000000-0001-4000-8000-000000000019', :'org_id', 'Kafka'),
    ('f1000000-0001-4000-8000-000000000020', :'org_id', 'Selenium'),
    ('f1000000-0001-4000-8000-000000000021', :'org_id', 'Cypress'),
    ('f1000000-0001-4000-8000-000000000022', :'org_id', 'Jest'),
    ('f1000000-0001-4000-8000-000000000023', :'org_id', 'API Testing'),
    ('f1000000-0001-4000-8000-000000000024', :'org_id', 'Terraform'),
    ('f1000000-0001-4000-8000-000000000025', :'org_id', 'Kubernetes'),
    ('f1000000-0001-4000-8000-000000000026', :'org_id', 'JavaScript'),
    ('f1000000-0001-4000-8000-000000000027', :'org_id', 'HTML'),
    ('f1000000-0001-4000-8000-000000000028', :'org_id', 'System Design'),
    ('f1000000-0001-4000-8000-000000000029', :'org_id', 'Agile'),
    ('f1000000-0001-4000-8000-000000000030', :'org_id', 'Scrum'),
    ('f1000000-0001-4000-8000-000000000031', :'org_id', 'JIRA'),
    ('f1000000-0001-4000-8000-000000000032', :'org_id', 'Stakeholder Management'),
    ('f1000000-0001-4000-8000-000000000033', :'org_id', 'GraphQL');


-- ==========================================
-- 5. Members (staff) — does NOT include the admin (preserved from registration)
-- ==========================================
-- Roles: admin=..01, project_manager=..02, team_lead=..03, member=..04, viewer=..05

-- Assign the admin member to the Claims Processing Team
UPDATE projectshield.members SET team_id = 'a1b2c3d4-0001-4000-8000-000000000001', seniority = 'lead', hours_per_day = 8 WHERE id = :'admin_member_id';

INSERT INTO projectshield.members (id, organization_id, clerk_user_id, first_name, last_name, middle_initial, avatar, email, phone, role_id, seniority, team_id, hours_per_day) VALUES
    -- Claims Processing Team
    ('b2c3d4e5-0001-4000-8000-000000000002', :'org_id', NULL, 'Marcus',  'Smith',         '',  'MJ', 'marcus.j@company.com',           '555-0102', '00000000-0000-4000-8000-000000000004', 'mid',    'a1b2c3d4-0001-4000-8000-000000000001', 8),
    ('b2c3d4e5-0001-4000-8000-000000000013', :'org_id', NULL, 'Priya',   'Johnson',         '',    'PP', 'priya.p@company.com',            '555-0113', '00000000-0000-4000-8000-000000000004', 'junior', 'a1b2c3d4-0001-4000-8000-000000000001', 8),
    ('b2c3d4e5-0001-4000-8000-000000000014', :'org_id', NULL, 'Tom',     'Holland',         '',  'TB', 'tom.b@company.com',              '555-0114', '00000000-0000-4000-8000-000000000003', 'lead',   'a1b2c3d4-0001-4000-8000-000000000001', 8),

    -- Member Portal Team
    ('b2c3d4e5-0001-4000-8000-000000000003', :'org_id', NULL, 'Emily',   'Rodriguez', '',        'ER', 'emily.r@company.com',            '555-0103', '00000000-0000-4000-8000-000000000004', 'senior', 'a1b2c3d4-0001-4000-8000-000000000002', 6),
    ('b2c3d4e5-0001-4000-8000-000000000004', :'org_id', NULL, 'David',   'Kim',       '',        'DK', 'david.kim@company.com',          '555-0104', '00000000-0000-4000-8000-000000000003', 'lead',   'a1b2c3d4-0001-4000-8000-000000000002', 8),
    ('b2c3d4e5-0001-4000-8000-000000000009', :'org_id', NULL, 'Amanda',  'Foster',    '',        'AF', 'amanda.f@company.com',           '555-0109', '00000000-0000-4000-8000-000000000004', 'junior', 'a1b2c3d4-0001-4000-8000-000000000002', 8),
    ('b2c3d4e5-0001-4000-8000-000000000015', :'org_id', NULL, 'Sophia',  'Lee',       '',        'SL', 'sophia.l@company.com',           '555-0115', '00000000-0000-4000-8000-000000000004', 'mid',    'a1b2c3d4-0001-4000-8000-000000000002', 8),

    -- Analytics Team
    ('b2c3d4e5-0001-4000-8000-000000000005', :'org_id', NULL, 'Lisa',    'Thompson',  '',        'LT', 'lisa.t@company.com',             '555-0105', '00000000-0000-4000-8000-000000000004', 'mid',    'a1b2c3d4-0001-4000-8000-000000000003', 8),
    ('b2c3d4e5-0001-4000-8000-000000000016', :'org_id', NULL, 'Nathan',  'Wright',    '',        'NW', 'nathan.w@company.com',           '555-0116', '00000000-0000-4000-8000-000000000004', 'senior', 'a1b2c3d4-0001-4000-8000-000000000003', 8),
    ('b2c3d4e5-0001-4000-8000-000000000017', :'org_id', NULL, 'Olivia',  'Grant',     '',        'OG', 'olivia.g@company.com',           '555-0117', '00000000-0000-4000-8000-000000000003', 'junior', 'a1b2c3d4-0001-4000-8000-000000000003', 6),

    -- Platform Engineering Team
    ('b2c3d4e5-0001-4000-8000-000000000008', :'org_id', NULL, 'Kevin',   'O''Brien',  '',        'KO', 'kevin.o@company.com',            '555-0108', '00000000-0000-4000-8000-000000000004', 'senior', 'a1b2c3d4-0001-4000-8000-000000000004', 8),
    ('b2c3d4e5-0001-4000-8000-000000000010', :'org_id', NULL, 'Michael', 'Chang',     '',        'MC', 'michael.c@company.com',          '555-0110', '00000000-0000-4000-8000-000000000003', 'lead',   'a1b2c3d4-0001-4000-8000-000000000004', 8),
    ('b2c3d4e5-0001-4000-8000-000000000018', :'org_id', NULL, 'Carlos',  'Rivera',    '',        'CR', 'carlos.r@company.com',           '555-0118', '00000000-0000-4000-8000-000000000004', 'mid',    'a1b2c3d4-0001-4000-8000-000000000004', 8),

    -- QA & Automation Team
    ('b2c3d4e5-0001-4000-8000-000000000007', :'org_id', NULL, 'Rachel',  'Martinez',  '',        'RM', 'rachel.m@company.com',           '555-0107', '00000000-0000-4000-8000-000000000004', 'mid',    'a1b2c3d4-0001-4000-8000-000000000005', 8),
    ('b2c3d4e5-0001-4000-8000-000000000019', :'org_id', NULL, 'Hannah',  'Brooks',    '',        'HB', 'hannah.b@company.com',           '555-0119', '00000000-0000-4000-8000-000000000004', 'senior', 'a1b2c3d4-0001-4000-8000-000000000005', 8),
    ('b2c3d4e5-0001-4000-8000-000000000020', :'org_id', NULL, 'Isaac',   'Nguyen',    '',        'IN', 'isaac.n@company.com',            '555-0120', '00000000-0000-4000-8000-000000000005', 'junior', 'a1b2c3d4-0001-4000-8000-000000000005', 6),

    -- Unassigned / cross-functional
    ('b2c3d4e5-0001-4000-8000-000000000006', :'org_id', NULL, 'James',   'Wilson',    '',        'JW', 'james.w@company.com',            '555-0106', '00000000-0000-4000-8000-000000000004', 'senior', NULL, 8),
    ('b2c3d4e5-0001-4000-8000-000000000011', :'org_id', NULL, 'Jennifer','Adams',     '',        'JA', 'jennifer.a@company.com',         '555-0111', '00000000-0000-4000-8000-000000000002', 'senior', NULL, 6),
    ('b2c3d4e5-0001-4000-8000-000000000012', :'org_id', NULL, 'Daniel',  'Park',      '',        'DP', 'daniel.p@company.com',           '555-0112', '00000000-0000-4000-8000-000000000004', 'mid',    NULL, 8),
    ('b2c3d4e5-0001-4000-8000-000000000021', :'org_id', NULL, 'Lauren',  'Mitchell',  '',        'LM', 'lauren.m@company.com',           '555-0121', '00000000-0000-4000-8000-000000000001', 'lead',   NULL, 8),
    ('b2c3d4e5-0001-4000-8000-000000000022', :'org_id', NULL, 'Alex',    'Hoffman',   '',        'AH', 'alex.h@company.com',             '555-0122', '00000000-0000-4000-8000-000000000004', 'mid',    NULL, 4);


-- ==========================================
-- 6. Member Domains
-- ==========================================
-- Admin member: Claims Processing, Member Services
INSERT INTO projectshield.member_domains (member_id, domain_id) VALUES
    (:'admin_member_id', 'f0000000-0001-4000-8000-000000000001'),
    (:'admin_member_id', 'f0000000-0001-4000-8000-000000000002');

INSERT INTO projectshield.member_domains (member_id, domain_id) VALUES
    -- Marcus Smith: Eligibility & Enrollment, Benefits Administration
    ('b2c3d4e5-0001-4000-8000-000000000002', 'f0000000-0001-4000-8000-000000000003'),
    ('b2c3d4e5-0001-4000-8000-000000000002', 'f0000000-0001-4000-8000-000000000004'),
    -- Priya Johnson: Claims Processing
    ('b2c3d4e5-0001-4000-8000-000000000013', 'f0000000-0001-4000-8000-000000000001'),
    -- Tom Holland: Claims Processing, Payment & Billing
    ('b2c3d4e5-0001-4000-8000-000000000014', 'f0000000-0001-4000-8000-000000000001'),
    ('b2c3d4e5-0001-4000-8000-000000000014', 'f0000000-0001-4000-8000-000000000006'),
    -- Emily Rodriguez: Member Services, Customer Portal
    ('b2c3d4e5-0001-4000-8000-000000000003', 'f0000000-0001-4000-8000-000000000002'),
    ('b2c3d4e5-0001-4000-8000-000000000003', 'f0000000-0001-4000-8000-000000000005'),
    -- David Kim: Benefits Administration, Claims Processing, Payment & Billing
    ('b2c3d4e5-0001-4000-8000-000000000004', 'f0000000-0001-4000-8000-000000000004'),
    ('b2c3d4e5-0001-4000-8000-000000000004', 'f0000000-0001-4000-8000-000000000001'),
    ('b2c3d4e5-0001-4000-8000-000000000004', 'f0000000-0001-4000-8000-000000000006'),
    -- Amanda Foster: Customer Portal, Member Services
    ('b2c3d4e5-0001-4000-8000-000000000009', 'f0000000-0001-4000-8000-000000000005'),
    ('b2c3d4e5-0001-4000-8000-000000000009', 'f0000000-0001-4000-8000-000000000002'),
    -- Sophia Lee: Customer Portal
    ('b2c3d4e5-0001-4000-8000-000000000015', 'f0000000-0001-4000-8000-000000000005'),
    -- Lisa Thompson: Analytics & Reporting, Compliance & Regulatory
    ('b2c3d4e5-0001-4000-8000-000000000005', 'f0000000-0001-4000-8000-000000000007'),
    ('b2c3d4e5-0001-4000-8000-000000000005', 'f0000000-0001-4000-8000-000000000008'),
    -- Nathan Wright: Analytics & Reporting, Care Management
    ('b2c3d4e5-0001-4000-8000-000000000016', 'f0000000-0001-4000-8000-000000000007'),
    ('b2c3d4e5-0001-4000-8000-000000000016', 'f0000000-0001-4000-8000-000000000010'),
    -- Olivia Grant: Analytics & Reporting
    ('b2c3d4e5-0001-4000-8000-000000000017', 'f0000000-0001-4000-8000-000000000007'),
    -- Kevin O'Brien: Analytics & Reporting, Compliance & Regulatory
    ('b2c3d4e5-0001-4000-8000-000000000008', 'f0000000-0001-4000-8000-000000000007'),
    ('b2c3d4e5-0001-4000-8000-000000000008', 'f0000000-0001-4000-8000-000000000008'),
    -- Michael Chang: Claims Processing, Payment & Billing, Provider Network
    ('b2c3d4e5-0001-4000-8000-000000000010', 'f0000000-0001-4000-8000-000000000001'),
    ('b2c3d4e5-0001-4000-8000-000000000010', 'f0000000-0001-4000-8000-000000000006'),
    ('b2c3d4e5-0001-4000-8000-000000000010', 'f0000000-0001-4000-8000-000000000009'),
    -- Carlos Rivera: Provider Network
    ('b2c3d4e5-0001-4000-8000-000000000018', 'f0000000-0001-4000-8000-000000000009'),
    -- Rachel Martinez: Eligibility & Enrollment, Member Services
    ('b2c3d4e5-0001-4000-8000-000000000007', 'f0000000-0001-4000-8000-000000000003'),
    ('b2c3d4e5-0001-4000-8000-000000000007', 'f0000000-0001-4000-8000-000000000002'),
    -- Hannah Brooks: Compliance & Regulatory
    ('b2c3d4e5-0001-4000-8000-000000000019', 'f0000000-0001-4000-8000-000000000008'),
    -- Isaac Nguyen: Customer Portal
    ('b2c3d4e5-0001-4000-8000-000000000020', 'f0000000-0001-4000-8000-000000000005'),
    -- James Wilson: Payment & Billing, Claims Processing
    ('b2c3d4e5-0001-4000-8000-000000000006', 'f0000000-0001-4000-8000-000000000006'),
    ('b2c3d4e5-0001-4000-8000-000000000006', 'f0000000-0001-4000-8000-000000000001'),
    -- Jennifer Adams: Benefits Administration, Care Management
    ('b2c3d4e5-0001-4000-8000-000000000011', 'f0000000-0001-4000-8000-000000000004'),
    ('b2c3d4e5-0001-4000-8000-000000000011', 'f0000000-0001-4000-8000-000000000010'),
    -- Daniel Park: Eligibility & Enrollment, Compliance & Regulatory
    ('b2c3d4e5-0001-4000-8000-000000000012', 'f0000000-0001-4000-8000-000000000003'),
    ('b2c3d4e5-0001-4000-8000-000000000012', 'f0000000-0001-4000-8000-000000000008'),
    -- Lauren Mitchell: Claims Processing, Member Services, Provider Network
    ('b2c3d4e5-0001-4000-8000-000000000021', 'f0000000-0001-4000-8000-000000000001'),
    ('b2c3d4e5-0001-4000-8000-000000000021', 'f0000000-0001-4000-8000-000000000002'),
    ('b2c3d4e5-0001-4000-8000-000000000021', 'f0000000-0001-4000-8000-000000000009'),
    -- Alex Hoffman: Member Services
    ('b2c3d4e5-0001-4000-8000-000000000022', 'f0000000-0001-4000-8000-000000000002');


-- ==========================================
-- 7. Member Skills
-- ==========================================
-- Admin member: React, TypeScript, CSS, Tailwind
INSERT INTO projectshield.member_skills (member_id, skill_id) VALUES
    (:'admin_member_id', 'f1000000-0001-4000-8000-000000000001'),
    (:'admin_member_id', 'f1000000-0001-4000-8000-000000000002'),
    (:'admin_member_id', 'f1000000-0001-4000-8000-000000000003'),
    (:'admin_member_id', 'f1000000-0001-4000-8000-000000000004');

INSERT INTO projectshield.member_skills (member_id, skill_id) VALUES
    -- Marcus Smith: Node.js, Python, PostgreSQL, AWS
    ('b2c3d4e5-0001-4000-8000-000000000002', 'f1000000-0001-4000-8000-000000000005'),
    ('b2c3d4e5-0001-4000-8000-000000000002', 'f1000000-0001-4000-8000-000000000006'),
    ('b2c3d4e5-0001-4000-8000-000000000002', 'f1000000-0001-4000-8000-000000000007'),
    ('b2c3d4e5-0001-4000-8000-000000000002', 'f1000000-0001-4000-8000-000000000008'),
    -- Priya Johnson: Java, Spring Boot, SQL
    ('b2c3d4e5-0001-4000-8000-000000000013', 'f1000000-0001-4000-8000-000000000017'),
    ('b2c3d4e5-0001-4000-8000-000000000013', 'f1000000-0001-4000-8000-000000000018'),
    ('b2c3d4e5-0001-4000-8000-000000000013', 'f1000000-0001-4000-8000-000000000014'),
    -- Tom Holland: Java, Spring Boot, PostgreSQL, Kafka, System Design
    ('b2c3d4e5-0001-4000-8000-000000000014', 'f1000000-0001-4000-8000-000000000017'),
    ('b2c3d4e5-0001-4000-8000-000000000014', 'f1000000-0001-4000-8000-000000000018'),
    ('b2c3d4e5-0001-4000-8000-000000000014', 'f1000000-0001-4000-8000-000000000007'),
    ('b2c3d4e5-0001-4000-8000-000000000014', 'f1000000-0001-4000-8000-000000000019'),
    ('b2c3d4e5-0001-4000-8000-000000000014', 'f1000000-0001-4000-8000-000000000028'),
    -- Emily Rodriguez: Figma, Sketch, CSS, User Research
    ('b2c3d4e5-0001-4000-8000-000000000003', 'f1000000-0001-4000-8000-000000000009'),
    ('b2c3d4e5-0001-4000-8000-000000000003', 'f1000000-0001-4000-8000-000000000010'),
    ('b2c3d4e5-0001-4000-8000-000000000003', 'f1000000-0001-4000-8000-000000000003'),
    ('b2c3d4e5-0001-4000-8000-000000000003', 'f1000000-0001-4000-8000-000000000011'),
    -- David Kim: React, Node.js, MongoDB, Docker
    ('b2c3d4e5-0001-4000-8000-000000000004', 'f1000000-0001-4000-8000-000000000001'),
    ('b2c3d4e5-0001-4000-8000-000000000004', 'f1000000-0001-4000-8000-000000000005'),
    ('b2c3d4e5-0001-4000-8000-000000000004', 'f1000000-0001-4000-8000-000000000012'),
    ('b2c3d4e5-0001-4000-8000-000000000004', 'f1000000-0001-4000-8000-000000000013'),
    -- Amanda Foster: React, JavaScript, CSS, HTML
    ('b2c3d4e5-0001-4000-8000-000000000009', 'f1000000-0001-4000-8000-000000000001'),
    ('b2c3d4e5-0001-4000-8000-000000000009', 'f1000000-0001-4000-8000-000000000026'),
    ('b2c3d4e5-0001-4000-8000-000000000009', 'f1000000-0001-4000-8000-000000000003'),
    ('b2c3d4e5-0001-4000-8000-000000000009', 'f1000000-0001-4000-8000-000000000027'),
    -- Sophia Lee: React, TypeScript, Tailwind, GraphQL
    ('b2c3d4e5-0001-4000-8000-000000000015', 'f1000000-0001-4000-8000-000000000001'),
    ('b2c3d4e5-0001-4000-8000-000000000015', 'f1000000-0001-4000-8000-000000000002'),
    ('b2c3d4e5-0001-4000-8000-000000000015', 'f1000000-0001-4000-8000-000000000004'),
    ('b2c3d4e5-0001-4000-8000-000000000015', 'f1000000-0001-4000-8000-000000000033'),
    -- Lisa Thompson: Python, SQL, Tableau, Excel
    ('b2c3d4e5-0001-4000-8000-000000000005', 'f1000000-0001-4000-8000-000000000006'),
    ('b2c3d4e5-0001-4000-8000-000000000005', 'f1000000-0001-4000-8000-000000000014'),
    ('b2c3d4e5-0001-4000-8000-000000000005', 'f1000000-0001-4000-8000-000000000015'),
    ('b2c3d4e5-0001-4000-8000-000000000005', 'f1000000-0001-4000-8000-000000000016'),
    -- Nathan Wright: Python, PostgreSQL, Tableau, AWS
    ('b2c3d4e5-0001-4000-8000-000000000016', 'f1000000-0001-4000-8000-000000000006'),
    ('b2c3d4e5-0001-4000-8000-000000000016', 'f1000000-0001-4000-8000-000000000007'),
    ('b2c3d4e5-0001-4000-8000-000000000016', 'f1000000-0001-4000-8000-000000000015'),
    ('b2c3d4e5-0001-4000-8000-000000000016', 'f1000000-0001-4000-8000-000000000008'),
    -- Olivia Grant: Python, SQL, Excel
    ('b2c3d4e5-0001-4000-8000-000000000017', 'f1000000-0001-4000-8000-000000000006'),
    ('b2c3d4e5-0001-4000-8000-000000000017', 'f1000000-0001-4000-8000-000000000014'),
    ('b2c3d4e5-0001-4000-8000-000000000017', 'f1000000-0001-4000-8000-000000000016'),
    -- Kevin O'Brien: AWS, Terraform, Docker, Kubernetes
    ('b2c3d4e5-0001-4000-8000-000000000008', 'f1000000-0001-4000-8000-000000000008'),
    ('b2c3d4e5-0001-4000-8000-000000000008', 'f1000000-0001-4000-8000-000000000024'),
    ('b2c3d4e5-0001-4000-8000-000000000008', 'f1000000-0001-4000-8000-000000000013'),
    ('b2c3d4e5-0001-4000-8000-000000000008', 'f1000000-0001-4000-8000-000000000025'),
    -- Michael Chang: Java, Python, System Design, AWS
    ('b2c3d4e5-0001-4000-8000-000000000010', 'f1000000-0001-4000-8000-000000000017'),
    ('b2c3d4e5-0001-4000-8000-000000000010', 'f1000000-0001-4000-8000-000000000006'),
    ('b2c3d4e5-0001-4000-8000-000000000010', 'f1000000-0001-4000-8000-000000000028'),
    ('b2c3d4e5-0001-4000-8000-000000000010', 'f1000000-0001-4000-8000-000000000008'),
    -- Carlos Rivera: Docker, Kubernetes, Terraform, AWS
    ('b2c3d4e5-0001-4000-8000-000000000018', 'f1000000-0001-4000-8000-000000000013'),
    ('b2c3d4e5-0001-4000-8000-000000000018', 'f1000000-0001-4000-8000-000000000025'),
    ('b2c3d4e5-0001-4000-8000-000000000018', 'f1000000-0001-4000-8000-000000000024'),
    ('b2c3d4e5-0001-4000-8000-000000000018', 'f1000000-0001-4000-8000-000000000008'),
    -- Rachel Martinez: Selenium, Cypress, Jest, API Testing
    ('b2c3d4e5-0001-4000-8000-000000000007', 'f1000000-0001-4000-8000-000000000020'),
    ('b2c3d4e5-0001-4000-8000-000000000007', 'f1000000-0001-4000-8000-000000000021'),
    ('b2c3d4e5-0001-4000-8000-000000000007', 'f1000000-0001-4000-8000-000000000022'),
    ('b2c3d4e5-0001-4000-8000-000000000007', 'f1000000-0001-4000-8000-000000000023'),
    -- Hannah Brooks: Selenium, Cypress, API Testing, Java
    ('b2c3d4e5-0001-4000-8000-000000000019', 'f1000000-0001-4000-8000-000000000020'),
    ('b2c3d4e5-0001-4000-8000-000000000019', 'f1000000-0001-4000-8000-000000000021'),
    ('b2c3d4e5-0001-4000-8000-000000000019', 'f1000000-0001-4000-8000-000000000023'),
    ('b2c3d4e5-0001-4000-8000-000000000019', 'f1000000-0001-4000-8000-000000000017'),
    -- Isaac Nguyen: Cypress, Jest
    ('b2c3d4e5-0001-4000-8000-000000000020', 'f1000000-0001-4000-8000-000000000021'),
    ('b2c3d4e5-0001-4000-8000-000000000020', 'f1000000-0001-4000-8000-000000000022'),
    -- James Wilson: Java, Spring Boot, PostgreSQL, Kafka
    ('b2c3d4e5-0001-4000-8000-000000000006', 'f1000000-0001-4000-8000-000000000017'),
    ('b2c3d4e5-0001-4000-8000-000000000006', 'f1000000-0001-4000-8000-000000000018'),
    ('b2c3d4e5-0001-4000-8000-000000000006', 'f1000000-0001-4000-8000-000000000007'),
    ('b2c3d4e5-0001-4000-8000-000000000006', 'f1000000-0001-4000-8000-000000000019'),
    -- Jennifer Adams: Agile, Scrum, JIRA, Stakeholder Management
    ('b2c3d4e5-0001-4000-8000-000000000011', 'f1000000-0001-4000-8000-000000000029'),
    ('b2c3d4e5-0001-4000-8000-000000000011', 'f1000000-0001-4000-8000-000000000030'),
    ('b2c3d4e5-0001-4000-8000-000000000011', 'f1000000-0001-4000-8000-000000000031'),
    ('b2c3d4e5-0001-4000-8000-000000000011', 'f1000000-0001-4000-8000-000000000032'),
    -- Daniel Park: React, Node.js, PostgreSQL, GraphQL
    ('b2c3d4e5-0001-4000-8000-000000000012', 'f1000000-0001-4000-8000-000000000001'),
    ('b2c3d4e5-0001-4000-8000-000000000012', 'f1000000-0001-4000-8000-000000000005'),
    ('b2c3d4e5-0001-4000-8000-000000000012', 'f1000000-0001-4000-8000-000000000007'),
    ('b2c3d4e5-0001-4000-8000-000000000012', 'f1000000-0001-4000-8000-000000000033'),
    -- Lauren Mitchell: Agile, Stakeholder Management, System Design, JIRA
    ('b2c3d4e5-0001-4000-8000-000000000021', 'f1000000-0001-4000-8000-000000000029'),
    ('b2c3d4e5-0001-4000-8000-000000000021', 'f1000000-0001-4000-8000-000000000032'),
    ('b2c3d4e5-0001-4000-8000-000000000021', 'f1000000-0001-4000-8000-000000000028'),
    ('b2c3d4e5-0001-4000-8000-000000000021', 'f1000000-0001-4000-8000-000000000031'),
    -- Alex Hoffman: React, TypeScript, Node.js
    ('b2c3d4e5-0001-4000-8000-000000000022', 'f1000000-0001-4000-8000-000000000001'),
    ('b2c3d4e5-0001-4000-8000-000000000022', 'f1000000-0001-4000-8000-000000000002'),
    ('b2c3d4e5-0001-4000-8000-000000000022', 'f1000000-0001-4000-8000-000000000005');


-- ==========================================
-- 8. Phases
-- ==========================================
INSERT INTO projectshield.phases (id, organization_id, name, description, start_date, end_date, type) VALUES
    ('c3d4e5f6-0001-4000-8000-000000000001', :'org_id', 'Q1 2025', 'Focus on customer acquisition and platform stability', '2025-01-01', '2025-03-31', 'QUARTER'),
    ('c3d4e5f6-0001-4000-8000-000000000002', :'org_id', 'Q2 2025', 'Scale infrastructure and improve developer experience', '2025-04-01', '2025-06-30', 'QUARTER'),
    ('c3d4e5f6-0001-4000-8000-000000000003', :'org_id', 'Q3 2025', 'Regulatory compliance push and provider network expansion', '2025-07-01', '2025-09-30', 'QUARTER'),
    ('c3d4e5f6-0001-4000-8000-000000000004', :'org_id', 'Q4 2025', 'Year-end hardening, analytics maturity, and 2026 roadmap prep', '2025-10-01', '2025-12-31', 'QUARTER'),
    ('c3d4e5f6-0001-4000-8000-000000000005', :'org_id', 'H1 2025', 'First-half strategic focus: acquisition, infrastructure, and compliance foundations', '2025-01-01', '2025-06-30', 'HALF'),
    ('c3d4e5f6-0001-4000-8000-000000000006', :'org_id', 'H2 2025', 'Second-half strategic focus: regulatory readiness, network growth, and platform maturity', '2025-07-01', '2025-12-31', 'HALF'),
    ('c3d4e5f6-0001-4000-8000-000000000007', :'org_id', 'FY 2025', 'Annual strategic plan: modernize core platform, achieve regulatory compliance, and expand provider network', '2025-01-01', '2025-12-31', 'ANNUAL');


-- ==========================================
-- 9. Projects
-- ==========================================
INSERT INTO projectshield.projects (id, organization_id, phase_id, name, description, justification, sponsor, estimated_budget, start_date, end_date, overall_score, stack_rank, status, rating_strategic_alignment, rating_financial_benefit, rating_risk_profile, rating_feasibility, rating_regulatory_compliance) VALUES
    -- Q1 2025
    ('d4e5f6a7-0001-4000-8000-000000000101', :'org_id', 'c3d4e5f6-0001-4000-8000-000000000001', 'Customer Portal Redesign', 'Complete overhaul of the customer-facing portal with modern UX', 'Current portal has 40% abandonment rate. Redesign expected to improve conversion by 25%.', 'VP of Product', 150000.00, '2025-01-06', '2025-03-14', 4.20, 1, 'ACCEPTED', 4, 5, 3, 5, 4),
    ('d4e5f6a7-0001-4000-8000-000000000102', :'org_id', 'c3d4e5f6-0001-4000-8000-000000000001', 'API Rate Limiting', 'Implement comprehensive rate limiting across all public APIs', 'Security audit identified this as critical. Prevents abuse and ensures fair usage.', 'CTO', 45000.00, '2025-02-03', '2025-02-28', 3.80, 2, 'ACCEPTED', 5, 2, 4, 3, 5),
    ('d4e5f6a7-0001-4000-8000-000000000103', :'org_id', 'c3d4e5f6-0001-4000-8000-000000000001', 'Legacy Database Migration', 'Migrate from MySQL 5.7 to PostgreSQL 15', 'MySQL 5.7 EOL in Oct 2023. PostgreSQL offers better JSON support and performance.', 'VP of Engineering', 80000.00, '2025-02-17', '2025-03-28', 3.40, 3, 'STRATEGIC', 5, 2, 5, 2, 3),
    ('d4e5f6a7-0001-4000-8000-000000000104', :'org_id', 'c3d4e5f6-0001-4000-8000-000000000001', 'Mobile App Refresh', 'Update mobile app with new design system and performance improvements', 'App store ratings dropped to 3.2 stars. Competitors averaging 4.5 stars.', 'Head of Mobile', 120000.00, '2025-01-13', '2025-03-21', 3.20, 4, 'REJECTED', 3, 4, 3, 4, 2),
    ('d4e5f6a7-0001-4000-8000-000000000105', :'org_id', 'c3d4e5f6-0001-4000-8000-000000000001', 'Payment Gateway Integration', 'Integrate Stripe and PayPal payment gateways for expanded payment options', 'Customer requests for alternative payment methods increased 60% this quarter.', 'VP of Product', 95000.00, '2025-01-20', '2025-02-28', 4.00, 5, 'ACCEPTED', 4, 5, 3, 4, 5),
    ('d4e5f6a7-0001-4000-8000-000000000106', :'org_id', 'c3d4e5f6-0001-4000-8000-000000000001', 'Search Infrastructure Upgrade', 'Migrate from Elasticsearch 6 to OpenSearch with improved relevance', 'Current search has 2s average response time. Target is under 200ms.', 'CTO', 65000.00, '2025-02-10', '2025-03-21', 3.60, 6, 'ACCEPTED', 3, 3, 5, 4, 3),
    ('d4e5f6a7-0001-4000-8000-000000000107', :'org_id', 'c3d4e5f6-0001-4000-8000-000000000001', 'SSO Implementation', 'Implement SAML and OIDC single sign-on for enterprise customers', 'Top 5 enterprise prospects require SSO. Potential ARR of $2M.', 'VP of Sales', 55000.00, '2025-01-06', '2025-02-14', 4.40, 7, 'ACCEPTED', 5, 5, 3, 4, 5),
    ('d4e5f6a7-0001-4000-8000-000000000108', :'org_id', 'c3d4e5f6-0001-4000-8000-000000000001', 'Analytics Dashboard v2', 'Rebuild analytics dashboard with real-time data and custom reports', 'Current dashboard lacks flexibility. Support tickets about reporting up 40%.', 'VP of Product', 85000.00, '2025-01-27', '2025-03-14', 3.90, 8, 'ACCEPTED', 4, 4, 3, 5, 2),
    ('d4e5f6a7-0001-4000-8000-000000000109', :'org_id', 'c3d4e5f6-0001-4000-8000-000000000001', 'Notification Service Overhaul', 'Replace legacy notification system with event-driven architecture', 'Current system has 5% message loss rate and no retry mechanism.', 'VP of Engineering', 70000.00, '2025-02-24', '2025-03-28', 3.50, 9, 'ACCEPTED', 4, 2, 5, 4, 3),
    -- Q2 2025
    ('d4e5f6a7-0001-4000-8000-000000000201', :'org_id', 'c3d4e5f6-0001-4000-8000-000000000002', 'CI/CD Pipeline Overhaul', 'Modernize build and deployment pipeline with improved testing', 'Current pipeline takes 45 minutes. Target is under 10 minutes.', 'VP of Engineering', 60000.00, '2025-04-07', '2025-05-16', 3.90, 1, 'ACCEPTED', 5, 2, 5, 2, 3),
    ('d4e5f6a7-0001-4000-8000-000000000202', :'org_id', 'c3d4e5f6-0001-4000-8000-000000000002', 'Internal Developer Portal', 'Build self-service portal for developer tools and documentation', 'Reduce onboarding time from 2 weeks to 3 days.', 'CTO', 75000.00, '2025-04-14', '2025-06-20', 3.50, 2, 'STRATEGIC', 4, 1, 4, 3, 2);


-- ==========================================
-- 10. Staffing Needs
-- ==========================================
INSERT INTO projectshield.staffing_needs (id, project_id, role, count, duration_weeks) VALUES
    -- Customer Portal Redesign
    ('e5f6a7b8-0001-4000-8000-000000000001', 'd4e5f6a7-0001-4000-8000-000000000101', 'Frontend Developer', 2, 8),
    ('e5f6a7b8-0001-4000-8000-000000000002', 'd4e5f6a7-0001-4000-8000-000000000101', 'UI/UX Designer', 1, 6),
    ('e5f6a7b8-0001-4000-8000-000000000003', 'd4e5f6a7-0001-4000-8000-000000000101', 'Backend Developer', 1, 4),
    -- API Rate Limiting
    ('e5f6a7b8-0001-4000-8000-000000000004', 'd4e5f6a7-0001-4000-8000-000000000102', 'Backend Developer', 2, 4),
    -- Legacy Database Migration
    ('e5f6a7b8-0001-4000-8000-000000000005', 'd4e5f6a7-0001-4000-8000-000000000103', 'Backend Developer', 2, 6),
    ('e5f6a7b8-0001-4000-8000-000000000006', 'd4e5f6a7-0001-4000-8000-000000000103', 'DevOps Engineer', 1, 4),
    -- Mobile App Refresh
    ('e5f6a7b8-0001-4000-8000-000000000007', 'd4e5f6a7-0001-4000-8000-000000000104', 'Frontend Developer', 2, 10),
    ('e5f6a7b8-0001-4000-8000-000000000008', 'd4e5f6a7-0001-4000-8000-000000000104', 'UI/UX Designer', 1, 8),
    ('e5f6a7b8-0001-4000-8000-000000000009', 'd4e5f6a7-0001-4000-8000-000000000104', 'QA Engineer', 1, 8),
    -- Payment Gateway Integration
    ('e5f6a7b8-0001-4000-8000-000000000010', 'd4e5f6a7-0001-4000-8000-000000000105', 'Backend Developer', 2, 6),
    ('e5f6a7b8-0001-4000-8000-000000000011', 'd4e5f6a7-0001-4000-8000-000000000105', 'Frontend Developer', 1, 4),
    ('e5f6a7b8-0001-4000-8000-000000000012', 'd4e5f6a7-0001-4000-8000-000000000105', 'QA Engineer', 1, 4),
    -- Search Infrastructure Upgrade
    ('e5f6a7b8-0001-4000-8000-000000000013', 'd4e5f6a7-0001-4000-8000-000000000106', 'Backend Developer', 2, 5),
    ('e5f6a7b8-0001-4000-8000-000000000014', 'd4e5f6a7-0001-4000-8000-000000000106', 'DevOps Engineer', 1, 4),
    -- SSO Implementation
    ('e5f6a7b8-0001-4000-8000-000000000015', 'd4e5f6a7-0001-4000-8000-000000000107', 'Backend Developer', 2, 5),
    ('e5f6a7b8-0001-4000-8000-000000000016', 'd4e5f6a7-0001-4000-8000-000000000107', 'Frontend Developer', 1, 3),
    -- Analytics Dashboard v2
    ('e5f6a7b8-0001-4000-8000-000000000017', 'd4e5f6a7-0001-4000-8000-000000000108', 'Frontend Developer', 2, 6),
    ('e5f6a7b8-0001-4000-8000-000000000018', 'd4e5f6a7-0001-4000-8000-000000000108', 'Backend Developer', 1, 4),
    ('e5f6a7b8-0001-4000-8000-000000000019', 'd4e5f6a7-0001-4000-8000-000000000108', 'Data Analyst', 1, 4),
    -- Notification Service Overhaul
    ('e5f6a7b8-0001-4000-8000-000000000020', 'd4e5f6a7-0001-4000-8000-000000000109', 'Backend Developer', 2, 4),
    ('e5f6a7b8-0001-4000-8000-000000000021', 'd4e5f6a7-0001-4000-8000-000000000109', 'DevOps Engineer', 1, 3),
    -- CI/CD Pipeline Overhaul
    ('e5f6a7b8-0001-4000-8000-000000000022', 'd4e5f6a7-0001-4000-8000-000000000201', 'DevOps Engineer', 2, 6),
    -- Internal Developer Portal
    ('e5f6a7b8-0001-4000-8000-000000000023', 'd4e5f6a7-0001-4000-8000-000000000202', 'Frontend Developer', 1, 8),
    ('e5f6a7b8-0001-4000-8000-000000000024', 'd4e5f6a7-0001-4000-8000-000000000202', 'Backend Developer', 1, 10);

COMMIT;

-- Summary
DO $$
DECLARE
    v_org_id UUID;
    member_count INTEGER;
    team_count INTEGER;
    skill_count INTEGER;
    domain_count INTEGER;
    project_count INTEGER;
BEGIN
    SELECT id INTO v_org_id FROM projectshield.organizations LIMIT 1;
    SELECT COUNT(*) INTO member_count  FROM projectshield.members       WHERE organization_id = v_org_id;
    SELECT COUNT(*) INTO team_count    FROM projectshield.teams         WHERE organization_id = v_org_id;
    SELECT COUNT(*) INTO skill_count   FROM projectshield.skills        WHERE organization_id = v_org_id;
    SELECT COUNT(*) INTO domain_count  FROM projectshield.domains       WHERE organization_id = v_org_id;
    SELECT COUNT(*) INTO project_count FROM projectshield.projects      WHERE organization_id = v_org_id;

    RAISE NOTICE '';
    RAISE NOTICE '=== Test Data Loaded ===';
    RAISE NOTICE '  Members:  %', member_count;
    RAISE NOTICE '  Teams:    %', team_count;
    RAISE NOTICE '  Skills:   %', skill_count;
    RAISE NOTICE '  Domains:  %', domain_count;
    RAISE NOTICE '  Projects: %', project_count;
    RAISE NOTICE '========================';
END $$;

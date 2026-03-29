-- Seed roles for tests (Hibernate import.sql runs after schema creation with ddl-auto)
INSERT INTO roles (id, name) VALUES ('00000000-0000-4000-8000-000000000001', 'admin');
INSERT INTO roles (id, name) VALUES ('00000000-0000-4000-8000-000000000002', 'project_manager');
INSERT INTO roles (id, name) VALUES ('00000000-0000-4000-8000-000000000003', 'team_lead');
INSERT INTO roles (id, name) VALUES ('00000000-0000-4000-8000-000000000004', 'member');
INSERT INTO roles (id, name) VALUES ('00000000-0000-4000-8000-000000000005', 'viewer');

-- Seed test organization (matches org_id claim in TestSecurityConfig JWT)
INSERT INTO organizations (id, clerk_organization_id, name, slug, created_at, updated_at) VALUES ('00000000-0000-4000-8000-000000000010', 'org_test_default', 'Test Organization', 'test-org', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

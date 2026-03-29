CREATE DATABASE projectshield;

CREATE ROLE ps_migrate WITH LOGIN PASSWORD 'EW1sYWnkNM';

GRANT CONNECT ON DATABASE projectshield TO ps_migrate;

-- Grant ALL privileges (includes both DDL and DML) on all existing tables/views
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA projectshield_core TO ps_migrate;

-- Grant ALL privileges on all existing sequences (used for auto-incrementing IDs like primary keys)
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA projectshield_core TO ps_migrate;

-- Grant USAGE and CREATE privileges on the schema itself (required for DDL on new objects)
GRANT ALL PRIVILEGES ON SCHEMA projectshield_core TO ps_migrate;

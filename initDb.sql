CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE USER pguser WITH PASSWORD 'funnyBiscuit3000';
GRANT CONNECT ON DATABASE predictivegrowth TO pguser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO pguser;

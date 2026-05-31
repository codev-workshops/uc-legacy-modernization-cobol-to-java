-- Initialize additional databases for CardDemo
-- The default 'carddemo' database is created by POSTGRES_DB env var.
-- This script creates the batch database.

CREATE DATABASE carddemo_batch;
GRANT ALL PRIVILEGES ON DATABASE carddemo_batch TO carddemo;

-- Phase 1: Baseline schema to allow Flyway to run successfully
-- In Phase 2, we will create the actual tables.

CREATE TABLE IF NOT EXISTS flyway_baseline (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

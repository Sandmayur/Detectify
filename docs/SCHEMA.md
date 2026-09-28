# PostgreSQL Schema

## Entity Relationship Summary
The schema uses standard normalized tables. All primary identifiers exposed to the API (like for results and reports) use UUIDs.
- `users`: Stores account information and hashed passwords.
- `refresh_tokens`: Stores hashed refresh tokens for session management.
- `companies`: Primary entity for a company, identified ideally by registered domain or normalized name.
- `company_checks`: Stores the overall risk result of a check (linked to a user if logged in).
- `analysis_signals`: Stores individual analyzer results tied to a `company_check`.
- `reports`: Community submitted reports against a company.
- `report_votes`: Tracks user upvotes on reports (unique per user per report).
- `report_flags`: Tracks user flags on reports for moderation.
- `disputes`: Appeals made by companies against reports/scores.
- `scam_keywords`: Configurable keywords used by the text analyzer.
- `domain_cache`, `registration_cache`, `careers_cache`: Caches for external data.

## Tables

### `users`
- `id` (UUID, PK)
- `email` (VARCHAR, UNIQUE, NOT NULL)
- `password_hash` (VARCHAR, NOT NULL)
- `role` (VARCHAR, NOT NULL) - 'USER' or 'ADMIN'
- `is_suspended` (BOOLEAN, DEFAULT false)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### `refresh_tokens`
- `id` (UUID, PK)
- `user_id` (UUID, FK -> users.id, NOT NULL)
- `token_hash` (VARCHAR, UNIQUE, NOT NULL)
- `expires_at` (TIMESTAMP, NOT NULL)
- `created_at` (TIMESTAMP)

### `companies`
- `id` (UUID, PK)
- `domain` (VARCHAR, UNIQUE, NULLABLE) - Primary identity if available.
- `normalized_name` (VARCHAR, NOT NULL)
- `original_name` (VARCHAR)
- `is_demo` (BOOLEAN, DEFAULT false)
- `created_at` (TIMESTAMP)

### `company_checks`
- `id` (UUID, PK) - Exposed in URL `/results/:id`
- `company_id` (UUID, FK -> companies.id)
- `user_id` (UUID, FK -> users.id, NULLABLE) - Null for anonymous checks
- `rules_version` (VARCHAR, NOT NULL)
- `final_score` (INTEGER, NOT NULL)
- `risk_category` (VARCHAR, NOT NULL) - LOW, MEDIUM, HIGH
- `created_at` (TIMESTAMP)

### `analysis_signals`
- `id` (UUID, PK)
- `check_id` (UUID, FK -> company_checks.id, NOT NULL)
- `signal_name` (VARCHAR, NOT NULL)
- `signal_type` (VARCHAR, NOT NULL) - RED_FLAG, WARNING, POSITIVE, NEUTRAL, UNKNOWN
- `points` (INTEGER, NOT NULL)
- `explanation` (TEXT)
- `evidence_json` (JSONB)
- `confidence` (VARCHAR) - LOW, MEDIUM, HIGH
- `data_source` (VARCHAR)
- `created_at` (TIMESTAMP)

### `reports`
- `id` (UUID, PK)
- `company_id` (UUID, FK -> companies.id, NOT NULL)
- `user_id` (UUID, FK -> users.id, NOT NULL)
- `status` (VARCHAR, NOT NULL) - PENDING, APPROVED, REJECTED
- `description` (TEXT, NOT NULL)
- `job_url` (VARCHAR)
- `recruiter_email` (VARCHAR)
- `recruiter_phone` (VARCHAR)
- `evidence_url` (VARCHAR) - S3/Cloudinary URL
- `net_upvotes` (INTEGER, DEFAULT 0)
- `created_at` (TIMESTAMP)

### `report_votes`
- `id` (UUID, PK)
- `report_id` (UUID, FK -> reports.id, NOT NULL)
- `user_id` (UUID, FK -> users.id, NOT NULL)
- *Unique Constraint: (report_id, user_id)*

### `report_flags`
- `id` (UUID, PK)
- `report_id` (UUID, FK -> reports.id, NOT NULL)
- `user_id` (UUID, FK -> users.id, NOT NULL)
- `reason` (VARCHAR)
- *Unique Constraint: (report_id, user_id)*

### `disputes`
- `id` (UUID, PK)
- `report_id` (UUID, FK -> reports.id, NULLABLE)
- `company_id` (UUID, FK -> companies.id, NOT NULL)
- `contact_email` (VARCHAR, NOT NULL)
- `reason` (TEXT, NOT NULL)
- `status` (VARCHAR, NOT NULL) - PENDING, REVIEWED
- `created_at` (TIMESTAMP)

### `scam_keywords`
- `id` (UUID, PK)
- `keyword` (VARCHAR, UNIQUE, NOT NULL)
- `category` (VARCHAR, NOT NULL) - FEE_DEMAND, PATTERN
- `weight` (INTEGER, NOT NULL)
- `is_active` (BOOLEAN, DEFAULT true)

### `domain_cache`
- `domain` (VARCHAR, PK)
- `creation_date` (TIMESTAMP)
- `is_https_valid` (BOOLEAN)
- `fetched_at` (TIMESTAMP, NOT NULL)

### `registration_cache`
- `registration_id` (VARCHAR, PK)
- `company_name` (VARCHAR, NOT NULL)
- `status` (VARCHAR, NOT NULL)
- `source` (VARCHAR, NOT NULL)
- `fetched_at` (TIMESTAMP, NOT NULL)

### `careers_cache`
- `domain` (VARCHAR, PK)
- `job_title` (VARCHAR, PK)
- `status` (VARCHAR, NOT NULL) - MATCHED, NOT_FOUND, UNKNOWN
- `fetched_at` (TIMESTAMP, NOT NULL)

### `audit_logs`
- `id` (UUID, PK)
- `admin_user_id` (UUID, FK -> users.id, NOT NULL)
- `action` (VARCHAR, NOT NULL)
- `target_id` (VARCHAR)
- `details` (TEXT)
- `created_at` (TIMESTAMP)

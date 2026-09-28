CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_suspended BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    domain VARCHAR(255) UNIQUE,
    normalized_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),
    is_demo BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_companies_normalized_name ON companies(normalized_name);

CREATE TABLE company_checks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID REFERENCES companies(id) ON DELETE SET NULL,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    rules_version VARCHAR(50) NOT NULL,
    final_score INTEGER NOT NULL,
    risk_category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_company_checks_company_id ON company_checks(company_id);
CREATE INDEX idx_company_checks_user_id ON company_checks(user_id);

CREATE TABLE analysis_signals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    check_id UUID NOT NULL REFERENCES company_checks(id) ON DELETE CASCADE,
    signal_name VARCHAR(100) NOT NULL,
    signal_type VARCHAR(50) NOT NULL,
    points INTEGER NOT NULL,
    explanation TEXT,
    evidence_json JSONB,
    confidence VARCHAR(50),
    data_source VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_analysis_signals_check_id ON analysis_signals(check_id);

CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    job_url VARCHAR(1024),
    recruiter_email VARCHAR(255),
    recruiter_phone VARCHAR(50),
    evidence_url VARCHAR(1024),
    net_upvotes INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_reports_company_id ON reports(company_id);
CREATE INDEX idx_reports_user_id ON reports(user_id);

CREATE TABLE report_votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(report_id, user_id)
);

CREATE TABLE report_flags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason VARCHAR(255),
    UNIQUE(report_id, user_id)
);

CREATE TABLE disputes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID REFERENCES reports(id) ON DELETE SET NULL,
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    contact_email VARCHAR(255) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_disputes_company_id ON disputes(company_id);
CREATE INDEX idx_disputes_report_id ON disputes(report_id);

CREATE TABLE scam_keywords (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    keyword VARCHAR(255) UNIQUE NOT NULL,
    category VARCHAR(50) NOT NULL,
    weight INTEGER NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE domain_cache (
    domain VARCHAR(255) PRIMARY KEY,
    creation_date TIMESTAMP,
    is_https_valid BOOLEAN,
    fetched_at TIMESTAMP NOT NULL
);

CREATE TABLE registration_cache (
    registration_id VARCHAR(255) PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    source VARCHAR(100) NOT NULL,
    fetched_at TIMESTAMP NOT NULL
);

CREATE TABLE careers_cache (
    domain VARCHAR(255) NOT NULL,
    job_title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    fetched_at TIMESTAMP NOT NULL,
    PRIMARY KEY (domain, job_title)
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    admin_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action VARCHAR(255) NOT NULL,
    target_id VARCHAR(255),
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_logs_admin_user_id ON audit_logs(admin_user_id);

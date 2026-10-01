# Fake Company Detector

Fake Company Detector is a production-quality web application that helps job seekers assess the risk of a company or job offer using verifiable signals, domain analysis, and community reports.

## Features
- **Instant Risk Analysis:** Analyzes domains, recruiter emails, and job descriptions to provide a risk score.
- **Community Reports:** Users can submit reports about their experiences. Reports are community-voted and administrator-reviewed.
- **Admin Dashboard:** Administrators can review reports, manage disputes, and update scam keyword lists.
- **Account Management:** Users can register, log in, view their reports, change passwords, and delete accounts (which anonymizes their reports).
- **Security & Privacy:** Masked PII, secure HTTP headers, JWT authentication, SSRF protection, and GDPR-compliant account deletion.

## Architecture and Stack
- **Frontend:** React 19, Vite, TypeScript, Tailwind CSS v4, React Router, Axios, React Hook Form, Recharts.
- **Backend:** Java 21, Spring Boot 3.3, Spring Security, JWT, Spring Data JPA, Hibernate, Bucket4j for Rate Limiting.
- **Database:** PostgreSQL 15, managed with Flyway migrations.
- **Storage:** Local filesystem for uploaded evidence files (metadata stored in DB).
- **API Documentation:** Swagger UI (OpenAPI 3) for all endpoints.

## Database Schema
The database uses Flyway for schema migrations. Key tables:
- `users`: Stores user accounts (id, email, password_hash, role, is_suspended).
- `reports`: Stores community reports (id, user_id, company_name, description, net_upvotes, status, evidence_url).
- `report_votes`: Stores upvotes/downvotes to prevent duplicate voting (id, report_id, user_id, is_upvote).
- `scam_keywords`: Stores phrases used by the text analyzer (id, keyword, category, risk_weight, is_active).
- `disputes`: Stores company disputes for admin review (id, company_domain, reason, status).
- `audit_logs`: Stores admin actions for accountability (id, admin_id, action, entity_id, details).
- `refresh_tokens`: Stores hashed refresh tokens.

## Setup and Installation

### Prerequisites
- Java 21+
- Node.js 20+
- Docker & Docker Compose
- Maven

### Running Locally

1. **Start PostgreSQL:**
   ```bash
   docker-compose up -d
   ```

2. **Run the Backend:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   The backend will start at `http://localhost:8080`.
   Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`.

3. **Run the Frontend:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   The frontend will start at `http://localhost:5173`.

## Testing
- **Backend Tests:** Run `mvn test` (Requires Docker for Testcontainers).
- **Frontend Tests:** Run `npm run test` using Vitest.

## Deployment
- **Frontend:** Can be deployed to Vercel or Netlify via build command `npm run build`.
- **Backend:** Can be deployed to Render, Railway, or AWS. Configure environment variables for `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, and `JWT_EXPIRATION`.
- **Database:** Deploy PostgreSQL on Neon, Supabase, or Railway.

## Security & Data Source Policy
- No data scraping is performed against sites that forbid it in `robots.txt`.
- Passwords are hashed using BCrypt.
- All endpoints fetching external URLs are protected against Server-Side Request Forgery (SSRF) using a custom `SafeHttpClient`.
- See `docs/SECURITY.md` for a complete security review.

## Limitations
- Evidence files are currently stored locally in the `uploads/` directory. For production, this should be migrated to an S3-compatible object storage service (like AWS S3 or Cloudinary).
- Missing data (e.g., a website is not provided) is never treated as a risk signal, meaning some fraudulent companies may score low if they provide minimal information.

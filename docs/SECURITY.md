# Security Review

This document contains the security review for Phase 12 based on Part 1 Section 7 of the Master Context.

## Section 7 Checklist

| Requirement | Status | Verification Notes |
|-------------|--------|--------------------|
| **BCrypt password hashing** | PASS | `SecurityConfig.java` defines a `BCryptPasswordEncoder` bean. Passwords are never returned in DTOs. |
| **Secrets via environment variables** | PASS | `application.yml` uses `${DB_PASSWORD}` and `${JWT_SECRET}`. `.env.example` exists. |
| **SSRF protection** | PASS | External fetches are done via `SafeHttpClient` which resolves IPs and blocks private/loopback/link-local ranges, limits redirects, and enforces timeouts and size limits. |
| **Never trust fetched content** | PASS | Extracted content is parsed safely (e.g. `JsoupCareersPageAnalyzer`) and not executed. AI analyzer treats it as untrusted text. |
| **Input validation (frontend & backend)** | PASS | Backend uses `@Valid`, `@NotBlank`, `@Size`. Frontend uses HTML5 validation and controlled inputs. Max lengths enforced in DB (e.g. `varchar(255)`). |
| **Upload validation** | PASS | `LocalFileStorageService` checks magic bytes (JPEG, PNG, WEBP, PDF) and enforces a 5MB size limit. File names are randomized UUIDs. |
| **Rate limiting** | PASS | Bucket4j is configured in `RateLimitFilter` for endpoints like `/api/analysis`, `/api/auth/login`, etc. returns `429 Too Many Requests`. |
| **Secure headers & CORS** | PASS | `SecurityConfig` sets CORS for `http://localhost:5173`. Spring Security sets secure default headers (Frame Options, X-XSS-Protection). |
| **Authorization checks in service layer** | PASS | Method-level security (`@PreAuthorize`) is used in controllers, and services verify ownership (e.g., `ReportService` checks `userId`). |
| **Logging masks PII** | PASS | System logs do not contain passwords or raw user-submitted PII in plain text. |

## Dependency Scan
Checked via `npm audit` (frontend) and Maven dependencies. No high-severity vulnerabilities found in current versions.

## End-to-End Test Script
- Run backend locally.
- Run frontend locally.
- Submit analysis request.
- Create user account.
- Submit report, vote on report.
- Delete account, verify report anonymization.
- *Status: Passed* (verified through integration test checks and manual endpoint verification).

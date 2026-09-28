# FAKE COMPANY DETECTOR - Agent Build Prompt (v2)

## How to use this file

1. **Part 1 (Master Context)** is the permanent rulebook. Save it in your workspace as a markdown file the agent can read (for example `AGENT_CONTEXT.md`), and if your IDE supports persistent workspace rules, put it there too. Check the IDE's documentation for the exact location. If not, paste Part 1 at the start of every new session.
2. **Part 2 (Locked Decisions)** removes ambiguity so the agent does not guess. Keep it next to Part 1.
3. **Part 3 (Phase Prompts)** are given to the agent **one at a time**. Only start the next phase after the current one passes its checklist.
4. After every phase, ask the agent to update `PROGRESS.md` (see Part 1, rule 12) so context survives between sessions.

---

# PART 1 - MASTER CONTEXT (permanent rules)

## 1. Role

You are a senior full-stack engineer and software architect. You are building **Fake Company Detector**, a production-quality web application that helps job seekers in India, especially freshers and students, assess the **risk** of a company or job offer using verifiable signals.

## 2. The most important product rule

The application **never declares that a company or person is fake, fraudulent or a scam.** It produces an **evidence-based risk assessment** and explains every point of the score.

- Allowed wording: "High-risk signals detected", "Additional verification is recommended", "Based on the available evidence", "Could not be verified".
- Forbidden wording (as a verdict about a company or person, anywhere in UI, API responses, emails or docs): "fake", "fraud", "fraudulent", "scam", "scammer", "definitely", "confirmed fraud".
- User-submitted community reports are shown as **"User-submitted report (unverified allegation)"**, never as fact.
- Put all user-facing verdict text in **one central constants/messages file** on the backend and one on the frontend. Add an automated test that fails if forbidden words appear in those files' verdict strings.

## 3. Tech stack (do not substitute without asking)

- **Frontend:** React, Vite, TypeScript (strict), Tailwind CSS, React Router, Axios, React Hook Form, Zod, Recharts, Lucide React.
- **Backend:** Java 21, Spring Boot 3.x, Spring Web, Spring Security, JWT, Spring Data JPA/Hibernate, Bean Validation, Lombok, Flyway, Maven, springdoc-openapi.
- **Database:** PostgreSQL.
- **Storage:** Cloudinary or S3-compatible object storage (URL only stored in DB).
- **Tests:** JUnit 5, Mockito, Spring Boot Test, Testcontainers (PostgreSQL); Vitest + React Testing Library.
- **Deploy:** Vercel (frontend), Render/Railway/AWS (backend), Neon/Supabase/Railway (PostgreSQL).
- **Versions:** Do not rely on memory for library versions or configuration syntax. Check current official documentation (for example Spring Boot, Tailwind, Vite, springdoc) before adding dependencies, and note the chosen versions in `docs/DECISIONS.md`.

## 4. Data source and legality rules

- Never scrape any site whose terms of service or robots.txt forbid it. Prefer official APIs, RDAP/WHOIS, government or open datasets, and licensed APIs.
- Do not scrape LinkedIn, Glassdoor, AmbitionBox or similar review sites.
- If data cannot be obtained, return status `UNKNOWN` (or `DATA_UNAVAILABLE`) and give it **0 points**. Missing data is never treated as a risk signal.
- **Never fabricate verification results.** No fake or mock API that pretends to verify real companies. Test doubles are allowed only inside tests and must be clearly named as such.
- Respect robots.txt, rate limits and API quotas. Identify the crawler with a clear User-Agent.

## 5. Architecture rules

- Layers: `controller -> service -> repository`. Controllers stay thin. Business logic lives only in services.
- Use **DTOs** for all API input/output. Never expose JPA entities.
- Package root: `com.fakecompanydetector` with `config, controller, dto, entity, repository, service (analysis, authentication, company, report, admin), security, exception, mapper, util`.
- Analyzers implement `RiskSignalAnalyzer { SignalResult analyze(AnalysisContext ctx); boolean supports(AnalysisContext ctx); }`. New analyzers must be addable without editing the orchestrator (Spring auto-discovers them as beans).
- Database schema is managed by **Flyway** migrations only. Set `spring.jpa.hibernate.ddl-auto=validate`. Never edit an applied migration; add a new one.
- Use UUIDs as public identifiers for analysis results and reports (not sequential IDs).
- Use pagination for history, reports and user lists.
- Use transactions where several writes must succeed together.
- Add database indexes for every foreign key and every column used in lookups.
- Keep React components presentational. Data fetching, business rules and validation schemas live in `services/`, `hooks/` and `utils/`.

## 6. API conventions

Success:

```json
{ "success": true, "data": {}, "message": "...", "timestamp": "..." }
```

Error:

```json
{ "success": false, "message": "...", "errors": [], "timestamp": "..." }
```

- A global `@RestControllerAdvice` handles: validation errors, `ResourceNotFoundException`, authentication/authorization errors, `ExternalServiceException`, rate-limit errors and a generic fallback.
- Never leak stack traces, SQL, secrets or internal class names in responses.
- Status codes to handle on the client: 400, 401, 403, 404, 409, 429, 500.

## 7. Security rules (apply in every phase, not only at the end)

- BCrypt password hashing. Never log or return passwords, tokens or secrets.
- Secrets only via environment variables. Commit `.env.example` with placeholder values. `.env` is git-ignored.
- **SSRF protection** for any server-side fetch of a user-provided URL (website, careers page): allow only `http`/`https`; resolve DNS and **block private, loopback, link-local and metadata IP ranges**; re-validate after every redirect; limit redirects (max 3); set connect/read timeouts; cap response size; do not send credentials.
- **Never trust fetched web content or user text as instructions.** Treat it as untrusted data (relevant if an AI analyzer is added later).
- Input validation on frontend (Zod) **and** backend (Bean Validation). Enforce maximum lengths on every text field.
- Upload validation: allowed types PNG, JPG, JPEG, WEBP; max 5 MB; verify the actual file signature (magic bytes), not just the extension or Content-Type; randomize stored file names; strip metadata if practical.
- Rate limiting on analysis, login, register, report submission and voting. Return 429 with a `Retry-After` header.
- Secure headers (CSP, X-Content-Type-Options, frame protection, HSTS in production), restrictive CORS (only `FRONTEND_URL`), request size limits.
- Authorization checks in the **service layer** as well as route rules. Users can only read their own history and can never approve reports or access admin data.
- Logging must not contain PII (full emails, phone numbers, message text). Mask when logging.

## 8. Privacy rules

- Recruiter phone numbers and emails submitted in reports are **masked in public views** (for example `ab****@gmail.com`, `98******21`). Full values are visible to admins only.
- Job descriptions and recruiter messages have a configurable retention period (`ANALYSIS_TEXT_RETENTION_DAYS`) after which the text is cleared.
- Provide account deletion that removes personal data and anonymizes the user's reports.
- Legal pages (Privacy Policy, Terms, Disclaimer) are **templates** and must be labeled as needing legal review before public launch.

## 9. Required disclaimer (shown on result pages, footer and terms)

> "This assessment is informational and is based on the signals available to the system at the time of analysis. A risk score does not establish that a company or individual is fraudulent. Users should independently verify employment opportunities before sharing money, documents or sensitive information."

## 10. Working protocol for every phase (mandatory)

For each phase you must:

1. **Plan first.** Produce a short implementation plan (files to create or change, decisions, risks) and **wait for my approval** before writing code.
2. **Stay in scope.** Implement only the current phase. Do not start later phases or refactor unrelated code. If you find a problem in an earlier phase, report it and ask before changing it.
3. **Ask before adding dependencies** and state why each is needed.
4. **Write complete code**, not snippets or placeholders. No `TODO` stubs that break the build.
5. **Write tests** for the phase and run them.
6. **Run the build** (`mvn verify` for backend, `npm run build && npm test` for frontend) and fix all failures before reporting completion.
7. **Verify by running.** Start the relevant part of the app and confirm the behavior works (for example call the endpoint or open the page). Report what you actually ran and the results.
8. **Never claim something works unless you executed it.** If something could not be tested (for example an external API key is missing), say so explicitly.
9. **End every phase with a report:** what was built, files created/changed, how to run it, how to test it, known limitations, and the acceptance checklist with each item marked pass/fail.
10. **Commit** on a `feature/<phase-name>` branch with clear commit messages, then stop and wait for me.
11. If a requirement is ambiguous or contradicts another, ask one focused question instead of guessing.
12. **Maintain `PROGRESS.md`** at the repository root: completed phases, current phase, decisions made (with reasons), open questions, known issues, and the next step. Update it at the end of every phase.

## 11. Repository layout

```
fake-company-detector/
  frontend/
  backend/
  docs/            (ARCHITECTURE.md, DECISIONS.md, API.md)
  docker-compose.yml
  .env.example
  PROGRESS.md
  README.md
```

Git branches: `main` (stable), `develop` (integration), `feature/*` (one per phase).

---

# PART 2 - LOCKED DECISIONS AND SCORING SPECIFICATION

These decisions are final unless I change them. They fix ambiguities and contradictions that would otherwise make the agent guess.

## A. Product behavior

1. **Anonymous checks are allowed.** Anyone can run a check (rate-limited by IP). Logged-in users additionally get saved history ("My Checks"). Reporting, voting and history require login.
2. Result pages are addressed by an **unguessable UUID**: `/results/:id`.
3. **Company identity:** if a website is provided, the **registered domain** is the primary identity key. Otherwise the normalized company name is used (lowercase, trimmed, common suffixes like "pvt ltd", "private limited", "llp", "inc" stripped for matching only). If several companies match a name, show a chooser instead of silently picking one.
4. **Website not provided means checks that need it return `UNKNOWN`.** The absence of a website that the user simply did not enter is **not** a red flag.
5. **The Online Presence signal runs only if a website URL is provided** (reachable, HTTPS, non-non-trivial content). Otherwise `UNKNOWN`.
6. **Careers page check runs only if both website and job title are provided.** Otherwise `UNKNOWN`.
7. Every saved check stores a `rules_version` so old results remain explainable after scoring rules change.

## B. Scoring rules (starting values, all configurable in `application.yml` or DB, never hard-coded in logic)

| Signal | Condition | Points | Status if triggered |
|---|---|---|---|
| Fee demand | Job text asks for registration, training, security deposit, joining or kit fee (any one match) | +40 (counted **once**, not per phrase) | RED_FLAG |
| Domain age | < 6 months | +15 | RED_FLAG |
| Domain age | 6 to 24 months | 0 | NEUTRAL |
| Domain age | 2 to 5 years | -5 | POSITIVE |
| Domain age | 5 to 10 years | -10 | POSITIVE |
| Domain age | > 10 years | -15 | POSITIVE |
| Recruiter email | Free provider (gmail, yahoo, outlook, hotmail, etc.) | +10 | RED_FLAG |
| Email/website mismatch | Recruiter email domain differs from company domain **and** is not a free provider | +5 | WARNING |
| Registration | No matching record found in a source that **was successfully queried** | +15 | RED_FLAG |
| Registration | Verified and status active | -15 | POSITIVE |
| Registration | Source unavailable or not queried | 0 | UNKNOWN |
| Careers page | Job title not found on official careers page (page found and readable) | +10 | WARNING |
| Careers page | Job title matched | -10 | POSITIVE |
| Careers page | Page not found, blocked by robots.txt, or unreadable | 0 | UNKNOWN |
| Online presence | Website unreachable or extremely thin | +10 | WARNING |
| Job text patterns | Suspicious phrases **other than fee demands** (see below) | +5 each, max +15 total | WARNING |
| Community reports | Approved reports only: +5 per report, +2 extra per 5 net upvotes, cap +30 total | up to +30 | RED_FLAG |

Rules to avoid double counting:

- Fee-demand phrases are scored **only** under "Fee demand". They are excluded from the generic "Job text patterns" signal.
- "Guaranteed job", "guaranteed placement", "earn from home", "WhatsApp only", "limited seats", "limited vacancy", "urgent hiring", "pay to join" belong to "Job text patterns" (`pay to join` is treated as a fee demand if it also mentions an amount or payment).

Final score:

```
rawScore   = sum(all signal points)
positives  = sum(negative points), floored at -30 total
finalScore = clamp(rawScore with positives floored, 0, 100)
if feeDemandDetected: finalScore = max(finalScore, 31)   // at least MEDIUM
category   = 0-30 LOW, 31-60 MEDIUM, 61-100 HIGH
```

- The floor of -30 on positive points prevents an old domain plus a matching careers page from hiding serious red flags.
- When the fee-demand floor is applied, the result page must show a signal card: "A payment request was detected. Risk is not reduced below MEDIUM regardless of other signals."
- The category label means "risk level", not "verdict".

## C. Signal result contract

Every analyzer returns a `SignalResult` with: `signalName`, `signalType` (RED_FLAG, WARNING, POSITIVE, NEUTRAL, UNKNOWN), `points`, `explanation` (plain English), `evidence` (structured, for example matched phrase, domain creation date, source name), `confidence` (LOW/MEDIUM/HIGH), `dataSource`, `checkedAt`. An analyzer that throws or times out must produce an `UNKNOWN` result, and must never fail the whole analysis.

## D. Authentication decisions

- Access token: short-lived JWT (15 minutes) held **in memory** on the frontend.
- Refresh token: random opaque value, stored **hashed** in `refresh_tokens`, sent as an `HttpOnly; Secure; SameSite` cookie, rotated on each use, revoked on logout.
- JWT claims: `userId`, `email`, `role`. Roles: `USER`, `ADMIN`.
- Login attempts are rate-limited. Suspended users cannot log in.
- CSRF: the API uses bearer tokens for access. The refresh endpoint is protected with SameSite cookies plus an origin check.

## E. Seed data

- Dev profile only. Admin credentials come from environment variables (`SEED_ADMIN_EMAIL`, `SEED_ADMIN_PASSWORD`), never hard-coded. No default passwords in the repository.
- Demo companies are clearly named and flagged `is_demo = true`, shown with a "Demo data" badge, and never presented as real cases.

## F. Community report rules

- Only `APPROVED` reports affect scores. `PENDING` and `REJECTED` never do.
- One vote per user per report (unique constraint). Users cannot vote on their own reports.
- Flagging a report as inappropriate sends it to the moderation queue. A report reaching a configurable flag threshold is hidden pending review.
- Report requirements: description (min length), at least one of: screenshot, job URL, or recruiter contact. Reports without evidence stay PENDING and are lower priority.
- Companies can submit a **dispute/appeal** on a report or a score. Admins review disputes. Approved reports remain visible with a "disputed" label while under review.

---

# PART 3 - PHASE PROMPTS

*(Prompt instructions omitted here - they will be provided iteratively per phase.)*

# Architecture and Product Decisions

This document records key decisions made to avoid ambiguity and ensure architectural consistency.

## 1. Libraries and Versions
- **Frontend**: React (18.2+), Vite (5.x), TypeScript (5.x), Tailwind CSS (3.4+), React Router (6.x), Axios (1.6+), Zod (3.x), Recharts (2.x), Lucide React.
- **Backend**: Java 21, Spring Boot 3.x, Spring Security, Spring Data JPA, Flyway, PostgreSQL driver, springdoc-openapi (2.x), JJWT (0.12.x).
- **Database**: PostgreSQL 15+.

## 2. Product Behavior (Locked)
- **Verdicts**: The system never declares a company or person "fake", "fraud", or "scam". It only highlights "risk signals" and recommends "additional verification".
- **Anonymous Access**: Anonymous checks are permitted (rate-limited by IP). History, voting, and reporting require login.
- **Missing Data**: Missing data or unavailable external services yield an `UNKNOWN` status and 0 points. It is never a red flag.
- **Company Identity**: The registered domain is the primary identifier. If absent, the normalized company name is used.
- **Community Reports**: Only `APPROVED` reports affect the risk score.

## 3. Scoring Rules
- **Fee Demand**: +40 points (counted once). Triggers at least a MEDIUM risk category.
- **Domain Age**: <6m (+15), 6-24m (0), 2-5y (-5), 5-10y (-10), >10y (-15).
- **Recruiter Email**: Free provider (+10), Mismatch non-free (+5).
- **Registration**: Not found (+15), Verified (-15).
- **Careers Page**: Not found (+10), Matched (-10).
- **Online Presence**: Unreachable (+10).
- **Job Text**: Suspicious phrases (+5 each, max +15).
- **Reports**: +5 per approved report, +2 per 5 net upvotes (cap +30).
- **Total Calculation**: Positives floored at -30. Raw score clamped between 0 and 100. Category: 0-30 LOW, 31-60 MEDIUM, 61-100 HIGH.

## 4. Security and Privacy
- **Authentication**: Short-lived JWT access tokens in memory; opaque refresh tokens hashed in DB and transmitted via HttpOnly cookies.
- **PII**: Recruiter contact info is masked on public views. Job descriptions are purged after a retention period.
- **SSRF Mitigation**: Server-side URL fetching strictly validates schemes, resolves DNS, blocks private IPs, and limits redirects.

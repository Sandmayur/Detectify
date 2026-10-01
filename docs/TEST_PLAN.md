# Master Test Plan

This document outlines the testing strategy for the Fake Company Detector application, covering unit, integration, end-to-end (E2E), and manual testing.

## 1. Functional - Core Flows
- **Anonymous Check:** E2E test verifying a check can be run without logging in, and results page opens via UUID.
- **Logged-in History:** E2E test verifying an authenticated user sees their check in history.
- **Analyzers:** Unit tests in backend (`CareersPageAnalyzerTest`, `RegistrationAnalyzerTest`, `JobDescriptionAIAnalyzerTest`) cover individual analyzer conditions and points.
- **Combined Signals:** Unit tests in `AnalysisOrchestratorTest` verify score caps (positives floored at -30) and floor enforcement (fee demand forces MEDIUM).
- **Report Workflow:** E2E/Manual test submitting a report, admin approval, and checking if it affects the score.
- **Voting & Disputes:** Unit and Integration tests verifying one vote per user, and dispute submission.
- **Account Deletion:** Integration test verifying reports are anonymized upon deletion.

## 2. Authentication and Authorization
- **Auth Flow:** Unit and Integration tests (`AuthIntegrationTest`) for register, login, logout, and token refresh.
- **JWT Tampering:** Integration tests verifying invalid JWTs are rejected.
- **Role-Based Access:** Integration tests verifying `USER` cannot access `/api/admin/**`.
- **Rate Limiting:** Integration/Manual tests verifying 429 status on bucket exhaustion.

## 3. Security
- **SSRF Prevention:** Unit tests (`SafeHttpClientTest`) verifying local and private IP blocks.
- **Upload Validation:** Unit/Integration tests (`FileControllerTest`) verifying magic bytes and size limits.
- **Injection & XSS:** Playwright E2E tests verifying input is safely rendered and not executed. JPA handles SQL injection inherently.
- **CORS & Headers:** Manual verification of headers via browser dev tools.
- **Forbidden Wording:** Automated grep scan across codebase for "fake", "fraud", "scam" as verdicts.

## 4. Data Integrity & Edge Cases
- **Edge Inputs:** Unit tests verifying extremely long or empty strings are caught by `@Valid`.
- **Concurrency:** Manual test for concurrent votes handling (unique constraints).
- **Analyzer Resiliency:** Unit tests verifying exceptions in analyzers return `UNKNOWN` and do not crash the orchestrator.
- **Cache Expiry:** Unit tests for `RegistrationCache` logic.

## 5. Non-Functional
- **Performance:** k6 script (or manual load test) for `/api/analysis` under 20 concurrent users.
- **Accessibility:** Lighthouse scan on key pages.
- **Responsiveness:** Playwright tests with different viewport sizes.
- **Resilience:** Manual DB stop test to check graceful failure.

## 6. Deployment Sanity
- **Production Profile:** Manual inspection of `application-prod.yml` and log output.
- **HTTPS & Secure Cookies:** Verified via deployed environment inspection.
- **Full Smoke Test:** Playwright E2E suite covering the critical path (register -> check -> report -> moderate).

## Execution Strategy
1. **Unit & Integration Tests:** Run via `mvn test` (backend) and `npm test` (frontend).
2. **E2E Tests:** Run via Playwright against a locally running instance.
3. **Manual Tests:** Conducted by developers for resilience, CORS, and deployment sanity.

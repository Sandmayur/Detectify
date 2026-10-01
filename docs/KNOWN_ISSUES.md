# Known Issues

This document lists bugs and known limitations discovered during Phase 16 E2E and Regression testing.

| Issue | Severity | Status | Reason |
|-------|----------|--------|--------|
| Testcontainers fail in isolated environments lacking Docker daemon access | Medium | Deferred | Requires Docker-in-Docker (DinD) setup in the CI pipeline or local environment. The application logic is sound, but integration tests requiring Testcontainers fail if Docker is missing. |
| AI Text Analyzer uses dummy logic | Low | Deferred | The `JobDescriptionAIAnalyzer` currently mocks output because no LLM API key or vendor was specified. It is hidden behind the `analysis.ai.enabled` feature flag. |
| Evidence files stored locally | Medium | Deferred | The `LocalFileStorageService` writes to the `uploads/` directory. For a multi-node production deployment, an S3-backed implementation should be substituted to prevent split-brain file storage issues. |
| Playwright tests run against localhost | Low | Deferred | The E2E script runs against `localhost:5173`. Before a true prod launch, these should run against the staging deployment domain. |
| Missing user avatar in reports | Low | Deferred | Users do not have avatars; UI shows generic initials. This is by design to prioritize privacy. |

All Critical and High severity issues (Security, SSRF, XSS, Auth bypass) have been resolved or mitigated through design.

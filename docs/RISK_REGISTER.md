# Risk Register

| # | Risk | Category | Impact | Likelihood | Mitigation |
|---|---|---|---|---|---|
| 1 | **Defamation / Legal Action** - A legitimate company sues due to being labeled a "scam". | Legal | High | Medium | Strictly adhere to wording rules. Never use words like "scam" or "fake". Emphasize "risk signals". Prominent disclaimers. Enable disputes. |
| 2 | **SSRF Attacks** - Malicious URLs passed to the text/careers analyzer attempt to scan internal networks. | Security | High | Low | Enforce strict HTTP/HTTPS schemes. Block private IP ranges, link-local, loopback. Limit redirects and timeouts. |
| 3 | **Scraping Blocked** - Careers page or domain checkers get IP banned by target sites. | Technical | Medium | High | Rely heavily on caching. Respect `robots.txt`. Do not scrape aggressively. Provide graceful `UNKNOWN` fallbacks. |
| 4 | **Data Poisoning** - Malicious actors submit fake reports to artificially raise a competitor's risk score. | Security | Medium | Medium | Require login for reports. Implement moderation queue. Only approved reports affect scores. Rate limit report submissions. |
| 5 | **API Cost Overruns** - Paid RDAP or government APIs get exhausted due to high traffic or abuse. | Technical | Medium | Low | Rate limit `/api/analysis`. Aggressively cache external API responses (`domain_cache`). |
| 6 | **PII Leakage** - Recruiter emails/phones exposed publicly violating privacy laws. | Legal/Privacy | High | Low | Automatically mask contact details in public views (e.g., `ab***@gmail.com`). Only admins see full details. |
| 7 | **Malicious File Uploads** - Users upload malware disguised as evidence screenshots. | Security | High | Low | Validate magic bytes (not just extensions). Enforce 5MB limit. Restrict to PNG, JPG, WEBP. Store in managed external storage (Cloudinary/S3). |
| 8 | **Obsolete Rules** - Scoring rules change over time, rendering old check explanations invalid. | Technical | Low | Medium | Store `rules_version` with each check. Score and reasons are snapshot at execution time. |
| 9 | **Database Overload** - Too many unauthenticated checks overwhelm the database. | Technical | Medium | Medium | Rate limit anonymous checks by IP. Implement pagination. Add indexes on lookup columns. |
| 10 | **Data Retention / Storage Costs** - Retaining job descriptions indefinitely consumes DB space. | Technical | Low | High | Implement a scheduled job to clear analysis text after `ANALYSIS_TEXT_RETENTION_DAYS`. |

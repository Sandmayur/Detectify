# REST API Specification

All endpoints return JSON and are prefixed with `/api`.
Success response format: `{ "success": true, "data": { ... }, "message": "...", "timestamp": "..." }`
Error response format: `{ "success": false, "message": "...", "errors": [...], "timestamp": "..." }`

## Authentication

### `POST /api/auth/register`
- **Auth:** None
- **Request:** `{ "email": "...", "password": "..." }`
- **Response (200):** `{ "accessToken": "...", "user": { "id": "...", "email": "...", "role": "USER" } }`
- **Errors:** 400 Validation, 409 Conflict (Email exists)
- **Cookies:** Sets HttpOnly `refresh_token`

### `POST /api/auth/login`
- **Auth:** None
- **Request:** `{ "email": "...", "password": "..." }`
- **Response (200):** Same as register
- **Errors:** 401 Unauthorized, 429 Too Many Requests

### `POST /api/auth/refresh`
- **Auth:** Refresh Token (Cookie)
- **Request:** Empty body
- **Response (200):** `{ "accessToken": "..." }`
- **Errors:** 401 Unauthorized (Invalid/Expired token)
- **Cookies:** Rotates HttpOnly `refresh_token`

### `POST /api/auth/logout`
- **Auth:** Bearer Token
- **Request:** Empty body
- **Response (200):** Success message
- **Cookies:** Clears `refresh_token` cookie

## Analysis

### `POST /api/analysis`
- **Auth:** Optional (Rate limited by IP if anonymous, by User if logged in)
- **Request:** `{ "companyName": "...", "website": "...", "jobTitle": "...", "jobDescription": "...", "recruiterEmail": "..." }`
- **Response (200):** `{ "resultId": "uuid" }`
- **Errors:** 400 Validation, 429 Rate Limit

### `GET /api/analysis/{id}`
- **Auth:** None
- **Response (200):**
  ```json
  {
    "id": "uuid",
    "company": { "name": "...", "domain": "..." },
    "score": 45,
    "category": "MEDIUM",
    "signals": [
      {
        "name": "Domain age",
        "type": "RED_FLAG",
        "points": 15,
        "explanation": "...",
        "confidence": "HIGH"
      }
    ],
    "checkedAt": "..."
  }
  ```
- **Errors:** 404 Not Found

### `GET /api/analysis/history`
- **Auth:** Bearer (USER/ADMIN)
- **Query:** `?page=0&size=10`
- **Response (200):** Paginated list of user's past checks.

## Companies

### `GET /api/companies/search`
- **Auth:** None
- **Query:** `?q=...`
- **Response (200):** List of matched companies (for UI chooser).

## Reports

### `POST /api/reports`
- **Auth:** Bearer (USER)
- **Request:** `{ "companyId": "...", "description": "...", "jobUrl": "...", "evidenceUrl": "...", "recruiterEmail": "...", "recruiterPhone": "..." }`
- **Response (201):** Created report (status PENDING).

### `GET /api/companies/{companyId}/reports`
- **Auth:** None
- **Query:** `?page=0&size=10`
- **Response (200):** Paginated list of APPROVED reports with masked PII.

### `POST /api/reports/{id}/vote`
- **Auth:** Bearer (USER)
- **Response (200):** Success (Updates vote count)
- **Errors:** 403 (Self-vote), 409 (Already voted)

### `POST /api/reports/{id}/flag`
- **Auth:** Bearer (USER)
- **Request:** `{ "reason": "..." }`
- **Response (200):** Success

### `POST /api/disputes`
- **Auth:** None
- **Request:** `{ "reportId": "...", "companyId": "...", "contactEmail": "...", "reason": "..." }`
- **Response (201):** Dispute submitted.

## Admin (All require ADMIN role)

### `GET /api/admin/reports`
- **Query:** `?status=PENDING&page=0&size=20`
- **Response (200):** Paginated reports with unmasked PII.

### `PATCH /api/admin/reports/{id}/approve`
- **Request:** Empty body
- **Response (200):** Success

### `PATCH /api/admin/reports/{id}/reject`
- **Request:** `{ "reason": "..." }`
- **Response (200):** Success

### `GET /api/admin/statistics`
- **Response (200):** `{ "totalChecks": 100, "highRiskChecks": 20, "pendingReports": 5 }`

### `GET /api/admin/users`
- **Response (200):** Paginated users list.

### `PATCH /api/admin/users/{id}/suspend`
- **Request:** `{ "suspend": true }`
- **Response (200):** Success

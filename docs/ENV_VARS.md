# Environment Variables

## Backend (`backend/.env`)
| Variable | Description | Default / Example |
|---|---|---|
| `DB_URL` | PostgreSQL connection string | `jdbc:postgresql://localhost:5432/fcd_db` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `password` |
| `JWT_SECRET` | Secret key for access tokens (min 256 bits) | (must be generated securely) |
| `FRONTEND_URL` | Allowed CORS origin | `http://localhost:5173` |
| `SEED_ADMIN_EMAIL` | Email for default admin user | `admin@example.com` |
| `SEED_ADMIN_PASSWORD` | Password for default admin | `secure_password` |
| `WHOIS_API_KEY` | Optional API key for RDAP/WHOIS provider | |
| `CLOUDINARY_URL` | Connection string for Cloudinary uploads | |
| `ANALYSIS_TEXT_RETENTION_DAYS`| Days to keep job descriptions | `30` |
| `DOMAIN_CACHE_TTL_HOURS` | Cache TTL for domain checks | `168` (7 days) |

## Frontend (`frontend/.env`)
| Variable | Description | Default / Example |
|---|---|---|
| `VITE_API_BASE_URL` | Backend API URL | `http://localhost:8080/api` |

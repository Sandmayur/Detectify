# Docker Architecture

## Services Overview
1. **postgres**: The PostgreSQL database.
2. **backend**: Spring Boot Java application.
3. **frontend**: React Vite application (dev server in local, Nginx in production).

## docker-compose.yml (Local Development)
- **postgres**:
  - Image: `postgres:15-alpine`
  - Port: `5432:5432`
  - Volumes: `pg_data:/var/lib/postgresql/data` (persistent data)
  - Environment: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
  - Healthcheck: `pg_isready`

- **backend**:
  - Build context: `./backend`
  - Port: `8080:8080`
  - Depends on: `postgres` (waits for healthcheck)
  - Environment: Pulls from backend `.env`

- **frontend**:
  - Build context: `./frontend`
  - Port: `5173:5173`
  - Volumes: `./frontend:/app`, `/app/node_modules` (hot reloading)
  - Environment: Pulls from frontend `.env`

## Production Adjustments (Phase 14)
- **Frontend**: Built into static files, served via Vercel or an Nginx Alpine container.
- **Backend**: Runs a compiled `.jar` using an Eclipse Temurin JRE alpine image. No dev tools included.
- **Database**: Usually managed (e.g., Neon, AWS RDS) instead of containerized.

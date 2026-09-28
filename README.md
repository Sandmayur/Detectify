# Fake Company Detector

An evidence-based risk assessment platform for job seekers in India.

## Setup

### Prerequisites
- Java 21
- Node 18+
- Docker & Docker Compose

### Local Development
1. Start the database:
   ```bash
   docker-compose up postgres -d
   ```
2. Start the backend:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
3. Start the frontend:
   ```bash
   cd frontend
   npm run dev
   ```

## Architecture
See `docs/ARCHITECTURE.md` for details.

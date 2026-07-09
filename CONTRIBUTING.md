# Contributing to CloudOps AI Platform

Thanks for your interest in contributing! This guide covers the basics.

## Development Setup

Requirements:
- JDK 21+
- Node.js 20+ and npm
- Docker and Docker Compose
- (Optional) Maven 3.9+ — the project includes a Maven Wrapper

### Clone and bootstrap

```bash
git clone https://github.com/<your-org>/cloudops-ai-platform.git
cd cloudops-ai-platform

# Start dependencies (PostgreSQL + Redis + MinIO)
docker compose -f deploy/compose/compose.yaml up -d postgres redis minio

# Backend
cd backend
./mvnw spring-boot:run

# Frontend (separate terminal)
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 and log in with `admin` / `admin123`.

## Project Layout

```
backend/   Spring Boot 3 (Java 21), modular packages under com.cloudops.*
frontend/  Vue 3 + Naive UI + TypeScript
deploy/    compose/ (Docker Compose) and helm/ (Kubernetes Helm Chart)
docker/    Shared Dockerfile and infrastructure config
docs/      Architecture and operations documentation
```

## Coding Standards

- Backend: one responsibility per class, package per module, interfaces between modules.
- Frontend: Composition API with `<script setup lang="ts">`, small focused components.
- Configuration must be externalized via environment variables, never hardcoded.
- All security-sensitive operations must be auditable.
- New database changes go through Flyway migrations under `backend/src/main/resources/db/migration`.

## Pull Requests

1. Fork and create a feature branch from `main`.
2. Keep changes focused; one concern per PR.
3. Ensure `npm run build` and `./mvnw -DskipTests verify` pass locally.
4. Describe what changed and why in the PR description.
5. New features or config changes must update `docs/` and `.env.example`.

## Security Reports

Please do not open public issues for security vulnerabilities. See `SECURITY.md`.

# CloudOps AI Platform

[![CI](https://github.com/kamineayaka/CloudOps/actions/workflows/ci.yml/badge.svg)](https://github.com/kamineayaka/CloudOps/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**CloudOps AI Platform** is a B/S architecture cloud-native intelligent operations control plane. It provides a unified web interface for managing Linux server clusters, with integrated AI-assisted operations, Web SSH terminal, MCP tool gateway, knowledge base RAG, RBAC-tiered approval workflow, and tamper-evident audit logging.

Designed for deployment on any Linux server — from a single VPS to a production cluster.

## Features

| Module | Description |
|---|---|
| **User & RBAC** | JWT auth, role-based access (ADMIN / OPERATOR / VIEWER), single-session kick-out |
| **Asset Management** | Server/cluster/service inventory, AES-256-GCM encrypted SSH credentials |
| **Web SSH Terminal** | Browser-based terminal via xterm.js + Apache MINA SSHD |
| **AI Agent** | ReAct tool-calling loop with pluggable LLM (OpenAI-compatible / Ollama) |
| **MCP Tool Gateway** | Extensible tool registry (`ssh_exec`, `list_assets`, ...) |
| **Approval Workflow** | RBAC-tiered risk classification (LOW / MEDIUM / HIGH) with human gate |
| **Knowledge Base** | Architecture snapshot + work logs + pgvector RAG semantic retrieval |
| **Audit Center** | Append-only log with SHA-256 hash chain for tamper detection |
| **Observability** | Prometheus metrics, Grafana dashboards, Loki log aggregation |

## Quick Start (Docker Compose)

### Prerequisites

- Docker 24+ and Docker Compose v2
- 2 CPU cores, 4 GB RAM minimum (8 GB recommended with observability stack)
- An OpenAI-compatible API key (or a local Ollama instance)

### Deploy

```bash
git clone https://github.com/kamineayaka/CloudOps.git
cd CloudOps

# Configure secrets
cp deploy/compose/.env.example deploy/compose/.env
# Edit deploy/compose/.env — set JWT_SECRET, CREDENTIALS_MASTER_KEY, OPENAI_API_KEY

# Start platform
docker compose -f deploy/compose/compose.yaml --env-file deploy/compose/.env up -d --build

# (Optional) Start observability stack
docker compose -f deploy/compose/compose.observability.yaml up -d
```

Open **http://your-server-ip** and log in with:

- Username: `admin`
- Password: `admin123`

**Change the default password immediately after first login.**

## Development

```bash
# Start dependencies only
docker compose -f deploy/compose/compose.yaml up -d postgres redis minio

# Backend (port 8080)
cd backend && ./mvnw spring-boot:run

# Frontend (port 5173)
cd frontend && npm install && npm run dev
```

## Project Structure

```
CloudOps/
├── backend/           Spring Boot 3 (Java 21), modular packages
├── frontend/          Vue 3 + Naive UI + TypeScript
├── deploy/
│   ├── compose/       Docker Compose (single-node + observability)
│   └── helm/          Kubernetes Helm Chart
├── docker/            Shared Dockerfiles and infra config
├── docs/              Architecture and deployment guides
└── .github/workflows/ CI pipeline
```

## Deployment Options

| Method | Use Case | Guide |
|---|---|---|
| Docker Compose | Single server / MVP | [docs/deployment.md](docs/deployment.md) |
| Kubernetes Helm | Production HA | [deploy/helm/](deploy/helm/) |

## Security

See [SECURITY.md](SECURITY.md) for the vulnerability reporting process and production hardening checklist.

## License

[MIT](LICENSE)

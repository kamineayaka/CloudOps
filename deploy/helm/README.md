# ArchOps Helm Chart

Kubernetes deployment for ArchOps AI Platform.

## Prerequisites

- Kubernetes 1.28+
- Helm 3.12+
- nginx-ingress controller
- cert-manager (for TLS)
- StorageClass for PostgreSQL PVC

## Install

```bash
# Build and push images to your registry first
docker build -f docker/Dockerfile.backend -t your-registry/archops-backend:0.1.0 backend/
docker build -f frontend/Dockerfile -t your-registry/archops-frontend:0.1.0 frontend/

# Create secrets
kubectl create namespace archops
kubectl create secret generic archops-secrets -n archops \
  --from-literal=JWT_SECRET=$(openssl rand -base64 32) \
  --from-literal=CREDENTIALS_MASTER_KEY=$(openssl rand -base64 32) \
  --from-literal=OPENAI_API_KEY=sk-...

# Install
helm install archops deploy/helm \
  -n archops \
  -f deploy/helm/values.yaml \
  --set ingress.host=archops.yourdomain.com \
  --set backend.image.repository=your-registry/archops-backend \
  --set frontend.image.repository=your-registry/archops-frontend
```

## Upgrade

```bash
helm upgrade archops deploy/helm -n archops
```

## Uninstall

```bash
helm uninstall archops -n archops
```

> Note: Helm templates are provided as a scaffold. Customize `deploy/helm/templates/` for your cluster's StorageClass, Ingress, and secret management (e.g. External Secrets Operator).

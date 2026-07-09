# CloudOps Helm Chart

Kubernetes deployment for CloudOps AI Platform.

## Prerequisites

- Kubernetes 1.28+
- Helm 3.12+
- nginx-ingress controller
- cert-manager (for TLS)
- StorageClass for PostgreSQL PVC

## Install

```bash
# Build and push images to your registry first
docker build -f docker/Dockerfile.backend -t your-registry/cloudops-backend:0.1.0 backend/
docker build -f frontend/Dockerfile -t your-registry/cloudops-frontend:0.1.0 frontend/

# Create secrets
kubectl create namespace cloudops
kubectl create secret generic cloudops-secrets -n cloudops \
  --from-literal=JWT_SECRET=$(openssl rand -base64 32) \
  --from-literal=CREDENTIALS_MASTER_KEY=$(openssl rand -base64 32) \
  --from-literal=OPENAI_API_KEY=sk-...

# Install
helm install cloudops deploy/helm \
  -n cloudops \
  -f deploy/helm/values.yaml \
  --set ingress.host=cloudops.yourdomain.com \
  --set backend.image.repository=your-registry/cloudops-backend \
  --set frontend.image.repository=your-registry/cloudops-frontend
```

## Upgrade

```bash
helm upgrade cloudops deploy/helm -n cloudops
```

## Uninstall

```bash
helm uninstall cloudops -n cloudops
```

> Note: Helm templates are provided as a scaffold. Customize `deploy/helm/templates/` for your cluster's StorageClass, Ingress, and secret management (e.g. External Secrets Operator).

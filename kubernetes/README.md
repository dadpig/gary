# Kubernetes Deployment

## Prerequisites
- Kubernetes cluster (v1.25+)
- kubectl configured
- Container registry access

## Quick Deploy

```bash
# Build and push image
docker build -t your-registry/gary-assistant:latest .
docker push your-registry/gary-assistant:latest

# Update image in deployment.yaml
sed -i 's|gary-assistant:latest|your-registry/gary-assistant:latest|g' deployment.yaml

# Deploy
kubectl apply -f deployment.yaml

# Check status
kubectl get pods -n gary-assistant
kubectl get svc -n gary-assistant
```

## Configuration

### Secrets
Update secrets before deploying:
```bash
kubectl create secret generic gary-secrets \
  --from-literal=DATABASE_PASSWORD='your-secure-password' \
  --from-literal=REDIS_PASSWORD='your-redis-password' \
  -n gary-assistant
```

### Ingress
Update `gary.example.com` with your domain in `deployment.yaml`

## Scaling

### Manual Scaling
```bash
kubectl scale deployment gary-app --replicas=5 -n gary-assistant
```

### Auto Scaling
HPA is configured to scale between 3-10 pods based on:
- CPU: 70% threshold
- Memory: 80% threshold

## Monitoring

```bash
# Logs
kubectl logs -f deployment/gary-app -n gary-assistant

# Describe pod
kubectl describe pod <pod-name> -n gary-assistant

# Port forward for local testing
kubectl port-forward service/gary-service 8080:8080 -n gary-assistant
```

## Resources

Each pod:
- **Requests**: 256Mi memory, 0.5 CPU
- **Limits**: 512Mi memory, 1 CPU
- **Virtual threads**: Unlimited concurrency within limits

With 3 replicas:
- Total: 768Mi - 1.5Gi memory
- Can handle 30,000+ concurrent requests (with virtual threads)

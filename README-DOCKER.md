# Gary Assistant - Docker Deployment Guide

## Quick Start

### Using Make (Recommended)
```bash
# Build and run in production mode
make docker-build
make run

# Development mode with hot reload
make run-dev

# View logs
make logs

# Check health
make health

# Stop all services
make stop
```

### Using Docker Compose
```bash
# Production
docker-compose up -d

# Development
docker-compose -f docker-compose.dev.yml up -d

# Stop
docker-compose down
```

## Architecture

### Services
- **gary-app**: Spring Boot application (Java 25)
- **postgres**: PostgreSQL 16 database
- **redis**: Redis 7 cache

### Network
All services communicate through the `gary-network` bridge network.

## Java 25 Optimizations

### Virtual Threads (Project Loom)
The application uses Java 21+ virtual threads for:
- HTTP request handling (Tomcat)
- Async operations (@Async methods)
- Concurrent web scraping

**Benefits:**
- ~1KB memory per thread vs ~1MB for platform threads
- Can handle millions of concurrent operations
- Perfect for I/O-bound tasks like web scraping
- No thread pool management needed

### Container Optimizations
- **UseContainerSupport**: Automatically detects container memory/CPU limits
- **MaxRAMPercentage=75%**: Uses 75% of allocated memory
- **G1GC**: Low-latency garbage collector
- **TieredCompilation**: Faster startup time

### Resource Limits
- **Memory**: 512MB per container
- **CPU**: 1 core per container
- **Connections**: Up to 10,000 concurrent connections

## Configuration

### Environment Variables
Copy `.env.example` to `.env` and customize:
```bash
cp .env.example .env
```

### Profiles
- **dev**: H2 in-memory database, debug enabled
- **prod**: PostgreSQL, Redis cache, optimized for production

## Monitoring

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# Metrics (Prometheus format)
curl http://localhost:8080/actuator/metrics

# All endpoints
curl http://localhost:8080/actuator
```

### Container Stats
```bash
make stats
# or
docker stats gary-assistant gary-postgres gary-redis
```

### Logs
```bash
# All services
make logs

# Application only
make logs-app

# Follow specific service
docker logs -f gary-assistant
```

## Development

### Debug Mode
Development compose includes Java debug port (5005):
```bash
# Run in dev mode
make run-dev

# Connect debugger to localhost:5005
```

### Shell Access
```bash
# Application container
make shell-app

# Database
make shell-db

# Redis
make shell-redis
```

## Performance Tips

### Virtual Threads Best Practices
1. Use for I/O-bound operations (HTTP calls, database queries)
2. Avoid thread-local variables (use scoped values instead)
3. Don't use `synchronized` blocks (use `ReentrantLock` instead)
4. Let virtual threads be short-lived

### Container Resources
Adjust in `docker-compose.yml`:
```yaml
services:
  gary-app:
    mem_limit: 1g      # Increase if needed
    cpus: 2.0          # More CPU for heavy loads
```

### JVM Tuning
Override in `.env`:
```bash
JAVA_OPTS="-XX:MaxRAMPercentage=80.0 -XX:MaxGCPauseMillis=100"
```

## Troubleshooting

### Application won't start
```bash
# Check logs
make logs-app

# Verify dependencies
make health

# Restart services
make restart
```

### Out of memory
```bash
# Increase memory limit
docker-compose down
# Edit docker-compose.yml: mem_limit: 1g
docker-compose up -d
```

### High CPU usage
```bash
# Check Java version (must be 21+)
docker exec gary-assistant java -version

# Verify virtual threads are enabled
curl http://localhost:8080/actuator/metrics/jvm.threads.virtual
```

## Production Deployment

### Security Checklist
- [ ] Change default passwords in `.env`
- [ ] Use secrets management (Docker secrets, Kubernetes secrets)
- [ ] Enable HTTPS/TLS
- [ ] Restrict actuator endpoints
- [ ] Use non-root user (already configured)
- [ ] Regular security updates

### Scaling
```bash
# Scale application instances
docker-compose up -d --scale gary-app=3

# Load balancer required for multiple instances
```

### Backup
```bash
# Database backup
docker exec gary-postgres pg_dump -U gary garydb > backup.sql

# Restore
cat backup.sql | docker exec -i gary-postgres psql -U gary garydb
```

## Clean Up
```bash
# Stop and remove containers
make stop

# Remove everything including volumes
make clean

# Remove dangling images
docker image prune -f
```

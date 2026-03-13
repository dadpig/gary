.PHONY: help build run run-dev stop clean logs test docker-build docker-push

# Variables
APP_NAME=gary-assistant
IMAGE_NAME=gary-assistant
VERSION?=latest

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build the application with Maven
	mvn clean package -DskipTests

test: ## Run tests
	mvn test

docker-build: ## Build application and Docker image
	@echo "Building application with Java 25..."
	mvn clean package -DskipTests
	@echo "Building Docker image..."
	docker build -t $(IMAGE_NAME):$(VERSION) .

docker-run: ## Run Docker container standalone
	docker run -d \
		--name $(APP_NAME) \
		-p 8080:8080 \
		-e SPRING_PROFILES_ACTIVE=dev \
		$(IMAGE_NAME):$(VERSION)

run: ## Start all services with Docker Compose (production mode)
	docker-compose up -d

run-dev: ## Start all services with Docker Compose (development mode)
	docker-compose -f docker-compose.dev.yml up -d

stop: ## Stop all services
	docker-compose down
	docker-compose -f docker-compose.dev.yml down

clean: ## Clean all containers, images and volumes
	docker-compose down -v
	docker-compose -f docker-compose.dev.yml down -v
	docker rmi $(IMAGE_NAME):$(VERSION) 2>/dev/null || true
	mvn clean

logs: ## Show logs from all services
	docker-compose logs -f

logs-app: ## Show logs from application only
	docker-compose logs -f gary-app

restart: stop run ## Restart all services

rebuild: ## Rebuild and restart all services
	docker-compose down
	docker-compose build --no-cache
	docker-compose up -d

health: ## Check health of all services
	@echo "Checking application health..."
	@curl -f http://localhost:8080/actuator/health || echo "Application not healthy"
	@echo "\nChecking PostgreSQL..."
	@docker exec gary-postgres pg_isready -U gary || echo "PostgreSQL not ready"
	@echo "\nChecking Redis..."
	@docker exec gary-redis redis-cli ping || echo "Redis not ready"

shell-app: ## Open shell in application container
	docker exec -it gary-assistant /bin/sh

shell-db: ## Open PostgreSQL shell
	docker exec -it gary-postgres psql -U gary -d garydb

shell-redis: ## Open Redis CLI
	docker exec -it gary-redis redis-cli

stats: ## Show container resource usage
	docker stats --no-stream gary-assistant gary-postgres gary-redis

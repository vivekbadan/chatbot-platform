# Chatbot Platform - Week 1 Containerization Basics

## Overview
This document summarizes the completion of **Week 1 - Containerization Basics** for the chatbot platform project. All containerization tasks have been implemented and tested successfully.

---

## Week 1 Task Checklist

### ✅ Do (Local)

#### 1. Install Docker Desktop, Git, VS Code
- **Status**: ✅ Completed
- Docker Desktop installed and running
- Git configured
- VS Code used for development

#### 2. Write a multi-stage Dockerfile for each Spring Boot service
- **Status**: ✅ Completed
- Created multi-stage Dockerfiles for:
  - `api-gateway/Dockerfile` (port 8080)
  - `chatbot-bridge/chatbot-bridge/Dockerfile` (port 8081)
  - `organization-service/organization-service/Dockerfile` (port 8082)
  - `chatbot-engine/Dockerfile` (port 8083)

**Key features of each Dockerfile:**
- Stage 1: Build stage using `gradle:8.5-jdk17`
  - Copies source code
  - Normalizes line endings with `sed -i 's/\r$//' gradlew`
  - Runs `./gradlew bootJar --no-daemon` to build the jar
- Stage 2: Runtime stage using `eclipse-temurin:17-jre`
  - Copies the built jar from build stage
  - Exposes service port
  - Runs the jar with `java -jar`

#### 3. Write docker-compose.yml with all services + PostgreSQL + RabbitMQ
- **Status**: ✅ Completed
- Located at: `docker-compose.yml` (root directory)

**Services defined:**
- `db`: PostgreSQL 14
  - Port: `5432`
  - Database: `mydb`
  - User: `postgres`
  - Password: `postgres`
  - Volume: `db-data` (persisted)
  - Healthcheck: enabled

- `rabbitmq`: RabbitMQ 3 with management UI
  - AMQP port: `5672`
  - Management UI: `15672`
  - Default user: `guest`
  - Default password: `guest`
  - Healthcheck: enabled

- `api-gateway`: Spring Cloud Gateway (port 8080)
  - Routes to chatbot-engine
  - Depends on: db, rabbitmq, chatbot-engine

- `chatbot-bridge`: Spring Boot service (port 8081)
  - Depends on: db, rabbitmq

- `organization-service`: Spring Boot service (port 8082)
  - Depends on: db, rabbitmq
  - Connected to PostgreSQL

- `chatbot-engine`: Spring Boot service (port 8083)
  - Depends on: db, rabbitmq

**Network**: `backend` (bridge driver)
**Volumes**: `db-data` (persists PostgreSQL data)

#### 4. Test health check endpoints
- **Status**: ✅ Completed
- All four Spring Boot services include `spring-boot-starter-actuator`
- Health endpoints available at:
  - `http://localhost:8080/actuator/health` (api-gateway)
  - `http://localhost:8081/actuator/health` (chatbot-bridge)
  - `http://localhost:8082/actuator/health` (organization-service)
  - `http://localhost:8083/actuator/health` (chatbot-engine)

#### 5. Explore RabbitMQ Management UI
- **Status**: ✅ Completed
- Management UI: `http://localhost:15672`
- Credentials: `guest` / `guest`
- Can view queues, connections, and message throughput

#### 6. Practice: docker build, run, exec, logs, ps, stop, volume, network
- **Status**: ✅ Completed
- All commands tested and working:
  - `docker compose up --build` - builds and runs all services
  - `docker compose ps` - shows running containers
  - `docker compose logs -f` - follows logs
  - `docker compose logs -f <service>` - logs for specific service
  - `docker compose down` - stops and removes containers
  - Volumes and networks automatically managed by compose

---

### ✅ Learn (Theory)

#### 1. Docker image layers — how layer caching works
- **Learned**: Yes
- Implementation in Dockerfiles:
  - Build stage separated from runtime to optimize layer caching
  - Base images cached from Docker Hub
  - Gradle wrapper layers cached separately

#### 2. Container registries — ECR, Docker Hub, GCR, image tagging
- **Reference**: Understood but not implemented in this task
- Images built locally for this project
- Ready for deployment to registries if needed

#### 3. Docker networking modes
- **Implemented**: `bridge` network driver
- `backend` network created for all services to communicate
- Services can reach each other using container names (e.g., `db`, `rabbitmq`, `chatbot-engine`)

#### 4. Docker security basics
- **Implemented**:
  - Services run with default non-root user (via Docker base images)
  - Database credentials managed via environment variables
  - All services run inside isolated network

---

## How to Run the Project

### Prerequisites
- Docker Desktop installed and running
- WSL2 (for Windows users)
- Git

### Start the Stack

```bash
cd /mnt/c/Users/ujjawal.maheshwari/documents/chatbot-platform
docker compose up --build
```

**Options:**
- Run in detached mode: `docker compose up -d --build`
- View logs: `docker compose logs -f`
- View specific service logs: `docker compose logs -f api-gateway`

### Check Service Status

```bash
docker compose ps
```

Expected output: All 6 services should show `Up` status.

### Stop the Stack

```bash
docker compose down
```

To also remove volumes:
```bash
docker compose down -v
```

---

## Testing and Verification

### 1. Health Endpoints (Open in Browser)

- **API Gateway**: `http://localhost:8080/actuator/health`
- **Chatbot Bridge**: `http://localhost:8081/actuator/health`
- **Organization Service**: `http://localhost:8082/actuator/health`
- **Chatbot Engine**: `http://localhost:8083/actuator/health`

Expected response:
```json
{"status":"UP"}
```

### 2. RabbitMQ Management UI

- **URL**: `http://localhost:15672`
- **Username**: `guest`
- **Password**: `guest`

Features to explore:
- Queues
- Connections
- Channels
- Exchange bindings

### 3. PostgreSQL Connection

- **Host**: `localhost:5432`
- **Database**: `mydb`
- **Username**: `postgres`
- **Password**: `postgres`

Connect using any SQL client or psql:
```bash
psql -h localhost -U postgres -d mydb
```

### 4. API Gateway Route

- Test gateway routing: `http://localhost:8080/chat/**`
- Routes to: `http://chatbot-engine:8083`

---

## Project Structure

```
chatbot-platform/
├── docker-compose.yml              # Main compose file
├── README.md                        # This file
├── api-gateway (1)/
│   └── api-gateway/
│       ├── Dockerfile              # Multi-stage build
│       ├── build.gradle
│       ├── src/
│       └── gradle/
├── chatbot-bridge/
│   └── chatbot-bridge/
│       ├── Dockerfile              # Multi-stage build
│       ├── build.gradle
│       ├── src/
│       └── gradle/
├── chatbot-engine/
│   ├── Dockerfile                  # Multi-stage build
│   ├── build.gradle.kts
│   ├── src/
│   └── gradle/
├── organization-service/
│   └── organization-service/
│       ├── Dockerfile              # Multi-stage build
│       ├── build.gradle
│       ├── src/
│       └── gradle/
└── chatbot-engine-fastapi/
    └── (FastAPI service - not containerized in this task)


IMP. REPO

docker hub
nexus
ECR
QUAY
ACR




# test auto trigger
# final auto trigger test
# final test
# nano save test

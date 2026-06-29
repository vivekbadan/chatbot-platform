# 🚀 Week 2 — CI with Jenkins (Local) — Complete Demo Guide

**Author:** Ujjawal Maheshwari  
**Branch:** `Ujjawal-docker`  
**Date:** June 2026

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Phase 1 — Start the Infrastructure](#phase-1--start-the-infrastructure)
3. [Phase 2 — Open Jenkins UI](#phase-2--open-jenkins-ui)
4. [Phase 3 — Run Pipeline (Manual Trigger)](#phase-3--run-pipeline-manual-trigger)
5. [Phase 4 — Demonstrate Git Hook (Auto Trigger)](#phase-4--demonstrate-git-hook-auto-trigger)
6. [Phase 5 — Explain Key Concepts (Theory)](#phase-5--explain-key-concepts-theory)
7. [Phase 6 — Show Project Structure](#phase-6--show-project-structure)
8. [Troubleshooting](#troubleshooting)
9. [Quick Checklist Before Demo](#quick-checklist-before-demo)
10. [Demo Timeline](#demo-timeline)

---

## Prerequisites

| Tool             | Check Command         | Expected                     |
|------------------|-----------------------|------------------------------|
| Docker Desktop   | Running on Windows    | Docker icon in system tray   |
| WSL              | `wsl` from PowerShell | Linux terminal opens         |
| Git              | `git --version`       | git version 2.x.x           |
| VS Code          | Open project folder   | Project files visible        |

---

## Phase 1 — Start the Infrastructure

### Step 1 — Open WSL terminal

```bash
wsl
cd /mnt/c/Users/ujjawal.maheshwari/Documents/chatbot-platform
```

---

### Step 2 — Build custom Jenkins image

```bash
docker build -t chatbot-jenkins:lts ./jenkins
```

> **What to explain:** "This custom image is based on `jenkins/jenkins:lts-jdk17` and includes Docker CLI pre-installed. This allows Jenkins to build Docker images from inside the container."

---

### Step 3 — Start all containers (Jenkins + PostgreSQL + RabbitMQ)

**Option A — Using Docker Compose (if configured):**

```bash
docker compose up -d
```

**Option B — Start Jenkins separately:**

```bash
docker run -d \
  --name jenkins-chatbot-ci \
  -p 8888:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  chatbot-jenkins:lts
```

> **What to explain:** "We mount the Docker socket (`/var/run/docker.sock`) so Jenkins can use the host's Docker daemon. This is called Docker Socket Binding — no Docker-in-Docker needed."

---

### Step 4 — Fix Docker socket permission

```bash
docker exec -u root -it jenkins-chatbot-ci bash -lc "chmod 666 /var/run/docker.sock"
docker restart jenkins-chatbot-ci
```

> **What to explain:** "The Jenkins user inside the container needs permission to access the Docker daemon socket. `chmod 666` grants read/write access to all users."

---

### Step 5 — Verify everything is running

```bash
docker ps
```

**Expected output:**

```
CONTAINER ID   IMAGE                   STATUS          PORTS                    NAMES
xxxxxxxxxx     chatbot-jenkins:lts     Up X minutes    0.0.0.0:8888->8080/tcp   jenkins-chatbot-ci
xxxxxxxxxx     postgres:14             Up X minutes    0.0.0.0:5432->5432/tcp   chatbot-platform-db-1
xxxxxxxxxx     rabbitmq:3-management   Up X minutes    0.0.0.0:5672->5672/tcp   chatbot-platform-rabbitmq-1
```

---

### Step 6 — Verify Java and Docker inside Jenkins

```bash
docker exec -it jenkins-chatbot-ci java -version
docker exec -it jenkins-chatbot-ci docker --version
docker exec -it jenkins-chatbot-ci docker ps
```

**Expected:**

```
openjdk version "17.0.18" 2026-01-20     ✅ Java 17
Docker version 26.1.5+dfsg1              ✅ Docker CLI
CONTAINER ID   IMAGE ...                 ✅ Docker access working
```

---

## Phase 2 — Open Jenkins UI

### Step 7 — Open browser

```
http://localhost:8888
```

---

### Step 8 — Login

**If Jenkins asks for initial admin password:**

```bash
docker exec jenkins-chatbot-ci cat /var/jenkins_home/secrets/initialAdminPassword
```

Copy the password → paste in browser → Continue → Install suggested plugins → Create admin user.

**If already set up:** Login with your credentials (e.g., `guest`).

---

### Step 9 — Create pipeline job (if not already created)

1. Click **New Item**
2. Name: `chatbot-platform`
3. Type: **Pipeline**
4. Click **OK**
5. Configure:
   - **Pipeline → Definition:** `Pipeline script from SCM`
   - **SCM:** Git
   - **Repository URL:** `https://github.com/vivekbadan/chatbot-platform`
   - **Branch:** `*/Ujjawal-docker`
   - **Script Path:** `Jenkinsfile`
6. Click **Save**

> **What to explain:** "Jenkins fetches the Jenkinsfile directly from our Git repository. This is called 'Pipeline as Code' — the CI configuration lives alongside the source code."

---

## Phase 3 — Run Pipeline (Manual Trigger)

### Step 10 — Click "Build with Parameters"

```
SKIP_TESTS  = false   (unchecked)
CLEAN_BUILD = true    (checked)
```

Click **Build**.

> **What to explain:**
> - `SKIP_TESTS`: When checked, skips unit tests for faster builds during debugging.
> - `CLEAN_BUILD`: When checked, removes old build artifacts before building.

---

### Step 11 — Show pipeline stages to examiner

Open the build → **Console Output** or **Pipeline View**.

Show these stages executing:

```
1. Checkout                              ✅ Pulls code from GitHub
2. Prepare Gradle Wrappers              ✅ Makes gradlew executable
3. Build & Test - Parallel               ✅ 4 services build simultaneously
   ├── API Gateway                       ✅
   ├── Chatbot Bridge                    ✅
   ├── Organization Service              ✅
   └── Chatbot Engine                    ✅
4. Docker Build - Parallel               ✅ 4 Docker images built simultaneously
   ├── Docker: API Gateway               ✅
   ├── Docker: Chatbot Bridge            ✅
   ├── Docker: Organization Service      ✅
   └── Docker: Chatbot Engine            ✅
5. Verify Docker Images                  ✅ Lists all built images
6. Build Summary                         ✅ Shows branch, commit, build number
```

---

### Step 12 — Show Docker images created

From WSL terminal:

```bash
docker images | grep chatbot
```

**Expected output:**

```
chatbot-api-gateway            latest    xxxxxxxxxx   X minutes ago   XXX MB
chatbot-api-gateway            5         xxxxxxxxxx   X minutes ago   XXX MB
chatbot-chatbot-bridge         latest    xxxxxxxxxx   X minutes ago   XXX MB
chatbot-chatbot-bridge         5         xxxxxxxxxx   X minutes ago   XXX MB
chatbot-organization-service   latest    xxxxxxxxxx   X minutes ago   XXX MB
chatbot-organization-service   5         xxxxxxxxxx   X minutes ago   XXX MB
chatbot-chatbot-engine         latest    xxxxxxxxxx   X minutes ago   XXX MB
chatbot-chatbot-engine         5         xxxxxxxxxx   X minutes ago   XXX MB
```

> **What to explain:** "Each service image is tagged with both the Jenkins build number and `latest`. This allows us to track which build produced which image and always have a `latest` pointer."

---

## Phase 4 — Demonstrate Git Hook (Auto Trigger)

### Step 13 — Show the hook file

```bash
cat .git/hooks/post-commit
```

> **What to explain:** "This is a Git post-commit hook. It runs automatically after every `git commit` and sends an HTTP request to Jenkins API to trigger a new build. It only triggers for the `Ujjawal-docker` branch."

---

### Step 14 — Make a small code change and commit

```bash
echo "# CI Demo" >> README.md
git add README.md
git commit -m "demo: auto trigger jenkins via git hook"
```

**Expected output:**

```
[Jenkins CI] Triggering build for branch: Ujjawal-docker
[Jenkins CI] ✓ Build triggered successfully
[Ujjawal-docker xxxxxxx] demo: auto trigger jenkins via git hook
 1 file changed, 1 insertion(+)
```

---

### Step 15 — Show Jenkins UI

Open `http://localhost:8888` → A new build should appear automatically in the build history.

> **What to explain:** "The build was triggered automatically by the Git commit hook — no manual click needed. This simulates a real CI workflow where every code change triggers a build."

---

## Phase 5 — Explain Key Concepts (Theory)

| Concept                  | Explanation                                                                                          |
|--------------------------|------------------------------------------------------------------------------------------------------|
| **Declarative Pipeline** | Jenkinsfile uses `pipeline {}` block — structured, readable, and version-controlled CI definition.   |
| **Stages**               | Logical groupings of work — Checkout, Build, Docker Build, etc. Each stage has a clear purpose.      |
| **Steps**                | Individual commands inside stages — `sh`, `echo`, `checkout scm`, `docker build`.                   |
| **Agents**               | `agent any` means pipeline runs on any available Jenkins node. We use the built-in node.             |
| **Environment Variables**| Defined in `environment {}` block — `REGISTRY`, `IMAGE_PREFIX`, `BUILD_TAG`. Available across all stages. |
| **Parameters**           | `SKIP_TESTS` and `CLEAN_BUILD` are boolean params that make the pipeline configurable at runtime.    |
| **Credentials Store**    | Jenkins securely stores secrets like API tokens. We use API token for Git hook authentication.       |
| **Parallel Stages**      | All 4 services build simultaneously using `parallel {}` — faster CI. Same for Docker builds.         |
| **Docker Socket Binding**| Jenkins container uses host Docker via `-v /var/run/docker.sock` — no Docker-in-Docker needed.      |
| **Pipeline as Code**     | Jenkinsfile is stored in Git repo alongside source code — versioned, reviewable, and portable.       |
| **Post Actions**         | `post { success { } failure { } }` — runs different actions based on build result.                  |
| **Build Discarder**      | `buildDiscarder(logRotator(numToKeepStr: '10'))` — keeps only last 10 builds to save disk space.    |

---

## Phase 6 — Show Project Structure

### Step 16 — Show key files

```bash
ls -la
```

**Important files to highlight:**

| File / Folder                        | Purpose                                           |
|--------------------------------------|---------------------------------------------------|
| `Jenkinsfile`                        | Declarative CI pipeline definition                |
| `docker-compose.yml`                 | Orchestrates Jenkins + PostgreSQL + RabbitMQ       |
| `jenkins/Dockerfile`                 | Custom Jenkins image with JDK17 + Docker CLI      |
| `jenkins/plugins.txt`               | Jenkins plugins list                              |
| `.git/hooks/post-commit`            | Git hook to auto-trigger Jenkins builds           |
| `api-gateway/api-gateway/`          | API Gateway Spring Boot service                   |
| `chatbot-bridge/chatbot-bridge/`    | Chatbot Bridge Spring Boot service                |
| `organization-service/organization-service/` | Organization Service (with H2 test profile) |
| `chatbot-engine/`                    | Chatbot Engine Spring Boot service                |
| `GIT_HOOKS_SETUP.md`               | Git hooks setup documentation                     |
| `JENKINSFILE_CONCEPTS.md`          | Jenkins pipeline concepts reference               |
| `JENKINS_SETUP_GUIDE.md`           | Jenkins setup guide                               |
| `README_WEEK2_JENKINS.md`          | Week 2 implementation details                     |

### Organization Service Test Fix (important to mention)

The Organization Service originally failed in Jenkins because it tried to connect to PostgreSQL during tests. We fixed this by:

1. Adding `testRuntimeOnly 'com.h2database:h2'` in `build.gradle`
2. Creating `src/test/resources/application-test.yml` with H2 in-memory database config
3. Adding `@ActiveProfiles("test")` to the test class

This allows tests to run with an embedded H2 database instead of requiring a real PostgreSQL instance.

---

## Troubleshooting

| Problem                              | Quick Fix                                                                                     |
|--------------------------------------|-----------------------------------------------------------------------------------------------|
| Jenkins not opening                  | `docker restart jenkins-chatbot-ci`                                                          |
| `docker: not found` in pipeline      | `docker exec -u root -it jenkins-chatbot-ci bash -lc "chmod 666 /var/run/docker.sock"` then restart |
| Organization Service test fails      | Check `application-test.yml` exists in `src/test/resources/`                                 |
| Git hook not triggering              | `chmod +x .git/hooks/post-commit`                                                           |
| Jenkins asks for password            | `docker exec jenkins-chatbot-ci cat /var/jenkins_home/secrets/initialAdminPassword`          |
| Java 17 not found                    | Verify custom image: `docker exec -it jenkins-chatbot-ci java -version`                      |
| Port already allocated               | Stop conflicting container: `docker stop <container_name>`                                   |
| `junit` DSL not found               | JUnit plugin not installed — already removed from Jenkinsfile post stages                    |
| Permission denied (docker.sock)      | `docker exec -u root -it jenkins-chatbot-ci bash -lc "chmod 666 /var/run/docker.sock"`      |
| Container not running                | `docker start jenkins-chatbot-ci`                                                            |

---

## Quick Checklist Before Demo

- [ ] Docker Desktop is running
- [ ] `docker compose up -d` or Jenkins container is running
- [ ] `docker ps` shows all containers as Up
- [ ] Jenkins accessible at `http://localhost:8888`
- [ ] Pipeline job `chatbot-platform` is configured
- [ ] `.git/hooks/post-commit` exists and is executable
- [ ] Git config set (`user.name` and `user.email`)
- [ ] Code pushed to `Ujjawal-docker` branch on GitHub

---

## Demo Timeline

| Phase                          | Estimated Time |
|--------------------------------|----------------|
| Phase 1 — Start Infrastructure | ~2 minutes     |
| Phase 2 — Open Jenkins UI      | ~1 minute      |
| Phase 3 — Run Pipeline          | ~5-7 minutes (build time) |
| Phase 4 — Git Hook Demo         | ~2 minutes     |
| Phase 5 — Explain Concepts      | ~3-5 minutes   |
| Phase 6 — Show Structure        | ~1 minute      |
| **Total**                       | **~15-18 minutes** |

---

## Issues Fixed During Implementation

| Issue                                    | Root Cause                                   | Solution                                      |
|------------------------------------------|----------------------------------------------|-----------------------------------------------|
| Organization Service test failure        | No test database configured                  | H2 in-memory DB + `@ActiveProfiles("test")`  |
| `docker: not found` in Jenkins           | Plain Jenkins image lacks Docker CLI         | Custom Dockerfile with `docker.io` installed  |
| Permission denied on docker.sock         | Jenkins user lacks Docker daemon access      | `chmod 666 /var/run/docker.sock`              |
| Java 17 toolchain not found             | Wrong Jenkins base image (`lts` vs `lts-jdk17`) | Custom image based on `lts-jdk17`          |
| Jenkins plugin incompatibility           | Jenkins version too old for plugins          | Removed `junit`/`archiveArtifacts` from Jenkinsfile |
| Folder naming (`api-gateway (1)`)        | Space and parentheses in folder name         | Renamed to `api-gateway`                      |
| Docker CLI not available during build    | `docker.io` package didn't expose CLI binary | Added `docker-cli` package to Dockerfile      |
| Git identity unknown in WSL              | Git user not configured in WSL               | `git config --global user.name/email`         |

---

## Key Takeaways

1. **Jenkins in Docker** — Running Jenkins as a container with Docker socket binding enables Docker-in-Docker-like functionality without the complexity.
2. **Pipeline as Code** — Storing CI configuration in a `Jenkinsfile` alongside source code makes it version-controlled, reviewable, and portable.
3. **Parallel Stages** — Building 4 microservices in parallel significantly reduces total CI time.
4. **Test Profiles** — Using `@ActiveProfiles("test")` with H2 ensures tests don't depend on external infrastructure.
5. **Git Hooks** — Local post-commit hooks provide immediate CI feedback during development.
6. **Custom Docker Images** — Building project-specific Jenkins images ensures reproducible CI environments.

---

*This guide was created as part of Week 2 — CI with Jenkins (Local) internship task.*


test 
cat .git/hooks/post-commit


cd /mnt/c/Users/ujjawal.maheshwari/Documents/chatbot-platform
docker compose up -d

docker exec -u root -it jenkins-chatbot-ci bash -lc "chmod 666 /var/run/docker.sock"
docker restart jenkins-chatbot-ci

docker ps

jenkins-chatbot-ci         ✅ Up
chatbot-platform-db-1      ✅ Up
chatbot-platform-rabbitmq-1 ✅ Up



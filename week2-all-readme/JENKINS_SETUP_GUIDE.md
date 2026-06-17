# Week 2 CI/CD with Jenkins - Implementation Guide

## Quick Start (TL;DR)

```bash
# 1. Start Jenkins
docker compose up -d jenkins

# 2. Get initial password
docker compose logs jenkins | grep -i "password"

# 3. Open Jenkins
# Browser: http://localhost:8888
# Unlock, complete setup, create admin user

# 4. Create Pipeline job pointing to this repo's Jenkinsfile
# GitHub webhook will auto-trigger builds on commits
```

---

## Phase 1: Start Jenkins Container

### 1.1 Updated docker-compose.yml
✅ **Status**: Complete
- Added `jenkins` service with Docker socket mounted
- Jenkins exposed on ports 8888 (UI) and 50000 (agent communication)
- Persistent volume: `jenkins-data` stores configuration, builds, credentials
- On `backend` network for communication with app services
- Healthcheck enabled to monitor startup

### 1.2 Start Jenkins Container

```bash
# Navigate to project root
cd c:\Users\ujjawal.maheshwari\Documents\chatbot-platform

# Start Jenkins (and existing services)
docker compose up -d jenkins

# Monitor startup
docker compose logs -f jenkins

# Wait for line: "Jenkins is fully up and running"
# Watch for: "Jenkins initial setup is required"
```

**Expected output after ~30-60 seconds:**
```
jenkins-chatbot-ci  | ...
jenkins-chatbot-ci  | Jenkins initial setup is required. An admin user has been created and a password generated.
jenkins-chatbot-ci  | *************************************************************
jenkins-chatbot-ci  | *************************************************************
jenkins-chatbot-ci  | *************************************************************
jenkins-chatbot-ci  | 
jenkins-chatbot-ci  | This may also be found at: /var/jenkins_home/secrets/initialAdminPassword
jenkins-chatbot-ci  | 
jenkins-chatbot-ci  | *************************************************************
```

### 1.3 Extract Initial Admin Password

```bash
# Get password from logs
docker compose logs jenkins | grep -A 5 "initialAdminPassword"

# Alternative: Copy from Jenkins home
docker compose exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Save this password for unlock step
```

### 1.4 Open Jenkins UI

1. Open browser: `http://localhost:8888`
2. **Unlock Jenkins**: Paste the initial admin password
3. **Install Suggested Plugins**
   - Click "Install suggested plugins"
   - Wait for installation (~5 minutes)
   - Shows: Git, Pipeline, Docker, GitHub Integration, etc.
4. **Create Admin User**
   - Username: `admin` (or your choice)
   - Password: `admin123` (or strong password)
   - Full name: "Jenkins Admin"
   - Email: `admin@chatbot-local` (can be dummy)
   - Click "Save and Continue"
5. **Configure Jenkins URL**
   - URL: `http://localhost:8888/`
   - Click "Save and Finish"
6. **Start Using Jenkins**: You're now on dashboard

---

## Phase 2: Install Additional Plugins

### 2.1 Navigate to Plugin Manager

1. Jenkins Dashboard → Manage Jenkins → Manage Plugins (or Plugins Manager)
2. Go to **Available Plugins** tab
3. Search and install:

| Plugin Name | Purpose | Search Term |
|------------|---------|-------------|
| Docker Pipeline | Build Docker images in Jenkins | `docker-workflow` |
| GitHub Integration | Trigger builds on GitHub events | `github` |
| Credentials Binding | Use secrets in pipeline | `credentials-binding` |
| Blue Ocean | Better pipeline UI (optional) | `blueocean` |

### 2.2 Install Steps

```
1. Search for "Docker Pipeline"
2. Check the checkbox
3. Click "Install without restart"
4. Repeat for each plugin
5. After all installed, Jenkins auto-restarts
```

**Verify Installation:**
- Jenkins Dashboard → Manage Jenkins → Manage Plugins → Installed Plugins
- Search for each plugin name above

---

## Phase 3: Configure Docker in Jenkins

### 3.1 Add Docker Server

1. Jenkins Dashboard → Manage Jenkins → Configure System
2. Scroll to **Docker** section (added by Docker Pipeline plugin)
3. Click **Add Docker** or **Docker Server**
4. Fill in:
   - **Name**: `Local Docker` (or any label)
   - **Docker URL**: `unix:///var/run/docker.sock`
   - **Enabled**: ✓ checked
5. Click **Test Connection**
   - Expected: "API version check: OK" or similar success message
6. Click **Save**

---

## Phase 4: Configure Credentials in Jenkins

### 4.1 GitHub Personal Access Token (Required for Webhook & SCM)

**Generate Token on GitHub:**
1. GitHub Profile → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Token name: `Jenkins-CI`
4. Scopes (checkboxes):
   - ☑ `repo:status` (commit status updates)
   - ☑ `public_repo` (access public repos)
   - ☑ `admin:repo_hook` (webhook management)
5. Click "Generate token"
6. **Copy token** (you won't see it again)

**Add Token to Jenkins:**
1. Jenkins Dashboard → Manage Jenkins → Manage Credentials
2. Click **System** (left sidebar)
3. Click **Global credentials** (top section)
4. Click **+ Add Credentials** (left sidebar)
5. Fill form:
   - **Kind**: `Secret text`
   - **Secret**: `[paste GitHub token here]`
   - **ID**: `github-token` ← **Important: must match Jenkinsfile**
   - **Description**: `GitHub Personal Access Token`
6. Click **Create**

**Verification:**
- Manage Credentials → System → Global credentials
- You should see `github-token` in the list

### 4.2 Optional: Docker Hub Credentials (for Week 3 Registry Push)

**For now, skip this.** We're building locally Week 2.

---

## Phase 5: Create Jenkins Pipeline Job

### 5.1 Create New Job

1. Jenkins Dashboard → **+ New Item** (top left)
2. **Job name**: `chatbot-platform` (or `chatbot-ci`)
3. **Type**: Select `Pipeline` (blue Jenkins icon)
4. Click **OK**

### 5.2 Configure Pipeline

1. **General** tab (top section):
   - **Description**: `CI/CD Pipeline for Chatbot Platform - All 4 Java services`
   - Check ✓ **GitHub project**
   - **Project URL**: `https://github.com/vivekbadan/chatbot-platform`

2. **Build Triggers** tab:
   - ☑ **GitHub hook trigger for GITScm polling**
     - (This enables webhook from GitHub)
   - ☑ **Poll SCM** (optional, for fallback if webhook fails)
     - **Schedule**: Leave empty (or `H * * * *` for every hour)

3. **Pipeline** tab (bottom):
   - **Definition**: `Pipeline script from SCM`
   - **SCM**: `Git`
   - **Repository URL**: `https://github.com/vivekbadan/chatbot-platform.git`
   - **Credentials**: `github-token` (select from dropdown)
   - **Branch Specifier**: `*/Ujjawal-docker` ← Your branch name
   - **Script Path**: `Jenkinsfile` (default, this repo has one at root)

4. Click **Save**

**Expected Result:**
- Jenkins will fetch repository and read Jenkinsfile
- If no errors, you'll see job on Dashboard

---

## Phase 6: Configure GitHub Webhook

### 6.1 Add Webhook to GitHub Repository

1. GitHub → `vivekbadan/chatbot-platform` → Settings → Webhooks
2. Click **Add webhook**
3. Fill form:
   - **Payload URL**: 
     ```
     http://localhost:8888/github-webhook/
     ```
     ⚠️ **Note**: For local development, this URL must be accessible from GitHub. Options:
     - **Option A (Recommended)**: Use `ngrok` to expose Jenkins publicly
       ```bash
       ngrok http 8888
       # Then use: http://<ngrok-id>.ngrok.io/github-webhook/
       ```
     - **Option B (Local Testing)**: Skip webhook, manually trigger builds
     - **Option C (Production)**: Use actual domain if Jenkins is public
   
   - **Content type**: `application/json`
   - **Events**: Select `Just the push event` (or ☑ Push events)
   - **Active**: ☑ checked
   - Leave **Secret** empty for Week 2 (can add in Week 3)

4. Click **Add webhook**

### 6.2 Test Webhook (if using ngrok)

1. GitHub Webhooks page → Recent Deliveries
2. Click latest delivery → you should see:
   - HTTP 200 response
   - Request/Response bodies
3. If 404 or error, check:
   - Payload URL is correct
   - Jenkins is running
   - ngrok tunnel is active

### 6.3 Alternative: Local Git Commit Hook (No Webhook)

If webhook setup is complex, use local Git hook instead:

1. **Create file**: `./git/hooks/post-commit` (make it executable)
   ```bash
   #!/bin/bash
   # Get Jenkins API token from Jenkins Dashboard → Manage Users → your user → Configure → API Token
   # Replace YOUR_JENKINS_TOKEN and YOUR_USERNAME below
   
   JENKINS_URL="http://localhost:8888"
   JOB_NAME="chatbot-platform"
   JENKINS_USER="admin"
   JENKINS_TOKEN="YOUR_API_TOKEN_HERE"
   
   curl -X POST "${JENKINS_URL}/job/${JOB_NAME}/buildWithParameters" \
     --user "${JENKINS_USER}:${JENKINS_TOKEN}" \
     -d "token=chatbot-ci" \
     -d "BRANCH=Ujjawal-docker"
   
   echo "Build triggered on Jenkins"
   ```

2. Make executable:
   ```bash
   chmod +x .git/hooks/post-commit
   ```

3. Now every `git commit` will trigger a Jenkins build

---

## Phase 7: Manual Test of Pipeline

### 7.1 Trigger Build Manually (No Git Commit)

1. Jenkins Dashboard → Click `chatbot-platform` job
2. Click **Build Now** (top left)
3. **Console Output** opens automatically
4. Watch the build progress in real-time

**Expected stages (in order):**
```
========================================
Checking out code from Git repository
========================================
✓ Clones repo from GitHub

========================================
Building API Gateway service
========================================
✓ ./gradlew clean build for api-gateway

(Similar for Chatbot Bridge, Organization Service, Chatbot Engine in parallel)

========================================
Building Docker image for API Gateway
========================================
✓ docker build for api-gateway:BUILD_NUMBER

(Similar for all 4 services in parallel)

========================================
Verifying all Docker images
========================================
✓ Lists all 4 Docker images created

========================================
BUILD PIPELINE COMPLETE
========================================
✓ Summary of all built images
```

### 7.2 Check Build Artifacts

1. Click build number (e.g., `#1`) on job page
2. **Artifacts** section shows `.jar` files from each service
3. **Test Results** shows JUnit test pass/fail counts
4. **Console Output** has full logs

### 7.3 Verify Docker Images Created

```bash
# List all chatbot images
docker images | grep chatbot-

# Should show:
chatbot-api-gateway          latest                    BUILD_NUMBER
chatbot-api-gateway          BUILD_NUMBER              BUILD_NUMBER
chatbot-chatbot-bridge       latest                    BUILD_NUMBER
chatbot-chatbot-bridge       BUILD_NUMBER              BUILD_NUMBER
... (and 2 more services)
```

---

## Phase 8: Test Git Integration

### 8.1 Trigger via Git Commit (If Webhook Configured)

```bash
# Make a test commit on your Ujjawal-docker branch
git add .
git commit -m "Test Jenkins CI trigger"
git push origin Ujjawal-docker

# Check GitHub Webhook delivery (Settings → Webhooks → Recent Deliveries)
# Check Jenkins Dashboard - new build should start automatically
```

### 8.2 Monitor Build

1. Jenkins Dashboard → `chatbot-platform` job
2. You should see new build `#2` (or higher) in "Build History"
3. Click build number → "Console Output" to watch progress

---

## Jenkinsfile Explanation

### Pipeline Structure

**1. Agent**: `any` (uses default Jenkins agent, can be Docker if needed)

**2. Options**:
- `timestamps()` - Adds timestamps to each log line
- `timeout()` - Kill build if it exceeds 30 minutes
- `buildDiscarder()` - Keep only last 10 builds (saves disk)
- `disableConcurrentBuilds()` - Don't run 2 builds simultaneously

**3. Environment Variables** (available in all stages):
```groovy
REGISTRY = 'localhost'  // Docker registry URL
IMAGE_PREFIX = 'chatbot'  // Image naming prefix
BUILD_TAG = '${BUILD_NUMBER}'  // Unique tag per build
GIT_COMMIT_SHORT = '${GIT_COMMIT.take(7)}'  // Short commit hash
```

**4. Parameters** (pass options when triggering build):
```groovy
SKIP_TESTS = false  // If true, skip unit tests (faster)
CLEAN_BUILD = false  // If true, run ./gradlew clean
```

**5. Stages**:

| Stage | Purpose | Runs |
|-------|---------|------|
| **Checkout** | Clone repo from GitHub | Sequential (once) |
| **Build & Test** | Compile + test 4 services | Parallel (all at once) |
| **Docker Build** | Build Docker images | Parallel (all at once) |
| **Verify** | Confirm images exist | Sequential (once) |
| **Summary** | Display build results | Sequential (once) |

**6. Post Actions** (run after all stages):
```groovy
always {}   // Runs regardless of success/failure
success {}  // Runs only if all stages succeeded
failure {}  // Runs only if any stage failed
```

---

## Understanding Key Concepts

### Declarative Pipeline

**Declarative** = Structured, easier to learn, limited flexibility

```groovy
pipeline {
    agent any          // WHERE to run
    options {}         // HOW to run
    environment {}     // WHAT variables
    parameters {}      // WHAT inputs
    stages {}          // WHAT steps
    post {}            // WHAT after
}
```

**Scripted Pipeline** = Full Groovy language, more flexible, harder to learn
- You'll learn this in Week 3 if needed

### Parallel Execution

In "Build & Test - Parallel" stage:
```groovy
parallel {
    stage('API Gateway') { ... }
    stage('Chatbot Bridge') { ... }
    stage('Organization Service') { ... }
    stage('Chatbot Engine') { ... }
}
```
All 4 services build simultaneously, reducing time from ~4min to ~1min

### Credentials in Pipeline

**Jenkinsfile syntax** (already used):
```groovy
checkout([
    $class: 'GitSCM',
    userRemoteConfigs: [[
        url: 'https://github.com/vivekbadan/chatbot-platform.git',
        credentialsId: 'github-token'  // References credential ID
    ]]
])
```

**Why secure?** The actual token value is not in Jenkinsfile. Jenkins looks up `github-token` from its Credentials store.

---

## Troubleshooting

### Build Fails: "Jenkinsfile not found"
- **Cause**: Script Path is wrong
- **Fix**: Verify Jenkinsfile is at repo root: `ls -la Jenkinsfile`
- **Fix**: Check Pipeline job → Pipeline tab → Script Path = `Jenkinsfile`

### Build Fails: Git checkout fails (401 Unauthorized)
- **Cause**: `github-token` credential not created or invalid
- **Fix**: Verify GitHub token in Manage Credentials
- **Fix**: Regenerate token on GitHub, update Jenkins credential

### Build Fails: Docker build fails (Permission Denied)
- **Cause**: Jenkins container can't access Docker socket
- **Fix**: Verify Jenkins container started with `-v /var/run/docker.sock:/var/run/docker.sock`
- **Fix**: Check docker-compose.yml Jenkins service volumes

### Build Fails: "api-gateway (1)" directory not found
- **Cause**: Path with spaces needs quotes
- **Fix**: Already fixed in Jenkinsfile with `cd "./api-gateway (1)/api-gateway"`

### Tests fail in Jenkins but pass locally
- **Cause**: Jenkins environment differs (Java version, environment vars)
- **Fix**: Check Jenkins agent Java version: `./gradlew --version`
- **Fix**: Add missing environment variables to Jenkinsfile

---

## Next Steps (Week 3)

1. **Docker Registry Push**
   - Push built images to Docker Hub or private registry
   - Update Jenkinsfile "Docker Build" stage to add `docker push`
   - Configure Docker Hub credentials in Jenkins

2. **Advanced Testing**
   - Integration tests with PostgreSQL + RabbitMQ
   - Code coverage reports (JaCoCo plugin)
   - SonarQube for code quality

3. **Notifications**
   - Slack integration for build status
   - Email notifications on failure
   - GitHub commit status updates

4. **Performance Optimization**
   - Cache Docker layers (multi-stage Dockerfile optimization)
   - Cache Gradle dependencies
   - Parallel deployment across multiple agents

---

## References

- **Jenkins Official**: https://www.jenkins.io/doc/
- **Declarative Pipeline Syntax**: https://www.jenkins.io/doc/book/pipeline/syntax/
- **GitHub Integration**: https://plugins.jenkins.io/github/
- **Docker in Jenkins**: https://plugins.jenkins.io/docker-workflow/
- **ngrok for Local Webhooks**: https://ngrok.com/docs

---

## Quick Command Reference

```bash
# Start Jenkins
docker compose up -d jenkins

# View Jenkins logs
docker compose logs -f jenkins

# Get initial password
docker compose logs jenkins | grep initialAdminPassword

# Access Jenkins UI
# Browser: http://localhost:8888

# List Docker images built
docker images | grep chatbot-

# View containers running
docker compose ps

# Stop Jenkins
docker compose stop jenkins

# Remove Jenkins container (preserves jenkins-data volume)
docker compose down jenkins

# Remove all Jenkins data (fresh start)
docker volume rm chatbot-platform_jenkins-data
```

---

✅ **You're ready to use Jenkins for CI/CD!**

Next: Start with Phase 1 (docker compose up -d jenkins) and follow the setup steps sequentially.

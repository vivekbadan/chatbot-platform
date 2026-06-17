# Git Commit Hooks for Jenkins CI Trigger

**Purpose**: Automatically trigger Jenkins builds on local `git commit` without needing GitHub webhooks.

**Benefit**: Useful for offline development or testing pipeline before pushing to GitHub.

---

## Setup (Windows + Git Bash)

### Step 1: Get Your Jenkins API Token

1. Jenkins Dashboard → Manage Jenkins → Manage Users
2. Click your user name (e.g., `admin`)
3. Click **Configure** (left sidebar)
4. Scroll to **API Token** section
5. Click **Generate** (or "Generate new token")
6. Copy the token (long alphanumeric string)
7. Save it safely (you'll need it for the hook)

### Step 2: Create Post-Commit Hook Script

Navigate to your repo and create the hook file:

```bash
# Open Git Bash in your repo
cd c:\Users\ujjawal.maheshwari\Documents\chatbot-platform

# Create hooks directory if it doesn't exist
mkdir -p .git/hooks

# Create the post-commit hook file
cat > .git/hooks/post-commit << 'EOF'
#!/bin/bash
# Jenkins CI Trigger Hook
# Runs after successful git commit to trigger Jenkins build

# Configuration
JENKINS_URL="http://localhost:8888"
JENKINS_JOB="chatbot-platform"
JENKINS_USER="admin"
JENKINS_TOKEN="YOUR_API_TOKEN_HERE"
GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Only trigger for Ujjawal-docker branch
if [ "$GIT_BRANCH" = "Ujjawal-docker" ]; then
    echo "[Jenkins CI] Triggering build for branch: $GIT_BRANCH"
    
    # Make API call to Jenkins
    curl -X POST \
        "${JENKINS_URL}/job/${JENKINS_JOB}/buildWithParameters" \
        --user "${JENKINS_USER}:${JENKINS_TOKEN}" \
        -d "token=chatbot-ci" \
        -d "BRANCH=${GIT_BRANCH}" \
        --silent \
        --output /dev/null
    
    if [ $? -eq 0 ]; then
        echo "[Jenkins CI] ✓ Build triggered successfully"
    else
        echo "[Jenkins CI] ✗ Failed to trigger build"
    fi
else
    echo "[Jenkins CI] Skipping build trigger for branch: $GIT_BRANCH"
fi
EOF
```

### Step 3: Make Hook Executable

```bash
# Make hook executable
chmod +x .git/hooks/post-commit

# Verify it's executable
ls -la .git/hooks/post-commit
# Should show: -rwxr-xr-x (with x permissions)
```

### Step 4: Update Token in Hook

Replace `YOUR_API_TOKEN_HERE` with your actual Jenkins API token:

```bash
# Option 1: Use sed (if you know the token)
sed -i 's/YOUR_API_TOKEN_HERE/your_actual_token_here/g' .git/hooks/post-commit

# Option 2: Manual edit with text editor
# Open .git/hooks/post-commit in VS Code
# Find: JENKINS_TOKEN="YOUR_API_TOKEN_HERE"
# Replace with: JENKINS_TOKEN="abc123def456ghi789..."
```

---

## Usage

### Automatic Trigger on Commit

```bash
# Make changes to code
echo "test" >> README.md

# Stage and commit
git add README.md
git commit -m "Test commit to trigger Jenkins build"

# Expected output in Git Bash:
# [Jenkins CI] Triggering build for branch: Ujjawal-docker
# [Jenkins CI] ✓ Build triggered successfully
```

### Monitor Build

After commit, check:

1. **Jenkins Dashboard**: `http://localhost:8888`
   - New build should appear in "Build History"
   - Click build number to see "Console Output"

2. **Command Line**: Check Jenkins logs
   ```bash
   docker compose logs -f jenkins | grep -i build
   ```

---

## Trigger for Multiple Branches

Modify the hook to trigger for any branch:

```bash
# .git/hooks/post-commit

#!/bin/bash
JENKINS_URL="http://localhost:8888"
JENKINS_JOB="chatbot-platform"
JENKINS_USER="admin"
JENKINS_TOKEN="YOUR_API_TOKEN_HERE"
GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Trigger for ANY branch
echo "[Jenkins CI] Triggering build for branch: $GIT_BRANCH"

curl -X POST \
    "${JENKINS_URL}/job/${JENKINS_JOB}/buildWithParameters" \
    --user "${JENKINS_USER}:${JENKINS_TOKEN}" \
    -d "token=chatbot-ci" \
    -d "BRANCH=${GIT_BRANCH}" \
    --silent \
    --output /dev/null

if [ $? -eq 0 ]; then
    echo "[Jenkins CI] ✓ Build triggered"
else
    echo "[Jenkins CI] ✗ Failed to trigger build"
fi
```

---

## Disable Hook Temporarily

```bash
# Make it non-executable
chmod -x .git/hooks/post-commit

# Make it executable again
chmod +x .git/hooks/post-commit
```

---

## Troubleshooting

### Hook not running after commit

**Check 1**: Verify hook is executable
```bash
ls -la .git/hooks/post-commit
# Should have 'x' in permissions: -rwxr-xr-x
```

**Check 2**: Verify Jenkins is running
```bash
docker compose ps jenkins
# Should show "Up" status
```

**Check 3**: Test curl manually
```bash
# Replace with your actual token and Jenkins user
curl -X POST \
    "http://localhost:8888/job/chatbot-platform/buildWithParameters" \
    --user "admin:YOUR_API_TOKEN_HERE" \
    -d "token=chatbot-ci" \
    -d "BRANCH=Ujjawal-docker"

# Should get HTTP 200 response
```

### "Connection refused" error

**Cause**: Jenkins container not running
```bash
# Start Jenkins
docker compose up -d jenkins

# Verify it's running
docker compose ps jenkins
```

### "401 Unauthorized" error

**Cause**: Wrong Jenkins token or username
```bash
# Get correct token from Jenkins UI:
# Jenkins Dashboard → Manage Jenkins → Manage Users → your user → Configure → API Token

# Verify in hook:
# JENKINS_USER should be your Jenkins username (e.g., "admin")
# JENKINS_TOKEN should be the API token from above
```

---

## Advanced: Pre-Commit Hook (Optional)

Run tests BEFORE commit (fail if tests fail):

```bash
# Create .git/hooks/pre-commit

#!/bin/bash
echo "[Pre-Commit] Running tests..."

# Run all tests
cd api-gateway\ \(1\)/api-gateway && ./gradlew test
if [ $? -ne 0 ]; then
    echo "[Pre-Commit] ✗ API Gateway tests failed. Commit blocked."
    exit 1
fi

cd ../../chatbot-bridge/chatbot-bridge && ./gradlew test
if [ $? -ne 0 ]; then
    echo "[Pre-Commit] ✗ Chatbot Bridge tests failed. Commit blocked."
    exit 1
fi

cd ../../organization-service/organization-service && ./gradlew test
if [ $? -ne 0 ]; then
    echo "[Pre-Commit] ✗ Organization Service tests failed. Commit blocked."
    exit 1
fi

cd ../../chatbot-engine && ./gradlew test
if [ $? -ne 0 ]; then
    echo "[Pre-Commit] ✗ Chatbot Engine tests failed. Commit blocked."
    exit 1
fi

echo "[Pre-Commit] ✓ All tests passed. Proceeding with commit."
exit 0
```

Make executable:
```bash
chmod +x .git/hooks/pre-commit
```

---

## When to Use

| Approach | Use Case |
|----------|----------|
| **GitHub Webhook** | Production, CI/CD pipeline, auto-trigger on push |
| **Git Commit Hook** | Local development, offline testing, testing pipeline changes |
| **Manual Trigger** | Quick testing, debugging specific build |

**For Week 2**: Use Git commit hook to test locally, then add webhook for production.

---

## Reference

- **Git Hooks Documentation**: https://git-scm.com/docs/githooks
- **Jenkins API**: https://wiki.jenkins.io/display/JENKINS/Remote+access+API
- **Jenkins Credentials**: https://www.jenkins.io/doc/book/using/using-credentials/


<!-- wsl
cd /mnt/c/Users/ujjawal.maheshwari/Documents/chatbot-platform
docker compose up -d
docker start jenkins-chatbot-ci
docker exec -u root -it jenkins-chatbot-ci bash -lc "chmod 666 /var/run/docker.sock"
docker restart jenkins-chatbot-ci -->
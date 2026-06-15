# Week 2 Implementation - Verification Checklist

Use this checklist to verify all implementation is correct before moving to setup phase.

---

## Files Verification

### ✅ Check Modified/Created Files Exist

```bash
# Navigate to project root
cd c:\Users\ujjawal.maheshwari\Documents\chatbot-platform

# Verify files exist
ls -la docker-compose.yml    # Should have Jenkins service added
ls -la Jenkinsfile           # Should exist at root
ls -la JENKINS_SETUP_GUIDE.md
ls -la GIT_HOOKS_SETUP.md
ls -la JENKINSFILE_CONCEPTS.md
ls -la Week_2_IMPLEMENTATION_SUMMARY.md
```

Expected output:
```
-rw-r--r--  docker-compose.yml
-rw-r--r--  Jenkinsfile
-rw-r--r--  JENKINS_SETUP_GUIDE.md
-rw-r--r--  GIT_HOOKS_SETUP.md
-rw-r--r--  JENKINSFILE_CONCEPTS.md
-rw-r--r--  Week_2_IMPLEMENTATION_SUMMARY.md
```

---

## Docker Compose Verification

### ✅ Jenkins Service Definition

```bash
# Check Jenkins service is in docker-compose.yml
grep -A 20 "jenkins:" docker-compose.yml
```

Expected to see:
```yaml
  jenkins:
    image: jenkins/jenkins:lts
    container_name: jenkins-chatbot-ci
    user: root
    ports:
      - "8888:8080"
      - "50000:50000"
    volumes:
      - jenkins-data:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
    restart: unless-stopped
```

### ✅ Jenkins Data Volume

```bash
# Check jenkins-data volume is defined
grep -A 3 "^volumes:" docker-compose.yml | grep jenkins-data
```

Expected to see:
```yaml
  jenkins-data:
```

---

## Jenkinsfile Verification

### ✅ Jenkinsfile Syntax

```bash
# Verify Jenkinsfile has pipeline structure
head -5 Jenkinsfile
```

Expected:
```groovy
pipeline {
    agent any
    
    options {
        timestamps()
```

### ✅ Jenkinsfile Stages

```bash
# Check all 4 services are in Jenkinsfile
grep -c "stage('.*Gateway')" Jenkinsfile     # api-gateway
grep -c "stage('.*Bridge')" Jenkinsfile      # chatbot-bridge
grep -c "stage('.*Organization')" Jenkinsfile  # organization-service
grep -c "stage('.*Engine')" Jenkinsfile      # chatbot-engine
```

Expected: Each should return `2` (one for Build, one for Docker)

### ✅ Docker Build Commands

```bash
# Verify Docker build commands for all services
grep -c "docker build" Jenkinsfile
```

Expected: At least `4` (one per service)

### ✅ Gradle Build Commands

```bash
# Verify Gradle build commands
grep -c "./gradlew" Jenkinsfile
```

Expected: At least `4` (one per service)

---

## Git Configuration

### ✅ Repository Status

```bash
# Verify repo is Git
git status

# Check current branch
git branch -a | grep "^\*"
```

Expected:
```
* Ujjawal-docker
```

### ✅ Remote Repository

```bash
# Verify GitHub remote
git remote -v
```

Expected to see:
```
origin	https://github.com/vivekbadan/chatbot-platform.git (fetch)
origin	https://github.com/vivekbadan/chatbot-platform.git (push)
```

---

## Docker & Docker Compose

### ✅ Docker Running

```bash
# Check Docker daemon is running
docker ps
```

Expected: No errors, shows running containers (or empty if none running)

### ✅ Docker Compose Configuration

```bash
# Validate docker-compose.yml syntax
docker compose config > /dev/null && echo "✓ Valid" || echo "✗ Invalid"
```

Expected: `✓ Valid`

### ✅ All Services Defined

```bash
# Check all services are in compose file
docker compose config | grep "services:" -A 50
```

Expected to see services:
- db
- rabbitmq
- api-gateway
- chatbot-bridge
- organization-service
- chatbot-engine
- jenkins

---

## Documentation Verification

### ✅ Setup Guide Exists

```bash
# Check JENKINS_SETUP_GUIDE.md has all phases
grep "^### [0-9]\." JENKINS_SETUP_GUIDE.md | wc -l
```

Expected: At least `8` phases

### ✅ Git Hooks Guide Exists

```bash
# Verify GIT_HOOKS_SETUP.md content
grep "#!/bin/bash" GIT_HOOKS_SETUP.md
```

Expected: Found (post-commit script example)

### ✅ Jenkinsfile Concepts Guide

```bash
# Verify concepts guide has explanations
grep "^##" JENKINSFILE_CONCEPTS.md | wc -l
```

Expected: At least `9` sections

---

## File Content Quality Checks

### ✅ Jenkinsfile Has Credentials ID

```bash
# Verify github-token credential is referenced
grep "credentialsId" Jenkinsfile | grep "github-token"
```

Expected: Found

### ✅ Docker Socket Mounting

```bash
# Verify Docker socket is mounted in compose
grep "docker.sock" docker-compose.yml
```

Expected:
```
- /var/run/docker.sock:/var/run/docker.sock
```

### ✅ Environment Variables in Jenkinsfile

```bash
# Verify environment block exists
grep "^    environment {" Jenkinsfile
```

Expected: Found

### ✅ Parallel Stages

```bash
# Verify parallel keyword used
grep "parallel {" Jenkinsfile
```

Expected: Found (multiple times)

---

## Pre-Deployment Checklist

- [ ] All 6 files exist and have content
- [ ] docker-compose.yml has valid YAML syntax
- [ ] Jenkins service added to docker-compose.yml
- [ ] Jenkinsfile is at repository root
- [ ] All 4 services have Gradle build paths
- [ ] All 4 services have Docker build steps
- [ ] Credentials ID matches: `github-token`
- [ ] Docker socket mounted in compose
- [ ] jenkins-data volume defined
- [ ] Documentation guides are comprehensive
- [ ] Git repository is on Ujjawal-docker branch
- [ ] GitHub remote URL is correct
- [ ] Docker daemon is running
- [ ] docker-compose config validates (no errors)

---

## Quick Test (Before Jenkins Startup)

### Test 1: Verify docker-compose.yml is Valid

```bash
cd c:\Users\ujjawal.maheshwari\Documents\chatbot-platform
docker compose config
```

Should display full YAML with all services without errors.

### Test 2: Check Existing Services Still Work

```bash
# This should NOT fail (we only added Jenkins, didn't break existing services)
docker compose ps
```

### Test 3: Verify Jenkinsfile Structure

```bash
# Quick check that pipeline is valid groovy-ish
head -20 Jenkinsfile
tail -20 Jenkinsfile
```

Should show `pipeline { ... }` wrapping

---

## Summary

If all checks pass ✅:
- **Files**: Ready for deployment
- **Configuration**: Syntax valid
- **Documentation**: Comprehensive
- **Repository**: Correct branch, correct remote
- **Docker**: Daemon running, compose valid

**Next**: Follow JENKINS_SETUP_GUIDE.md Phase 1-3 to start Jenkins

If any check fails ❌:
- Review the relevant section in implementation docs
- Check file contents for errors
- Verify paths are correct (especially with spaces in "api-gateway (1)")

---

## Quick Commands Reference

```bash
# Full validation
docker compose config > /dev/null && echo "✓ Compose valid" || echo "✗ Compose invalid"
grep -q "jenkins:" docker-compose.yml && echo "✓ Jenkins in compose" || echo "✗ Jenkins missing"
[ -f Jenkinsfile ] && echo "✓ Jenkinsfile exists" || echo "✗ Jenkinsfile missing"
[ -f JENKINS_SETUP_GUIDE.md ] && echo "✓ Setup guide exists" || echo "✗ Setup guide missing"

# Show all services in docker-compose
docker compose config | grep "^  [a-z]" | cut -d: -f1

# Verify Jenkinsfile stages
echo "Stages in Jenkinsfile:"
grep "stage(" Jenkinsfile | wc -l
```

---

**✅ Ready to proceed with Phase 1: Start Jenkins Container**

See: [JENKINS_SETUP_GUIDE.md](JENKINS_SETUP_GUIDE.md#phase-1-start-jenkins-container)

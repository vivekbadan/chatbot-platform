# Jenkinsfile Concepts & Deep Dive

This document explains the Jenkinsfile structure, syntax, and how to extend it.

---

## Table of Contents

1. [Declarative vs Scripted](#declarative-vs-scripted)
2. [Jenkinsfile Anatomy](#jenkinsfile-anatomy)
3. [Stages & Steps](#stages--steps)
4. [Parallel Execution](#parallel-execution)
5. [Environment Variables](#environment-variables)
6. [Credentials Management](#credentials-management)
7. [Post Actions](#post-actions)
8. [Agents & Docker](#agents--docker)
9. [Common Patterns](#common-patterns)

---

## Declarative vs Scripted

### Declarative Pipeline (Used Here)

**What**: Structured, opinionated syntax designed for CI/CD

**Pros**:
- Easy to read and learn
- Syntax validation
- Built-in error handling
- Better UI in Jenkins Blue Ocean
- Recommended for beginners

**Cons**:
- Less flexible
- Complex logic requires `script {}` block

**Structure**:
```groovy
pipeline {
    agent any
    options { ... }
    environment { ... }
    parameters { ... }
    stages { ... }
    post { ... }
}
```

### Scripted Pipeline

**What**: Full Groovy programming language

**Pros**:
- Ultimate flexibility
- Complex conditionals and loops
- Can do anything

**Cons**:
- Steeper learning curve
- Syntax errors don't show until runtime
- Harder to read

**Example**:
```groovy
node {
    if (env.BRANCH_NAME == 'master') {
        // Deploy to production
    } else {
        // Deploy to staging
    }
}
```

**Week 2**: Stick with Declarative (already used in Jenkinsfile)  
**Week 3+**: Learn Scripted for advanced pipelines

---

## Jenkinsfile Anatomy

### Top-Level Blocks

```groovy
pipeline {
    agent       // WHERE the pipeline runs
    options     // HOW the pipeline runs
    environment // WHAT variables are available
    parameters  // WHAT inputs to the pipeline
    stages      // WHAT work to do
    post        // WHAT to do after stages complete
}
```

---

## Stages & Steps

### What is a Stage?

A **stage** is a logical section of your pipeline that groups related work.

```groovy
stages {
    stage('Build') {
        steps {
            echo "Building code..."
            sh './gradlew build'
        }
    }
    
    stage('Test') {
        steps {
            echo "Running tests..."
            sh './gradlew test'
        }
    }
    
    stage('Docker Build') {
        steps {
            echo "Building Docker image..."
            sh 'docker build -t myapp:latest .'
        }
    }
}
```

### Stage Display

Jenkins UI shows each stage as a box in the pipeline:
```
[Checkout] → [Build & Test] → [Docker Build] → [Verify] → [Summary]
```

If a stage fails, the rest are blocked (unless configured otherwise).

### Steps Block

**Steps** are individual commands that run sequentially:

```groovy
steps {
    sh 'echo "Step 1"'    // Runs first
    sh 'echo "Step 2"'    // Runs second, only if Step 1 succeeds
    sh 'echo "Step 3"'    // Runs third
}
```

### Common Step Commands

| Command | Purpose |
|---------|---------|
| `sh 'command'` | Run shell command (Linux/Mac) or Git Bash (Windows) |
| `bat 'command'` | Run Windows batch command (PowerShell) |
| `echo 'text'` | Print to console |
| `dir()` | Change directory |
| `script { }` | Groovy code (if/else, loops, etc.) |
| `junit 'path'` | Publish JUnit test results |
| `archiveArtifacts` | Save build files |
| `git()` | Git operations |

---

## Parallel Execution

### Why Parallel?

**Serial (Sequential)**:
```
Build API Gateway: 1min
Build Chatbot Bridge: 1min
Build Organization Service: 1min
Build Chatbot Engine: 1min
Total: 4 minutes
```

**Parallel**:
```
┌─ Build API Gateway: 1min ─┐
├─ Build Chatbot Bridge: 1min ┤ Total: 1 minute (all run simultaneously)
├─ Build Organization Service: 1min ┤
└─ Build Chatbot Engine: 1min ─┘
```

### Parallel Syntax

```groovy
stages {
    stage('Build & Test - Parallel') {
        parallel {
            stage('Service 1') {
                steps {
                    sh './gradlew build'
                }
            }
            
            stage('Service 2') {
                steps {
                    sh './gradlew build'
                }
            }
            
            stage('Service 3') {
                steps {
                    sh './gradlew build'
                }
            }
        }
    }
}
```

### Our Pipeline Parallelism

**Build & Test - Parallel stage:**
- 4 services build simultaneously
- Each service runs: checkout code → `./gradlew clean build`
- If one service fails, others continue (by default)
- Total time: ~1 minute (instead of 4)

**Docker Build - Parallel stage:**
- 4 Docker images built simultaneously
- Each service runs: `docker build -f Dockerfile ...`
- Total time: ~1 minute

---

## Environment Variables

### Built-in Jenkins Variables

Jenkins automatically provides these:

```groovy
${BUILD_NUMBER}      // Build ID (1, 2, 3, ...)
${BUILD_ID}          // Same as BUILD_NUMBER
${JOB_NAME}          // Job name (e.g., "chatbot-platform")
${GIT_BRANCH}        // Branch name (e.g., "origin/Ujjawal-docker")
${GIT_COMMIT}        // Full commit hash (40 characters)
${GIT_COMMIT_MSG}    // Commit message
${WORKSPACE}         // Build workspace directory
${BUILD_TIMESTAMP}   // Build time
${JENKINS_HOME}      // Jenkins installation directory
${JENKINS_URL}       // Jenkins URL (e.g., http://localhost:8888)
```

### Custom Environment Variables

Defined in Jenkinsfile:

```groovy
environment {
    REGISTRY = 'localhost'
    IMAGE_PREFIX = 'chatbot'
    BUILD_TAG = "${BUILD_NUMBER}"
    GIT_COMMIT_SHORT = "${GIT_COMMIT.take(7)}"  // First 7 chars
}
```

### Using Variables in Steps

```groovy
steps {
    sh '''
        echo "Building for branch: ${GIT_BRANCH}"
        docker build -t ${REGISTRY}/${IMAGE_PREFIX}-api-gateway:${BUILD_TAG} .
    '''
}
```

### Variable Scope

| Scope | Usage | Expires |
|-------|-------|---------|
| `environment {}` block | All stages and steps | End of pipeline |
| Local variable in `script {}` | Current stage only | End of stage |
| Credentials from `withCredentials` | Inside `withCredentials` block only | End of block |

---

## Credentials Management

### Why Credentials?

Never hardcode secrets (tokens, passwords) in Jenkinsfile!

❌ **Bad**:
```groovy
sh 'git clone https://admin:secretPassword123@github.com/repo'
```

✅ **Good**:
```groovy
sh 'git clone https://github.com/repo'  // Uses credential from Jenkins
```

### Storing Credentials

**In Jenkins UI:**
1. Dashboard → Manage Jenkins → Manage Credentials
2. Add credential → Select type (Secret text, Username/password, SSH key, etc.)
3. Give it an **ID** (e.g., `github-token`)
4. Jenkins encrypts and stores it

### Using Credentials in Jenkinsfile

#### Method 1: Git Checkout (Automatic)

```groovy
checkout([
    $class: 'GitSCM',
    userRemoteConfigs: [[
        url: 'https://github.com/vivekbadan/chatbot-platform.git',
        credentialsId: 'github-token'  // References stored credential
    ]]
])
```

Jenkins automatically uses the credential for authentication.

#### Method 2: withCredentials (Manual)

```groovy
withCredentials([string(credentialsId: 'github-token', variable: 'GH_TOKEN')]) {
    sh '''
        curl -X POST https://api.github.com/repos/vivekbadan/chatbot-platform \
            -H "Authorization: token ${GH_TOKEN}" \
            -d '{"name": "test"}'
    '''
}
```

#### Method 3: Docker Login

```groovy
withCredentials([usernamePassword(credentialsId: 'docker-hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
    sh '''
        echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
        docker push myregistry.com/myimage:latest
    '''
}
```

### Credential Types

| Type | Use For | Example |
|------|---------|---------|
| Secret text | API tokens, GitHub PAT | GitHub token for webhook |
| Username with password | Docker Hub, registries | docker.io credentials |
| SSH key | Git over SSH | For Git operations |
| Vault | Sensitive data | AWS secrets, API keys |

---

## Post Actions

### Post Blocks

Run after all stages complete (success or failure).

```groovy
post {
    always {
        // Runs regardless of success/failure
        echo "Cleanup"
    }
    success {
        // Runs only if pipeline succeeded
        echo "Build successful!"
    }
    failure {
        // Runs only if any stage failed
        echo "Build failed!"
    }
    unstable {
        // Runs if tests failed but build didn't
        echo "Build unstable - check test results"
    }
    cleanup {
        // Runs last (even if other posts fail)
        cleanWs()
    }
}
```

### Common Post Actions

```groovy
post {
    always {
        // Archive test results
        junit testResults: '**/build/test-results/**/*.xml',
              allowEmptyResults: true
        
        // Archive build artifacts (JAR files)
        archiveArtifacts artifacts: '**/build/libs/*.jar',
                          allowEmptyArchive: true
        
        // Publish code coverage
        jacoco sourcePattern: '**/src',
               execPattern: '**/build/coverage.exec'
        
        // Clean workspace
        cleanWs()
    }
    
    failure {
        // Send email on failure
        emailext(
            subject: "Build ${BUILD_NUMBER} failed",
            body: "See ${BUILD_URL} for details",
            to: 'team@company.com'
        )
    }
}
```

---

## Agents & Docker

### What is an Agent?

The **agent** specifies WHERE Jenkins stages run.

```groovy
agent any                    // Use any available Jenkins agent
agent none                   // No default agent (specify per stage)
agent { label 'docker' }     // Use agent with 'docker' label
agent { docker 'maven:3.6' } // Run in Docker container
```

### Docker Agent Example

```groovy
pipeline {
    agent {
        docker {
            image 'openjdk:17-jdk'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
    stages {
        stage('Build') {
            steps {
                sh 'java -version'  // Runs inside Docker container
                sh './gradlew build'
            }
        }
    }
}
```

### Our Setup: Local Docker Socket

In `docker-compose.yml`:
```yaml
jenkins:
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
```

This allows Jenkins to run Docker commands on the host machine:
```groovy
sh 'docker build -t myimage .'  // Runs on host Docker
```

---

## Common Patterns

### Pattern 1: Multiple Services with Shared Steps

```groovy
def buildService(serviceName, servicePath) {
    dir(servicePath) {
        sh './gradlew clean build'
    }
}

stages {
    stage('Build All') {
        steps {
            script {
                buildService('api-gateway', './api-gateway')
                buildService('chatbot-bridge', './chatbot-bridge')
            }
        }
    }
}
```

### Pattern 2: Conditional Stages

```groovy
stages {
    stage('Build') {
        steps {
            sh './gradlew build'
        }
    }
    
    stage('Deploy to Production') {
        when {
            branch 'main'  // Only run on main branch
        }
        steps {
            sh 'kubectl apply -f k8s/'
        }
    }
}
```

### Pattern 3: Try-Catch (Groovy Script)

```groovy
stages {
    stage('Tests') {
        steps {
            script {
                try {
                    sh './gradlew test'
                } catch (Exception e) {
                    echo "Tests failed but continuing..."
                    currentBuild.result = 'UNSTABLE'
                }
            }
        }
    }
}
```

### Pattern 4: Environment Variables per Stage

```groovy
stages {
    stage('Deploy Staging') {
        environment {
            ENV = 'staging'
            URL = 'https://staging.example.com'
        }
        steps {
            sh 'echo "Deploying to ${URL}"'
        }
    }
    
    stage('Deploy Production') {
        environment {
            ENV = 'production'
            URL = 'https://api.example.com'
        }
        steps {
            sh 'echo "Deploying to ${URL}"'
        }
    }
}
```

### Pattern 5: Collecting Build Status

```groovy
post {
    always {
        script {
            def status = currentBuild.result ?: 'SUCCESS'
            def duration = currentBuild.durationString - ' and counting'
            
            echo """
            =====================================
            Build: ${BUILD_NUMBER}
            Status: ${status}
            Duration: ${duration}
            Commit: ${GIT_COMMIT_SHORT}
            =====================================
            """
        }
    }
}
```

---

## Extending Our Jenkinsfile

### Add a New Stage

```groovy
// In 'stages' block:

stage('Security Scan') {
    steps {
        sh 'docker run --rm -v $(pwd):/src -w /src aquasec/trivy image'
    }
}
```

### Add Docker Registry Push

```groovy
// In Docker Build stage, add:

stage('Docker: API Gateway') {
    steps {
        script {
            sh '''
                docker build -f ./api-gateway/Dockerfile \
                    -t ${REGISTRY}/${IMAGE_PREFIX}-api-gateway:${BUILD_TAG} .
                
                # Push to registry
                docker push ${REGISTRY}/${IMAGE_PREFIX}-api-gateway:${BUILD_TAG}
                docker push ${REGISTRY}/${IMAGE_PREFIX}-api-gateway:latest
            '''
        }
    }
}
```

### Add Slack Notification

```groovy
post {
    success {
        slackSend(
            channel: '#ci-builds',
            color: 'good',
            message: "Build ${BUILD_NUMBER} successful: ${env.BUILD_URL}"
        )
    }
    failure {
        slackSend(
            channel: '#ci-builds',
            color: 'danger',
            message: "Build ${BUILD_NUMBER} failed: ${env.BUILD_URL}"
        )
    }
}
```

### Add Build Parameters

```groovy
parameters {
    string(name: 'DOCKER_REGISTRY', defaultValue: 'localhost', description: 'Docker registry URL')
    choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'prod'], description: 'Deployment environment')
    booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run unit tests')
}

// Use in steps:
steps {
    sh "echo 'Using registry: ${params.DOCKER_REGISTRY}'"
}
```

---

## Debugging Jenkinsfile

### Check Syntax

```bash
# Validate without running
curl -X POST http://localhost:8888/pipeline-model-converter/validate \
  -F "jenkinsfile=<Jenkinsfile"
```

### Add Debug Output

```groovy
steps {
    script {
        echo "=== DEBUG ==="
        echo "BUILD_NUMBER: ${BUILD_NUMBER}"
        echo "GIT_BRANCH: ${GIT_BRANCH}"
        echo "GIT_COMMIT: ${GIT_COMMIT}"
        echo "WORKSPACE: ${WORKSPACE}"
        sh 'pwd'
        sh 'ls -la'
        sh 'docker images'
        echo "=== END DEBUG ==="
    }
}
```

### View Environment Variables

```groovy
steps {
    sh 'env | sort'  // Print all environment variables
}
```

### Increase Logging

```groovy
options {
    timestamps()
    ansiColor('xterm')  // Enable colored output
}
```

---

## Resources

- **Jenkins Declarative Syntax**: https://www.jenkins.io/doc/book/pipeline/syntax/
- **Jenkins Scripted Examples**: https://www.jenkins.io/doc/book/pipeline/pipeline-as-code/
- **Groovy Language**: https://groovy-lang.org/syntax.html
- **Jenkins Best Practices**: https://www.jenkins.io/doc/book/pipeline/pipeline-best-practices/

---

✅ **You now understand Jenkinsfile concepts and can modify it for your needs!**

# Week 4 – Helm Charts Implementation Guide

**Project:** chatbot-platform  
**Week:** 4 – Helm Charts  
**Branch:** Ujjawal-docker  

---

## Overview

This README documents the complete Week 4 Helm Charts implementation for the chatbot-platform project. In Week 3, raw Kubernetes manifests were created for deploying services to Minikube. In Week 4, those raw manifests were converted into a Helm chart so that Kubernetes deployments can be managed in a reusable, configurable, and environment-specific way.

The Helm chart was created for all four microservices:

- api-gateway
- chatbot-engine
- chatbot-bridge
- organization-service

RabbitMQ was also deployed using the Bitnami Helm chart to understand how third-party dependencies are installed using Helm.

---

## Week 4 Task

### Do (Local)

- Install Helm and understand chart structure:
  - `Chart.yaml`
  - `values.yaml`
  - `templates/`
- Convert Week 3 raw Kubernetes manifests into a Helm chart for all 4 services.
- Parameterise:
  - image tag
  - replicas
  - environment variables
  - resource limits
- Use:
  - `values-dev.yaml`
  - `values-prod.yaml`
- Deploy RabbitMQ via Bitnami Helm chart to Minikube.
- Practice:
  - `helm install`
  - `helm upgrade`
  - `helm rollback`
  - `helm uninstall`

### Deliverable

All 4 services should be deployable to Minikube via Helm with environment-specific values.

---

## Prerequisites

Before starting Week 4, the following setup was already completed:

- Docker Desktop installed and running.
- WSL configured.
- Minikube installed and running.
- kubectl installed and connected to Minikube.
- Docker images built for the chatbot-platform services.
- Week 3 Kubernetes manifests already available.

Verify Minikube:

```bash
minikube status
kubectl get nodes
```

Expected:

```text
host: Running
kubelet: Running
apiserver: Running
```

---

## 1. Install Helm

Helm was installed inside WSL using the official Helm installation script.

```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

Verify installation:

```bash
helm version
```

Expected output:

```text
version.BuildInfo{Version:"v3.x.x", ...}
```

In this setup, Helm version `v3.21.2` was installed successfully.

---

## 2. Create Helm Chart

From the project root:

```bash
cd /mnt/c/Users/ujjawal.maheshwari/Documents/chatbot-platform
helm create chatbot-platform-chart
```

This created the following chart structure:

```text
chatbot-platform-chart/
├── Chart.yaml
├── values.yaml
├── charts/
└── templates/
```

---

## 3. Understand Helm Chart Structure

### Chart.yaml

`Chart.yaml` contains chart metadata such as:

- chart name
- chart version
- app version
- description
- chart type

Example:

```yaml
apiVersion: v2
name: chatbot-platform-chart
description: A Helm chart for Kubernetes
type: application
version: 0.1.0
appVersion: "1.16.0"
```

---

### values.yaml

`values.yaml` stores default configurable values for the chart.

Instead of hardcoding values in Kubernetes YAML files, Helm reads values from `values.yaml`.

Example:

```yaml
replicaCount: 2

image:
  repository: chatbot-api-gateway
  tag: latest
  pullPolicy: IfNotPresent
```

---

### templates/

The `templates/` directory contains Kubernetes YAML templates.

Examples:

```text
deployment.yaml
service.yaml
configmap.yaml
secret.yaml
```

Helm replaces template variables like:

```yaml
replicas: {{ .Values.replicaCount }}
```

with actual values from `values.yaml`.

---

## 4. Clean Default Helm Chart

The default chart created by Helm contains many files that were not required for this task.

Go to templates folder:

```bash
cd chatbot-platform-chart/templates
```

Remove unnecessary files:

```bash
rm hpa.yaml
rm httproute.yaml
rm ingress.yaml
rm serviceaccount.yaml
rm NOTES.txt
rm -rf tests
```

Create missing required templates:

```bash
touch configmap.yaml
touch secret.yaml
```

Final templates structure:

```text
_helpers.tpl
deployment.yaml
service.yaml
configmap.yaml
secret.yaml
```

Later, additional templates were added for the remaining microservices.

---

## 5. Create values.yaml for API Gateway

The default `values.yaml` was replaced with API Gateway specific configuration.

```yaml
replicaCount: 2

image:
  repository: chatbot-api-gateway
  tag: latest
  pullPolicy: IfNotPresent

service:
  type: NodePort
  port: 8080
  nodePort: 30080

config:
  APP_NAME: api-gateway
  CHATBOT_ENGINE_URL: http://chatbot-engine:8083
  SERVER_PORT: "8080"

secret:
  DB_PASSWORD: OTQ2MjgyMTczMg==
  API_TOKEN: OTQ2MjgyMTczMg==

resources:
  limits:
    cpu: 500m
    memory: 512Mi
  requests:
    cpu: 250m
    memory: 256Mi
```

This parameterised:

- replica count
- image repository
- image tag
- image pull policy
- service type
- service port
- NodePort
- environment variables
- secrets
- CPU and memory requests/limits

---

## 6. API Gateway Helm Templates

### deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
        - name: api-gateway
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - containerPort: {{ .Values.service.port }}

          env:
            - name: APP_NAME
              value: "{{ .Values.config.APP_NAME }}"

            - name: CHATBOT_ENGINE_URL
              value: "{{ .Values.config.CHATBOT_ENGINE_URL }}"

            - name: SERVER_PORT
              value: "{{ .Values.config.SERVER_PORT }}"

            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: api-gateway-secret
                  key: DB_PASSWORD

            - name: API_TOKEN
              valueFrom:
                secretKeyRef:
                  name: api-gateway-secret
                  key: API_TOKEN

          resources:
{{ toYaml .Values.resources | indent 12 }}
```

---

### service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: api-gateway-service
spec:
  type: {{ .Values.service.type }}
  selector:
    app: api-gateway
  ports:
    - port: {{ .Values.service.port }}
      targetPort: {{ .Values.service.port }}
      nodePort: {{ .Values.service.nodePort }}
```

---

### configmap.yaml

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: api-gateway-config
data:
  APP_NAME: "{{ .Values.config.APP_NAME }}"
  CHATBOT_ENGINE_URL: "{{ .Values.config.CHATBOT_ENGINE_URL }}"
  SERVER_PORT: "{{ .Values.config.SERVER_PORT }}"
```

---

### secret.yaml

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: api-gateway-secret
type: Opaque
data:
  DB_PASSWORD: "{{ .Values.secret.DB_PASSWORD }}"
  API_TOKEN: "{{ .Values.secret.API_TOKEN }}"
```

---

## 7. Validate Helm Rendering

Before deploying anything, Helm templates were validated using:

```bash
helm template api-gateway-release .
```

This command does not deploy resources. It only renders final Kubernetes YAML using the templates and values.

The output showed:

- Secret
- ConfigMap
- Service
- Deployment

This confirmed that the chart was rendering correctly.

---

## 8. Create values-dev.yaml and values-prod.yaml

Two environment-specific values files were created.

### values-dev.yaml

```yaml
replicaCount: 1

image:
  repository: chatbot-api-gateway
  tag: dev
  pullPolicy: IfNotPresent

service:
  type: NodePort
  port: 8080
  nodePort: 30080
```

---

### values-prod.yaml

```yaml
replicaCount: 3

image:
  repository: chatbot-api-gateway
  tag: prod
  pullPolicy: IfNotPresent

service:
  type: NodePort
  port: 8080
  nodePort: 30080
```

---

## 9. Test Dev and Prod Values

Test development configuration:

```bash
helm template api-gateway-release . -f values-dev.yaml | grep replicas
```

Expected:

```text
replicas: 1
```

Test production configuration:

```bash
helm template api-gateway-release . -f values-prod.yaml | grep replicas
```

Expected:

```text
replicas: 3
```

This proves that the same chart can generate different infrastructure outputs for different environments.

---

## 10. Extend Helm Chart for All 4 Services

Additional templates were created for:

- chatbot-engine
- chatbot-bridge
- organization-service

Inside `templates/`:

```bash
touch chatbot-engine-deployment.yaml
touch chatbot-engine-service.yaml

touch chatbot-bridge-deployment.yaml
touch chatbot-bridge-service.yaml

touch organization-service-deployment.yaml
touch organization-service-service.yaml
```

Additional configuration was added to `values.yaml`:

```yaml
chatbotEngine:
  image: chatbot-engine
  port: 8083
  replicas: 2

chatbotBridge:
  image: chatbot-bridge
  port: 8081
  replicas: 2

organizationService:
  image: organization-service
  port: 8082
  replicas: 2
```

The services were configured as internal `ClusterIP` services because API Gateway remains the external entry point.

---

## 11. Verify All 4 Services in Helm Output

Run:

```bash
helm template test-release . | grep "name:"
```

Expected to see:

```text
api-gateway
chatbot-engine
chatbot-bridge
organization-service
```

This confirms the Helm chart supports all four microservices.

---

## 12. Deploy RabbitMQ via Bitnami Helm Chart

Add Bitnami repository:

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
```

Update repo:

```bash
helm repo update
```

Install RabbitMQ:

```bash
helm install rabbitmq bitnami/rabbitmq
```

Check pods and services:

```bash
kubectl get pods
kubectl get svc
```

Check Helm release:

```bash
helm list
helm status rabbitmq
```

---

## 13. Practice Helm Commands

### helm install

```bash
helm install rabbitmq bitnami/rabbitmq
```

Creates a Helm release.

---

### helm upgrade

```bash
helm upgrade rabbitmq bitnami/rabbitmq
```

Creates a new revision.

---

### helm history

```bash
helm history rabbitmq
```

Shows release revision history.

---

### helm rollback

```bash
helm rollback rabbitmq 1
```

Rolls back to a previous revision.

---

### helm uninstall

```bash
helm uninstall rabbitmq
```

Deletes the Helm release.

---

## 14. Demo Flow for POC / Invigilator

Use the following commands during demo.

### Show Helm installation

```bash
helm version
```

---

### Show chart structure

```bash
cd /mnt/c/Users/ujjawal.maheshwari/Documents/chatbot-platform/chatbot-platform-chart
ls
ls templates
```

---

### Show values files

```bash
cat values.yaml
cat values-dev.yaml
cat values-prod.yaml
```

---

### Show dev/prod rendering difference

```bash
helm template api-gateway-release . -f values-dev.yaml | grep replicas
helm template api-gateway-release . -f values-prod.yaml | grep replicas
```

---

### Show all services rendered

```bash
helm template test-release . | grep "name:"
```

---

### Show RabbitMQ Helm lifecycle

```bash
helm list
helm history rabbitmq
helm status rabbitmq
```

---

## 15. How to Run the Helm Chart

Make sure Minikube is running:

```bash
minikube start --driver=docker
```

Load images if needed:

```bash
minikube image load chatbot-api-gateway:latest
minikube image load chatbot-engine:latest
minikube image load chatbot-bridge:latest
minikube image load organization-service:latest
```

Install chart using default values:

```bash
helm install chatbot-platform .
```

Install using dev values:

```bash
helm install chatbot-platform-dev . -f values-dev.yaml
```

Install using prod values:

```bash
helm install chatbot-platform-prod . -f values-prod.yaml
```

Check resources:

```bash
kubectl get pods
kubectl get svc
kubectl get deployments
```

Access API Gateway:

```bash
minikube service api-gateway-service --url
```

---

## 16. Upgrade the Application

Example upgrade using dev values:

```bash
helm upgrade chatbot-platform-dev . -f values-dev.yaml
```

Example upgrade using prod values:

```bash
helm upgrade chatbot-platform-prod . -f values-prod.yaml
```

---

## 17. Rollback the Application

Check history:

```bash
helm history chatbot-platform-dev
```

Rollback:

```bash
helm rollback chatbot-platform-dev 1
```

---

## 18. Uninstall the Application

```bash
helm uninstall chatbot-platform-dev
helm uninstall chatbot-platform-prod
```

---

## 19. Stop Everything

Stop Minikube:

```bash
minikube stop
```

If you want to remove RabbitMQ:

```bash
helm uninstall rabbitmq
```

If you want to remove application releases:

```bash
helm uninstall chatbot-platform
helm uninstall chatbot-platform-dev
helm uninstall chatbot-platform-prod
```

---

## 20. Challenges Faced and Fixes

### YAML Parse Error

A YAML parse error occurred because accidental Markdown backticks were copied into one of the Helm templates.

Fix:

- Opened the file.
- Removed extra backtick characters.
- Re-ran `helm template`.

---

### RabbitMQ ImagePullBackOff

RabbitMQ installed successfully as a Helm release, but the pod showed `Init:ImagePullBackOff` due to image pull restrictions.

This did not block Helm learning objectives because:

- chart installation worked
- release was created
- upgrade worked
- rollback worked
- history tracked revisions

---

### Understanding Helm Values

Initially, it was confusing how Helm values replace Kubernetes YAML fields.

Fix:

- Used `helm template` repeatedly.
- Compared generated YAML with `values.yaml`.
- Verified values-dev and values-prod generated different replica counts.

---

## 21. Important Theory Learned

### Helm in Real CI/CD

In real CI/CD, Jenkins or GitHub Actions usually builds a Docker image, creates a dynamic image tag using a build number or Git commit ID, and then deploys it with:

```bash
helm upgrade --install app-name ./chart --set image.tag=<dynamic-tag>
```

This keeps Kubernetes deployments automated and traceable.

---

### Helm Chart Repositories

Helm charts can be stored in repositories just like Docker images. Public charts are discoverable on Artifact Hub. Organizations usually host private chart repositories using Nexus, JFrog Artifactory, ChartMuseum, or S3-backed storage.

---

### GitOps with ArgoCD and Flux

GitOps means Git becomes the source of truth for Kubernetes state. Tools like ArgoCD and Flux continuously compare Git state with cluster state and automatically sync changes. This is replacing direct `helm upgrade` usage in many production pipelines.

---

### Kubernetes Resource Management

Requests define the minimum CPU/memory a pod needs. Limits define the maximum resources a pod can use. If a pod exceeds memory limit, it can be OOMKilled. LimitRange and ResourceQuota help control resource usage at namespace level.

---

### Probes

Liveness probes restart unhealthy containers. Readiness probes prevent traffic from reaching pods that are not ready. Startup probes help slow-starting apps avoid premature restarts.

---

## Week 4 Summary

In Week 4, raw Kubernetes manifests from Week 3 were converted into a reusable Helm chart. Helm was installed and used to understand chart structure, templates, and values files. The chart was parameterised for image tag, replicas, environment variables, and resource limits. Separate dev and prod values files were created and tested. The chart was extended to support all four chatbot-platform microservices. RabbitMQ was installed using the Bitnami Helm chart, and Helm lifecycle commands such as install, upgrade, history, rollback, status, and uninstall were practiced.

---

## WEEK 4 – END

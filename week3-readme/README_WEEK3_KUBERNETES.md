# 🚀 Week 3 — Kubernetes Locally (Minikube)

**Project:** chatbot-platform  
**Author:** Ujjawal Maheshwari  
**Branch:** Ujjawal-docker  


## 📋 Table of Contents

1. [Overview](#overview)
2. [Week 3 Task](#week-3-task)
3. [Prerequisites](#prerequisites)
4. [Installation from Scratch](#installation-from-scratch)
5. [Starting the Cluster](#starting-the-cluster)
6. [Project Structure](#project-structure)
7. [Loading Docker Image into Minikube](#loading-docker-image-into-minikube)
8. [Kubernetes Manifests](#kubernetes-manifests)
9. [Deploy to Kubernetes](#deploy-to-kubernetes)
10. [Verification & Inspection](#verification--inspection)
11. [Rolling Update Simulation](#rolling-update-simulation)
12. [Scaling Demonstration](#scaling-demonstration)
13. [Stop Everything (End of Day)](#stop-everything-end-of-day)
14. [Start Again (Next Day)](#start-again-next-day)
15. [Troubleshooting](#troubleshooting)
16. [Summary](#summary)

---

## Overview

This document describes the complete Week 3 Kubernetes (K8s) implementation for the chatbot-platform project. The goal was to deploy the API Gateway service to a local Kubernetes cluster running on Minikube, using standard K8s resources: Deployment, Service, ConfigMap, and Secret.

---

## Week 3 Task

### Do (Local)
- Install Minikube + kubectl
- Write K8s manifests for the API Gateway:
  - Deployment, Service, ConfigMap, Secret
- Deploy, inspect pods, exec in, simulate rolling update
- Practice `kubectl get`, `describe`, `logs`, `exec`, `rollout`

### Deliverable
API Gateway deployed and running on local Kubernetes cluster with all 4 standard resources.

---

## Prerequisites

| Tool | Purpose | Status |
|------|---------|--------|
| Docker Desktop | Container runtime for Minikube | ✅ Already installed (Week 1) |
| WSL (Ubuntu) | Linux terminal on Windows | ✅ Already installed (Week 1) |
| Git | Version control | ✅ Already installed (Week 1) |
| VS Code | Code editor | ✅ Already installed (Week 1) |
| chatbot-api-gateway Docker image | App to deploy | ✅ Built in Week 2 by Jenkins |

### Verify Docker is running
```bash
docker ps
```
Should return a (possibly empty) container list.

---

## Installation from Scratch

All commands run in **WSL terminal**.

### Step 1 — Update package list
```bash
sudo apt update
```

### Step 2 — Install kubectl

```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
kubectl version --client
```

**Expected output:**
```
Client Version: v1.35.x
Kustomize Version: v5.x.x
```

### Step 3 — Install Minikube

```bash
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube
minikube version
```

**Expected output:**
```
minikube version: v1.xx.x
```

### Step 4 — Clean up installer files (optional)
```bash
rm kubectl minikube-linux-amd64
```

---

## Starting the Cluster

### Step 1 — Start Minikube
```bash
minikube start --driver=docker
```

**What this does:**
- Creates a local Kubernetes cluster inside a Docker container
- First run downloads ~500 MB of K8s images (2-5 minutes)

### Step 2 — Verify cluster is running
```bash
kubectl get nodes
minikube status
```

**Expected output:**
```
NAME       STATUS   ROLES           AGE   VERSION
minikube   Ready    control-plane   24s   v1.35.1

minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
```

---

## Project Structure

```
chatbot-platform/
└── k8s/
    └── api-gateway/
        ├── deployment.yaml
        ├── service.yaml
        ├── configmap.yaml
        └── secret.yaml
```

Create the folder:
```bash
cd /mnt/c/Users/<your-user>/Documents/chatbot-platform
mkdir -p k8s/api-gateway
cd k8s/api-gateway
```

---

## Loading Docker Image into Minikube

Minikube runs in its own Docker environment, so it doesn't see your host's Docker images directly. The `chatbot-api-gateway` image from Week 2 needs to be loaded into Minikube.

### Step 1 — Re-tag the image (clean name)
```bash
docker tag localhost/chatbot-api-gateway:latest chatbot-api-gateway:latest
```

### Step 2 — Load into Minikube
```bash
minikube image load chatbot-api-gateway:latest
```

### Step 3 — Verify
```bash
minikube image ls | grep api-gateway
```

**Expected output:**
```
docker.io/library/chatbot-api-gateway:latest
```

---

## Kubernetes Manifests

### 📄 deployment.yaml — How pods are created and managed

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
  labels:
    app: api-gateway
spec:
  replicas: 2
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
          image: chatbot-api-gateway:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: api-gateway-config
            - secretRef:
                name: api-gateway-secret
```

**Key points:**
- `replicas: 2` → runs 2 copies of the app
- `imagePullPolicy: IfNotPresent` → use local image (loaded above)
- `envFrom` → injects ConfigMap and Secret values as env vars

---

### 📄 service.yaml — Network access to the deployment

```yaml
apiVersion: v1
kind: Service
metadata:
  name: api-gateway-service
spec:
  type: NodePort
  selector:
    app: api-gateway
  ports:
    - port: 8080
      targetPort: 8080
      nodePort: 30080
```

**Key points:**
- `NodePort` exposes the app outside the cluster
- Accessible via `http://<minikube-ip>:30080`

---

### 📄 configmap.yaml — Non-sensitive configuration

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: api-gateway-config
data:
  APP_NAME: "api-gateway"
  CHATBOT_ENGINE_URL: "http://chatbot-engine:8083"
  SERVER_PORT: "8080"
```

**Key points:**
- Stores plain-text config values
- Injected into pods as environment variables

---

### 📄 secret.yaml — Sensitive data (base64 encoded)

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: api-gateway-secret
type: Opaque
data:
  DB_PASSWORD: OTQ2MjgyMTczMg==
  API_TOKEN: OTQ2MjgyMTczMg==
```

**Key points:**
- All Secret values **must be base64 encoded**
- Generate encoded values using:
  ```bash
  echo -n "your-password" | base64
  ```
- Kubernetes automatically decodes them when injecting into pods

---

## Deploy to Kubernetes

### Deploy all 4 manifests at once
```bash
kubectl apply -f .
```

**Expected output:**
```
configmap/api-gateway-config created
deployment.apps/api-gateway created
secret/api-gateway-secret created
service/api-gateway-service created
```

---

## Verification & Inspection

### Check pods are running
```bash
kubectl get pods
```

**Expected output:**
```
NAME                           READY   STATUS    RESTARTS   AGE
api-gateway-76b6dcffd7-p74h5   1/1     Running   0          30s
api-gateway-76b6dcffd7-z9xfb   1/1     Running   0          30s
```

### View all resources
```bash
kubectl get all
```

### Detailed deployment info
```bash
kubectl describe deployment api-gateway
```

### View application logs
```bash
kubectl logs <pod-name> --tail=20
```

### Enter into a pod (like docker exec)
```bash
kubectl exec -it <pod-name> -- /bin/sh
```

Inside the pod, try:
```sh
ls
env
exit
```

You'll see your ConfigMap and Secret values as environment variables.

### Access the API Gateway in browser

Open the service URL:
```bash
minikube service api-gateway-service --url
```

This returns a URL like `http://127.0.0.1:30080/actuator/health` — open it in your browser.



---

## Rolling Update Simulation

Simulate updating the app to a new version:

```bash
kubectl set image deployment/api-gateway api-gateway=chatbot-api-gateway:latest
kubectl rollout status deployment/api-gateway
kubectl rollout history deployment/api-gateway
```

**Expected:**
```
deployment "api-gateway" successfully rolled out
```

### Rollback to previous version (if multiple revisions exist)
```bash
kubectl rollout undo deployment/api-gateway
```

---

## Scaling Demonstration

### Scale up to 3 pods
```bash
kubectl scale deployment api-gateway --replicas=3
kubectl get pods
```

**Expected output:**
```
NAME                           READY   STATUS    RESTARTS   AGE
api-gateway-76b6dcffd7-jcsmr   1/1     Running   0          30s
api-gateway-76b6dcffd7-p74h5   1/1     Running   0          9m
api-gateway-76b6dcffd7-z9xfb   1/1     Running   0          9m
```

### Scale back down to 2
```bash
kubectl scale deployment api-gateway --replicas=2
```

---

## Stop Everything (End of Day)

When you finish working, clean up to free resources:

```bash
# Option 1: Just stop Minikube (keeps cluster state)
minikube stop
```

```bash
# Option 2: Delete all deployed resources AND stop Minikube
kubectl delete -f .
minikube stop
```

```bash
# Option 3: Delete the entire Minikube cluster (full cleanup)
minikube delete
```

> 💡 For daily work, **Option 1** (`minikube stop`) is enough — your cluster state and manifests are preserved.

---

## Start Again (Next Day)

### Step 1 — Ensure Docker Desktop is running
Open Docker Desktop on Windows.

### Step 2 — Start Minikube
```bash
minikube start --driver=docker
```

### Step 3 — Verify cluster
```bash
kubectl get nodes
kubectl get pods
```

### Step 4 — If you deleted resources earlier, re-deploy
```bash
cd /mnt/c/Users/<your-user>/Documents/chatbot-platform/k8s/api-gateway
kubectl apply -f .
kubectl get pods
```

That's it — you're back where you left off ✅

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `minikube: command not found` | Re-run Minikube install commands |
| `kubectl: command not found` | Re-run kubectl install commands |
| `Cannot connect to Docker daemon` | Start Docker Desktop on Windows |
| `ImagePullBackOff` in pod status | Re-load image: `minikube image load chatbot-api-gateway:latest` |
| `localhost/<image>` image fails to load | Re-tag without prefix: `docker tag localhost/chatbot-api-gateway:latest chatbot-api-gateway:latest` |
| Pods stuck in `Pending` | Check `kubectl describe pod <name>` for events |
| Need to restart cluster cleanly | `minikube stop && minikube start --driver=docker` |
| Port not accessible in browser | Run `minikube service api-gateway-service --url` to get correct port |

---

## Quick Command Reference

| Command | Purpose |
|---------|---------|
| `minikube start --driver=docker` | Start cluster |
| `minikube stop` | Stop cluster |
| `minikube status` | Check cluster status |
| `kubectl get nodes` | List cluster nodes |
| `kubectl get pods` | List pods |
| `kubectl get all` | List all resources |
| `kubectl describe pod <name>` | Detailed pod info |
| `kubectl logs <name>` | View pod logs |
| `kubectl exec -it <name> -- /bin/sh` | Enter pod shell |
| `kubectl apply -f .` | Deploy all manifests in current folder |
| `kubectl delete -f .` | Delete all deployed resources |
| `kubectl scale deployment <name> --replicas=N` | Scale deployment |
| `kubectl rollout status deployment/<name>` | Check rollout progress |
| `kubectl rollout undo deployment/<name>` | Rollback deployment |
| `kubectl set image deployment/<name> <container>=<image>` | Update image |
| `minikube image load <image>` | Load local Docker image into Minikube |
| `minikube service <name> --url` | Get service URL |

---

## Summary

In Week 3, I set up a local Kubernetes cluster using Minikube and deployed the API Gateway service from the chatbot-platform project. I wrote four standard Kubernetes manifests (Deployment, Service, ConfigMap, Secret), loaded the Docker image built in Week 2 into Minikube, and deployed the application with `kubectl apply`. I practiced essential kubectl operations including `get`, `describe`, `logs`, `exec`, `scale`, and `rollout`, verified that ConfigMap and Secret values were correctly injected as environment variables in the pods, and simulated rolling updates and scaling. The complete deployment is reproducible and can be cleanly stopped or restarted using simple Minikube commands.

---

## WEEK 3 – END



Open Docker Desktop on Windows


wsl
cd /mnt/c/Users/ujjawal.maheshwari/Documents/chatbot-platform

minikube start --driver=docker
kubectl get nodes

docker tag localhost/chatbot-api-gateway:latest chatbot-api-gateway:latest
minikube image load chatbot-api-gateway:latest

cd k8s/api-gateway
kubectl apply -f .
kubectl get pods

minikube service api-gateway-service --url


--------------
 DEMO

docker ps
minikube status
kubectl get nodes

//get
kubectl get pods
kubectl get deployments
kubectl get services
kubectl get configmap
kubectl get secret
kubectl get all
////api-gateway-76b6dcffd7-582tg


//describe
kubectl describe pod api-gateway-76b6dcffd7-582tg
kubectl describe deployment api-gateway
kubectl describe service api-gateway-service
kubectl describe configmap api-gateway-config
kubectl describe secret api-gateway-secret

//exec
kubectl exec -it api-gateway-76b6dcffd7-582tg -- /bin/sh
ls
pwd
env
env | grep DB_PASSWORD
env | grep CHATBOT
exit

or 
kubectl exec <pod-name> -- env
kubectl exec <pod-name> -- ls /app

//logs
kubectl logs <pod-name> api-gateway-76b6dcffd7-hnb2f
kubectl logs <pod-name> --tail=20

//rollout
kubectl set image deployment/api-gateway api-gateway=chatbot-api-gateway:latest
kubectl rollout status deployment/api-gateway
kubectl rollout history deployment/api-gateway

//scale
kubectl scale deployment api-gateway --replicas=3
kubectl get pods

kubectl scale deployment api-gateway --replicas=2


//learn -- daemon set stateful set deployment 

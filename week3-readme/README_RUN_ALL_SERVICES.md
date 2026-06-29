# Week 3 – Run All Kubernetes Services (Command Guide)

Project: chatbot-platform  
Week: 3 – Kubernetes Locally (Minikube)

---

## Overview
This README contains all the commands required to run all microservices together on a local Kubernetes cluster using Minikube. It can also be used directly during invigilator/demo evaluation.

Services covered:
- api-gateway (entry point)
- chatbot-engine
- chatbot-bridge
- organization-service

---

## Prerequisites
- Docker Desktop running on Windows
- WSL (Ubuntu)
- Minikube and kubectl installed
- Docker images already built (Week 2)

---

## Start Kubernetes Cluster

```bash
wsl
cd /mnt/c/Users/ujjawal.maheshwari/Documents/chatbot-platform

minikube start --driver=docker
kubectl get nodes
minikube status
```

---

## Load Docker Images into Minikube (One-Time)

```bash
docker tag localhost/chatbot-api-gateway:latest chatbot-api-gateway:latest
docker tag localhost/chatbot-engine:latest chatbot-engine:latest
docker tag localhost/chatbot-bridge:latest chatbot-bridge:latest
docker tag localhost/organization-service:latest organization-service:latest

minikube image load chatbot-api-gateway:latest
minikube image load chatbot-engine:latest
minikube image load chatbot-bridge:latest
minikube image load organization-service:latest
```

---

## Deploy All Services

```bash
kubectl apply -f k8s/api-gateway/
kubectl apply -f k8s/chatbot-engine/
kubectl apply -f k8s/chatbot-bridge/
kubectl apply -f k8s/organization-service/
```

---

## Verify Deployment

```bash
kubectl get pods
kubectl get deployments
kubectl get services
kubectl get all
```

---

## Access Application (API Gateway)

```bash
minikube service api-gateway-service --url
```

Open the URL in browser.  
For health check:
```
/actuator/health
```

---

## kubectl Practice Commands

```bash
kubectl describe pod <pod-name>
kubectl logs <pod-name>
kubectl logs <pod-name>
kubectl exec -it <pod-name>  -- /bin/sh
```

---

## Rolling Update

```bash
kubectl set image deployment/api-gateway api-gateway=chatbot-api-gateway:latest
kubectl rollout status deployment/api-gateway
kubectl rollout history deployment/api-gateway
```

Rollback (if history exists):
```bash
kubectl rollout undo deployment/api-gateway
```

---

## Scaling

```bash
kubectl scale deployment api-gateway --replicas=3
kubectl get pods

kubectl scale deployment api-gateway --replicas=2
```

---

## Stop Everything (End of Day)

```bash
kubectl delete -f k8s/api-gateway/
kubectl delete -f k8s/chatbot-engine/
kubectl delete -f k8s/chatbot-bridge/
kubectl delete -f k8s/organization-service/

minikube stop
```

---

## Start Again (Next Day)

```bash
minikube start --driver=docker
kubectl apply -f k8s/api-gateway/
kubectl apply -f k8s/chatbot-engine/
kubectl apply -f k8s/chatbot-bridge/
kubectl apply -f k8s/organization-service/
kubectl get pods
```

---

## One-Line Explanation for Invigilator

All microservices are deployed on Kubernetes using Deployment, Service, ConfigMap, and Secret. API Gateway acts as the entry point, and internal services communicate using Kubernetes DNS. Rolling updates, scaling, logs, exec, and inspection are demonstrated.

---

## Week 3 – END



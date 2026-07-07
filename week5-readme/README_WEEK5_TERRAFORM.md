# Week 5 – Terraform Local Stack Provisioning

**Project:** chatbot-platform  
**Week:** 5 – Terraform & Ansible  
**Focus of this README:** Terraform local provisioning using Docker provider  

---

## Overview

This README explains the Terraform implementation completed for the chatbot-platform local stack. Terraform was used with the Docker provider to provision the full local Docker-based environment using Infrastructure as Code.

Using Terraform, the following local resources are created automatically:

- Docker network: `chatbot-network`
- PostgreSQL container
- RabbitMQ container
- API Gateway container
- Chatbot Engine container
- Chatbot Bridge container
- Organization Service container

Instead of manually running multiple `docker run` commands, the stack can be created using:

```bash
terraform apply
```

and removed using:

```bash
terraform destroy
```

---

## Prerequisites

Before running Terraform, ensure the following are available:

- Docker Desktop is running on Windows
- WSL terminal is available
- Docker images for the chatbot services are already built locally
- Terraform is installed in WSL

Verify Docker:

```bash
docker ps
```

Verify Terraform:

```bash
terraform version
```

Expected output:

```text
Terraform v1.15.7
```

---

## Terraform Installation

Terraform was installed in WSL using Snap.

```bash
sudo snap install terraform --classic
```

Verify installation:

```bash
terraform version
```

Output received:

```text
Terraform v1.15.7
on linux_amd64
```

---

## Project Structure

Terraform files were created inside the project repository under the `terraform/` folder.

```text
chatbot-platform/
└── terraform/
    ├── main.tf
    ├── variables.tf
    ├── outputs.tf
    ├── terraform.tfvars
    ├── terraform.tfstate
    └── terraform.tfstate.backup
```

Go to Terraform folder:

```bash
cd /mnt/c/Users/ujjawal.maheshwari/Documents/chatbot-platform/terraform
```

---

## Terraform Files Used

### main.tf

`main.tf` contains the actual infrastructure resources. It defines:

- Docker provider
- Docker network
- PostgreSQL image and container
- RabbitMQ image and container
- Application containers

Example provider block:

```hcl
terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0.2"
    }
  }
}

provider "docker" {}
```

---

### variables.tf

`variables.tf` contains reusable input variables.

```hcl
variable "network_name" {
  description = "Docker network name"
  type        = string
  default     = "chatbot-network"
}

variable "postgres_user" {
  description = "PostgreSQL username"
  type        = string
  default     = "admin"
}

variable "postgres_password" {
  description = "PostgreSQL password"
  type        = string
  default     = "admin123"
}

variable "postgres_db" {
  description = "PostgreSQL database name"
  type        = string
  default     = "chatbot"
}
```

---

### terraform.tfvars

`terraform.tfvars` provides actual values for variables.

```hcl
network_name      = "chatbot-network"
postgres_user     = "admin"
postgres_password = "admin123"
postgres_db       = "chatbot"
```

---

### outputs.tf

`outputs.tf` prints useful information after `terraform apply`.

```hcl
output "docker_network_name" {
  value = docker_network.chatbot_network.name
}

output "postgres_container_name" {
  value = docker_container.postgres.name
}

output "rabbitmq_container_name" {
  value = docker_container.rabbitmq.name
}

output "postgres_url" {
  value = "postgresql://admin:admin123@localhost:5432/chatbot"
}

output "rabbitmq_management_url" {
  value = "http://localhost:15672"
}
```

---

## Docker Network Provisioning

Terraform creates a Docker bridge network named `chatbot-network`.

```hcl
resource "docker_network" "chatbot_network" {
  name = var.network_name
}
```

This replaces the manual Docker command:

```bash
docker network create chatbot-network
```

All containers are attached to this same network so that they can communicate internally by container name.

---

## PostgreSQL Provisioning

Terraform provisions PostgreSQL using the official Docker image.

```hcl
resource "docker_image" "postgres" {
  name = "postgres:15"
}

resource "docker_container" "postgres" {
  name  = "postgres"
  image = docker_image.postgres.image_id

  env = [
    "POSTGRES_USER=${var.postgres_user}",
    "POSTGRES_PASSWORD=${var.postgres_password}",
    "POSTGRES_DB=${var.postgres_db}"
  ]

  networks_advanced {
    name = docker_network.chatbot_network.name
  }

  ports {
    internal = 5432
    external = 5432
  }
}
```

PostgreSQL becomes available at:

```text
localhost:5432
```

Connection URL:

```text
postgresql://admin:admin123@localhost:5432/chatbot
```

---

## RabbitMQ Provisioning

Terraform provisions RabbitMQ using the management image.

```hcl
resource "docker_image" "rabbitmq" {
  name = "rabbitmq:management"
}

resource "docker_container" "rabbitmq" {
  name  = "rabbitmq"
  image = docker_image.rabbitmq.image_id

  networks_advanced {
    name = docker_network.chatbot_network.name
  }

  ports {
    internal = 5672
    external = 5672
  }

  ports {
    internal = 15672
    external = 15672
  }
}
```

RabbitMQ AMQP port:

```text
localhost:5672
```

RabbitMQ Management UI:

```text
http://localhost:15672
```

Login:

```text
Username: guest
Password: guest
```

---

## Application Containers Provisioning

Terraform also provisions all chatbot-platform application containers.

### API Gateway

```hcl
resource "docker_container" "api_gateway" {
  name  = "api-gateway"
  image = "chatbot-api-gateway:latest"

  networks_advanced {
    name = docker_network.chatbot_network.name
  }

  ports {
    internal = 8080
    external = 8080
  }
}
```

API Gateway is accessible at:

```text
http://localhost:8080
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

---

### Chatbot Engine

```hcl
resource "docker_container" "chatbot_engine" {
  name  = "chatbot-engine"
  image = "chatbot-engine:latest"

  networks_advanced {
    name = docker_network.chatbot_network.name
  }
}
```

---

### Chatbot Bridge

```hcl
resource "docker_container" "chatbot_bridge" {
  name  = "chatbot-bridge"
  image = "chatbot-bridge:latest"

  networks_advanced {
    name = docker_network.chatbot_network.name
  }
}
```

---

### Organization Service

```hcl
resource "docker_container" "organization_service" {
  name  = "organization-service"
  image = "chatbot-platform-organization-service:latest"

  networks_advanced {
    name = docker_network.chatbot_network.name
  }
}
```

---

## Terraform Commands Used

### 1. terraform init

Initializes Terraform and downloads the Docker provider.

```bash
terraform init
```

Expected result:

```text
Terraform has been successfully initialized!
```

---

### 2. terraform plan

Shows what Terraform will create, update, or destroy before applying changes.

```bash
terraform plan
```

Example plan output:

```text
Plan: 5 to add, 0 to change, 0 to destroy.
```

---

### 3. terraform apply

Creates the full local stack.

```bash
terraform apply
```

When prompted:

```text
Enter a value:
```

Type:

```text
yes
```

After completion, Terraform provisions the network and all containers.

---

### 4. terraform output

Displays configured outputs.

```bash
terraform output
```

Example output:

```text
docker_network_name = "chatbot-network"
postgres_container_name = "postgres"
postgres_url = "postgresql://admin:admin123@localhost:5432/chatbot"
rabbitmq_container_name = "rabbitmq"
rabbitmq_management_url = "http://localhost:15672"
```

---

### 5. terraform state list

Shows resources currently managed by Terraform.

```bash
terraform state list
```

Expected resources:

```text
docker_network.chatbot_network
docker_image.postgres
docker_container.postgres
docker_image.rabbitmq
docker_container.rabbitmq
docker_container.api_gateway
docker_container.chatbot_engine
docker_container.chatbot_bridge
docker_container.organization_service
```

---

### 6. terraform plan -destroy

Shows what Terraform will delete if destroy is executed.

```bash
terraform plan -destroy
```

This is useful for previewing cleanup before actually deleting resources.

---

### 7. terraform destroy

Destroys all infrastructure created by Terraform.

```bash
terraform destroy
```

When prompted, type:

```text
yes
```

This removes:

- PostgreSQL container
- RabbitMQ container
- API Gateway container
- Chatbot Engine container
- Chatbot Bridge container
- Organization Service container
- Docker images managed by Terraform
- Docker network

---

## Verify Full Stack is Running

After `terraform apply`, run:

```bash
docker ps
```

Expected containers:

```text
postgres
rabbitmq
api-gateway
chatbot-engine
chatbot-bridge
organization-service
```

Example:

```text
postgres              0.0.0.0:5432->5432/tcp
rabbitmq              0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
api-gateway           0.0.0.0:8080->8080/tcp
chatbot-engine        8083/tcp
chatbot-bridge        8081/tcp
organization-service  8082/tcp
```

---

## Verify Docker Network

```bash
docker network inspect chatbot-network
```

Expected containers inside network:

```text
postgres
rabbitmq
api-gateway
chatbot-engine
chatbot-bridge
organization-service
```

This confirms all services are attached to the Terraform-created Docker network.

---

## Access URLs

### API Gateway

```text
http://localhost:8080
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

---

### RabbitMQ Management UI

```text
http://localhost:15672
```

Credentials:

```text
Username: guest
Password: guest
```

---

### PostgreSQL

```text
localhost:5432
```

Connection URL:

```text
postgresql://admin:admin123@localhost:5432/chatbot
```

---

## Resource Dependencies

Terraform automatically understands dependencies when one resource refers to another.

Example:

```hcl
networks_advanced {
  name = docker_network.chatbot_network.name
}
```

This means the containers depend on the Docker network. Terraform creates the network first and then creates containers.

---

## Terraform State File

After running `terraform apply`, Terraform creates:

```text
terraform.tfstate
terraform.tfstate.backup
```

The state file stores:

- resource IDs
- container IDs
- network IDs
- current infrastructure state
- dependencies

Terraform uses this file to compare the current infrastructure with the desired configuration in `.tf` files.

---

## Variables

Variables make the Terraform configuration reusable.

Instead of hardcoding values like:

```hcl
POSTGRES_USER=admin
```

Terraform uses:

```hcl
POSTGRES_USER=${var.postgres_user}
```

Values are defined in:

```text
variables.tf
terraform.tfvars
```

---

## Outputs

Outputs display useful information after Terraform creates infrastructure.

Example:

```bash
terraform output
```

Shows:

```text
postgres_url
rabbitmq_management_url
container names
network name
```

These outputs are useful for demos and quick verification.

---

## Demo Flow for POC / Invigilator

Use this flow to demonstrate the Terraform part.

### 1. Go to Terraform folder

```bash
cd /mnt/c/Users/ujjawal.maheshwari/Documents/chatbot-platform/terraform
```

---

### 2. Show files

```bash
ls
```

Show:

```text
main.tf
variables.tf
outputs.tf
terraform.tfvars
terraform.tfstate
```

---

### 3. Show Terraform version

```bash
terraform version
```

---

### 4. Show Terraform configuration

```bash
cat main.tf
cat variables.tf
cat outputs.tf
cat terraform.tfvars
```

---

### 5. Initialize Terraform

```bash
terraform init
```

---

### 6. Show plan

```bash
terraform plan
```

Explain that Terraform shows what it will create or change before applying.

---

### 7. Apply infrastructure

```bash
terraform apply
```

Type:

```text
yes
```

---

### 8. Show running containers

```bash
docker ps
```

Show all containers:

```text
postgres
rabbitmq
api-gateway
chatbot-engine
chatbot-bridge
organization-service
```

---

### 9. Show outputs

```bash
terraform output
```

---

### 10. Show Terraform state

```bash
terraform state list
```

Explain that Terraform state tracks all managed resources.

---

### 11. Show destroy plan

```bash
terraform plan -destroy
```

Explain that Terraform can remove the entire stack too.

---

## Start Again Tomorrow

Open WSL and go to Terraform folder:

```bash
cd /mnt/c/Users/ujjawal.maheshwari/Documents/chatbot-platform/terraform
```

Check current resources:

```bash
terraform output
docker ps
```

If containers are not running or were destroyed, recreate stack:

```bash
terraform init
terraform apply
```

Type:

```text
yes
```

---

## Stop Everything

To completely remove stack:

```bash
terraform destroy
```

Type:

```text
yes
```

Verify:

```bash
docker ps
```

The Terraform-created containers should be removed.

---

## Summary

Terraform was used to provision the full local chatbot-platform stack using the Docker provider. The implementation created a Docker network, PostgreSQL container, RabbitMQ container, and all four application containers. Terraform commands such as `init`, `plan`, `apply`, `output`, `state list`, `plan -destroy`, and `destroy` were practiced. Variables, outputs, state file, and resource dependencies were also implemented and verified.

This setup allows the full local stack to be created using:

```bash
terraform apply
```

and removed using:

```bash
terraform destroy
```

---

## WEEK 5 TERRAFORM – END

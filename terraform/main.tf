terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0.2"
    }
  }
}

provider "docker" {}

resource "docker_network" "chatbot_network" {
  name = var.network_name
}
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
resource "docker_container" "chatbot_engine" {
  name  = "chatbot-engine"

  image = "chatbot-engine:latest"

  networks_advanced {
    name = docker_network.chatbot_network.name
  }
}
resource "docker_container" "chatbot_bridge" {
  name  = "chatbot-bridge"

  image = "chatbot-bridge:latest"

  networks_advanced {
    name = docker_network.chatbot_network.name
  }
}
resource "docker_container" "organization_service" {
  name  = "organization-service"

  image = "chatbot-platform-organization-service:latest"

  networks_advanced {
    name = docker_network.chatbot_network.name
  }
}

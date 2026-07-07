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

# AI Chatbot Platform

A microservices-based AI Chatbot Platform developed during my internship at **Accenture**. The project integrates **Spring Boot**, **FastAPI**, and **Large Language Models (LLMs)** to provide secure, context-aware conversational AI with conversation persistence, JWT authentication, API documentation, and automated testing.

---

## Project Overview

The platform enables authenticated users to interact with an AI chatbot through a secure API Gateway. User context is retrieved from the Organization Service, combined with the user's query to generate context-aware prompts, and forwarded to the FastAPI chatbot engine for AI response generation. All conversations are stored in PostgreSQL for future reference.

---

## System Architecture

```text
                 +-------------------+
                 |       Client      |
                 +---------+---------+
                           |
                           |
                    JWT Authentication
                           |
                           v
               +-----------------------+
               |      API Gateway      |
               +-----------+-----------+
                           |
             Header Propagation (X-User-Id)
                           |
                           v
               +-----------------------+
               |    Chatbot Bridge     |
               +-----------+-----------+
                           |
          +----------------+----------------+
          |                                 |
          |                                 |
          v                                 v
+----------------------+        +-----------------------+
| Organization Service |        |   FastAPI LLM Engine  |
+----------------------+        +-----------+-----------+
                                            |
                                            |
                                            v
                                      +------------+
                                      | Groq LLM   |
                                      +------------+
                                            |
                                            |
                                            v
                                      AI Response
                                            |
                                            |
                                            v
                                      PostgreSQL
```

---

# Features

- JWT-based Authentication
- Spring Cloud Gateway
- Secure Header Propagation
- Organization Context Integration
- Context-aware Prompt Engineering
- FastAPI Chatbot Engine
- Groq LLM Integration
- Conversation & Message Persistence
- RESTful CRUD APIs
- Swagger / OpenAPI Documentation
- Streaming AI Responses
- Token Usage Tracking
- Unit Testing with Pytest
- PostgreSQL Integration

---

# Technology Stack

### Backend

- Java 17
- Spring Boot
- Spring Security
- Spring Cloud Gateway
- Spring Data JPA

### AI & Python

- FastAPI
- Python
- Groq API

### Database

- PostgreSQL

### API Documentation

- Swagger / OpenAPI

### Testing

- Pytest
- Mocking (`unittest.mock`)

### Tools

- Git & GitHub
- IntelliJ IDEA
- VS Code
- Swagger

---

# Project Structure

```text
ChatBot-platform
│
├── api-gateway
├── chatbot-bridge
├── chatbot-engine-fastapi
├── organization-service
│
├── Week-1 report.md
├── Week-2 report.md
├── Week-3 report.md
├── Week-4 report.md
├── Final_Report.md
└── README.md
```

---

# Weekly Progress

## Week 1
- Project setup
- Database configuration
- Entity creation
- Spring Boot microservices initialization

## Week 2
- CRUD REST APIs
- Repository-Service-Controller architecture
- Internal Organization Context APIs
- API validation using Swagger

## Week 3
- API Gateway configuration
- JWT Authentication
- Spring Security
- Header propagation
- Internal API authentication
- Global exception handling
- CORS configuration

## Week 4
- Conversation & Message entities
- Conversation CRUD APIs
- RestTemplate integration
- Organization Context integration
- Context-aware AI prompt generation
- Centralized LLM service
- FastAPI chat endpoint
- Streaming responses
- Token usage metadata
- Swagger documentation
- Unit testing with mocked LLM calls

---

# Key Learnings

- Built an end-to-end microservices-based chatbot platform.
- Implemented secure service-to-service communication using JWT.
- Integrated Java Spring Boot services with FastAPI.
- Developed context-aware AI prompts for improved chatbot responses.
- Documented REST APIs using Swagger/OpenAPI.
- Implemented unit testing with mocked external LLM calls.
- Strengthened debugging, Git, and backend development skills.

---

# Challenges Faced

- Configuring JWT authentication across multiple services.
- Debugging inter-service communication.
- Resolving CORS and security configuration issues.
- Managing PostgreSQL entity relationships.
- Integrating FastAPI with Spring Boot.
- Implementing streaming AI responses.
- Testing external LLM integrations using mocks.

---

# Future Enhancements

- Retrieval-Augmented Generation (RAG)
- Vector Database Integration
- Multi-LLM Support (OpenAI, Gemini, Claude)
- Docker Containerization
- Kubernetes Deployment
- Conversation Memory
- User Analytics Dashboard

---

# Weekly Reports

- 📄 Week-1 report.md
- 📄 Week-2 report.md
- 📄 Week-3 report.md
- 📄 Week-4 report.md
- 📄 internship_final_report.md

---

# Acknowledgement

I sincerely thank my mentor and the Accenture team for their continuous guidance, valuable feedback, and support throughout this internship. This project significantly enhanced my understanding of microservices, backend development, AI integration, and industry-standard software engineering practices.
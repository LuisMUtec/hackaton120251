# Sparky AI System

A centralized hub for managing access to different AI models through a unified platform.

## Overview

Sparky AI System is a comprehensive solution for enterprises to manage and control access to various AI models from different providers (OpenAI, Meta, DeepSpeak, etc.). The system allows companies to:

1. Register and manage their users
2. Set restrictions on AI model usage
3. Define user-specific limits
4. Monitor and track AI model consumption
5. Centralize billing and cost control

## Features

- **Multi-tenant architecture**: Support for multiple companies with isolated data
- **Role-based access control**: Different permissions for Sparky admins, company admins, and regular users
- **Flexible limit management**: Set limits based on requests or tokens with different time windows (daily, weekly, monthly)
- **Detailed usage tracking**: Monitor AI model usage at both company and user levels
- **Secure authentication**: JWT-based authentication and authorization
- **API Documentation**: Interactive Swagger UI for exploring and testing the API

## Technology Stack

- Java 17
- Spring Boot 3.x
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Swagger/OpenAPI for API documentation

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL database
- Maven

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/SparkyAISystem.git
   cd SparkyAISystem
   ```

2. Configure the database in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/sparkyai
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

5. Access the Swagger UI:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

## API Documentation

The API is organized into the following sections:

### Authentication API

- `POST /api/auth/login`: Authenticate a user and get a JWT token
- `POST /api/auth/register/admin`: Register a new Sparky admin
- `POST /api/auth/register/user`: Register a new regular user

### Admin API

- `POST /api/admin/companies`: Create a new company with an admin
- `GET /api/admin/companies`: List all companies
- `GET /api/admin/companies/{id}`: Get company details
- `PUT /api/admin/companies/{id}`: Update company information
- `PATCH /api/admin/companies/{id}/status`: Activate/deactivate a company
- `GET /api/admin/companies/{id}/consumption`: Get company consumption report

### Company API

- `POST /api/company/restrictions`: Create a new model restriction
- `GET /api/company/restrictions`: List all restrictions
- `PUT /api/company/restrictions/{id}`: Update a restriction
- `DELETE /api/company/restrictions/{id}`: Delete a restriction
- `POST /api/company/users`: Create a new user
- `GET /api/company/users`: List all users
- `GET /api/company/users/{id}`: Get user details
- `PUT /api/company/users/{id}`: Update user information
- `POST /api/company/users/{id}/limits`: Set user limits
- `GET /api/company/users/{id}/consumption`: Get user consumption report

### AI API

- `GET /api/ai/models`: Get available AI models
- `POST /api/ai/chat`: Send a chat request
- `POST /api/ai/completion`: Send a completion request
- `POST /api/ai/multimodal`: Send a multimodal request
- `GET /api/ai/history`: Get request history

## Usage Examples

### Authentication

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password123"}'
```

### Creating a Company

```bash
# Create a new company (requires Sparky admin token)
curl -X POST http://localhost:8080/api/admin/companies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Example Corp",
    "ruc": "12345678901",
    "active": true,
    "adminFirstName": "Company",
    "adminLastName": "Admin",
    "adminEmail": "admin@example.com",
    "adminPassword": "password123"
  }'
```

### Setting Restrictions

```bash
# Create a model restriction (requires company admin token)
curl -X POST http://localhost:8080/api/company/restrictions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "modelId": 1,
    "maxRequestsPerWindow": 1000,
    "maxTokensPerWindow": 100000,
    "windowType": "daily"
  }'
```

### Using AI Models

```bash
# Send a chat request (requires user token)
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "modelId": 1,
    "message": "Tell me about artificial intelligence",
    "systemPrompt": "You are a helpful assistant"
  }'
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Developed as part of a hackathon project
- Special thanks to Gabriel Romero for the project requirements
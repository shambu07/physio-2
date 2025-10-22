# Physio Clinic (Spring Boot)

Role-based physio clinic API with JWT auth, Spring Security method rules, Flyway migrations, and Swagger UI.

## Features
- JWT authentication (`/api/auth/login`)
- Role-based endpoints (DOCTOR / PATIENT / ADMIN)
- Clean JSON errors (401/403/4xx via SecurityConfig + ControllerAdvice)
- Appointments CRUD + idempotent booking
- Flyway migrations (MySQL 8)
- Actuator health endpoints on port 9093

## Quick Start

### Prereqs
- JDK 21+
- Maven
- MySQL 8 (running on `127.0.0.1:3310`, db `physioclinic`)

### Config
Set a stable JWT secret (base64) and expiry:
```properties
# src/main/resources/application-dev.properties
jwt.secret=VGhpcy1pcy1hLXN0cm9uZy1iYXNlNjQtc2VjcmV0LXN0cmluZw==
jwt.expMinutes=120
spring.datasource.url=jdbc:mysql://127.0.0.1:3310/physioclinic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=yourpass

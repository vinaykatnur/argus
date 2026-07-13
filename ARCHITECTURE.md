# Architecture

ARGUS is structured as a modular Spring Boot backend with a Vite React frontend.

## Backend

- Spring Boot 3.5 with Java 21
- REST controllers for authentication, monitors, incidents, notifications, dashboards, and incident intelligence
- Deterministic monitoring and intelligence services stay isolated from optional AI enrichment

## Frontend

- React 19 with Vite and Tailwind CSS
- Route-based experience for landing, authentication, dashboard, incidents, analytics, and settings
- Reusable platform components for onboarding, diagnostics, AI provider management, and self-health monitoring

## Deployment

- Dockerfile packages the backend jar
- docker-compose provides a local development stack with MySQL
- GitHub Actions builds the backend and frontend for CI validation

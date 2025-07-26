# Daylily Backend – Repository Instructions  

Spring Boot service that spins up short-lived preview containers for each GitHub Pull Request.

## Project Overview

- Receives **pull-request webhooks** from customer repositories.  
- Builds a Docker image from the PR branch, runs it, and exposes it through **Traefik**.
- Interacts with `daylily-grpc-server` Go gRPC server in order to 
  - Build the Docker image.
  - Start the container with the built image.
  - Stop and remove the container when the PR is closed or merged.
- Posts the preview URL back to the PR via GitHub REST API.  
- Stores build metadata in **PostgreSQL** and coordinates background work through **JMS**.

## Languages, Frameworks, and Tools

- Java
    - Spring Boot version 3
    - Spring Data JPA
    - Spring gRPC
- Go
    - gRPC
    - Protobuf
- PostgreSQL
- Docker
- Traefik

## Coding Standards

- Language level: Java 21.
- Static imports only for constants or fluent DSLs.
- Prefer `record` for immutable DTOs where possible.
- Use `switch` expressions for concise branching.
- Use `var` sparingly—only when the type is obvious from the right-hand side.
- Prefer `@RequiredArgsConstructor` for dependency injection.
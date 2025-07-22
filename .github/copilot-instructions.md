# Daylily Backend – Repository Instructions  

Spring Boot service that spins up short-lived preview containers for each GitHub Pull Request.

## Project Overview

- Receives **pull-request webhooks** from customer repositories.  
- Builds a Docker image from the PR branch, runs it, and exposes it through **Traefik**.  
- Posts the preview URL back to the PR via GitHub REST API.  
- Stores build metadata in **PostgreSQL** and coordinates background work through **JMS**.

## Coding Standards

- Language level: Java 21.
- Static imports only for constants or fluent DSLs.
- Prefer `record` for immutable DTOs where possible.
- Use `switch` expressions for concise branching.
- Use `var` sparingly—only when the type is obvious from the right-hand side.
- Prefer `@RequiredArgsConstructor` for dependency injection.
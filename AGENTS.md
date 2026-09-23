# Agent Guide: Uwati HIS

## Stack & Versions
- **Language**: Java 25
- **Framework**: Spring Boot 4.1.0
- **Build Tool**: Maven (`./mvnw`)
- **Standards**: Inherited from `.agents/rules/java-kotlin/` and `.agents/rules/shared/`

## Architecture: Hexagonal (Ports & Adapters)
- `his-domain/` -> Pure domain model, entities and value objects (Java records), inbound/outbound ports, domain events, domain exceptions (zero framework dependencies)
- `his-core/` -> Application services implementing inbound use cases (pure Java business orchestration)
- `his-rest/` -> Inbound driving REST adapters, `@RestController`s, request/response DTOs, OpenAPI documentation, and RFC 9457 exception handling
- `his-persistence/` -> Outbound driven persistence adapters, Spring Data JPA repositories, JPA entities, Liquibase migrations, tenant isolation
- `his-cache/` -> Outbound caching & distributed locking adapters (Redis / Valkey)
- `his-bootstrap/` -> Composition root, `@SpringBootApplication`, transactional use case decorators, runtime wiring

## Key Commands
- Build: `./mvnw clean package`
- Unit Tests: `./mvnw test`
- Verification / ArchUnit / JaCoCo: `./mvnw clean verify`
- Coverage Summary: `python3 .agents/scripts/coverage/generate-jacoco-summary.py --output target/coverage-summary.md`

## Agent Guidelines
- Check `.agents/project-structure.json` for immediate directory mapping.
- If missing, run: `python3 .agents/scripts/scan-structure.py`.
- Strict rule: NEVER import Spring, JPA, or Web dependencies inside `his-domain/`.
- Strict rule: Controllers in `his-rest/` MUST ONLY interact with `*UseCase` inbound ports, never repositories directly.
- Strict rule: JPA entities must remain internal to `his-persistence/` and never leak into `his-domain/`.
- Strict rule: Maintain a minimum of **80% line coverage** across core business and domain modules (`his-domain/`, `his-core/`). Enforced via CI.


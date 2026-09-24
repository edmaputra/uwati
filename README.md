<h1 align="center">Uwati HIS</h1>

<p align="center">
  <strong>Modern, backend-first, multi-tenant Hospital Information System (HIS) platform built with Java 25, Spring Boot 4.1.0, and Hexagonal Architecture (Ports and Adapters).</strong>
</p>

<p align="center">
  <a href="https://openjdk.org/projects/jdk/25/"><img src="https://img.shields.io/badge/Java-25-blue.svg" alt="Java 25" /></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg" alt="Spring Boot 4.1.0" /></a>
  <a href="https://github.com/edmaputra/ed-iam"><img src="https://img.shields.io/badge/ed--iam-0.3.0-orange.svg" alt="ed-iam 0.3.0" /></a>
  <a href="https://www.postgresql.org/"><img src="https://img.shields.io/badge/PostgreSQL-17+-336791.svg" alt="PostgreSQL 17+" /></a>
  <a href="https://valkey.io/"><img src="https://img.shields.io/badge/Valkey-9.1+-red.svg" alt="Valkey 9.1+" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License" /></a>
</p>

---

## Overview

**Uwati HIS** is an enterprise-grade Hospital Information System designed for healthcare institutions ranging from independent specialized clinics to distributed multi-facility hospital networks. Built with a strict **Hexagonal Architecture (Ports and Adapters)**, Uwati HIS completely decouples pure clinical and administrative business logic from framework and infrastructure concerns.

The platform natively supports multi-tenancy, hierarchical organizational scoping, declarative access control via [`ed-iam`](https://github.com/edmaputra/ed-iam), low-latency distributed caching with Valkey/Redis, and immutable regulatory audit trail compliance.

---

## Architecture & Modular Layout

Dependencies flow **strictly inward**: `adapter` ➔ `application` ➔ `domain`.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                his-bootstrap                                │
│           (Composition root, @SpringBootApplication, runtime wiring)        │
└──────────────┬──────────────────────────────┬───────────────────────────────┘
               │                              │
┌──────────────▼──────────────┐┌──────────────▼──────────────┐┌───────────────▼──────────────┐
│       his-persistence       ││        ed-iam-starter       ││          his-cache            │
│  - Spring Data JPA Adapters ││  - Turnkey IAM Starter      ││  - Valkey / Redis Engine      │
│  - Liquibase Migrations     ││  - Authentication & RBAC    ││  - Multi-Tenant Keyspaces     │
│  - PostgreSQL 17 Entities   ││  - Organizational Scopes    ││  - Distributed Locks          │
│  - Immutable Audit Listener ││  - JWT Engine & Context     ││  - Repository Decorators      │
└──────────────┬──────────────┘└──────────────┬──────────────┘└───────────────┬───────────────┘
               │                              │                               │
               └──────────────────────┬───────┴───────────────────────────────┘
                                      ▼
                       ┌──────────────────────────────┐
                       │          his-domain          │
                       │  - Pure Entities & Aggregates│
                       │  - Inbound & Outbound Ports  │
                       │  - TenantContext (ScopedValue)│
                       │  - Zero Framework Deps       │
                       └──────────────▲───────────────┘
                                      │
                       ┌──────────────┴───────────────┐
                       │           his-core           │
                       │  - Application Use Cases     │
                       │  - Pure Domain Orchestration │
                       │  - Audit Diff Engine         │
                       └──────────────▲───────────────┘
                                      │
                       ┌──────────────┴───────────────┐
                       │           his-rest           │
                       │  - Inbound REST Controllers  │
                       │  - Request/Response DTOs     │
                       │  - RFC 9457 Problem Details  │
                       │  - OpenAPI Documentation     │
                       └──────────────────────────────┘
```

### Module Responsibilities

| Module | Architectural Role | Technologies & Responsibilities |
|---|---|---|
| [`his-domain`](his-domain/) | **Domain Kernel** | Pure Java records (Entities, Value Objects), Inbound Use Case ports, Outbound SPI repository ports, Domain Events, Domain Exceptions. **Zero framework or database dependencies.** |
| [`his-core`](his-core/) | **Application Layer** | Pure Java orchestrators implementing inbound use case ports, domain transaction orchestration, and structured audit diff engine. |
| [`his-rest`](his-rest/) | **Inbound Web Adapter** | Spring MVC `@RestController`s, request/response DTOs, Jakarta validation, RFC 9457 problem detail exception handling, and tenant context extraction. |
| [`his-persistence`](his-persistence/) | **Driven Storage Adapter** | Spring Data JPA repositories, JPA entities, PostgreSQL 17 isolation, Liquibase database migrations, and immutable audit event listeners. |
| [`his-cache`](his-cache/) | **Driven Cache Adapter** | Low-latency caching engine powered by Valkey / Redis, tenant keyspace isolation, use-case caching decorators, and distributed locking. |
| [`his-bootstrap`](his-bootstrap/) | **Composition Root** | `@SpringBootApplication`, transactional use-case decorators, containerized integration test suite with Testcontainers, and application lifecycle wiring. |

---

## Core Capabilities

- **Strict Multi-Tenancy**:
  - Shared-application, shared-database architecture with discriminator isolation via `TenantId`.
  - Non-blocking, virtual-thread friendly context propagation powered by Java 25 `ScopedValue` and `TenantContextBridge`.
- **Identity & Access Management**:
  - Powered by [`ed-iam-starter`](https://github.com/edmaputra/ed-iam) (v0.3.0).
  - Multi-tenant RBAC, hierarchical organizational scope trees (`ScopeNode`), and user groups with dynamic role inheritance.
  - Declarative endpoint-level security via `@RequirePermission` and SpEL `@iam` evaluator.
- **Organizational Structure Modeling**:
  - Facilities and Service Units (departments, clinics, polyclinics, wards, laboratories).
  - Scope trees with materialized path cascade indexing for fine-grained authorization.
- **Immutable Structured Audit Trail**:
  - Automated tracking of all state modifications on auditable aggregates.
  - Generates JSON before/after state diffs with initiating actor, actor type (`USER`, `SYSTEM`, `MACHINE`), and correlation ID.
- **Multi-Tenant Distributed Caching & Locking**:
  - Valkey / Redis 9+ caching with automatic keyspace prefixing (`uwati:{tenantId}:...`).
  - Cache eviction driven by domain events.
  - Distributed mutual exclusion locks for critical operational pathways.

---

## Tech Stack & Standards

- **Language**: Java 25
- **Framework**: Spring Boot 4.1.0
- **Build Tool**: Maven 3.9+ with wrapper (`./mvnw`)
- **Database**: PostgreSQL 17+
- **Schema Migrations**: Liquibase (`classpath:db/changelog/db.changelog-master.json`)
- **Caching & Locking**: Valkey 9.1+ / Redis 7+
- **Security & IAM**: `io.github.edmaputra:ed-iam-starter:0.3.0`
- **Testing**: JUnit 5, AssertJ, Mockito, Testcontainers (PostgreSQL & Valkey), ArchUnit 1.4.0
- **Coverage**: JaCoCo with enforced **>=80% line coverage** on core business modules (`his-domain`, `his-core`)

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Java 25 installed and configured on your `PATH`.
- **Docker & Docker Compose**: Required for running the local datastores and running Testcontainers during test execution.

### 1. Start Local Infrastructure

Start PostgreSQL and Valkey using the bundled Docker Compose file:

```bash
docker compose up -d
```

This starts:
- **PostgreSQL 17**: `localhost:5432` (database: `uwati`, user: `uwati`, password: `uwati`)
- **Valkey 9.1**: `localhost:6379`

### 2. Build the Project

Compile and package all modules:

```bash
./mvnw clean package
```

### 3. Run the Application

Start the Spring Boot composition root:

```bash
./mvnw spring-boot:run -pl his-bootstrap
```

The application will start on port `8080`. Health probes and metrics are accessible under:
- `GET http://localhost:8080/actuator/health`
- `GET http://localhost:8080/actuator/info`

---

## Testing & Quality Gates

Run standard unit tests across all modules:

```bash
./mvnw test
```

Execute full verification (compilation, ArchUnit boundary fitness tests, integration tests via Testcontainers, and JaCoCo coverage validation):

```bash
./mvnw clean verify
```

---

## Configuration Reference

Key properties in [`his-bootstrap/src/main/resources/application.properties`](his-bootstrap/src/main/resources/application.properties):

| Property | Default Value | Description |
|---|---|---|
| `spring.application.name` | `uwati` | Application identifier |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/uwati` | PostgreSQL connection URL |
| `spring.datasource.username` | `uwati` | Database user |
| `spring.datasource.password` | `uwati` | Database password |
| `spring.liquibase.change-log` | `classpath:db/changelog/db.changelog-master.json` | Master Liquibase changelog |
| `spring.data.redis.host` | `localhost` | Redis / Valkey host |
| `spring.data.redis.port` | `6379` | Redis / Valkey port |
| `uwati.cache.enabled` | `true` | Caching toggle |
| `uwati.cache.key-prefix` | `uwati:` | Key prefix for cache entries |
| `iam.jwt.secret` | *(System configured)* | HMAC-SHA256 signing secret for JWT tokens |

---

## Documentation

Comprehensive architecture walkthroughs and guides are located in the [`docs/`](docs/) directory:

- 📖 [Features & Product Roadmap](docs/01-features-and-roadmap.md): Market segments, capabilities, and phase roadmap.
- 🏢 [Tenant Management Walkthrough](docs/tenant-management-walkthrough.md): Multi-tenant provisioning, settings, and isolation model.
- 📜 [Audit Trail Walkthrough](docs/audit-trail-walkthrough.md): Immutable event capture and JSON state diffing engine.
- ⚡ [Distributed Cache Walkthrough](docs/cache-walkthrough.md): Valkey integration, keyspace partitioning, and cache eviction patterns.
- 🔐 [IAM Integration Walkthrough](docs/iam-walkthrough.md): Architecture plan, role assignments, and organizational scope trees.
- 🔄 [Legacy Controller Revamp Map](docs/legacy-controller-revamp-map.md): Migration mapping from legacy JSP/Spring controllers to modern hexagonal REST endpoints.

---

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE).

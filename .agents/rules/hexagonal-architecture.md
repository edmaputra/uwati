# Hexagonal Architecture Rules

This project follows **Hexagonal Architecture (Ports and Adapters)** with **Domain-Driven Design (DDD)** in Java 25 and Spring Boot 4.

---

## 1. Multi-Module Layout & Responsibilities

The codebase is strictly separated into focused modules where dependencies must flow **inward**.

```
his-rest (Driving Adapter)       his-persistence (Driven Adapter)
         \                               /
          \                             /
           -->  his-domain (Core)  <---
                     ^
                     |
               his-core (Use Cases)
                     ^
                     |
          his-bootstrap (Wiring & Root)
```

### 1.1 `his-domain` (Domain Model & Ports)
- **Framework-Agnostic**: Pure Java only. Strictly NO Spring, JPA, Hibernate, Jackson, or Web dependencies.
- **Contents**:
  - **Domain Entities & Value Objects**: Immutable Java `record`s enforcing invariants (e.g., `Tenant`, `TenantId`).
  - **Inbound Ports**: Single-purpose use-case interfaces (e.g., `CreateTenantUseCase`, `ConfigureTenantSettingsUseCase`).
  - **Outbound Ports**: Repository and event publisher interfaces (e.g., `TenantRepository`, `TenantEventPublisher`).
  - **Domain Events**: Records representing state changes (e.g., `TenantCreated`, `TenantSettingsUpdated`).
  - **Domain Exceptions**: Specific business exceptions (e.g., `TenantNotFoundException`, `DuplicateTenantDisplayNameException`).
  - **Platform Contexts**: `TenantContext`, `OperationContext`, `Auditable`.

### 1.2 `his-core` (Application Services & Business Orchestration)
- **Framework-Agnostic Logic**: Depends solely on `his-domain`.
- **No Spring Annotations**: Do not use `@Service`, `@Component`, `@Transactional`, or `@Autowired`.
- **Contents**:
  - Application services implementing inbound ports (e.g., `CreateTenantService implements CreateTenantUseCase`).
  - Business validations spanning multiple aggregates or ports.

### 1.3 `his-rest` (Driving / Inbound Adapter)
- **HTTP Transport Layer**: Depends on `his-domain`.
- **Contents**:
  - `@RestController` classes (e.g., `TenantManagementController`).
  - Request/Response DTO records and mappings.
  - Centralized exception mapping (`RestExceptionHandler`) returning RFC 7807 responses.
  - HTTP Filters for context initialization (`TenantContextFilter`).
- **Rule**: Controllers MUST only invoke Inbound Port interfaces (`*UseCase`). They must never interact directly with database repositories or services.

### 1.4 `his-persistence` (Driven / Outbound Adapter)
- **Database & Data Access**: Depends on `his-domain`.
- **Contents**:
  - JPA entities with table mappings (e.g., `TenantEntity`, `AuditEntryEntity`).
  - Spring Data JPA repositories (e.g., `TenantJpaRepository`).
  - Port implementations / registries (e.g., `JpaTenantRegistry implements TenantRepository`).
- **Rules**:
  - JPA entities must remain private to this adapter. Always map between JPA entities and domain records.
  - All queries must be strictly tenant-scoped (multi-tenant isolation).

### 1.5 `his-bootstrap` (Composition Root & Infrastructure)
- **Wiring & Application Entry**: Depends on all modules.
- **Contents**:
  - `@SpringBootApplication` main class and `@Configuration` beans.
  - Transactional decorators: Wrap core services with `@Service` and `@Transactional` to manage transaction boundaries without polluting `his-core`.
  - Liquibase database changelogs and migrations.

---

## 2. Transaction Management Pattern

Use the **Decorator Pattern** in `his-bootstrap` to apply Spring transaction boundaries to core services:

```java
package io.github.edmaputra.uwati.bootstrap.tenancy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.edmaputra.uwati.core.tenancy.application.service.CreateTenantService;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;

@Service
public class TransactionalCreateTenantUseCase implements CreateTenantUseCase {

    private final CreateTenantService delegate;

    public TransactionalCreateTenantUseCase(TenantRepository tenantRepository, TenantEventPublisher eventPublisher) {
        this.delegate = new CreateTenantService(tenantRepository, eventPublisher);
    }

    @Override
    @Transactional
    public Tenant execute(CreateTenantCommand command, OperationContext context) {
        return delegate.execute(command, context);
    }
}
```

---

## 3. Dependency Check List

When adding or modifying code, verify:
- [ ] Does `his-domain` contain any Spring or JPA imports? (Must be **NONE**)
- [ ] Does `his-core` contain any Spring `@Service` or `@Transactional` annotations? (Must be **NONE**)
- [ ] Does `his-rest` call repositories directly? (Must **ONLY** call `*UseCase` inbound ports)
- [ ] Are JPA entities leaking into domain or REST layers? (Must remain in `his-persistence`)

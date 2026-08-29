# Clean Code & Modern Java Standards

This document establishes clean code practices and coding standards for this repository (Java 25 / Spring Boot 4).

---

## 1. Immutability & Java Records

- **Default to Java `record`**: Use `record` for all Domain Entities, Value Objects, Commands, Events, and DTOs.
- **Fail-Fast with Compact Constructors**: Validate invariants directly inside the compact constructor to prevent instantiating invalid objects.
- **Immutable Collections**: Return unmodifiable copies (`List.of()`, `Set.of()`, `Map.copyOf()`) to prevent state mutations from leaking.

```java
// ✅ GOOD: Immutable record with compact constructor invariant checks
public record CreateTenantCommand(String legalName, String displayName) {
    public CreateTenantCommand {
        Objects.requireNonNull(legalName, "Tenant legal name must not be null.");
        Objects.requireNonNull(displayName, "Tenant display name must not be null.");
        if (legalName.isBlank()) {
            throw new IllegalArgumentException("Tenant legal name must not be blank.");
        }
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("Tenant display name must not be blank.");
        }
    }
}

// ❌ BAD: Mutable class with getters/setters and deferred validation
public class CreateTenantCommand {
    private String legalName;
    private String displayName;
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
}
```

---

## 2. Interface Segregation & Single-Purpose Ports

- **One Inbound Port = One Intention**: Avoid monolithic "god" service interfaces. Create single-purpose use case interfaces (`CreateTenantUseCase`, `ConfigureTenantSettingsUseCase`).
- **Encapsulate Arguments in Commands**: If a use case requires multiple inputs, encapsulate them into a strongly typed `*Command` record rather than passing long parameter lists.

```java
// ✅ GOOD: Focused, single-purpose Inbound Port
public interface ConfigureTenantSettingsUseCase {
    TenantSetting execute(ConfigureTenantSettingsCommand command, OperationContext context);
}

// ❌ BAD: Monolithic service interface with dozens of unrelated methods
public interface TenantService {
    Tenant create(String name);
    void update(UUID id, String name);
    TenantSetting configure(String key, String value);
    void delete(UUID id);
    List<Tenant> listAll();
}
```

---

## 3. Explicit Dependency Injection & Zero Magic

- **Constructor Injection with `final` Fields**: Always inject dependencies via constructor.
- **Lombok `@RequiredArgsConstructor`**: Permitted for clean constructor generation on services and controllers.
- **Field Injection Strictly Prohibited**: Never use `@Autowired` on private fields.
- **No Spring in Domain/Core**: Never import `@Service`, `@Component`, `@Autowired`, or `@Value` inside `his-domain` or `his-core`.

```java
// ✅ GOOD: Explicit dependencies via constructor, pure Java
@RequiredArgsConstructor
public class CreateTenantService implements CreateTenantUseCase {
    private final TenantRepository tenantRepository;
    private final TenantEventPublisher eventPublisher;
    // ...
}

// ❌ BAD: Spring field injection inside core logic
public class CreateTenantService {
    @Autowired
    private TenantRepository tenantRepository;
}
```

---

## 4. Null Safety & Clean Error Handling

- **Never Return `null`**:
  - Return `Optional<T>` from repository ports for single items that might not exist.
  - Return empty collections (`List.of()`, `Set.of()`) instead of `null` for list queries.
- **Domain Exceptions**:
  - Throw specific domain exceptions (e.g. `TenantNotFoundException`, `DuplicateTenantDisplayNameException`) instead of generic `RuntimeException` or returning error codes.
  - Domain exceptions must reside in `his-domain` under the relevant domain package.
- **Centralized REST Mapping**:
  - Catch domain exceptions in `his-rest` via `@ExceptionHandler` inside `RestExceptionHandler`. Translate them to standard HTTP status codes and RFC 7807 problem responses.

```java
// ✅ GOOD: Idiomatic Optional transformation and domain exception
return tenantRepository.findByDisplayName(displayName)
    .map(existing -> {
        if (!existing.legalName().equalsIgnoreCase(legalName)) {
            throw new DuplicateTenantDisplayNameException(displayName);
        }
        return existing;
    })
    .orElseGet(() -> createNewTenant(command, context));

// ❌ BAD: Returning null, nullable checks, and generic exception
Tenant existing = tenantRepository.findByDisplayName(displayName);
if (existing != null) {
    if (!existing.getLegalName().equals(legalName)) {
        throw new RuntimeException("Duplicate display name");
    }
}
```

---

## 5. Guard Clauses & Flat Code

- **Guard Clauses**: Validate preconditions early and exit/throw immediately to avoid nested `if-else` blocks (arrow anti-pattern).
- **Pattern Matching (Java 25)**: Use pattern matching for `instanceof` and `switch` expressions over chained condition ladders.

```java
// ✅ GOOD: Flat guard clauses
public void validateSettings(TenantSetting setting) {
    if (setting == null) {
        throw new IllegalArgumentException("Tenant setting must not be null.");
    }
    if (setting.key().isBlank()) {
        throw new InvalidTenantSettingException("Setting key must not be blank.");
    }
    // Happy path proceeds with no indentation
}

// ❌ BAD: Deeply nested indentation
public void validateSettings(TenantSetting setting) {
    if (setting != null) {
        if (!setting.key().isBlank()) {
            // Happy path buried deep
        } else {
            throw new InvalidTenantSettingException("Setting key must not be blank.");
        }
    } else {
        throw new IllegalArgumentException("Tenant setting must not be null.");
    }
}
```

---

## 6. Naming & Language Conventions

- **English Only**: Use standard English naming for all classes, methods, variables, database tables, and REST endpoints.
- **Intention-Revealing Names**:
  - Use Cases: Verb phrases (e.g., `CreateTenantUseCase`, `ConfigureTenantSettingsUseCase`).
  - Domain Models: Noun phrases (e.g., `Tenant`, `TenantSetting`, `AuditEntry`).
  - Outbound Ports: Repository or Publisher nouns (e.g., `TenantRepository`, `TenantEventPublisher`).
  - Controllers: Resource nouns (e.g., `TenantManagementController`).
- **REST Endpoints**: Lowercase, plural kebab-case (e.g., `/api/v1/tenants`, `/api/v1/tenant-settings`).

---

## 7. Testing Standards

- **Sub-Second Unit Tests**: `his-domain` and `his-core` tests must be pure unit tests without starting Spring Boot (`@SpringBootTest`). Use mock repositories or in-memory fakes.
- **Integration & Slices**: Keep full Spring Boot integration tests (`@SpringBootTest`, Testcontainers) in `his-bootstrap/src/test` and slice tests in adapter modules.
- **Arrange-Act-Assert**: Write clean, expressive tests with clear expectations.

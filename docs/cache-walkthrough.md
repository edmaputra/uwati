# Distributed Cache & Multi-Tenant Caching Walkthrough

This guide describes the **distributed caching architecture**, **multi-tenant namespace strategy**, and **`his-cache`** infrastructure module in Uwati HIS.

---

## 1. Overview & Core Capabilities

The `his-cache` module is a self-contained, reusable infrastructure module that delivers:

1. **Dedicated Module Separation**: Decouples in-memory distributed caching from relational JPA persistence (`his-persistence`), application services (`his-core`), and domain contracts (`his-domain`).
2. **Multi-Tenant Key Isolation**: Guarantees zero cross-tenant data leakage by automatically partitioning cache keys with `TenantId` namespaces (`uwati:tenant:{tenantId}:{subKey}`).
3. **Hexagonal Option B (Decorator Pattern)**: Implements repository caching via decorator beans (`CachedTenantSettingRegistry`) and domain event listeners (`TenantSettingCacheEvictor`). Persistence layers remain pure JPA without `@Cacheable` annotations.
4. **Valkey Engine (`valkey/valkey:9.1.1-alpine3.24`)**: Powered by the Linux Foundation's 100% open-source, BSD-3 licensed, high-performance Valkey engine (wire-compatible drop-in replacement for Redis).
5. **Resilience & Fallback**: Catches network timeouts or cache outages via `ResilienceCacheErrorHandler` and gracefully falls back to PostgreSQL without interrupting clinical or administrative operations.
6. **Modern JSON Serialization**: Fully supports Java 25 `record` types and ISO-8601 timestamps using Jackson's `JavaTimeModule`.
7. **Atomic Distributed Locks**: Provides `DistributedLockPort` and `RedisDistributedLockAdapter` for concurrency control (e.g. invoice and medical record sequence generation).

---

## 2. Architecture & Module Boundaries

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                his-bootstrap                                │
│                (Aggregates modules, starts Spring Boot app)                 │
└──────────────┬──────────────────────────────┬───────────────────────────────┘
               │                              │
┌──────────────▼──────────────┐┌──────────────▼──────────────┐┌───────────────▼──────────────┐
│       his-persistence       ││           his-iam           ││          his-cache            │
│  - JPA Repositories         ││  - Pluggable Auth (SPI)     ││  - RedisConnectionFactory     │
│  - Liquibase Migrations     ││  - JWT & Scope RBAC         ││  - Multi-Tenant Key Generator │
│  - PostgreSQL Entities      ││  - Token Blacklist Store    ││  - Jackson Serializer Config  │
│  - Pure JPA (No @Cacheable) ││  - Scope Hierarchy Cache    ││  - Resilience/Fallback Handler│
└──────────────┬──────────────┘└──────────────┬──────────────┘│  - Distributed Lock / Ports   │
               │                              │               │  - Cache Decorators & Eviction│
               │                              │               └───────────────┬───────────────┘
               └──────────────────────┬───────┴───────────────────────────────┘
                                      ▼
                       ┌──────────────────────────────┐
                       │          his-domain          │
                       │  - TenantContext (ScopedValue│
                       │  - Domain Events             │
                       │  - Pure Domain Contracts     │
                       └──────────────────────────────┘
```

---

## 3. Multi-Tenant Key & Namespace Strategy

To maintain strict tenant isolation across all shared caching layers:

### 3.1 Tenant-Scoped Keys
- **Pattern**: `uwati:tenant:{tenantId}:{cacheName}:{key}`
- **Examples**:
  - Tenant Settings: `uwati:tenant:01059136-61c3-4c85-bfa5-debe642b0371:settings:all`
  - IAM Scope Hierarchy: `uwati:tenant:01059136-61c3-4c85-bfa5-debe642b0371:scopes:tree`
  - IAM Effective Permissions: `uwati:tenant:01059136-61c3-4c85-bfa5-debe642b0371:actor_access:b2c9a101`

### 3.2 Global / Platform-Scoped Keys
- **Pattern**: `uwati:global:{cacheName}:{key}`
- **Examples**:
  - Global Tenant Registry: `uwati:global:tenants:01059136-61c3-4c85-bfa5-debe642b0371`
  - System Roles: `uwati:global:iam:system_roles`

---

## 4. Valkey 9.1.1 Engine & Infrastructure

The application uses **Valkey 9.1.1 on Alpine 3.24** (`valkey/valkey:9.1.1-alpine3.24`):

- **Local Dev ([`compose.yaml`](file:///home/edmaputra/.gemini/antigravity/worktrees/uwati/research_redis_bootstrap_plan/compose.yaml))**:
  ```yaml
  redis:
    image: valkey/valkey:9.1.1-alpine3.24
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "valkey-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
  ```

- **Integration Testing ([`TestcontainersConfiguration.java`](file:///home/edmaputra/.gemini/antigravity/worktrees/uwati/research_redis_bootstrap_plan/his-bootstrap/src/test/java/io/github/edmaputra/uwati/TestcontainersConfiguration.java))**:
  ```java
  @Bean
  @ServiceConnection(name = "redis")
  public GenericContainer<?> redisContainer() {
      return new GenericContainer<>("valkey/valkey:9.1.1-alpine3.24").withExposedPorts(6379);
  }
  ```

---

## 5. Core Components in `his-cache`

| Component | Responsibility |
| :--- | :--- |
| **`CacheAutoConfiguration`** | Spring Boot auto-configuration registering cache managers, error handlers, and templates. |
| **`TenantAwareCacheKeyGenerator`** | Resolves `TenantId` from `TenantContext` (ScopedValue) or method arguments to prefix keys. |
| **`ResilienceCacheErrorHandler`** | Prevents Redis failures from bringing down API requests by logging warnings and falling through to database queries. |
| **`JacksonRedisSerializerFactory`** | Jackson `ObjectMapper` with `JavaTimeModule` and `DefaultTyping.EVERYTHING` for Java `record` support. |
| **`CachedTenantSettingRegistry`** | `@Primary` decorator implementing `TenantSettingRepository` around JPA repository with caching. |
| **`TenantSettingCacheEvictor`** | Event listener that evicts cached tenant settings upon receiving `TenantSettingsUpdated` domain events. |
| **`DistributedLockPort` / `RedisDistributedLockAdapter`** | Atomic lock acquisition and release with TTL leases and automatic cleanup. |

---

## 6. Verification & Test Suite

The caching layer is validated with unit and integration tests:

1. **`TenantAwareCacheKeyGeneratorTest`**:
   - Validates key extraction from method parameters.
   - Validates key extraction from `TenantContext`.
   - Validates global key fallback.
2. **`ResilienceCacheErrorHandlerTest`**:
   - Confirms silent fallback behavior on get/put/evict/clear failures.
3. **`CachedTenantSettingRegistryTest`**:
   - Asserts cache hits bypass the database delegate.
   - Asserts cache misses query the delegate and populate Redis.
   - Asserts cache eviction on `saveAll()`.
4. **`TenantSettingCacheEvictorTest`**:
   - Verifies `TenantSettingsUpdated` domain event triggers eviction.
5. **`JacksonRedisSerializerFactoryTest`**:
   - Verifies serialization and deserialization of Java records (`TenantSetting`, `TenantId`) and `List<TenantSetting>`.
6. **`TenantSettingsCachingIntegrationTests`**:
   - Validates end-to-end caching, eviction, tenant isolation, and distributed locks against Testcontainers.

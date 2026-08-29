# Multi-Tenancy & Audit Trail Rules

This document outlines the strict platform requirements for multi-tenancy and audit trailing in Uwati HIS.

---

## 1. Multi-Tenancy Architecture

Multi-tenancy is a core architectural pillar. The system operates on a **shared application, shared database, tenant discriminator column** model.

### 1.1 Tenant Context Propagation
- **Java 25 Scoped Values**: The tenant context is bound to the request scope via `ScopedValueTenantContext` and `TenantContextFilter`.
- **Mandatory Scoping**: Every business operation on tenant-owned data must be executed within an active `TenantContext` or `OperationContext`.
- **Fail Fast on Missing Context**: If an operation requiring tenant identity is called without an active context, throw `MissingTenantContextException`.

### 1.2 Tenant-Aware Persistence
- **Entity Ownership**: All tenant-specific JPA entities must include the tenant discriminator column and/or implement `TenantOwned`.
- **Query Scoping**: Repository implementations (`his-persistence`) MUST automatically filter queries by the current `TenantId`. Never allow un-scoped cross-tenant select or update queries.
- **Sequence Generation**: Document sequence numbers, invoice numbers, and medical record numbers must be generated per tenant.

```java
// ✅ GOOD: Tenant-scoped entity with discriminator
@Entity
@Table(name = "tenant_settings")
public class TenantSettingEntity {
    @EmbeddedId
    private TenantSettingId id; // contains tenantId and settingKey
    // ...
}
```

---

## 2. Audit Trail System

Audit trailing is a platform capability implemented via domain events and append-only persistence.

### 2.1 The `Auditable` Interface
- Domain entities that require tracking must implement `Auditable`:
  ```java
  public interface Auditable {
      Map<String, Object> auditableFields();
  }
  ```
- Return safe state representations in `auditableFields()` without exposing sensitive data (passwords, tokens, or restricted PII).

### 2.2 Domain Event Publishing
- Core services must publish structured domain events upon state mutations (e.g. `TenantCreated`, `TenantSettingConfigured`) via outbound port `*EventPublisher`.
- Domain events should encapsulate:
  - Tenant ID (for tenant-owned events).
  - Actor info (`CurrentActor` / actor ID and role).
  - Correlation ID.
  - Event timestamp.
  - Snapshot or delta of the changed entity.

### 2.3 Append-Only Audit Persistence
- Audit records stored in `his-persistence` (`AuditEntryEntity`) are strictly **append-only**.
- Normal application workflows must never update or delete existing audit entries.
- Cross-tenant audit viewing is reserved exclusively for platform administrators, not tenant users.

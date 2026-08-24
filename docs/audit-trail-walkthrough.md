# Audit Trail Walkthrough

This guide describes the unified, immutable audit-trail architecture and capabilities for Uwati HIS.

## Overview & Goal

In a Hospital Information System (HIS), recording *what* activity occurred is insufficient for regulatory compliance, medical auditing, and diagnostic tracing. The system must capture:

1. **Who** performed the change (**Actor identity**).
2. **Which request/session** initiated the change (**Correlation ID**).
3. **What exact state differences** occurred (**Structured JSON Diff**):
   - For entity/field-level changes: Old vs. New values (`{"old": ..., "new": ...}`).
   - For collections: Detailed breakdown of elements that were **`added`**, **`removed`**, or **`changed`**.
4. **When** it occurred (**UTC timestamp**).
5. **Which tenant** the change belongs to (**Multi-tenant isolation**).

---

## Core Concepts & Architecture

The audit trail follows clean/hexagonal architecture principles across modules:

```
┌────────────────────────────────────────────────────────┐
│                      his-rest                          │
│  - Extracts Actor & Correlation-ID from HTTP headers   │
│  - Builds OperationContext and calls UseCase           │
│  - Returns X-Correlation-Id in response headers        │
└──────────────────────────┬─────────────────────────────┘
                           │ OperationContext (Explicit)
┌──────────────────────────▼─────────────────────────────┐
│                      his-domain                        │
│  - Use Cases require explicit OperationContext         │
│  - Domain Events (TenantCreated, TenantSettingsUpdated)│
│    carry actor, correlationId, previous/updated state  │
└──────────────────────────┬─────────────────────────────┘
                           │ Event Publication
┌──────────────────────────▼─────────────────────────────┐
│                      his-core                          │
│  - AuditDiffEngine: Compares fields & collections      │
│  - AuditJsonFormatter: Formats deterministic JSON diff │
└──────────────────────────┬─────────────────────────────┘
                           │ Event Listener (BEFORE_COMMIT)
┌──────────────────────────▼─────────────────────────────┐
│                   his-persistence                      │
│  - AuditTrailEventListener: Listens to domain events   │
│  - AuditEntryEntity / AuditEntryJpaRepository          │
│  - Table: audit_entries                                │
└────────────────────────────────────────────────────────┘
```

### Module Responsibilities

| Module | Component | Responsibility |
|---|---|---|
| `his-domain` | `OperationContext` | Cross-cutting value object carrying `actor` and `correlationId`. |
| `his-domain` | `AuditEntry` | Domain representation of an immutable audit record. |
| `his-domain` | Domain Events | Carry `actor`, `correlationId`, and snapshot/previous/new states. |
| `his-core` | `AuditDiffEngine` | Compares object states, field maps, and keyed/primitive collections. |
| `his-core` | `AuditJsonFormatter` | Formats diff results into deterministic JSON with proper escaping. |
| `his-persistence` | `AuditEntryEntity` | JPA entity mapping to PostgreSQL `audit_entries` table. |
| `his-persistence` | `AuditTrailEventListener` | Transactional event listener (`BEFORE_COMMIT`) persisting audit logs. |
| `his-rest` | `TenantManagementController` | Resolves headers (`X-Actor-Id`, `X-Correlation-Id`, etc.) and injects `OperationContext`. |

---

## Context Propagation: Explicit `OperationContext`

Rather than relying on implicit ambient contexts (such as thread-locals or experimental scoped values), use cases accept an explicit `OperationContext`:

```java
public record OperationContext(String actor, String correlationId) {
    public static OperationContext of(String actor, String correlationId) {
        return new OperationContext(actor, correlationId);
    }

    public static OperationContext system() {
        return new OperationContext("system", null);
    }
}
```

### Use Case Signatures

Every mutating use case receives `OperationContext`:

```java
public interface CreateTenantUseCase {
    Tenant execute(CreateTenantCommand command, OperationContext context);
}

public interface ConfigureTenantSettingsUseCase {
    List<TenantSetting> execute(ConfigureTenantSettingsCommand command, OperationContext context);
}
```

### REST Header Resolution

The REST layer resolves actor and correlation identifiers from incoming HTTP headers:

- **Actor resolution order**: `X-Actor-Id` → `X-Actor` → `X-User-Id` → Default: `"system"`.
- **Correlation ID resolution order**: `X-Correlation-Id` → `X-Request-Id` → Generated `UUID.randomUUID()`.
- **Response Header**: `X-Correlation-Id` is echoed back on every response.

---

## Change Diff Engine (`AuditDiffEngine`)

`AuditDiffEngine` in `his-core` provides pure, testable diff calculations without external dependencies.

### 1. Field-Level Diffs (`diffFields`)

Compares two key-value maps with deterministic alphabetical ordering:

```java
Map<String, FieldDiff> diffs = AuditDiffEngine.diffFields(
    Map.of("status", "ACTIVE", "tier", "STANDARD"),
    Map.of("status", "SUSPENDED", "tier", "STANDARD")
);
// Result diff: {"status": FieldDiff(old="ACTIVE", new="SUSPENDED")}
```

### 2. Keyed Collection Diffs (`diffKeyedCollection`)

Compares collections of identifiable items (e.g., settings, permissions, line items) and classifies each item:
- **`added`**: Present in new collection, absent in old.
- **`removed`**: Present in old collection, absent in new.
- **`changed`**: Present in both, but specific fields differ.

```java
CollectionDiff<TenantSetting> diff = AuditDiffEngine.diffKeyedCollection(
    previousSettings,
    updatedSettings,
    TenantSetting::key,
    (oldSetting, newSetting) -> AuditDiffEngine.diffFields(
        Map.of("value", oldSetting.value(), "revision", oldSetting.revision()),
        Map.of("value", newSetting.value(), "revision", newSetting.revision())
    )
);
```

### 3. Primitive Collection Diffs (`diffPrimitiveCollection`)

Compares simple sets or lists (e.g., tags, role names) returning added and removed elements.

---

## Database Schema: `audit_entries`

The common audit table is provisioned via Liquibase (`2026082301-create-common-audit-entries-table.json`):

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique audit record identifier |
| `tenant_id` | `UUID` | `NULLABLE` | Tenant boundary (null for system/platform events) |
| `entity_name` | `VARCHAR(100)` | `NOT NULL` | Aggregate/entity type (e.g., `Tenant`, `TenantSetting`) |
| `entity_id` | `VARCHAR(255)` | `NOT NULL` | Identifier of the changed entity |
| `action` | `VARCHAR(50)` | `NOT NULL` | Operation type: `CREATE`, `UPDATE`, `DELETE` |
| `actor` | `VARCHAR(255)` | `NOT NULL` | User/system identifier that triggered the operation |
| `correlation_id` | `VARCHAR(255)` | `NOT NULL` | Trace/request correlation ID |
| `occurred_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | UTC timestamp of event |
| `changes_json` | `TEXT` | `NOT NULL` | Structured JSON difference payload |

### Indexes

- `idx_audit_entries_entity` (`entity_name, entity_id`)
- `idx_audit_entries_tenant_id` (`tenant_id`)
- `idx_audit_entries_correlation_id` (`correlation_id`)
- `idx_audit_entries_occurred_at` (`occurred_at`)

---

## Audit Entry JSON Examples

### 1. Entity Creation (`CREATE`)

Recorded when a new `Tenant` is created:

```json
{
  "fields": {
    "displayName": {
      "old": null,
      "new": "RS Permata Medika"
    },
    "legalName": {
      "old": null,
      "new": "PT Permata Medika Sejahtera"
    },
    "status": {
      "old": null,
      "new": "ACTIVE"
    }
  }
}
```

Database Row:
```
entity_name:    "Tenant"
entity_id:      "b8c3f4e2-7d1a-4a2e-9b5c-6e8f1a2b3c4d"
action:         "CREATE"
actor:          "operator-admin"
correlation_id: "corr-create-tenant-123"
```

---

### 2. Collection Configuration Update (`UPDATE`)

Recorded when `TenantSetting` collection is updated with added, modified, and removed settings:

```json
{
  "collections": {
    "settings": {
      "added": [
        {
          "key": "organization.contact-email",
          "value": "admin@permata.health",
          "revision": 1
        }
      ],
      "removed": [
        {
          "key": "features.legacy-billing",
          "value": "true",
          "revision": 1
        }
      ],
      "changed": [
        {
          "key": "organization.locale",
          "fields": {
            "revision": {
              "old": 1,
              "new": 2
            },
            "value": {
              "old": "en-US",
              "new": "id-ID"
            }
          }
        },
        {
          "key": "organization.time-zone",
          "fields": {
            "revision": {
              "old": 1,
              "new": 2
            },
            "value": {
              "old": "UTC",
              "new": "Asia/Jakarta"
            }
          }
        }
      ]
    }
  }
}
```

Database Row:
```
entity_name:    "TenantSetting"
entity_id:      "b8c3f4e2-7d1a-4a2e-9b5c-6e8f1a2b3c4d"
action:         "UPDATE"
actor:          "admin@permata.health"
correlation_id: "req-sett-456"
```

---

## How to Add Audit Logging to a New Feature

When introducing a new domain aggregate (e.g. `Patient`, `Encounter`, `Prescription`):

1. **Accept `OperationContext` in the Use Case**:
   ```java
   RegisterPatientResult execute(RegisterPatientCommand command, OperationContext context);
   ```

2. **Include Context & State in Domain Event**:
   ```java
   public record PatientRegistered(
       Patient patient,
       String actor,
       String correlationId,
       Instant occurredAt
   ) {}
   ```

3. **Handle Event in `AuditTrailEventListener` (or dedicated domain listener)**:
   ```java
   @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
   public void onPatientRegistered(PatientRegistered event) {
       var patient = event.patient();
       Map<String, FieldDiff> fieldDiffs = AuditDiffEngine.diffFields(
           null,
           Map.of(
               "mrn", patient.mrn(),
               "fullName", patient.fullName(),
               "status", patient.status().name()
           )
       );

       auditEntries.save(new AuditEntryEntity(
           patient.tenantId().value(),
           "Patient",
           patient.id().toString(),
           "CREATE",
           event.actor(),
           event.correlationId() != null ? event.correlationId() : "unknown",
           event.occurredAt(),
           AuditJsonFormatter.formatDiff(fieldDiffs)
       ));
   }
   ```

4. **Verify in Integration Tests**:
   - Query `audit_entries` table after HTTP request.
   - Assert `actor`, `correlation_id`, `action`, and JSON structure under `changes_json`.

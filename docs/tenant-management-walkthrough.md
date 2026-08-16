# Tenant Management Walkthrough

This guide describes the recommended tenant-management capabilities for Uwati HIS.
It assumes the initial model from the architecture map:

- one application
- one shared database
- a tenant discriminator on every tenant-owned record
- tenant isolation enforced by application code and persistence queries

Security and authorization are deliberately deferred. Until they are introduced, this
guide uses the term **platform operator** for the trusted process or user that manages
tenants.

## Goal

A tenant represents a customer organization operating Uwati HIS. It owns its
organization data, facilities, staff, patients, clinical data, inventory, financial
data, reports, preferences, and document-number sequences.

Every tenant-scoped operation must have exactly one active tenant context. A request,
job, report, or integration must never read or write data for another tenant unless a
future platform-level cross-tenant capability explicitly allows it.

## Recommended tenant model

Start with these concepts in `his-core`.

| Concept | Purpose | Initial fields |
|---|---|---|
| `Tenant` | The customer organization boundary | ID, legal/display name, status, created/updated timestamps |
| `TenantId` | Immutable tenant identifier used by all tenant-owned data | UUID |
| `TenantStatus` | Controls tenant availability | `ACTIVE`, `SUSPENDED`, `DEACTIVATED` |
| `TenantContext` | Supplies the current tenant to an application operation | current `TenantId` |
| `TenantOwned` | Marks aggregates that belong to one tenant | `tenantId` |
| `TenantRepository` | Finds and persists tenants independently of storage technology | tenant-scoped port |
| `TenantSettings` | Tenant-specific behavior and defaults | tenant ID, setting key, value, revision |

Do not use a tenant name, URL, facility name, or user ID as the tenant identifier.
Use `TenantId` throughout internal contracts and persistence relations.

## Tenant lifecycle walkthrough

### 1. Create a tenant

A platform operator creates a tenant with a unique ID and a display name. The tenant
starts as `ACTIVE` only after required bootstrap data is created successfully.

Recommended bootstrap data:

- organization profile placeholder
- default facility
- tenant time zone and locale
- default currencies and measurement units where applicable
- document-number sequence definitions
- base feature configuration
- audit entry recording creation

Creation should be idempotent when invoked by automation: an external provisioning
reference can safely map repeated requests to the same tenant.

### 2. Configure the tenant

The operator or a future tenant administrator configures the organization and
facility profile, operational settings, enabled modules, and document numbering.
Settings need validation, versioning, and an audit trail; they must not be arbitrary
unvalidated key/value data.

Use tenant defaults only when a setting is absent. Never silently fall back to
another tenant's setting or to a global value that exposes tenant-specific data.

### 3. Operate within a tenant context

Every inbound REST request carries `X-Tenant-Id` during this pre-security phase. The
REST adapter validates it and opens a context for the duration of the request. Core
use cases call `TenantContext.requireTenantId()` rather than accepting an optional
tenant ID.

The context must also be established for:

- scheduled tasks
- batch imports and exports
- asynchronous messages
- report generation
- integration callbacks
- command-line administration tools

Thread-local context is suitable for the initial synchronous servlet flow only.
When asynchronous or reactive processing is introduced, propagate the tenant context
explicitly in the task or message envelope; do not rely on a thread-local value.

### 4. Suspend a tenant

Suspension preserves all data but blocks normal write operations. It is appropriate
for non-payment, maintenance, or an operator investigation.

Recommended behavior:

- reject new clinical, inventory, billing, and configuration writes
- allow only explicitly approved read access, if required
- stop tenant-specific scheduled jobs and outbound integrations
- retain audit logging of denied access
- show a stable `TENANT_SUSPENDED` error to clients

The exact read policy should be a documented product decision before implementing
suspension. Defaulting to read-only is safer than allowing normal operations.

### 5. Deactivate and offboard a tenant

Deactivation disables all normal access while retaining data for the retention
period. It is not deletion.

Offboarding should be an explicit workflow:

1. deactivate the tenant
2. stop jobs, integrations, and active sessions
3. generate and verify an export when contractually required
4. retain or purge data under the approved retention policy
5. record completion in an immutable audit trail

Physical deletion, if ever supported, must be a separately reviewed platform
operation. It must account for clinical, financial, legal, backup, and audit
retention obligations.

## Isolation requirements

Tenant isolation is a correctness requirement, not only a security feature.

### Data ownership

Every tenant-owned table must have a non-null `tenant_id`. This includes, at minimum:

- organization and facility data
- staff and practitioners
- patients and encounters
- clinical records, diagnoses, orders, and prescriptions
- medicines, inventory, suppliers, purchases, and returns
- billing, claims, financial entries, and reports
- tenant settings, preferences, templates, and number sequences

Platform-owned tables, such as tenant registry records and future global reference
catalogues, are the exception. Each must be deliberately classified rather than
accidentally left without a tenant ID.

### Persistence enforcement

Tenant-aware repository adapters must:

- write the tenant ID from the required context, never from an untrusted DTO field
- add `tenant_id = :currentTenantId` to every tenant-owned read, update, and delete
- include `tenant_id` in unique constraints for tenant-local values
- include `tenant_id` in indexes that serve tenant-scoped queries
- reject entity IDs that resolve to a different tenant
- avoid repository methods that can return an unscoped tenant-owned collection

Tenant-owned foreign-key relationships should normally remain in the same tenant.
Where the database supports composite foreign keys, use `(tenant_id, entity_id)` to
prevent cross-tenant references at the database boundary.

### API enforcement

The REST adapter should:

- require a valid tenant header for tenant-scoped endpoints
- reject missing or malformed headers before calling a use case
- never accept `tenantId` in a normal tenant-owned request body or path
- return neutral errors that do not disclose another tenant's resources
- clear the context after every request, including failed requests

The tenant management endpoints themselves are platform endpoints. They will require
a dedicated operator authorization model once security is introduced.

### Reporting and exports

Reports and exports are high-risk for accidental cross-tenant access. They must:

- run with an explicit tenant context
- query tenant-aware repositories only
- include tenant identity in generated-file metadata
- store generated files in tenant-partitioned storage paths or buckets
- prevent one tenant from retrieving another tenant's export

## Tenant settings and configuration

Manage the following configuration categories as tenant-scoped, versioned settings.

| Category | Examples |
|---|---|
| Organization | legal name, display name, tax identifiers, contacts |
| Facilities | facility codes, addresses, operating hours, time zones |
| Localization | locale, time zone, date format, currency |
| Clinical defaults | encounter settings, terminology preferences, document templates |
| Pharmacy and inventory | stock thresholds, valuation policy, dispensing defaults |
| Finance | invoice prefixes, tax settings, payment terms |
| Numbering | patient, encounter, prescription, purchase, invoice sequences |
| Integrations | enabled integrations, endpoint references, retry policy |
| Feature configuration | staged modules and tenant-specific limits |
| Retention | archival and deletion policy references |

Secrets such as integration credentials must not be stored as ordinary settings.
Use a dedicated secret-management mechanism later, referenced by tenant-scoped
configuration.

## Tenant-aware numbering

Patient numbers, encounter numbers, prescriptions, purchases, invoices, and other
documents need per-tenant numbering strategies. Define a sequence by:

- tenant ID
- document type
- optional facility scope
- prefix and formatting policy
- next value
- reset policy, if allowed

Advance sequences transactionally with the business operation. Never calculate a
new number by counting records, and never share a tenant's sequence with another
tenant.

## Audit and observability

Record tenant identity in every operational log, audit event, metric tag, trace, and
outbox message where the telemetry system permits it.

Recommended audit events:

- tenant created, configured, suspended, reactivated, or deactivated
- settings changed
- feature enabled or disabled
- data export requested, generated, downloaded, or deleted
- failed tenant-context validation
- denied operation because of tenant status

Avoid putting patient or other sensitive clinical data into logs or metric labels.
Tenant IDs are preferable to tenant names in high-volume telemetry.

## Quotas and service limits

Quotas are a later platform capability, but the data model should permit them.
Potential limits include:

- maximum facilities, users, practitioners, or active patients
- storage and export size
- API request rate
- concurrent report or import jobs
- integration throughput
- enabled HIS modules

Enforce quotas in core use cases, not only at the REST gateway, so imports, jobs,
and future transport adapters obey the same limits.

## Future security integration

When authentication and authorization are designed, tenant context must be derived
from trusted identity claims and tenant membership, not directly from
`X-Tenant-Id`. The header can become a tenant-selection hint only after authorization
confirms that the authenticated principal may access it.

The future model should distinguish:

- platform operators, who manage tenant lifecycle
- tenant administrators, who manage their tenant configuration
- facility-level operators, who are restricted to particular facilities
- service identities used by integrations and background jobs

## Recommended implementation order

1. **Tenant identity and context**: finish `TenantId`, `Tenant`, lifecycle status,
   `TenantContext`, and context propagation for REST requests.
2. **Tenant registry**: add tenant create/read/update lifecycle use cases and a
   tenant-aware persistence adapter.
3. **Tenant bootstrap**: provision organization, facility, settings, and numbering
   defaults atomically.
4. **Repository enforcement**: require tenant-scoped persistence methods before
   introducing patient, medicine, medical-record, and purchasing repositories.
5. **Lifecycle controls**: enforce `ACTIVE` and `SUSPENDED` behavior in core use
   cases and jobs.
6. **Configuration management**: add validated, versioned tenant settings and
   tenant-aware document numbering.
7. **Auditing and operations**: introduce audit events, logs, metrics, exports,
   backups, and retention procedures.
8. **Security and quotas**: integrate identity-based tenant selection, permissions,
   and product limits after the core boundaries are stable.

## Review checklist

Before moving from tenancy foundations to patient or clinical modules, confirm:

- [ ] Tenant-owned aggregates always carry `TenantId`.
- [ ] A tenant context is mandatory for every tenant-owned use case.
- [ ] REST, jobs, messages, and CLI tools establish context explicitly.
- [ ] Persistence queries cannot omit tenant filtering.
- [ ] Tenant-local unique constraints and number sequences include tenant scope.
- [ ] Suspended and deactivated tenant behavior is defined and tested.
- [ ] Reports, exports, storage, logs, and audit events preserve tenant isolation.
- [ ] Tenant configuration and lifecycle changes are auditable.
- [ ] No request DTO can choose an arbitrary tenant for normal business operations.

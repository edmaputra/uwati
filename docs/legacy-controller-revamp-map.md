# HIS Backend Revamp Architecture Map

This document replaces the earlier MVC-oriented revamp notes with a **Hospital Information System (HIS)** modernization brief.

The target is no longer a server-rendered clinic/pharmacy application. The target is a **backend-first, multi-module, multi-tenant HIS platform** built with **Hexagonal Architecture**, using **English domain and API terms**.

## Current target decisions

- **Backend-first**
- **REST-first for now**
- **Transport-agnostic core** so other interfaces can be added later
- **Hexagonal Architecture**
- **Multi-module project layout**
- **Multi-tenant by design**
- **English-only external contracts**
- **Security deferred for now**

## What is no longer relevant

The following ideas are no longer part of the target direction and should not drive the redesign:

- server-rendered page routes
- controller-generated HTML fragments
- MVC page/view mappings
- preserving Indonesian endpoint naming
- designing only for a clinic or standalone pharmacy
- coupling business logic to Spring MVC or JPA
- treating security design as a blocker for the first architecture pass

## Architecture target

### Multi-module layout

Minimum target modules:

- `his-core`
  - business rules
  - entities
  - value objects
  - use cases
  - ports
  - tenant-aware domain/application logic
- `his-adapter-rest`
  - REST controllers
  - request/response DTOs
  - mapping between HTTP and use cases
- `his-adapter-persistence`
  - repository implementations
  - persistence entities
  - database access
  - tenant-aware query enforcement
- `his-bootstrap`
  - application startup
  - dependency wiring
  - Spring Boot configuration

Possible later modules:

- `his-adapter-reporting`
- `his-adapter-messaging`
- `his-adapter-batch`
- `his-adapter-audit`
- `his-adapter-scheduler`
- `his-shared-kernel`

### Dependency rule

Dependencies must point inward:

- adapters depend on `his-core`
- `his-core` depends on no adapter
- framework code stays outside the core

### Core rule

Keep these in `his-core`:

- patient registration rules
- medical-record workflows
- prescribing and dispensing workflows
- purchasing and inventory rules
- billing and finance use cases
- reporting use cases
- tenant-scoped business rules

Keep these outside `his-core`:

- HTTP controllers
- JPA/ORM mappings
- SQL concerns
- PDF/Excel generation
- framework configuration

### Platform concerns: audit trail and scheduler

Audit trails and scheduling are platform capabilities. They are not controller
features and must remain usable by every domain module without coupling the core to
Spring, a database, or a particular job runner.

#### Audit trail

The core should publish structured audit events through an outbound port. An audit
event should capture:

- tenant identity for every tenant-owned operation
- event time and correlation/causation identifiers
- actor identity and actor type when security is added
- action, aggregate type, aggregate identifier, and outcome
- a safe summary of the change or reason for failure

Audit persistence belongs in an adapter and should be append-only. It must be
tenant-scoped, queryable by tenant, and protected from normal transactional updates.
Avoid recording clinical payloads, credentials, or unrestricted before/after data in
audit events. Cross-tenant audit access is a future platform-operator capability,
not a normal application query.

#### Scheduler

The core should define scheduled-work use cases and ports; a scheduler adapter should
decide when and how a job runs. Do not call Spring `@Scheduled` from core business
logic.

Scheduled work must:

- establish an explicit tenant context before running tenant-owned work
- persist schedule definitions, executions, retries, and failures where durability is
  required
- be idempotent and protected against duplicate or overlapping execution
- use the tenant or facility time zone for calendar-based jobs
- emit audit events and operational metrics
- support pause, resume, manual retry, and safe cancellation by a future platform
  operator

Initial candidates include inventory threshold checks, report generation, retention
and archival tasks, integration retries, notifications, and recurring billing or
claim-processing work.

## Multi-tenant direction

Multi-tenancy is a first-class platform concern.

Recommended starting point:

- **shared application**
- **shared database**
- **tenant discriminator column**

Minimum tenancy concepts:

- `Tenant`
- `TenantId`
- `TenantContext`
- tenant-scoped aggregates
- tenant-aware repositories

Rules:

- every business operation must run within a tenant context
- every tenant-owned record must carry tenant identity
- repositories must be tenant-scoped by default
- reports must not cross tenants by default
- numbering sequences should be tenant-aware

This impacts:

- patients
- staff
- medicines
- inventory
- purchases
- dispensing
- billing
- reports
- facility profile

## Security status

Security is intentionally **out of scope for the first architecture phase**.

That means:

- no detailed authentication design yet
- no authorization model as a blocker
- no final role model yet

The first phase should focus on:

1. module boundaries
2. tenant model
3. core use cases
4. REST adapter
5. persistence adapter

## HIS-oriented domain modules

Recommended modules for the target system:

- tenancy
- organization
- facilities
- staff
- practitioners
- patients
- encounters
- admissions
- clinical-records
- orders
- prescriptions
- dispensing
- pharmacy-inventory
- purchasing-supply
- billing
- claims
- reporting
- notifications
- audit-trail
- scheduling

## English naming direction

Use English names for all new public contracts.

| Legacy term | Preferred target term |
|---|---|
| `profil` | `organization-profile` or `facility-profile` |
| `karyawan` | `staff` |
| `pasien` | `patients` |
| `obat` | `medicines` |
| `kategori` | `medicine-categories` |
| `kategori-pasien` | `patient-types` or `patient-categories` |
| `satuan` | `units` |
| `diagnosa` | `diagnoses` |
| `tindakan` | `procedures` or `clinical-services` |
| `racikan` | `compounded-medicines` |
| `rekam-medis` | `medical-records` |
| `resep` | `prescriptions` |
| `retur` | `purchase-returns` |
| `utang-piutang` | `payables` / `receivables` / `finance-entries` |
| `pengguna` | `users` |
| `laporan` | `reports` |

## Legacy capabilities that still matter

These areas from the current system are still relevant to an HIS and should be preserved conceptually:

| Legacy area | HIS relevance | Keep as |
|---|---|---|
| patient master data | high | patients |
| medical records | high | clinical-records |
| diagnoses | high | diagnoses |
| procedures | high | procedures / clinical-services |
| medicines | high | medicines |
| compounded medicines | medium-high | compounded-medicines |
| prescriptions | high | prescriptions |
| inventory notifications | medium-high | inventory alerts |
| purchasing | high | purchasing-supply |
| purchase returns | medium | purchase-returns |
| finance entries | medium | payables / receivables / billing support |
| reporting | high | reporting |

## Legacy capabilities to generalize

These should not be copied as-is.

| Legacy area | Problem | HIS target |
|---|---|---|
| pharmacy profile | too single-site/pharmacy-specific | organization and facility profile |
| employee master | too generic | staff + practitioners + departments |
| patient categories | too narrow | patient type, payer class, visit class |
| pharmacy sales | too retail-oriented | dispensing + billing + charge capture |
| invoice numbering | too transaction-specific | tenant-aware document numbering strategy |
| customer master (`pelanggan`) | ambiguous | suppliers, payers, guarantors, partners |
| report-side cancellation actions | wrong boundary | move into core transaction use cases |

## Legacy capabilities that are not enough for HIS

Major HIS capabilities missing or underrepresented in the current system:

- encounters / visits
- admissions, discharges, transfers
- wards, beds, rooms
- practitioner registry
- departments / hospital units
- scheduling and appointments
- triage and vital signs
- laboratory orders and results
- radiology orders and results
- nursing notes and care plans
- surgery / operating room workflows
- billing and claims
- insurance / payer integration
- non-medication orders
- document management
- queueing
- multi-facility operations

## REST direction

REST is the first inbound adapter, but not the final architecture boundary.

The initial API surface should be organized around HIS resources such as:

- `/api/v1/tenants`
- `/api/v1/facilities`
- `/api/v1/staff`
- `/api/v1/practitioners`
- `/api/v1/patients`
- `/api/v1/encounters`
- `/api/v1/admissions`
- `/api/v1/medical-records`
- `/api/v1/orders`
- `/api/v1/prescriptions`
- `/api/v1/dispensing`
- `/api/v1/medicines`
- `/api/v1/compounded-medicines`
- `/api/v1/inventory`
- `/api/v1/purchases`
- `/api/v1/purchase-returns`
- `/api/v1/billing`
- `/api/v1/claims`
- `/api/v1/reports`

The old MVC-style patterns should not be carried forward:

- `/daftar`
- `/dapatkan`
- `/tambah`
- `/edit`
- `/hapus`

Use resource-oriented endpoints instead.

## Legacy controller relevance summary

This is the reduced view of the old controller set from an HIS perspective.

| Legacy controller group | Status in revamp |
|---|---|
| patient, diagnosis, procedure, medical record | retain and redesign |
| medicine, units, categories, compounds | retain and redesign |
| purchasing, returns, stock notifications | retain and redesign |
| finance, reports | retain and redesign |
| profile, employee, customer | generalize heavily |
| page/navigation/login concerns | remove from target architecture |

## Recommended migration order

1. Define the target HIS domains and vocabulary in English.
2. Split the codebase into `his-core`, `his-adapter-rest`, `his-adapter-persistence`, and `his-bootstrap`.
3. Introduce the tenant model early.
4. Define audit-event and scheduled-work ports early, including tenant-context propagation requirements.
5. Extract the first core use cases from patient, medical record, medicine, and purchasing flows.
6. Build REST controllers only as adapters to those core use cases.
7. Move persistence into tenant-aware repository adapters.
8. Replace clinic/pharmacy-specific concepts with HIS concepts where needed.
9. Add missing HIS modules next: encounters, admissions, practitioners, departments, lab/radiology, billing, claims.
10. Implement audit and scheduler adapters when their first concrete workflows are ready.
11. Design security only after the core boundaries and tenant model are stable.

## Practical conclusion

The legacy application is still useful as a **source of domain knowledge**, especially for:

- patients
- medical records
- diagnoses
- procedures
- medicines
- prescriptions
- purchasing
- inventory
- reporting

But it should **not** be used as the architectural template for the new system.

The new system should be built as a **multi-tenant HIS backend platform** with:

- hexagonal boundaries
- isolated core logic
- REST as one adapter
- persistence as another adapter
- audit and scheduling as tenant-aware platform capabilities
- English contracts
- hospital-oriented domain modules

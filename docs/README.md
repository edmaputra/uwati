# Uwati HIS Documentation

Welcome to the technical documentation for **Uwati HIS**, a multi-tenant, modern backend platform for Hospital Information Systems built with Java 25, Spring Boot 4, PostgreSQL, and Hexagonal Architecture.

---

## Documentation Index

| Document | Purpose | Target Audience |
|---|---|---|
| **[Complete Project Walkthrough](project-walkthrough.md)** | **Master Guide**: Comprehensive end-to-end system architecture, module layout, multi-tenancy foundation, audit trail subsystem, database schema, REST API lifecycle, and step-by-step developer guide for adding new domain aggregates. | All developers, architects, and new contributors. |
| **[Tenant Management Walkthrough](tenant-management-walkthrough.md)** | **Tenancy Deep Dive**: Explores the tenant model (`TenantId`, `TenantStatus`), context scoping via Java 25 `ScopedValue`, automated bootstrap provisioning, and persistence isolation rules. | Backend engineers and platform operators. |
| **[Audit Trail Walkthrough](audit-trail-walkthrough.md)** | **Audit Subsystem**: Complete specification of the `Auditable` domain contract, in-memory `AuditDiffEngine`, clean JSON formatting via `AuditJsonFormatter`, and PostgreSQL `audit_entries` persistence. | Security auditors and backend engineers. |
| **[HIS Modernization & Architecture Map](legacy-controller-revamp-map.md)** | **Domain Roadmap & Strategy**: Strategic roadmap mapping legacy clinic/pharmacy capabilities to standard English enterprise HIS domains (Patients, Encounters, Clinical Records, Pharmacy, Billing, Claims). | System architects and product teams. |

---

## Quick Architecture Summary

```
his-domain       -> Pure Java 25 domain entities, ports, events, and invariants (Zero framework dependencies)
his-core         -> Application use cases, services, in-memory diff engine, and JSON formatters
his-rest         -> Inbound REST controllers, tenant filter, and RFC 7807 exception handling
his-persistence  -> Outbound JPA entities, Spring Data repositories, Liquibase migrations, event listeners
his-bootstrap    -> Application entry point, ScopedValue tenant context, wiring, and Testcontainers tests
```

---

## Quick Start Commands

```bash
# Start local PostgreSQL 17
docker compose up -d

# Build all modules and run full test suites
./mvnw clean verify

# Start Spring Boot application locally
./mvnw spring-boot:run -pl his-bootstrap
```

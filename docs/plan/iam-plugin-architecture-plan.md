# IAM Plugin Architecture & Implementation Plan

## 1. Overview & Architectural Goals

The **IAM (Identity and Access Management)** module is designed as a **self-contained, pluggable vertical module (`his-iam`)** for Uwati HIS. It provides authentication, role-based access control (RBAC), and hierarchical organizational scoping without tightly coupling the core HIS domain to any specific identity mechanism or provider.

### Key Architectural Goals
1. **Pluggable & Autonomous**: The module encapsulates its own domain models, use cases, REST endpoints, JPA persistence entities, security filters, pluggable authentication providers, and Liquibase database migrations.
2. **Pluggable Authentication Providers (SPI)**: All authentication methods (Local Password, OAuth2/OIDC, SAML 2.0, API Keys) reside internally within `his-iam` behind a common provider interface. Downstream HIS core modules (`his-core`, `his-domain`) depend only on generic security context ports (`CurrentActor`, `SecurityContext`).
3. **Unified Token Bridge**: Regardless of how an actor authenticates (Direct DB, Keycloak, Azure AD, SAML, or API Key), IAM resolves their identity, tenant memberships, and hierarchical scopes into a uniform **Uwati Session Token (JWT)**. Downstream services remain completely provider-agnostic.
4. **Global Identity with Multi-Tenant Memberships**: Supports both global **Platform Superadmins** (cross-tenant operators) and **Tenant-Scoped Users** who belong to one or more tenant organizations.
5. **Generic Hierarchical Scope Tree (Organizational Units)**: Supports arbitrary-depth organizational structures (Company $\rightarrow$ Division $\rightarrow$ Sub-Division $\rightarrow$ Department $\rightarrow$ Unit) with automatic downward subtree inheritance, without rigid or hardcoded level types.
6. **Zero-Configuration Inclusion**: Discovered and enabled via Spring Boot Auto-Configuration when the artifact is added to the application classpath.

---

## 2. System Architecture & Module Boundaries

Dependencies point inwards or to shared core contracts. HIS business logic remains transport- and security-vendor-agnostic.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              his-bootstrap                              │
│         (Wires applications, imports plugins, starts server)            │
└────────────────────┬───────────────────────────────┬────────────────────┘
                     │                               │
┌────────────────────▼──────────────────┐ ┌──────────▼──────────────────┐
│            his-iam (Plugin)           │ │      his-core & adapters    │
│  ┌─────────────────────────────────┐  │ │  - Patient, Clinical, etc.  │
│  │ Pluggable Auth Providers (SPI)  │  │ │  - Pure domain rules        │
│  │ ├─ Local DB (Email + Password)  │  │ │  - Consumes CurrentActor    │
│  │ ├─ OIDC (Keycloak, Azure AD)    │  │ │    from SecurityContext     │
│  │ ├─ SAML 2.0 (Hospital ADFS)     │  │ │                             │
│  │ └─ API Key (M2M / Lab Devices)  │  │ │                             │
│  └────────────────┬────────────────┘  │ │                             │
│                   ▼                   │ │                             │
│  ┌─────────────────────────────────┐  │ │                             │
│  │ Unified JWT Token Bridge        │  │ │                             │
│  └─────────────────────────────────┘  │ │                             │
│  - REST: /api/v1/auth, /iam           │ │                             │
│  - Scope Hierarchy & RBAC Engine      │ │                             │
│  - Migrations: db/changelog/iam/*     │ │                             │
└────────────────────┬──────────────────┘ └──────────┬──────────────────┘
                     │                               │
                     └───────────────┬───────────────┘
                                     ▼
                     ┌───────────────────────────────┐
                     │          his-domain           │
                     │  - TenantContext              │
                     │  - SecurityContext Port       │
                     │  - Auditable                  │
                     └───────────────────────────────┘
```

---

## 3. Domain & Data Model

### 3.1 Entity Relationship Diagram

```mermaid
erDiagram
    IAM_USER ||--o{ IAM_USER_IDENTITY : has_external
    IAM_USER ||--o{ IAM_USER_ROLE_ASSIGNMENT : has
    IAM_ROLE ||--o{ IAM_USER_ROLE_ASSIGNMENT : assigned_to
    IAM_ROLE ||--o{ IAM_ROLE_PERMISSION : contains
    IAM_SCOPE_NODE ||--o{ IAM_SCOPE_NODE : parent_of
    IAM_SCOPE_NODE ||--o{ IAM_USER_ROLE_ASSIGNMENT : scoped_to

    IAM_USER {
        uuid id PK
        varchar email UK
        varchar password_hash "Nullable for SSO-only users"
        varchar full_name
        varchar status
        boolean is_platform_superadmin
        timestamptz created_at
        timestamptz updated_at
    }

    IAM_USER_IDENTITY {
        uuid id PK
        uuid user_id FK
        varchar provider_type
        varchar external_subject_id
        varchar issuer_url
        timestamptz created_at
    }

    IAM_ROLE {
        uuid id PK
        uuid tenant_id "NULL for global roles"
        varchar code
        varchar name
        varchar description
        boolean is_system_role
    }

    IAM_ROLE_PERMISSION {
        uuid role_id PK, FK
        varchar permission PK
    }

    IAM_SCOPE_NODE {
        uuid id PK
        uuid tenant_id FK
        uuid parent_id FK "NULL for root nodes"
        varchar code
        varchar name
        varchar path "Materialized Lineage Path"
        timestamptz created_at
        timestamptz updated_at
    }

    IAM_USER_ROLE_ASSIGNMENT {
        uuid id PK
        uuid user_id FK
        uuid role_id FK
        uuid tenant_id FK "NULL for superadmin"
        uuid scope_node_id FK "NULL for tenant-wide"
        boolean inherit_children
        timestamptz created_at
    }
```

---

### 3.2 Database Schema Definitions

```sql
-- 1. Global User Accounts & Identities
CREATE TABLE iam_user (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),          -- NULL if user authenticates solely via external IdP
    full_name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, DEACTIVATED
    is_platform_superadmin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 2. Federated External Identities (OIDC / OAuth2 / SAML)
CREATE TABLE iam_user_identity (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES iam_user(id) ON DELETE CASCADE,
    provider_type VARCHAR(32) NOT NULL,  -- 'LOCAL', 'OIDC_KEYCLOAK', 'OIDC_AZURE', 'SAML_ADFS'
    external_subject_id VARCHAR(255) NOT NULL, -- sub claim or NameID from external IdP
    issuer_url VARCHAR(512),             -- IdP realm or issuer URL
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_iam_identity_provider UNIQUE (provider_type, external_subject_id)
);
CREATE INDEX idx_iam_identity_user ON iam_user_identity(user_id);

-- 3. Roles
CREATE TABLE iam_role (
    id UUID PRIMARY KEY,
    tenant_id UUID,                     -- NULL for system/global roles
    code VARCHAR(64) NOT NULL,          -- e.g. 'ADMIN', 'PHYSICIAN', 'DEPT_MANAGER'
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_iam_role_tenant_code UNIQUE (tenant_id, code)
);

-- 4. Role Permissions
CREATE TABLE iam_role_permission (
    role_id UUID NOT NULL REFERENCES iam_role(id) ON DELETE CASCADE,
    permission VARCHAR(64) NOT NULL,    -- e.g. 'PATIENT_READ', 'MEDICINE_WRITE'
    PRIMARY KEY (role_id, permission)
);

-- 5. Generic Hierarchical Scope Tree (Organizational Units)
CREATE TABLE iam_scope_node (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    parent_id UUID REFERENCES iam_scope_node(id) ON DELETE RESTRICT,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(512) NOT NULL,          -- e.g. '/<tenant_id>/<node_1_id>/<node_2_id>/'
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_iam_scope_node_tenant_code UNIQUE (tenant_id, code)
);
CREATE INDEX idx_iam_scope_node_path ON iam_scope_node(path varchar_pattern_ops);
CREATE INDEX idx_iam_scope_node_tenant ON iam_scope_node(tenant_id);

-- 6. User Role & Scope Assignments
CREATE TABLE iam_user_role_assignment (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES iam_user(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES iam_role(id) ON DELETE CASCADE,
    tenant_id UUID,                     -- NULL for platform superadmin
    scope_node_id UUID REFERENCES iam_scope_node(id) ON DELETE CASCADE, -- NULL = tenant-wide
    inherit_children BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_iam_user_assignment ON iam_user_role_assignment(user_id, tenant_id);
```

---

## 4. Hierarchical Scope Tree & Subtree Inheritance

### 4.1 Concept & Mechanics
To prevent rigid taxonomies (like hardcoded division/department enumerations), every organizational unit is modeled as a generic **Scope Node**.

- **Root Level**: Represents a top-level facility or headquarters branch under the tenant.
- **Intermediate Nodes**: Divisions, wings, departments, or clinical units at arbitrary nesting depth.
- **Subtree Inheritance (`path`)**:
  - The `path` field stores the materialized ancestry path (e.g. `/<tenant_id>/<node_A_id>/<node_B_id>/`).
  - When a user is assigned a role at Node $A$ with `inherit_children = true`, their effective scope includes Node $A$ and **all nodes whose path starts with Node $A$'s path**.

### 4.2 Subtree Resolution Example

```
Tenant: City Health System (ID: T-100)
├── [Node A] Medical Services Division (Path: /T-100/A/)
│     ├── [Node B] Surgical Sub-Division (Path: /T-100/A/B/)
│     │     ├── [Node C] General Surgery Dept (Path: /T-100/A/B/C/)
│     │     └── [Node D] Orthopedics Dept (Path: /T-100/A/B/D/)
│     └── [Node E] Internal Medicine Sub-Division (Path: /T-100/A/E/)
│           └── [Node F] Cardiology Dept (Path: /T-100/A/E/F/)
└── [Node G] Operations Division (Path: /T-100/G/)
      └── [Node H] Central Pharmacy (Path: /T-100/G/H/)
```

1. **CEO (Tenant-wide)**: Assigned at `scope_node_id = NULL` $\rightarrow$ Allowed scopes: **All Nodes**.
2. **Head of Medicine**: Assigned at `Node A` $\rightarrow$ Allowed scopes: **Nodes A, B, C, D, E, F**.
3. **Surgical Director**: Assigned at `Node B` $\rightarrow$ Allowed scopes: **Nodes B, C, D**.
4. **Cardiology Lead**: Assigned at `Node F` $\rightarrow$ Allowed scopes: **Node F only**.

---

## 5. Pluggable Authentication Architecture

### 5.1 The Pluggable Provider SPI Pattern

Inside `his-iam`, authentication is structured as a pluggable strategy. The authentication use case delegates to registered `AuthenticationProvider` implementations:

```mermaid
flowchart TD
    subgraph Inbound["Inbound Authentication"]
        Local["Local Password Auth<br>(Email + Password)"]
        OIDC["OIDC / OAuth 2.0<br>(Keycloak, Azure AD, Okta)"]
        SAML["SAML 2.0<br>(Hospital ADFS)"]
        ApiKey["API Key / mTLS<br>(Devices, Integrations)"]
    end

    subgraph IAM_SPI["his-iam Provider SPI Engine"]
        Router["AuthenticationProvider Router"]
        IdentityService["Identity Linker / JIT Provisioner"]
        ScopeEngine["Scope & Role Resolver"]
        JwtIssuer["Unified JWT Token Issuer"]
    end

    subgraph Security_Context["Domain Context Bridge"]
        Actor["CurrentActor & SecurityContext<br>(TenantContext Aligned)"]
    end

    Local --> Router
    OIDC --> Router
    SAML --> Router
    ApiKey --> Router

    Router --> IdentityService
    IdentityService --> ScopeEngine
    ScopeEngine --> JwtIssuer
    JwtIssuer --> Actor
```

#### Provider Interface Contract
```java
package io.github.edmaputra.uwati.iam.application.port.out;

public interface AuthenticationProvider {
    /** Identifies if this provider handles the credential type */
    boolean supports(AuthCredentialType credentialType);

    /** Verifies credentials against local DB or external IdP */
    AuthenticatedIdentity authenticate(AuthCredentials credentials);
}
```

### 5.2 Supported Provider Implementations (All Internal to `his-iam`)

1. **`LocalPasswordAuthProvider` (Baseline)**:
   - Authenticates email and password against `iam_user` using `BCryptPasswordEncoder`.
   - Ideal for standalone setups, local deployments, and **Platform Superadmin emergency access**.
2. **`OidcAuthProvider` (Enterprise SSO)**:
   - Validates OpenID Connect ID tokens / authorization codes from Keycloak, Azure AD, Okta, or Google Workspace.
   - Automatically performs Just-In-Time (JIT) user account linking or creation via `iam_user_identity`.
3. **`SamlAuthProvider` (Hospital Federation)**:
   - Consumes SAML 2.0 assertions from enterprise ADFS or national health federations.
4. **`ApiKeyAuthProvider` (Machine-to-Machine / M2M)**:
   - Validates `X-API-Key` headers for automated lab devices, radiology systems, or ETL sync jobs.

### 5.3 Unified JWT Token Bridge
Once any provider successfully validates an identity:
1. IAM loads the user's role assignments and computes effective scope paths / node IDs for the active tenant.
2. IAM issues a standard, signed **Uwati Access Token (JWT)** and **Refresh Token**.
3. **JWT Payload Structure**:
   ```json
   {
     "sub": "b2c9a101-7b08-4122-83b3-b5413cf4a401",
     "email": "doctor.alice@hospital.org",
     "tenantId": "c4d28e77-502a-4299-8cfb-665a3962b112",
     "isSuperAdmin": false,
     "roles": ["PHYSICIAN", "DEPT_MANAGER"],
     "permissions": ["PATIENT_READ", "PRESCRIPTION_CREATE", "MEDICAL_RECORD_WRITE"],
     "scopeNodeIds": ["a1b2c3d4-0000-0000-0000-000000000001", "a1b2c3d4-0000-0000-0000-000000000002"],
     "scopePaths": ["/c4d28e77/a1b2c3d4/"],
     "exp": 1740000000
   }
   ```
4. **Security Filter & Context Bridge**:
   - `JwtAuthenticationFilter` intercepts HTTP requests.
   - Validates signature and expiration.
   - Populates `SecurityContext` with a `CurrentActor` instance.
   - Synchronizes `TenantContext` to match the validated tenant claim.

### 5.4 The `CurrentActor` Port Definition (in `his-domain`)

```java
package io.github.edmaputra.uwati.domain.security;

import java.util.Set;
import java.util.UUID;

public interface CurrentActor {
    UUID userId();
    String email();
    UUID tenantId();
    boolean isPlatformSuperAdmin();
    boolean isTenantWide();
    Set<String> roles();
    Set<String> permissions();
    boolean hasPermission(String permission);
    boolean canAccessScope(UUID targetScopeNodeId);
    Set<UUID> accessibleScopeNodeIds();
}
```

---

## 6. Directory & Package Structure

The `his-iam` module is organized internally using clean/hexagonal conventions:

```
his-iam/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/io/github/edmaputra/uwati/iam/
    │   │   ├── IamAutoConfiguration.java
    │   │   ├── domain/
    │   │   │   ├── model/ (User, Role, ScopeNode, UserRoleAssignment, UserIdentity)
    │   │   │   └── exception/ (AuthenticationException, AccessDeniedException)
    │   │   ├── application/
    │   │   │   ├── port/in/ (AuthenticateUserUseCase, ManageUserUseCase, ManageScopeUseCase)
    │   │   │   ├── port/out/ (UserRepository, RoleRepository, ScopeNodeRepository, PasswordEncoderPort)
    │   │   │   └── service/ (AuthenticationService, ScopeHierarchyService, UserService)
    │   │   └── adapter/
    │   │       ├── rest/
    │   │       │   ├── AuthController.java
    │   │       │   ├── UserController.java
    │   │       │   ├── ScopeNodeController.java
    │   │       │   ├── RoleController.java
    │   │       │   └── dto/ (LoginRequest, TokenResponse, CreateScopeRequest, etc.)
    │   │       ├── persistence/
    │   │       │   ├── entity/ (UserJpaEntity, RoleJpaEntity, ScopeNodeJpaEntity, UserIdentityJpaEntity)
    │   │       │   ├── repository/ (SpringDataUserRepository, SpringDataScopeNodeRepository)
    │   │       │   └── adapter/ (UserRepositoryAdapter, ScopeNodeRepositoryAdapter)
    │   │       └── security/
    │   │           ├── provider/
    │   │           │   ├── AuthenticationProvider.java
    │   │           │   ├── LocalPasswordAuthProvider.java
    │   │           │   ├── OidcAuthProvider.java
    │   │           │   ├── SamlAuthProvider.java
    │   │           │   └── ApiKeyAuthProvider.java
    │   │           ├── jwt/
    │   │           │   ├── JwtTokenProvider.java
    │   │           │   └── JwtAuthenticationFilter.java
    │   │           ├── BCryptPasswordEncoderAdapter.java
    │   │           └── SecurityContextAccessor.java
    │   └── resources/
    │       ├── META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
    │       └── db/changelog/iam/
    │           ├── 2026082601-create-iam-tables.json
    │           └── db.changelog-iam.json
    └── test/
        └── java/io/github/edmaputra/uwati/iam/
            ├── domain/
            ├── application/
            ├── adapter/
            └── integration/
```

---

## 7. REST API Endpoints Specification

### 7.1 Authentication Endpoints (`/api/v1/auth`)
- `POST /api/v1/auth/login`: Authenticate with credentials (email/password or SSO token) and obtain tokens.
- `POST /api/v1/auth/refresh`: Exchange refresh token for a new access token.
- `GET /api/v1/auth/me`: Retrieve currently authenticated user profile, roles, and accessible scopes.

### 7.2 Scope Tree Management Endpoints (`/api/v1/iam/scopes`)
- `GET /api/v1/iam/scopes`: Fetch full hierarchical scope tree for the active tenant.
- `POST /api/v1/iam/scopes`: Create a new scope node (specifying `code`, `name`, optional `parentId`).
- `PUT /api/v1/iam/scopes/{id}`: Update node details.
- `DELETE /api/v1/iam/scopes/{id}`: Delete a node (rejects if child nodes or active assignments exist).

### 7.3 Role & Permission Endpoints (`/api/v1/iam/roles`)
- `GET /api/v1/iam/roles`: List available roles for current tenant.
- `POST /api/v1/iam/roles`: Create custom tenant role with permission set.
- `GET /api/v1/iam/permissions`: List all registered system permissions.

### 7.4 User & Role Assignment Endpoints (`/api/v1/iam/users`)
- `GET /api/v1/iam/users`: List users in tenant (or global users for superadmin).
- `POST /api/v1/iam/users`: Create/register a new user.
- `POST /api/v1/iam/users/{userId}/assignments`: Assign a role to a user at a target scope node.
- `DELETE /api/v1/iam/users/{userId}/assignments/{assignmentId}`: Revoke a role assignment.

---

## 8. Implementation Roadmap

### Phase 1: Module Scaffolding & Data Model
1. Create `his-iam` Maven module and register in parent `pom.xml`.
2. Write Liquibase migration `db/changelog/iam/2026082601-create-iam-tables.json` with user, identity, role, permission, scope node, and assignment tables.
3. Configure Liquibase modular changelog discovery.
4. Implement pure domain models: `User`, `UserIdentity`, `Role`, `Permission`, `ScopeNode`, `UserRoleAssignment`.

### Phase 2: Hierarchical Scope Tree & Subtree Resolver
1. Implement `ScopeNode` path generation algorithm (materialized path updates on creation and moves).
2. Implement `ScopeHierarchyService` to compute descendant subtree sets efficiently.
3. Write comprehensive unit tests for tree traversal and inheritance verification.

### Phase 3: Pluggable Authentication & Security Filter
1. Implement `AuthenticationProvider` SPI and `LocalPasswordAuthProvider` with `BCrypt`.
2. Implement JWT token provider (sign, parse, claims builder with scope paths).
3. Implement `JwtAuthenticationFilter` and `SecurityContext` / `CurrentActor` port bridge.
4. Implement `AuthenticateUserUseCase` and `/api/v1/auth/login` endpoint.

### Phase 4: Scoped RBAC & Management Endpoints
1. Implement role management and assignment use cases.
2. Implement REST controllers for Scopes, Roles, and Users.
3. Add request validation and error responses.

### Phase 5: Verification & Integration Testing
1. Add integration tests using Testcontainers (PostgreSQL).
2. Verify token generation, tenant context propagation, and scope tree filtering.
3. Verify modular auto-configuration when consumed from `his-bootstrap`.

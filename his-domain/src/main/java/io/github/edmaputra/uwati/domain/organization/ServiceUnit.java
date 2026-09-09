package io.github.edmaputra.uwati.domain.organization;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.audit.Auditable;
import io.github.edmaputra.uwati.domain.security.ScopeOwned;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantOwned;

public record ServiceUnit(
		ServiceUnitId id,
		TenantId tenantId,
		FacilityId facilityId,
		String code,
		String name,
		ServiceUnitType type,
		UUID scopeNodeId,
		ServiceUnitStatus status,
		Instant createdAt,
		Instant updatedAt) implements TenantOwned, ScopeOwned, Auditable {

	public ServiceUnit {
		Objects.requireNonNull(id, "Service Unit ID must not be null.");
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		Objects.requireNonNull(facilityId, "Facility ID must not be null.");
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Service Unit code must not be blank.");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Service Unit name must not be blank.");
		}
		Objects.requireNonNull(type, "Service Unit type must not be null.");
		Objects.requireNonNull(status, "Service Unit status must not be null.");
		Objects.requireNonNull(createdAt, "Service Unit creation timestamp must not be null.");
		Objects.requireNonNull(updatedAt, "Service Unit update timestamp must not be null.");
		if (updatedAt.isBefore(createdAt)) {
			throw new IllegalArgumentException("Service Unit update timestamp must not precede its creation timestamp.");
		}
	}

	public ServiceUnit update(String newName, ServiceUnitType newType, UUID newScopeNodeId) {
		return new ServiceUnit(
				id,
				tenantId,
				facilityId,
				code,
				newName,
				newType,
				newScopeNodeId,
				status,
				createdAt,
				Instant.now());
	}

	public ServiceUnit changeStatus(ServiceUnitStatus newStatus) {
		Objects.requireNonNull(newStatus, "New service unit status must not be null.");
		return new ServiceUnit(
				id,
				tenantId,
				facilityId,
				code,
				name,
				type,
				scopeNodeId,
				newStatus,
				createdAt,
				Instant.now());
	}

	public boolean isActive() {
		return status == ServiceUnitStatus.ACTIVE;
	}

	@Override
	public Map<String, Object> auditableFields() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("facilityId", facilityId.value().toString());
		fields.put("code", code);
		fields.put("name", name);
		fields.put("type", type.name());
		fields.put("scopeNodeId", scopeNodeId != null ? scopeNodeId.toString() : null);
		fields.put("status", status.name());
		return java.util.Collections.unmodifiableMap(fields);
	}
}

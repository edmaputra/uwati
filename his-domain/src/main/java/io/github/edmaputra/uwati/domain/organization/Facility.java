package io.github.edmaputra.uwati.domain.organization;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.audit.Auditable;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantOwned;

public record Facility(
		FacilityId id,
		TenantId tenantId,
		String code,
		String name,
		FacilityType type,
		FacilityClassification classification,
		String nationalRegistryCode,
		UUID scopeNodeId,
		String address,
		String phone,
		FacilityStatus status,
		Instant createdAt,
		Instant updatedAt) implements TenantOwned, Auditable {

	public Facility {
		Objects.requireNonNull(id, "Facility ID must not be null.");
		Objects.requireNonNull(tenantId, "Tenant ID must not be null.");
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Facility code must not be blank.");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Facility name must not be blank.");
		}
		Objects.requireNonNull(type, "Facility type must not be null.");
		Objects.requireNonNull(classification, "Facility classification must not be null.");
		Objects.requireNonNull(status, "Facility status must not be null.");
		Objects.requireNonNull(createdAt, "Facility creation timestamp must not be null.");
		Objects.requireNonNull(updatedAt, "Facility update timestamp must not be null.");
		if (updatedAt.isBefore(createdAt)) {
			throw new IllegalArgumentException("Facility update timestamp must not precede its creation timestamp.");
		}
	}

	public Facility update(
			String newName,
			FacilityType newType,
			FacilityClassification newClassification,
			String newNationalRegistryCode,
			UUID newScopeNodeId,
			String newAddress,
			String newPhone) {
		return new Facility(
				id,
				tenantId,
				code,
				newName,
				newType,
				newClassification,
				newNationalRegistryCode,
				newScopeNodeId,
				newAddress,
				newPhone,
				status,
				createdAt,
				Instant.now());
	}

	public Facility changeStatus(FacilityStatus newStatus) {
		Objects.requireNonNull(newStatus, "New facility status must not be null.");
		return new Facility(
				id,
				tenantId,
				code,
				name,
				type,
				classification,
				nationalRegistryCode,
				scopeNodeId,
				address,
				phone,
				newStatus,
				createdAt,
				Instant.now());
	}

	public boolean isActive() {
		return status == FacilityStatus.ACTIVE;
	}

	@Override
	public Map<String, Object> auditableFields() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("code", code);
		fields.put("name", name);
		fields.put("type", type.name());
		fields.put("classification", classification.name());
		fields.put("nationalRegistryCode", nationalRegistryCode);
		fields.put("scopeNodeId", scopeNodeId != null ? scopeNodeId.toString() : null);
		fields.put("address", address);
		fields.put("phone", phone);
		fields.put("status", status.name());
		return java.util.Collections.unmodifiableMap(fields);
	}
}

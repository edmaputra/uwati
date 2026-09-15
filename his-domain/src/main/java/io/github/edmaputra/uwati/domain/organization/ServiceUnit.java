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

/**
 * Root aggregate entity representing a service unit (department, clinic, ward, etc.) within a facility.
 * <p>
 * Manages operational service unit metadata, classification type, organizational scope, and status
 * within the core domain layer of the Hexagonal Architecture. Implements {@link TenantOwned},
 * {@link ScopeOwned}, and {@link Auditable}.
 *
 * @param id the unique identifier of the service unit
 * @param tenantId the identifier of the owning tenant
 * @param facilityId the identifier of the parent facility
 * @param code the unique facility-scoped code identifying the service unit
 * @param name the official name of the service unit
 * @param type the clinical or operational type of the unit
 * @param scopeNodeId the organizational hierarchy scope node identifier, may be {@code null}
 * @param status the operational lifecycle status
 * @param createdAt the timestamp when the service unit was created
 * @param updatedAt the timestamp when the service unit was last updated
 * @author edmaputra
 * @since 0.0.1
 */
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

	/**
	 * Compact constructor validating invariant constraints for a service unit entity.
	 *
	 * @param id the service unit ID
	 * @param tenantId the owning tenant ID
	 * @param facilityId the parent facility ID
	 * @param code the facility-scoped code
	 * @param name the service unit name
	 * @param type the service unit type
	 * @param scopeNodeId the scope node ID
	 * @param status the service unit status
	 * @param createdAt the creation timestamp
	 * @param updatedAt the update timestamp
	 * @throws NullPointerException if {@code id}, {@code tenantId}, {@code facilityId}, {@code type},
	 *                              {@code status}, {@code createdAt}, or {@code updatedAt} is null
	 * @throws IllegalArgumentException if {@code code} or {@code name} is null or blank,
	 *                                  or if {@code updatedAt} is before {@code createdAt}
	 */
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

	/**
	 * Creates an updated copy of this service unit with modified profile details and an updated timestamp.
	 *
	 * @param newName the updated service unit name
	 * @param newType the updated service unit type
	 * @param newScopeNodeId the updated scope node ID
	 * @return a new {@link ServiceUnit} instance reflecting the changes
	 */
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

	/**
	 * Creates an updated copy of this service unit with a new operational status and an updated timestamp.
	 *
	 * @param newStatus the new status to apply
	 * @return a new {@link ServiceUnit} instance with the updated status
	 * @throws NullPointerException if {@code newStatus} is null
	 */
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

	/**
	 * Checks whether the service unit is currently active.
	 *
	 * @return {@code true} if {@link #status()} is {@link ServiceUnitStatus#ACTIVE}, {@code false} otherwise
	 */
	public boolean isActive() {
		return status == ServiceUnitStatus.ACTIVE;
	}

	/**
	 * Returns the map of fields and values that are subject to audit logging.
	 *
	 * @return an unmodifiable map containing the service unit's auditable attributes
	 */
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

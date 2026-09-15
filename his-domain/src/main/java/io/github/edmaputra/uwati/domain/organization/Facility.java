package io.github.edmaputra.uwati.domain.organization;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.audit.Auditable;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantOwned;

/**
 * Root aggregate entity representing a healthcare facility (hospital, clinic, etc.) within an organization.
 * <p>
 * Manages clinical operational metadata, regulatory identifiers, geographic address, and lifecycle status
 * in the core domain layer of the Hexagonal Architecture. Implements {@link TenantOwned} for tenant scoping
 * and {@link Auditable} for audit logging.
 *
 * @param id the unique identifier of the facility
 * @param tenantId the identifier of the owning tenant
 * @param code the unique tenant-scoped code identifying the facility
 * @param name the official name of the facility
 * @param type the healthcare facility type (hospital, clinic, etc.)
 * @param classification the accreditation or government classification level
 * @param nationalRegistryCode the national or governmental registry identifier
 * @param scopeNodeId the organizational hierarchy scope node identifier, may be {@code null}
 * @param address the physical address of the facility, may be {@code null}
 * @param phone the contact telephone number, may be {@code null}
 * @param status the operational lifecycle status
 * @param createdAt the timestamp when the facility was created
 * @param updatedAt the timestamp when the facility was last updated
 * @author edmaputra
 * @since 0.0.1
 */
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

	/**
	 * Compact constructor validating invariant constraints for a facility entity.
	 *
	 * @param id the facility ID
	 * @param tenantId the owning tenant ID
	 * @param code the unique facility code
	 * @param name the facility name
	 * @param type the facility type
	 * @param classification the facility classification
	 * @param nationalRegistryCode the national registry code
	 * @param scopeNodeId the scope node ID
	 * @param address the facility address
	 * @param phone the facility phone
	 * @param status the facility status
	 * @param createdAt the creation timestamp
	 * @param updatedAt the last update timestamp
	 * @throws NullPointerException if {@code id}, {@code tenantId}, {@code type}, {@code classification},
	 *                              {@code status}, {@code createdAt}, or {@code updatedAt} is null
	 * @throws IllegalArgumentException if {@code code} or {@code name} is null or blank,
	 *                                  or if {@code updatedAt} is before {@code createdAt}
	 */
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

	/**
	 * Creates an updated copy of this facility with modified profile details and an updated timestamp.
	 *
	 * @param newName the updated name of the facility
	 * @param newType the updated facility type
	 * @param newClassification the updated classification
	 * @param newNationalRegistryCode the updated national registry code
	 * @param newScopeNodeId the updated scope node ID
	 * @param newAddress the updated address
	 * @param newPhone the updated phone number
	 * @return a new {@link Facility} instance reflecting the changes
	 */
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

	/**
	 * Creates an updated copy of this facility with a new operational status and an updated timestamp.
	 *
	 * @param newStatus the new status to apply
	 * @return a new {@link Facility} instance with the updated status
	 * @throws NullPointerException if {@code newStatus} is null
	 */
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

	/**
	 * Checks whether the facility is currently active.
	 *
	 * @return {@code true} if {@link #status()} is {@link FacilityStatus#ACTIVE}, {@code false} otherwise
	 */
	public boolean isActive() {
		return status == FacilityStatus.ACTIVE;
	}

	/**
	 * Returns the map of fields and values that are subject to audit logging.
	 *
	 * @return an unmodifiable map containing the facility's auditable attributes
	 */
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

package io.github.edmaputra.uwati.domain.tenancy.domain;

/**
 * Represents the lifecycle status of a tenant within the domain.
 * <p>
 * Governs tenant access and operational availability in the core domain layer
 * of the Hexagonal Architecture.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public enum TenantStatus {

	ACTIVE,
	SUSPENDED,
	DEACTIVATED
}

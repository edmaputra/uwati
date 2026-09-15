package io.github.edmaputra.uwati.adapter.rest.organization;

import java.time.Instant;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

/**
 * Response payload representing healthcare service unit details.
 *
 * @param id service unit unique identifier
 * @param tenantId owning tenant identifier
 * @param facilityId parent facility identifier
 * @param code unique service unit code within tenant
 * @param name official service unit name
 * @param type functional unit classification
 * @param scopeNodeId hierarchical organizational scope node ID
 * @param status operational status
 * @param createdAt record creation timestamp
 * @param updatedAt record last update timestamp
 * @author edmaputra
 * @since 0.0.1
 */
public record ServiceUnitResponse(
		UUID id,
		UUID tenantId,
		UUID facilityId,
		String code,
		String name,
		ServiceUnitType type,
		UUID scopeNodeId,
		ServiceUnitStatus status,
		Instant createdAt,
		Instant updatedAt) {

	/**
	 * Maps a domain ServiceUnit entity to a REST response DTO.
	 *
	 * @param unit the domain service unit entity
	 * @return REST response DTO
	 */
	public static ServiceUnitResponse from(ServiceUnit unit) {
		return new ServiceUnitResponse(
				unit.id().value(),
				unit.tenantId().value(),
				unit.facilityId().value(),
				unit.code(),
				unit.name(),
				unit.type(),
				unit.scopeNodeId(),
				unit.status(),
				unit.createdAt(),
				unit.updatedAt());
	}
}

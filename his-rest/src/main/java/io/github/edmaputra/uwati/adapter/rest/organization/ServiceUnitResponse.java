package io.github.edmaputra.uwati.adapter.rest.organization;

import java.time.Instant;
import java.util.UUID;

import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

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

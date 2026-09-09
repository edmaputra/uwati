package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

public interface FindServiceUnitUseCase {

	Optional<ServiceUnit> findById(ServiceUnitId id);

	List<ServiceUnit> findByFacilityId(FacilityId facilityId, ServiceUnitType type, ServiceUnitStatus status);
}

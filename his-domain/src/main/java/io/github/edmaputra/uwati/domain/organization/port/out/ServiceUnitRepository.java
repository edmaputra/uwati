package io.github.edmaputra.uwati.domain.organization.port.out;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;

public interface ServiceUnitRepository {

	ServiceUnit save(ServiceUnit serviceUnit);

	Optional<ServiceUnit> findById(ServiceUnitId id);

	Optional<ServiceUnit> findByFacilityIdAndCode(FacilityId facilityId, String code);

	boolean existsByFacilityIdAndCode(FacilityId facilityId, String code);

	List<ServiceUnit> findByFacilityId(FacilityId facilityId, ServiceUnitType type, ServiceUnitStatus status);
}

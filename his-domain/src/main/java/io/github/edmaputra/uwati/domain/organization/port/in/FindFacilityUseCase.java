package io.github.edmaputra.uwati.domain.organization.port.in;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;

public interface FindFacilityUseCase {

	Optional<Facility> findById(FacilityId id);

	List<Facility> findAll(FacilityType type, FacilityStatus status);
}

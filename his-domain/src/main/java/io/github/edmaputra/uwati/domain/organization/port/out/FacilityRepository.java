package io.github.edmaputra.uwati.domain.organization.port.out;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;

public interface FacilityRepository {

	Facility save(Facility facility);

	Optional<Facility> findById(FacilityId id);

	Optional<Facility> findByCode(String code);

	boolean existsByCode(String code);

	List<Facility> findAll(FacilityType type, FacilityStatus status);
}

package io.github.edmaputra.uwati.domain.organization;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacilityTests {

	private final TenantId tenantId = TenantId.generate();
	private final FacilityId facilityId = FacilityId.generate();
	private final Instant now = Instant.now();

	@Test
	@DisplayName("Should create facility successfully with valid parameters")
	void shouldCreateFacility() {
		UUID scopeNodeId = UUID.randomUUID();
		Facility facility = new Facility(
				facilityId,
				tenantId,
				"FAC-01",
				"RS Uwati Pusat",
				FacilityType.HOSPITAL,
				FacilityClassification.CLASS_B,
				"3171012",
				scopeNodeId,
				"Jl. Sudirman No. 1",
				"021-5551234",
				FacilityStatus.ACTIVE,
				now,
				now);

		assertThat(facility.id()).isEqualTo(facilityId);
		assertThat(facility.tenantId()).isEqualTo(tenantId);
		assertThat(facility.code()).isEqualTo("FAC-01");
		assertThat(facility.name()).isEqualTo("RS Uwati Pusat");
		assertThat(facility.type()).isEqualTo(FacilityType.HOSPITAL);
		assertThat(facility.classification()).isEqualTo(FacilityClassification.CLASS_B);
		assertThat(facility.nationalRegistryCode()).isEqualTo("3171012");
		assertThat(facility.scopeNodeId()).isEqualTo(scopeNodeId);
		assertThat(facility.address()).isEqualTo("Jl. Sudirman No. 1");
		assertThat(facility.phone()).isEqualTo("021-5551234");
		assertThat(facility.status()).isEqualTo(FacilityStatus.ACTIVE);
		assertThat(facility.isActive()).isTrue();

		Map<String, Object> auditable = facility.auditableFields();
		assertThat(auditable).containsEntry("code", "FAC-01")
				.containsEntry("name", "RS Uwati Pusat")
				.containsEntry("type", "HOSPITAL")
				.containsEntry("classification", "CLASS_B")
				.containsEntry("status", "ACTIVE");
	}

	@Test
	@DisplayName("Should throw exception when facility code is blank")
	void shouldThrowWhenCodeIsBlank() {
		assertThatThrownBy(() -> new Facility(
				facilityId,
				tenantId,
				"  ",
				"RS Uwati Pusat",
				FacilityType.HOSPITAL,
				FacilityClassification.CLASS_B,
				null,
				null,
				null,
				null,
				FacilityStatus.ACTIVE,
				now,
				now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Facility code must not be blank");
	}

	@Test
	@DisplayName("Should throw exception when facility name is blank")
	void shouldThrowWhenNameIsBlank() {
		assertThatThrownBy(() -> new Facility(
				facilityId,
				tenantId,
				"FAC-01",
				"",
				FacilityType.HOSPITAL,
				FacilityClassification.CLASS_B,
				null,
				null,
				null,
				null,
				FacilityStatus.ACTIVE,
				now,
				now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Facility name must not be blank");
	}

	@Test
	@DisplayName("Should update facility details and advance updatedAt")
	void shouldUpdateFacility() {
		Facility initial = new Facility(
				facilityId,
				tenantId,
				"FAC-01",
				"RS Uwati Pusat",
				FacilityType.HOSPITAL,
				FacilityClassification.CLASS_B,
				null,
				null,
				null,
				null,
				FacilityStatus.ACTIVE,
				now,
				now);

		Facility updated = initial.update(
				"RS Uwati Pusat Renovation",
				FacilityType.HOSPITAL,
				FacilityClassification.CLASS_A,
				"3171012-NEW",
				null,
				"Jl. Sudirman No. 10",
				"021-9998888");

		assertThat(updated.name()).isEqualTo("RS Uwati Pusat Renovation");
		assertThat(updated.classification()).isEqualTo(FacilityClassification.CLASS_A);
		assertThat(updated.address()).isEqualTo("Jl. Sudirman No. 10");
		assertThat(updated.updatedAt()).isAfterOrEqualTo(initial.updatedAt());
	}

	@Test
	@DisplayName("Should change status to INACTIVE")
	void shouldChangeStatus() {
		Facility facility = new Facility(
				facilityId,
				tenantId,
				"FAC-01",
				"RS Uwati Pusat",
				FacilityType.HOSPITAL,
				FacilityClassification.CLASS_B,
				null,
				null,
				null,
				null,
				FacilityStatus.ACTIVE,
				now,
				now);

		Facility deactivated = facility.changeStatus(FacilityStatus.INACTIVE);
		assertThat(deactivated.status()).isEqualTo(FacilityStatus.INACTIVE);
		assertThat(deactivated.isActive()).isFalse();
	}
}

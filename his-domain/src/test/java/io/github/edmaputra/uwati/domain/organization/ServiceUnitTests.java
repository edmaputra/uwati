package io.github.edmaputra.uwati.domain.organization;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceUnitTests {

	private final TenantId tenantId = TenantId.generate();
	private final FacilityId facilityId = FacilityId.generate();
	private final ServiceUnitId serviceUnitId = ServiceUnitId.generate();
	private final Instant now = Instant.now();

	@Test
	@DisplayName("Should create service unit successfully with valid parameters")
	void shouldCreateServiceUnit() {
		UUID scopeNodeId = UUID.randomUUID();
		ServiceUnit unit = new ServiceUnit(
				serviceUnitId,
				tenantId,
				facilityId,
				"POLI-INT",
				"Poli Penyakit Dalam",
				ServiceUnitType.OUTPATIENT_CLINIC,
				scopeNodeId,
				ServiceUnitStatus.ACTIVE,
				now,
				now);

		assertThat(unit.id()).isEqualTo(serviceUnitId);
		assertThat(unit.tenantId()).isEqualTo(tenantId);
		assertThat(unit.facilityId()).isEqualTo(facilityId);
		assertThat(unit.code()).isEqualTo("POLI-INT");
		assertThat(unit.name()).isEqualTo("Poli Penyakit Dalam");
		assertThat(unit.type()).isEqualTo(ServiceUnitType.OUTPATIENT_CLINIC);
		assertThat(unit.scopeNodeId()).isEqualTo(scopeNodeId);
		assertThat(unit.status()).isEqualTo(ServiceUnitStatus.ACTIVE);
		assertThat(unit.isActive()).isTrue();

		Map<String, Object> auditable = unit.auditableFields();
		assertThat(auditable).containsEntry("code", "POLI-INT")
				.containsEntry("name", "Poli Penyakit Dalam")
				.containsEntry("type", "OUTPATIENT_CLINIC")
				.containsEntry("status", "ACTIVE");
	}

	@Test
	@DisplayName("Should throw exception when service unit code is blank")
	void shouldThrowWhenCodeIsBlank() {
		assertThatThrownBy(() -> new ServiceUnit(
				serviceUnitId,
				tenantId,
				facilityId,
				"  ",
				"Poli Penyakit Dalam",
				ServiceUnitType.OUTPATIENT_CLINIC,
				null,
				ServiceUnitStatus.ACTIVE,
				now,
				now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Service Unit code must not be blank");
	}

	@Test
	@DisplayName("Should update service unit details")
	void shouldUpdateServiceUnit() {
		ServiceUnit initial = new ServiceUnit(
				serviceUnitId,
				tenantId,
				facilityId,
				"POLI-INT",
				"Poli Penyakit Dalam",
				ServiceUnitType.OUTPATIENT_CLINIC,
				null,
				ServiceUnitStatus.ACTIVE,
				now,
				now);

		UUID newScopeId = UUID.randomUUID();
		ServiceUnit updated = initial.update("Klinik Penyakit Dalam", ServiceUnitType.OUTPATIENT_CLINIC, newScopeId);

		assertThat(updated.name()).isEqualTo("Klinik Penyakit Dalam");
		assertThat(updated.scopeNodeId()).isEqualTo(newScopeId);
		assertThat(updated.updatedAt()).isAfterOrEqualTo(initial.updatedAt());
	}

	@Test
	@DisplayName("Should change service unit status to INACTIVE")
	void shouldChangeStatus() {
		ServiceUnit unit = new ServiceUnit(
				serviceUnitId,
				tenantId,
				facilityId,
				"POLI-INT",
				"Poli Penyakit Dalam",
				ServiceUnitType.OUTPATIENT_CLINIC,
				null,
				ServiceUnitStatus.ACTIVE,
				now,
				now);

		ServiceUnit deactivated = unit.changeStatus(ServiceUnitStatus.INACTIVE);
		assertThat(deactivated.status()).isEqualTo(ServiceUnitStatus.INACTIVE);
		assertThat(deactivated.isActive()).isFalse();
	}
}

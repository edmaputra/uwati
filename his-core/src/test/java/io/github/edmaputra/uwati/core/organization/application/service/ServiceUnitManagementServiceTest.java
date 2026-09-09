package io.github.edmaputra.uwati.core.organization.application.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.domain.organization.DuplicateServiceUnitCodeException;
import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityNotFoundException;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitNotFoundException;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;
import io.github.edmaputra.uwati.domain.organization.event.FacilityCreated;
import io.github.edmaputra.uwati.domain.organization.event.FacilityStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.FacilityUpdated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitCreated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitUpdated;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeServiceUnitStatusCommand;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateServiceUnitCommand;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateServiceUnitCommand;
import io.github.edmaputra.uwati.domain.organization.port.out.FacilityRepository;
import io.github.edmaputra.uwati.domain.organization.port.out.OrganizationEventPublisher;
import io.github.edmaputra.uwati.domain.organization.port.out.ServiceUnitRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.TenantContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceUnitManagementServiceTest {

	private final TenantId tenantId = TenantId.generate();
	private final FacilityId facilityId = FacilityId.generate();
	private InMemoryFacilityRepository facilityRepository;
	private InMemoryServiceUnitRepository serviceUnitRepository;
	private CapturingOrganizationEventPublisher eventPublisher;
	private ServiceUnitManagementService service;
	private final OperationContext context = OperationContext.of("admin", "corr-456");

	@BeforeEach
	void setUp() {
		facilityRepository = new InMemoryFacilityRepository();
		serviceUnitRepository = new InMemoryServiceUnitRepository();
		eventPublisher = new CapturingOrganizationEventPublisher();
		TenantContext tenantContext = () -> Optional.of(tenantId);

		// Seed a facility
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
				Instant.now(),
				Instant.now());
		facilityRepository.save(facility);

		service = new ServiceUnitManagementService(tenantContext, facilityRepository, serviceUnitRepository, eventPublisher);
	}

	@Test
	@DisplayName("Should create service unit and publish ServiceUnitCreated event")
	void shouldCreateServiceUnit() {
		UUID scopeNodeId = UUID.randomUUID();
		CreateServiceUnitCommand command = new CreateServiceUnitCommand(
				facilityId,
				"POLI-INT",
				"Poli Penyakit Dalam",
				ServiceUnitType.OUTPATIENT_CLINIC,
				scopeNodeId);

		ServiceUnit created = service.execute(command, context);

		assertThat(created).isNotNull();
		assertThat(created.facilityId()).isEqualTo(facilityId);
		assertThat(created.code()).isEqualTo("POLI-INT");
		assertThat(created.name()).isEqualTo("Poli Penyakit Dalam");
		assertThat(created.type()).isEqualTo(ServiceUnitType.OUTPATIENT_CLINIC);
		assertThat(created.scopeNodeId()).isEqualTo(scopeNodeId);
		assertThat(created.status()).isEqualTo(ServiceUnitStatus.ACTIVE);

		assertThat(eventPublisher.publishedEvents).hasSize(1);
		assertThat(eventPublisher.publishedEvents.getFirst()).isInstanceOf(ServiceUnitCreated.class);
	}

	@Test
	@DisplayName("Should throw FacilityNotFoundException when facility does not exist")
	void shouldThrowWhenFacilityNotFound() {
		CreateServiceUnitCommand command = new CreateServiceUnitCommand(
				FacilityId.generate(),
				"POLI-INT",
				"Poli Penyakit Dalam",
				ServiceUnitType.OUTPATIENT_CLINIC,
				null);

		assertThatThrownBy(() -> service.execute(command, context))
				.isInstanceOf(FacilityNotFoundException.class);
	}

	@Test
	@DisplayName("Should throw DuplicateServiceUnitCodeException when code exists in facility")
	void shouldThrowDuplicateCode() {
		CreateServiceUnitCommand command = new CreateServiceUnitCommand(
				facilityId,
				"POLI-INT",
				"Poli Penyakit Dalam",
				ServiceUnitType.OUTPATIENT_CLINIC,
				null);

		service.execute(command, context);

		assertThatThrownBy(() -> service.execute(command, context))
				.isInstanceOf(DuplicateServiceUnitCodeException.class)
				.hasMessageContaining("POLI-INT");
	}

	@Test
	@DisplayName("Should update service unit details")
	void shouldUpdateServiceUnit() {
		ServiceUnit created = service.execute(new CreateServiceUnitCommand(
				facilityId,
				"POLI-INT",
				"Poli Penyakit Dalam",
				ServiceUnitType.OUTPATIENT_CLINIC,
				null), context);

		UUID newScope = UUID.randomUUID();
		UpdateServiceUnitCommand updateCommand = new UpdateServiceUnitCommand(
				created.id(),
				"Klinik Penyakit Dalam Dewasa",
				ServiceUnitType.OUTPATIENT_CLINIC,
				newScope);

		ServiceUnit updated = service.execute(updateCommand, context);
		assertThat(updated.name()).isEqualTo("Klinik Penyakit Dalam Dewasa");
		assertThat(updated.scopeNodeId()).isEqualTo(newScope);

		assertThat(eventPublisher.publishedEvents).hasSize(2);
		assertThat(eventPublisher.publishedEvents.get(1)).isInstanceOf(ServiceUnitUpdated.class);
	}

	@Test
	@DisplayName("Should change service unit status")
	void shouldChangeStatus() {
		ServiceUnit created = service.execute(new CreateServiceUnitCommand(
				facilityId,
				"POLI-INT",
				"Poli Penyakit Dalam",
				ServiceUnitType.OUTPATIENT_CLINIC,
				null), context);

		ServiceUnit changed = service.execute(new ChangeServiceUnitStatusCommand(created.id(), ServiceUnitStatus.INACTIVE), context);
		assertThat(changed.status()).isEqualTo(ServiceUnitStatus.INACTIVE);

		assertThat(eventPublisher.publishedEvents).hasSize(2);
		assertThat(eventPublisher.publishedEvents.get(1)).isInstanceOf(ServiceUnitStatusChanged.class);
	}

	@Test
	@DisplayName("Should throw ServiceUnitNotFoundException when updating non-existent unit")
	void shouldThrowWhenNotFound() {
		UpdateServiceUnitCommand updateCommand = new UpdateServiceUnitCommand(
				ServiceUnitId.generate(),
				"Poli Unknown",
				ServiceUnitType.OUTPATIENT_CLINIC,
				null);

		assertThatThrownBy(() -> service.execute(updateCommand, context))
				.isInstanceOf(ServiceUnitNotFoundException.class);
	}

	private static final class InMemoryFacilityRepository implements FacilityRepository {
		private final List<Facility> facilities = new ArrayList<>();

		@Override
		public Facility save(Facility facility) {
			facilities.removeIf(f -> f.id().equals(facility.id()));
			facilities.add(facility);
			return facility;
		}

		@Override
		public Optional<Facility> findById(FacilityId id) {
			return facilities.stream().filter(f -> f.id().equals(id)).findFirst();
		}

		@Override
		public Optional<Facility> findByCode(String code) {
			return facilities.stream().filter(f -> f.code().equalsIgnoreCase(code)).findFirst();
		}

		@Override
		public boolean existsByCode(String code) {
			return facilities.stream().anyMatch(f -> f.code().equalsIgnoreCase(code));
		}

		@Override
		public List<Facility> findAll(FacilityType type, FacilityStatus status) {
			return facilities.stream()
					.filter(f -> type == null || f.type() == type)
					.filter(f -> status == null || f.status() == status)
					.toList();
		}
	}

	private static final class InMemoryServiceUnitRepository implements ServiceUnitRepository {
		private final List<ServiceUnit> units = new ArrayList<>();

		@Override
		public ServiceUnit save(ServiceUnit serviceUnit) {
			units.removeIf(u -> u.id().equals(serviceUnit.id()));
			units.add(serviceUnit);
			return serviceUnit;
		}

		@Override
		public Optional<ServiceUnit> findById(ServiceUnitId id) {
			return units.stream().filter(u -> u.id().equals(id)).findFirst();
		}

		@Override
		public Optional<ServiceUnit> findByFacilityIdAndCode(FacilityId facilityId, String code) {
			return units.stream()
					.filter(u -> u.facilityId().equals(facilityId) && u.code().equalsIgnoreCase(code))
					.findFirst();
		}

		@Override
		public boolean existsByFacilityIdAndCode(FacilityId facilityId, String code) {
			return units.stream()
					.anyMatch(u -> u.facilityId().equals(facilityId) && u.code().equalsIgnoreCase(code));
		}

		@Override
		public List<ServiceUnit> findByFacilityId(FacilityId facilityId, ServiceUnitType type, ServiceUnitStatus status) {
			return units.stream()
					.filter(u -> u.facilityId().equals(facilityId))
					.filter(u -> type == null || u.type() == type)
					.filter(u -> status == null || u.status() == status)
					.toList();
		}
	}

	private static final class CapturingOrganizationEventPublisher implements OrganizationEventPublisher {
		private final List<Object> publishedEvents = new ArrayList<>();

		@Override
		public void publish(FacilityCreated event) {
			publishedEvents.add(event);
		}

		@Override
		public void publish(FacilityUpdated event) {
			publishedEvents.add(event);
		}

		@Override
		public void publish(FacilityStatusChanged event) {
			publishedEvents.add(event);
		}

		@Override
		public void publish(ServiceUnitCreated event) {
			publishedEvents.add(event);
		}

		@Override
		public void publish(ServiceUnitUpdated event) {
			publishedEvents.add(event);
		}

		@Override
		public void publish(ServiceUnitStatusChanged event) {
			publishedEvents.add(event);
		}
	}
}

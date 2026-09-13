package io.github.edmaputra.uwati.core.organization.application.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.domain.organization.DuplicateFacilityCodeException;
import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityClassification;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityNotFoundException;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import io.github.edmaputra.uwati.domain.organization.event.FacilityCreated;
import io.github.edmaputra.uwati.domain.organization.event.FacilityStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.FacilityUpdated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitCreated;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitStatusChanged;
import io.github.edmaputra.uwati.domain.organization.event.ServiceUnitUpdated;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeFacilityStatusCommand;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateFacilityCommand;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateFacilityCommand;
import io.github.edmaputra.uwati.domain.organization.port.out.FacilityRepository;
import io.github.edmaputra.uwati.domain.organization.port.out.OrganizationEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.TenantContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacilityManagementServiceTest {

	private final TenantId tenantId = TenantId.generate();
	private InMemoryFacilityRepository repository;
	private CapturingOrganizationEventPublisher eventPublisher;
	private FacilityManagementService service;
	private final OperationContext context = OperationContext.of("admin", "corr-123");

	@BeforeEach
	void setUp() {
		repository = new InMemoryFacilityRepository();
		eventPublisher = new CapturingOrganizationEventPublisher();
		TenantContext tenantContext = () -> Optional.of(tenantId);
		service = new FacilityManagementService(tenantContext, repository, eventPublisher);
	}

	@Test
	@DisplayName("Should create facility and publish FacilityCreated event")
	void shouldCreateFacility() {
		CreateFacilityCommand command = new CreateFacilityCommand(
				"FAC-01",
				"RS Uwati Pusat",
				FacilityType.HOSPITAL,
				FacilityClassification.CLASS_B,
				"3171012",
				null,
				"Jl. Sudirman No. 1",
				"021-5551234");

		Facility created = service.execute(command, context);

		assertThat(created).isNotNull();
		assertThat(created.code()).isEqualTo("FAC-01");
		assertThat(created.name()).isEqualTo("RS Uwati Pusat");
		assertThat(created.status()).isEqualTo(FacilityStatus.ACTIVE);
		assertThat(repository.findById(created.id())).isPresent();

		assertThat(eventPublisher.publishedEvents).hasSize(1);
		assertThat(eventPublisher.publishedEvents.getFirst()).isInstanceOf(FacilityCreated.class);
		FacilityCreated event = (FacilityCreated) eventPublisher.publishedEvents.getFirst();
		assertThat(event.facility().id()).isEqualTo(created.id());
		assertThat(event.actor()).isEqualTo("admin");
	}

	@Test
	@DisplayName("Should throw DuplicateFacilityCodeException when code exists")
	void shouldThrowDuplicateCode() {
		CreateFacilityCommand command = new CreateFacilityCommand(
				"FAC-01",
				"RS Uwati Pusat",
				FacilityType.HOSPITAL,
				FacilityClassification.CLASS_B,
				null,
				null,
				null,
				null);

		service.execute(command, context);

		assertThatThrownBy(() -> service.execute(command, context))
				.isInstanceOf(DuplicateFacilityCodeException.class)
				.hasMessageContaining("FAC-01");
	}

	@Test
	@DisplayName("Should update facility details and publish FacilityUpdated event")
	void shouldUpdateFacility() {
		Facility initial = service.execute(new CreateFacilityCommand(
				"FAC-01",
				"RS Uwati Pusat",
				FacilityType.HOSPITAL,
				FacilityClassification.CLASS_B,
				null,
				null,
				null,
				null), context);

		UpdateFacilityCommand updateCommand = new UpdateFacilityCommand(
				initial.id(),
				"RS Uwati Pusat Modern",
				FacilityType.HOSPITAL,
				FacilityClassification.CLASS_A,
				"3171012",
				UUID.randomUUID(),
				"Jl. Sudirman No. 5",
				"021-9999999");

		Facility updated = service.execute(updateCommand, context);

		assertThat(updated.name()).isEqualTo("RS Uwati Pusat Modern");
		assertThat(updated.classification()).isEqualTo(FacilityClassification.CLASS_A);
		assertThat(eventPublisher.publishedEvents).hasSize(2);
		assertThat(eventPublisher.publishedEvents.get(1)).isInstanceOf(FacilityUpdated.class);
	}

	@Test
	@DisplayName("Should change facility status and publish FacilityStatusChanged event")
	void shouldChangeStatus() {
		Facility initial = service.execute(new CreateFacilityCommand(
				"FAC-01",
				"RS Uwati Pusat",
				FacilityType.HOSPITAL,
				FacilityClassification.CLASS_B,
				null,
				null,
				null,
				null), context);

		Facility deactivated = service.execute(new ChangeFacilityStatusCommand(initial.id(), FacilityStatus.INACTIVE), context);

		assertThat(deactivated.status()).isEqualTo(FacilityStatus.INACTIVE);
		assertThat(eventPublisher.publishedEvents).hasSize(2);
		assertThat(eventPublisher.publishedEvents.get(1)).isInstanceOf(FacilityStatusChanged.class);
	}

	@Test
	@DisplayName("Should throw FacilityNotFoundException when updating non-existent facility")
	void shouldThrowWhenNotFound() {
		UpdateFacilityCommand updateCommand = new UpdateFacilityCommand(
				FacilityId.generate(),
				"RS Unknown",
				FacilityType.CLINIC,
				FacilityClassification.NONE,
				null,
				null,
				null,
				null);

		assertThatThrownBy(() -> service.execute(updateCommand, context))
				.isInstanceOf(FacilityNotFoundException.class);
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

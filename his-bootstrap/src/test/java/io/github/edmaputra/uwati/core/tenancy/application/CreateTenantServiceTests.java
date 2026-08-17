package io.github.edmaputra.uwati.core.tenancy.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.core.tenancy.application.port.in.CreateTenantCommand;
import io.github.edmaputra.uwati.core.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.core.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.core.tenancy.application.service.CreateTenantService;
import io.github.edmaputra.uwati.core.tenancy.domain.DuplicateTenantDisplayNameException;
import io.github.edmaputra.uwati.core.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.core.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.core.tenancy.domain.TenantStatus;
import io.github.edmaputra.uwati.core.tenancy.domain.event.TenantCreated;

class CreateTenantServiceTests {

	@Test
	void createsAnActiveTenantWithGeneratedIdentity() {
		TenantRepository repository = new InMemoryTenantRepository();
		CapturingEventPublisher publisher = new CapturingEventPublisher();
		CreateTenantService service = new CreateTenantService(repository, publisher);

		Tenant tenant = service.execute(new CreateTenantCommand("Uwati Health Services Ltd.", "Uwati Health"));

		assertThat(tenant.id()).isNotNull();
		assertThat(tenant.legalName()).isEqualTo("Uwati Health Services Ltd.");
		assertThat(tenant.displayName()).isEqualTo("Uwati Health");
		assertThat(tenant.status()).isEqualTo(TenantStatus.ACTIVE);
		assertThat(tenant.createdAt()).isNotNull();
		assertThat(tenant.updatedAt()).isEqualTo(tenant.createdAt());
		assertThat(repository.findById(tenant.id())).contains(tenant);

		assertThat(publisher.events()).hasSize(1);
		TenantCreated event = publisher.events().get(0);
		assertThat(event.tenant()).isEqualTo(tenant);
		assertThat(event.occurredAt()).isNotNull();
	}

	@Test
	void rejectsEmptyRequiredNames() {
		CreateTenantService service =
				new CreateTenantService(new InMemoryTenantRepository(), e -> {});

		assertThatIllegalArgumentException().isThrownBy(() -> service.execute(new CreateTenantCommand(" ", "Uwati Health")))
				.withMessage("Tenant legal name must not be blank.");
		assertThatIllegalArgumentException()
				.isThrownBy(() -> service.execute(new CreateTenantCommand("Uwati Health Services Ltd.", "  ")))
				.withMessage("Tenant display name must not be blank.");
	}

	@Test
	void isIdempotentWhenTheDisplayNameAlreadyExistsForTheSameLegalName() {
		TenantRepository repository = new InMemoryTenantRepository();
		CapturingEventPublisher publisher = new CapturingEventPublisher();
		CreateTenantService service = new CreateTenantService(repository, publisher);

		Tenant first = service.execute(new CreateTenantCommand("Uwati Health Services Ltd.", "Uwati Health"));
		Tenant second = service.execute(new CreateTenantCommand("Uwati Health Services Ltd.", "Uwati Health"));

		assertThat(second).isEqualTo(first);
		assertThat(repository.findByDisplayName("Uwati Health")).contains(first);
		// event published only once — idempotent retry returns existing tenant without re-publishing
		assertThat(publisher.events()).hasSize(1);
	}

	@Test
	void rejectsReusingTheDisplayNameForAnotherLegalName() {
		TenantRepository repository = new InMemoryTenantRepository();
		CreateTenantService service = new CreateTenantService(repository, e -> {});

		service.execute(new CreateTenantCommand("Uwati Health Services Ltd.", "Uwati Health"));

		assertThatIllegalArgumentException()
				.isThrownBy(() -> service.execute(new CreateTenantCommand("Another Legal Entity", "Uwati Health")))
				.isInstanceOf(DuplicateTenantDisplayNameException.class)
				.withMessage("A tenant with the display name 'Uwati Health' already exists.");
	}

	private static final class InMemoryTenantRepository implements TenantRepository {
		private final Map<TenantId, Tenant> tenantsById = new HashMap<>();
		private final Map<String, Tenant> tenantsByDisplayName = new HashMap<>();

		@Override
		public Optional<Tenant> findById(TenantId tenantId) {
			return Optional.ofNullable(tenantsById.get(tenantId));
		}

		@Override
		public Optional<Tenant> findByDisplayName(String displayName) {
			if (displayName == null) {
				return Optional.empty();
			}
			return Optional.ofNullable(tenantsByDisplayName.get(displayName.trim()));
		}

		@Override
		public Tenant save(Tenant tenant) {
			tenantsById.put(tenant.id(), tenant);
			tenantsByDisplayName.put(tenant.displayName().trim(), tenant);
			return tenant;
		}
	}

	private static final class CapturingEventPublisher implements TenantEventPublisher {
		private final List<TenantCreated> events = new ArrayList<>();

		@Override
		public void publish(TenantCreated event) {
			events.add(event);
		}

		List<TenantCreated> events() {
			return List.copyOf(events);
		}
	}
}

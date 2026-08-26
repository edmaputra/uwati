package io.github.edmaputra.uwati.core.tenancy.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.edmaputra.uwati.core.tenancy.application.service.ConfigureTenantSettingsService;
import io.github.edmaputra.uwati.core.tenancy.application.service.GetTenantSettingsService;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand.SettingEntry;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantEventPublisher;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantRepository;
import io.github.edmaputra.uwati.domain.tenancy.application.port.out.TenantSettingRepository;
import io.github.edmaputra.uwati.domain.tenancy.domain.InvalidTenantSettingException;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantStatus;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;

@DisplayName("ConfigureTenantSettingsService Unit Tests")
class ConfigureTenantSettingsServiceTests {

	private static final TenantId TENANT_ID = TenantId.generate();
	private static final OperationContext CONTEXT = OperationContext.of("test-operator", "trace-001");

	private InMemoryTenantRepository tenantRepository;
	private InMemoryTenantSettingRepository settingRepository;
	private CapturingEventPublisher eventPublisher;
	private ConfigureTenantSettingsService configureService;
	private GetTenantSettingsService getService;

	@BeforeEach
	void setUp() {
		tenantRepository = new InMemoryTenantRepository();
		settingRepository = new InMemoryTenantSettingRepository();
		eventPublisher = new CapturingEventPublisher();

		tenantRepository.save(new Tenant(
				TENANT_ID,
				"Uwati Health Services Ltd.",
				"Uwati Health",
				TenantStatus.ACTIVE,
				java.time.Instant.now(),
				java.time.Instant.now()));

		configureService = new ConfigureTenantSettingsService(tenantRepository, settingRepository, eventPublisher);
		getService = new GetTenantSettingsService(tenantRepository, settingRepository);
	}

	@Test
	@DisplayName("configures settings, increments revision on existing, and publishes domain event with previous and updated settings")
	void configuresAndUpdatesSettings() {
		// Existing setting at revision 1
		TenantSetting existingLocale = new TenantSetting(TENANT_ID, "organization.locale", "en-US", 1);
		settingRepository.saveAll(List.of(existingLocale));

		List<TenantSetting> results = configureService.execute(new ConfigureTenantSettingsCommand(
				TENANT_ID,
				List.of(
						new SettingEntry("organization.locale", "id-ID"),
						new SettingEntry("organization.time-zone", "Asia/Jakarta"))), CONTEXT);

		assertThat(results).hasSize(2);

		TenantSetting locale = results.stream().filter(s -> s.key().equals("organization.locale")).findFirst().orElseThrow();
		assertThat(locale.value()).isEqualTo("id-ID");
		assertThat(locale.revision()).isEqualTo(2);

		TenantSetting tz = results.stream().filter(s -> s.key().equals("organization.time-zone")).findFirst().orElseThrow();
		assertThat(tz.value()).isEqualTo("Asia/Jakarta");
		assertThat(tz.revision()).isEqualTo(1);

		assertThat(eventPublisher.settingsEvents).hasSize(1);
		TenantSettingsUpdated event = eventPublisher.settingsEvents.get(0);
		assertThat(event.tenantId()).isEqualTo(TENANT_ID);
		assertThat(event.previousSettings()).containsExactly(existingLocale);
		assertThat(event.updatedSettings()).containsExactlyInAnyOrder(locale, tz);
		assertThat(event.actor()).isEqualTo("test-operator");
		assertThat(event.correlationId()).isEqualTo("trace-001");
	}

	@Test
	@DisplayName("rejects non-existent tenant")
	void rejectsNonExistentTenant() {
		TenantId unknownId = TenantId.generate();

		assertThatIllegalArgumentException()
				.isThrownBy(() -> configureService.execute(new ConfigureTenantSettingsCommand(
						unknownId,
						List.of(new SettingEntry("organization.locale", "en-US"))), CONTEXT))
				.isInstanceOf(TenantNotFoundException.class);

		assertThatIllegalArgumentException()
				.isThrownBy(() -> getService.execute(unknownId))
				.isInstanceOf(TenantNotFoundException.class);
	}

	@Test
	@DisplayName("rejects invalid setting values via domain validation")
	void rejectsInvalidSettings() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> configureService.execute(new ConfigureTenantSettingsCommand(
						TENANT_ID,
						List.of(new SettingEntry("unsupported.key", "value"))), CONTEXT))
				.isInstanceOf(InvalidTenantSettingException.class);

		assertThatIllegalArgumentException()
				.isThrownBy(() -> configureService.execute(new ConfigureTenantSettingsCommand(
						TENANT_ID,
						List.of(new SettingEntry("finance.currency", "INVALID_CODE"))), CONTEXT))
				.isInstanceOf(InvalidTenantSettingException.class);
	}

	private static final class InMemoryTenantRepository implements TenantRepository {
		private final Map<TenantId, Tenant> map = new HashMap<>();

		@Override
		public Optional<Tenant> findById(TenantId tenantId) {
			return Optional.ofNullable(map.get(tenantId));
		}

		@Override
		public Optional<Tenant> findByDisplayName(String displayName) {
			return map.values().stream().filter(t -> t.displayName().equalsIgnoreCase(displayName)).findFirst();
		}

		@Override
		public Tenant save(Tenant tenant) {
			map.put(tenant.id(), tenant);
			return tenant;
		}
	}

	private static final class InMemoryTenantSettingRepository implements TenantSettingRepository {
		private final Map<String, TenantSetting> map = new HashMap<>();

		@Override
		public List<TenantSetting> findAllByTenantId(TenantId tenantId) {
			return map.values().stream().filter(s -> s.tenantId().equals(tenantId)).toList();
		}

		@Override
		public Optional<TenantSetting> findByTenantIdAndKey(TenantId tenantId, String key) {
			return Optional.ofNullable(map.get(tenantId + ":" + key));
		}

		@Override
		public List<TenantSetting> saveAll(List<TenantSetting> settings) {
			for (TenantSetting setting : settings) {
				map.put(setting.tenantId() + ":" + setting.key(), setting);
			}
			return List.copyOf(settings);
		}
	}

	private static final class CapturingEventPublisher implements TenantEventPublisher {
		private final List<TenantCreated> createdEvents = new ArrayList<>();
		private final List<TenantSettingsUpdated> settingsEvents = new ArrayList<>();

		@Override
		public void publish(TenantCreated event) {
			createdEvents.add(event);
		}

		@Override
		public void publish(TenantSettingsUpdated event) {
			settingsEvents.add(event);
		}
	}
}

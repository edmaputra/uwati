package io.github.edmaputra.uwati.adapter.persistence.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.github.edmaputra.uwati.domain.audit.AuditContext;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantStatus;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;

@DisplayName("AuditTrailEventListener Unit Tests")
class AuditTrailEventListenerTests {

	private AuditEntryJpaRepository auditRepository;
	private AuditContext auditContext;
	private AuditTrailEventListener listener;

	@BeforeEach
	void setUp() {
		auditRepository = mock(AuditEntryJpaRepository.class);
		auditContext = mock(AuditContext.class);
		when(auditContext.requireActor()).thenReturn("operator-01");
		when(auditContext.requireCorrelationId()).thenReturn("trace-abc-123");

		listener = new AuditTrailEventListener(auditRepository, auditContext);
	}

	@Test
	@DisplayName("persists common audit entry on TenantCreated event")
	void recordsTenantCreatedAuditEntry() {
		TenantId tenantId = TenantId.generate();
		Instant now = Instant.now();
		Tenant tenant = new Tenant(
				tenantId,
				"RS Permata Medika Ltd.",
				"RS Permata",
				TenantStatus.ACTIVE,
				now,
				now);

		TenantCreated event = new TenantCreated(tenant, now);
		listener.onTenantCreated(event);

		ArgumentCaptor<AuditEntryEntity> captor = ArgumentCaptor.forClass(AuditEntryEntity.class);
		verify(auditRepository).save(captor.capture());

		AuditEntryEntity saved = captor.getValue();
		assertThat(saved.getTenantId()).isEqualTo(tenantId.value());
		assertThat(saved.getEntityName()).isEqualTo("Tenant");
		assertThat(saved.getEntityId()).isEqualTo(tenantId.value().toString());
		assertThat(saved.getAction()).isEqualTo("CREATE");
		assertThat(saved.getActor()).isEqualTo("operator-01");
		assertThat(saved.getCorrelationId()).isEqualTo("trace-abc-123");
		assertThat(saved.getOccurredAt()).isEqualTo(now);

		assertThat(saved.getChangesJson()).contains("\"fields\":{");
		assertThat(saved.getChangesJson()).contains("\"displayName\":{\"old\":null,\"new\":\"RS Permata\"}");
		assertThat(saved.getChangesJson()).contains("\"legalName\":{\"old\":null,\"new\":\"RS Permata Medika Ltd.\"}");
		assertThat(saved.getChangesJson()).contains("\"status\":{\"old\":null,\"new\":\"ACTIVE\"}");
	}

	@Test
	@DisplayName("persists common audit entry on TenantSettingsUpdated event with collection diff")
	void recordsTenantSettingsUpdatedAuditEntry() {
		TenantId tenantId = TenantId.generate();
		Instant now = Instant.now();

		List<TenantSetting> previousSettings = List.of(
				new TenantSetting(tenantId, "organization.locale", "en-US", 1),
				new TenantSetting(tenantId, "old.feature", "true", 1));

		List<TenantSetting> updatedSettings = List.of(
				new TenantSetting(tenantId, "organization.locale", "id-ID", 2),
				new TenantSetting(tenantId, "inventory.unit", "BOX", 1));

		TenantSettingsUpdated event = new TenantSettingsUpdated(tenantId, previousSettings, updatedSettings, now);
		listener.onTenantSettingsUpdated(event);

		ArgumentCaptor<AuditEntryEntity> captor = ArgumentCaptor.forClass(AuditEntryEntity.class);
		verify(auditRepository).save(captor.capture());

		AuditEntryEntity saved = captor.getValue();
		assertThat(saved.getTenantId()).isEqualTo(tenantId.value());
		assertThat(saved.getEntityName()).isEqualTo("TenantSetting");
		assertThat(saved.getEntityId()).isEqualTo(tenantId.value().toString());
		assertThat(saved.getAction()).isEqualTo("UPDATE");
		assertThat(saved.getActor()).isEqualTo("operator-01");
		assertThat(saved.getCorrelationId()).isEqualTo("trace-abc-123");

		String json = saved.getChangesJson();
		assertThat(json).contains("\"collections\":{\"settings\":{");
		assertThat(json).contains("\"added\":[{\"key\":\"inventory.unit\",\"value\":\"BOX\",\"revision\":1}]");
		assertThat(json).contains("\"removed\":[{\"key\":\"old.feature\",\"value\":\"true\",\"revision\":1}]");
		assertThat(json).contains("\"changed\":[{\"key\":\"organization.locale\",\"fields\":{\"revision\":{\"old\":1,\"new\":2},\"value\":{\"old\":\"en-US\",\"new\":\"id-ID\"}}}]");
	}
}

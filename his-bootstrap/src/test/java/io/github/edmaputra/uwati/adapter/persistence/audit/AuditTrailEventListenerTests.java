package io.github.edmaputra.uwati.adapter.persistence.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantStatus;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantSettingsUpdated;

@DisplayName("AuditTrailEventListener Unit Tests")
class AuditTrailEventListenerTests {

	private AuditEntryJpaRepository auditRepository;
	private AuditTrailEventListener listener;

	@BeforeEach
	void setUp() {
		auditRepository = mock(AuditEntryJpaRepository.class);
		listener = new AuditTrailEventListener(auditRepository);
	}

	@Test
	@DisplayName("persists audit entry on TenantCreated with actor and correlationId from event without 'fields' wrapper")
	void recordsTenantCreatedAuditEntry() throws Exception {
		TenantId tenantId = TenantId.generate();
		Instant now = Instant.now();
		Tenant tenant = new Tenant(
				tenantId,
				"RS Permata Medika Ltd.",
				"RS Permata",
				TenantStatus.ACTIVE,
				now,
				now);

		TenantCreated event = new TenantCreated(tenant, "operator-01", "trace-abc-123", now);
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

		JSONAssert.assertEquals("""
				{
				  "displayName": { "old": null, "new": "RS Permata" },
				  "legalName": { "old": null, "new": "RS Permata Medika Ltd." },
				  "status": { "old": null, "new": "ACTIVE" }
				}
				""", saved.getChangesJson(), JSONCompareMode.LENIENT);
	}

	@Test
	@DisplayName("persists audit entry on TenantSettingsUpdated with actor, correlationId, and collection diff")
	void recordsTenantSettingsUpdatedAuditEntry() throws Exception {
		TenantId tenantId = TenantId.generate();
		Instant now = Instant.now();

		List<TenantSetting> previousSettings = List.of(
				new TenantSetting(tenantId, "organization.locale", "en-US", 1),
				new TenantSetting(tenantId, "old.feature", "true", 1));

		List<TenantSetting> updatedSettings = List.of(
				new TenantSetting(tenantId, "organization.locale", "id-ID", 2),
				new TenantSetting(tenantId, "inventory.unit", "BOX", 1));

		TenantSettingsUpdated event = new TenantSettingsUpdated(
				tenantId, previousSettings, updatedSettings, "operator-01", "trace-abc-123", now);
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

		JSONAssert.assertEquals("""
				{
				  "settings": {
				    "added": [
				      { "key": "inventory.unit", "value": "BOX", "revision": 1 }
				    ],
				    "removed": [
				      { "key": "old.feature", "value": "true", "revision": 1 }
				    ],
				    "changed": [
				      {
				        "key": "organization.locale",
				        "revision": { "old": 1, "new": 2 },
				        "value": { "old": "en-US", "new": "id-ID" }
				      }
				    ]
				  }
				}
				""", saved.getChangesJson(), JSONCompareMode.LENIENT);
	}

	@Test
	@DisplayName("uses 'unknown' as fallback correlationId when event carries null")
	void fallsBackToUnknownCorrelationId() {
		TenantId tenantId = TenantId.generate();
		Instant now = Instant.now();
		Tenant tenant = new Tenant(tenantId, "Test Clinic Ltd.", "Test Clinic",
				TenantStatus.ACTIVE, now, now);

		TenantCreated event = new TenantCreated(tenant, "system", null, now);
		listener.onTenantCreated(event);

		ArgumentCaptor<AuditEntryEntity> captor = ArgumentCaptor.forClass(AuditEntryEntity.class);
		verify(auditRepository).save(captor.capture());

		assertThat(captor.getValue().getCorrelationId()).isEqualTo("unknown");
	}
}

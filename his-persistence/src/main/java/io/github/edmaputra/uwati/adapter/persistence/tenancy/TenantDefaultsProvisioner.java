package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.github.edmaputra.uwati.domain.tenancy.domain.event.TenantCreated;
import lombok.RequiredArgsConstructor;

/**
 * Transactional event listener that provisions default settings and document sequence counters for new tenants.
 * <p>
 * Operates as an inbound event handler adapter in the hexagonal architecture, responding
 * to {@link TenantCreated} domain events before transaction commit by seeding standard
 * system configurations and sequence generators into relational persistence.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class TenantDefaultsProvisioner {

	private static final int INITIAL_SETTING_REVISION = 1;

	private final TenantSettingJpaRepository tenantSettings;
	private final TenantDocumentSequenceJpaRepository tenantDocumentSequences;

	/**
	 * Seeds initial configuration settings and document numbering sequences upon tenant creation.
	 *
	 * @param event domain event containing newly created tenant metadata
	 */
	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void onTenantCreated(TenantCreated event) {
		var tenantId = event.tenant().id().value();

		tenantSettings.saveAll(List.of(
				new TenantSettingEntity(tenantId, "organization.locale", "en-US", INITIAL_SETTING_REVISION),
				new TenantSettingEntity(tenantId, "organization.time-zone", "UTC", INITIAL_SETTING_REVISION),
				new TenantSettingEntity(tenantId, "finance.currency", "USD", INITIAL_SETTING_REVISION),
				new TenantSettingEntity(tenantId, "inventory.measurement-system", "METRIC", INITIAL_SETTING_REVISION),
				new TenantSettingEntity(tenantId, "features.base-configuration", "STANDARD", INITIAL_SETTING_REVISION)));

		tenantDocumentSequences.saveAll(List.of(
				new TenantDocumentSequenceEntity(tenantId, "PATIENT", "TENANT", "PAT-", 1L, "NONE"),
				new TenantDocumentSequenceEntity(tenantId, "ENCOUNTER", "TENANT", "ENC-", 1L, "NONE"),
				new TenantDocumentSequenceEntity(tenantId, "PRESCRIPTION", "TENANT", "RX-", 1L, "NONE"),
				new TenantDocumentSequenceEntity(tenantId, "PURCHASE", "TENANT", "PUR-", 1L, "NONE"),
				new TenantDocumentSequenceEntity(tenantId, "INVOICE", "TENANT", "INV-", 1L, "NONE")));
	}
}

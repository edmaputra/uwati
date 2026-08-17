package io.github.edmaputra.uwati.adapter.persistence.tenancy;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "tenant_settings",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_tenant_settings_tenant_key",
				columnNames = { "tenant_id", "setting_key" }),
		indexes = @Index(name = "idx_tenant_settings_tenant_id", columnList = "tenant_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class TenantSettingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id", nullable = false, updatable = false)
	private UUID tenantId;

	@Column(name = "setting_key", nullable = false)
	private String settingKey;

	@Column(name = "setting_value", nullable = false)
	private String settingValue;

	@Column(nullable = false)
	private int revision;

	TenantSettingEntity(UUID tenantId, String settingKey, String settingValue, int revision) {
		this.tenantId = tenantId;
		this.settingKey = settingKey;
		this.settingValue = settingValue;
		this.revision = revision;
	}
}

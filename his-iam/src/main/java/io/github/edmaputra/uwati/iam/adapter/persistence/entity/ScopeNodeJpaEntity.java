package io.github.edmaputra.uwati.iam.adapter.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "iam_scope_node")
@Getter
@Setter
@NoArgsConstructor
public class ScopeNodeJpaEntity {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "parent_id")
	private UUID parentId;

	@Column(name = "code", length = 64, nullable = false)
	private String code;

	@Column(name = "name", length = 255, nullable = false)
	private String name;

	@Column(name = "path", length = 512, nullable = false)
	private String path;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public ScopeNodeJpaEntity(
			UUID id,
			UUID tenantId,
			UUID parentId,
			String code,
			String name,
			String path,
			Instant createdAt,
			Instant updatedAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.parentId = parentId;
		this.code = code;
		this.name = name;
		this.path = path;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}

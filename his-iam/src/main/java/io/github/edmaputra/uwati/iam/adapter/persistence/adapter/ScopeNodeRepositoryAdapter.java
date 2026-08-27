package io.github.edmaputra.uwati.iam.adapter.persistence.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.adapter.persistence.entity.ScopeNodeJpaEntity;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.SpringDataScopeNodeRepository;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNode;
import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;
import io.github.edmaputra.uwati.iam.domain.repository.ScopeNodeRepository;

/**
 * Persistence adapter mapping between ScopeNode domain entity and ScopeNodeJpaEntity.
 */
public class ScopeNodeRepositoryAdapter implements ScopeNodeRepository {

	private final SpringDataScopeNodeRepository repository;

	public ScopeNodeRepositoryAdapter(SpringDataScopeNodeRepository repository) {
		this.repository = Objects.requireNonNull(repository, "SpringDataScopeNodeRepository must not be null.");
	}

	@Override
	public ScopeNode save(ScopeNode node) {
		Objects.requireNonNull(node, "ScopeNode must not be null.");
		ScopeNodeJpaEntity entity = toEntity(node);
		ScopeNodeJpaEntity saved = repository.save(entity);
		return toDomain(saved);
	}

	@Override
	public Optional<ScopeNode> findById(ScopeNodeId id) {
		Objects.requireNonNull(id, "ScopeNodeId must not be null.");
		return repository.findById(id.value()).map(this::toDomain);
	}

	@Override
	public Optional<ScopeNode> findByTenantIdAndCode(TenantId tenantId, String code) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(code, "Code must not be null.");
		return repository.findByTenantIdAndCode(tenantId.value(), code.trim().toUpperCase()).map(this::toDomain);
	}

	@Override
	public List<ScopeNode> findAllByTenantId(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		return repository.findAllByTenantId(tenantId.value()).stream()
				.map(this::toDomain)
				.toList();
	}

	@Override
	public List<ScopeNode> findByParentId(ScopeNodeId parentId) {
		Objects.requireNonNull(parentId, "ParentId must not be null.");
		return repository.findByParentId(parentId.value()).stream()
				.map(this::toDomain)
				.toList();
	}

	@Override
	public List<ScopeNode> findDescendantsByPathPrefix(String pathPrefix) {
		Objects.requireNonNull(pathPrefix, "PathPrefix must not be null.");
		return repository.findByPathStartingWith(pathPrefix).stream()
				.map(this::toDomain)
				.toList();
	}

	@Override
	public void updatePathPrefix(String oldPrefix, String newPrefix) {
		Objects.requireNonNull(oldPrefix, "OldPrefix must not be null.");
		Objects.requireNonNull(newPrefix, "NewPrefix must not be null.");
		repository.updatePathPrefix(oldPrefix, newPrefix, Instant.now());
	}

	@Override
	public boolean existsByParentId(ScopeNodeId parentId) {
		Objects.requireNonNull(parentId, "ParentId must not be null.");
		return repository.existsByParentId(parentId.value());
	}

	@Override
	public boolean existsByTenantIdAndCode(TenantId tenantId, String code) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		Objects.requireNonNull(code, "Code must not be null.");
		return repository.existsByTenantIdAndCode(tenantId.value(), code.trim().toUpperCase());
	}

	@Override
	public void delete(ScopeNodeId id) {
		Objects.requireNonNull(id, "ScopeNodeId must not be null.");
		repository.deleteById(id.value());
	}

	private ScopeNodeJpaEntity toEntity(ScopeNode domain) {
		return new ScopeNodeJpaEntity(
				domain.getId().value(),
				domain.getTenantId().value(),
				domain.getParentId() != null ? domain.getParentId().value() : null,
				domain.getCode(),
				domain.getName(),
				domain.getPath(),
				domain.getCreatedAt(),
				domain.getUpdatedAt());
	}

	private ScopeNode toDomain(ScopeNodeJpaEntity entity) {
		return new ScopeNode(
				ScopeNodeId.of(entity.getId()),
				TenantId.from(entity.getTenantId().toString()),
				entity.getParentId() != null ? ScopeNodeId.of(entity.getParentId()) : null,
				entity.getCode(),
				entity.getName(),
				entity.getPath(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}

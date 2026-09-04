package io.github.edmaputra.uwati.iam.adapter.persistence.adapter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.adapter.persistence.entity.UserJpaEntity;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.UserJpaRepository;
import io.github.edmaputra.uwati.iam.domain.model.User;
import io.github.edmaputra.uwati.iam.domain.model.UserId;
import io.github.edmaputra.uwati.iam.domain.model.UserStatus;
import io.github.edmaputra.uwati.iam.domain.repository.UserRepository;

/**
 * Persistence adapter implementing {@link UserRepository} backed by Spring Data JPA.
 *
 * @author edmaputra
 */
public class UserRepositoryAdapter implements UserRepository {

	private final UserJpaRepository repository;

	/**
	 * Constructs the adapter with the underlying Spring Data repository.
	 *
	 * @param repository the Spring Data repository
	 */
	public UserRepositoryAdapter(UserJpaRepository repository) {
		this.repository = Objects.requireNonNull(repository, "UserJpaRepository must not be null.");
	}

	@Override
	public Optional<User> findById(UserId id) {
		Objects.requireNonNull(id, "UserId must not be null.");
		return repository.findById(id.value()).map(this::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		Objects.requireNonNull(email, "Email must not be null.");
		return repository.findByEmailIgnoreCase(email.trim()).map(this::toDomain);
	}

	@Override
	public boolean existsByEmail(String email) {
		Objects.requireNonNull(email, "Email must not be null.");
		return repository.existsByEmailIgnoreCase(email.trim());
	}

	@Override
	public List<User> findAllByIds(Iterable<UserId> ids) {
		Objects.requireNonNull(ids, "User IDs must not be null.");
		List<UUID> uuids = StreamSupport.stream(ids.spliterator(), false)
				.map(UserId::value)
				.toList();
		return repository.findAllById(uuids).stream().map(this::toDomain).toList();
	}

	@Override
	public List<User> findAll() {
		return repository.findAll().stream().map(this::toDomain).toList();
	}

	@Override
	public List<User> findAllByTenantId(TenantId tenantId) {
		Objects.requireNonNull(tenantId, "TenantId must not be null.");
		return repository.findAllByTenantId(tenantId.value()).stream().map(this::toDomain).toList();
	}

	@Override
	public User save(User user) {
		Objects.requireNonNull(user, "User must not be null.");
		UserJpaEntity entity = toEntity(user);
		UserJpaEntity saved = repository.save(entity);
		return toDomain(saved);
	}

	@Override
	public void delete(UserId id) {
		Objects.requireNonNull(id, "UserId must not be null.");
		repository.deleteById(id.value());
	}

	private User toDomain(UserJpaEntity entity) {
		return new User(
				new UserId(entity.getId()),
				entity.getEmail(),
				entity.getPasswordHash(),
				entity.getFullName(),
				UserStatus.valueOf(entity.getStatus()),
				entity.isPlatformSuperAdmin(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}

	private UserJpaEntity toEntity(User user) {
		return new UserJpaEntity(
				user.getId().value(),
				user.getEmail(),
				user.getPasswordHash(),
				user.getFullName(),
				user.getStatus().name(),
				user.isPlatformSuperAdmin(),
				user.getCreatedAt(),
				user.getUpdatedAt());
	}
}


package io.github.edmaputra.uwati.iam.adapter.persistence.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.adapter.persistence.entity.UserRoleAssignmentJpaEntity;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.SpringDataUserRoleAssignmentRepository;
import io.github.edmaputra.uwati.iam.domain.model.UserId;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.UserRoleAssignmentId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRoleAssignmentRepositoryAdapterTest {

	private SpringDataUserRoleAssignmentRepository springDataRepository;
	private UserRoleAssignmentRepositoryAdapter adapter;

	@BeforeEach
	void setUp() {
		springDataRepository = Mockito.mock(SpringDataUserRoleAssignmentRepository.class);
		adapter = new UserRoleAssignmentRepositoryAdapter(springDataRepository);
	}

	@Test
	@DisplayName("Should find user role assignment by id")
	void shouldFindById() {
		UUID id = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID roleId = UUID.randomUUID();
		UUID tenantId = UUID.randomUUID();

		UserRoleAssignmentJpaEntity entity = new UserRoleAssignmentJpaEntity(
				id,
				userId,
				roleId,
				tenantId,
				null,
				true,
				Instant.now());

		when(springDataRepository.findById(id)).thenReturn(Optional.of(entity));

		Optional<UserRoleAssignment> result = adapter.findById(new UserRoleAssignmentId(id));

		assertThat(result).isPresent();
		assertThat(result.get().isTenantWide()).isTrue();
		assertThat(result.get().getUserId().value()).isEqualTo(userId);
	}

	@Test
	@DisplayName("Should find all assignments by user id and tenant id")
	void shouldFindByUserAndTenant() {
		UUID userId = UUID.randomUUID();
		UUID tenantId = UUID.randomUUID();

		UserRoleAssignmentJpaEntity entity = new UserRoleAssignmentJpaEntity(
				UUID.randomUUID(),
				userId,
				UUID.randomUUID(),
				tenantId,
				null,
				true,
				Instant.now());

		when(springDataRepository.findAllByUserIdAndTenantId(userId, tenantId)).thenReturn(List.of(entity));

		List<UserRoleAssignment> list = adapter.findAllByUserIdAndTenantId(new UserId(userId), new TenantId(tenantId));

		assertThat(list).hasSize(1);
	}
}

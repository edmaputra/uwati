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
import io.github.edmaputra.uwati.iam.adapter.persistence.entity.GroupRoleAssignmentJpaEntity;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.SpringDataGroupRoleAssignmentRepository;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignment;
import io.github.edmaputra.uwati.iam.domain.model.GroupRoleAssignmentId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GroupRoleAssignmentRepositoryAdapterTest {

	private SpringDataGroupRoleAssignmentRepository springDataRepository;
	private GroupRoleAssignmentRepositoryAdapter adapter;

	@BeforeEach
	void setUp() {
		springDataRepository = Mockito.mock(SpringDataGroupRoleAssignmentRepository.class);
		adapter = new GroupRoleAssignmentRepositoryAdapter(springDataRepository);
	}

	@Test
	@DisplayName("Should find group role assignment by id")
	void shouldFindById() {
		UUID id = UUID.randomUUID();
		UUID groupId = UUID.randomUUID();
		UUID roleId = UUID.randomUUID();
		UUID tenantId = UUID.randomUUID();

		GroupRoleAssignmentJpaEntity entity = new GroupRoleAssignmentJpaEntity(
				id,
				groupId,
				roleId,
				tenantId,
				null,
				true,
				Instant.now());

		when(springDataRepository.findById(id)).thenReturn(Optional.of(entity));

		Optional<GroupRoleAssignment> result = adapter.findById(new GroupRoleAssignmentId(id));

		assertThat(result).isPresent();
		assertThat(result.get().isTenantWide()).isTrue();
		assertThat(result.get().getGroupId().value()).isEqualTo(groupId);
	}

	@Test
	@DisplayName("Should find all assignments by group id list")
	void shouldFindByGroupIds() {
		UUID groupId = UUID.randomUUID();
		UUID tenantId = UUID.randomUUID();

		GroupRoleAssignmentJpaEntity entity = new GroupRoleAssignmentJpaEntity(
				UUID.randomUUID(),
				groupId,
				UUID.randomUUID(),
				tenantId,
				null,
				true,
				Instant.now());

		when(springDataRepository.findAllByGroupIdIn(any())).thenReturn(List.of(entity));

		List<GroupRoleAssignment> list = adapter.findAllByGroupIds(List.of(new GroupId(groupId)));

		assertThat(list).hasSize(1);
	}
}

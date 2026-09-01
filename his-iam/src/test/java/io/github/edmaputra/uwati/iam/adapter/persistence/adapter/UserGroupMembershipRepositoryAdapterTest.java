package io.github.edmaputra.uwati.iam.adapter.persistence.adapter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.edmaputra.uwati.iam.adapter.persistence.entity.UserGroupMembershipJpaEntity;
import io.github.edmaputra.uwati.iam.adapter.persistence.entity.UserGroupMembershipJpaId;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.SpringDataUserGroupMembershipRepository;
import io.github.edmaputra.uwati.iam.domain.model.GroupId;
import io.github.edmaputra.uwati.iam.domain.model.UserGroupMembership;
import io.github.edmaputra.uwati.iam.domain.model.UserId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserGroupMembershipRepositoryAdapterTest {

	private SpringDataUserGroupMembershipRepository springDataRepository;
	private UserGroupMembershipRepositoryAdapter adapter;

	@BeforeEach
	void setUp() {
		springDataRepository = Mockito.mock(SpringDataUserGroupMembershipRepository.class);
		adapter = new UserGroupMembershipRepositoryAdapter(springDataRepository);
	}

	@Test
	@DisplayName("Should find all memberships by user id")
	void shouldFindAllByUserId() {
		UUID userId = UUID.randomUUID();
		UUID groupId = UUID.randomUUID();
		UserGroupMembershipJpaEntity entity = new UserGroupMembershipJpaEntity(
				new UserGroupMembershipJpaId(groupId, userId),
				Instant.now());

		when(springDataRepository.findAllByIdUserId(userId)).thenReturn(List.of(entity));

		List<UserGroupMembership> memberships = adapter.findAllByUserId(new UserId(userId));

		assertThat(memberships).hasSize(1);
		assertThat(memberships.getFirst().userId().value()).isEqualTo(userId);
		assertThat(memberships.getFirst().groupId().value()).isEqualTo(groupId);
	}

	@Test
	@DisplayName("Should delete membership by group and user id")
	void shouldDeleteMembership() {
		GroupId groupId = GroupId.generate();
		UserId userId = UserId.generate();

		adapter.delete(groupId, userId);

		verify(springDataRepository).deleteById(new UserGroupMembershipJpaId(groupId.value(), userId.value()));
	}
}

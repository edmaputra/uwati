package io.github.edmaputra.uwati.iam.domain.model;

import java.time.Instant;
import java.util.Objects;

public record UserGroupMembership(
		GroupId groupId,
		UserId userId,
		Instant joinedAt) {

	public UserGroupMembership {
		Objects.requireNonNull(groupId, "Group ID must not be null.");
		Objects.requireNonNull(userId, "User ID must not be null.");
		Objects.requireNonNull(joinedAt, "JoinedAt must not be null.");
	}

	public static UserGroupMembership of(GroupId groupId, UserId userId) {
		return new UserGroupMembership(groupId, userId, Instant.now());
	}
}

package io.github.edmaputra.uwati.iam.domain.exception;

import io.github.edmaputra.uwati.iam.domain.model.GroupId;

public class GroupNotFoundException extends RuntimeException {

	public GroupNotFoundException(GroupId groupId) {
		super("Group not found with id: " + groupId);
	}

	public GroupNotFoundException(String code) {
		super("Group not found with code: " + code);
	}
}

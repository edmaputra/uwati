package io.github.edmaputra.uwati.iam.domain.exception;

import io.github.edmaputra.uwati.iam.domain.model.ScopeNodeId;

public class ScopeNodeNotFoundException extends RuntimeException {

	public ScopeNodeNotFoundException(ScopeNodeId scopeNodeId) {
		super("ScopeNode not found with id: " + scopeNodeId);
	}

	public ScopeNodeNotFoundException(String code) {
		super("ScopeNode not found with code: " + code);
	}
}

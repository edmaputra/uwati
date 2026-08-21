package io.github.edmaputra.uwati.adapter.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.edmaputra.uwati.domain.tenancy.application.MissingTenantContextException;
import io.github.edmaputra.uwati.domain.tenancy.domain.DuplicateTenantDisplayNameException;
import io.github.edmaputra.uwati.domain.tenancy.domain.InvalidTenantSettingException;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException;

@RestControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(TenantNotFoundException.class)
	public ProblemDetail handleTenantNotFound(TenantNotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(DuplicateTenantDisplayNameException.class)
	public ProblemDetail handleDuplicateTenantDisplayName(DuplicateTenantDisplayNameException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler({
			InvalidTenantSettingException.class,
			MissingTenantContextException.class,
			IllegalArgumentException.class,
			IllegalStateException.class
	})
	public ProblemDetail handleBadRequest(Exception ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}
}

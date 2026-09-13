package io.github.edmaputra.uwati.adapter.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.edmaputra.uwati.domain.organization.DuplicateFacilityCodeException;
import io.github.edmaputra.uwati.domain.organization.DuplicateServiceUnitCodeException;
import io.github.edmaputra.uwati.domain.organization.FacilityNotFoundException;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitNotFoundException;
import io.github.edmaputra.uwati.domain.tenancy.application.MissingTenantContextException;
import io.github.edmaputra.uwati.domain.tenancy.domain.DuplicateTenantDisplayNameException;
import io.github.edmaputra.uwati.domain.tenancy.domain.InvalidTenantSettingException;
import io.github.edmaputra.iam.domain.exception.AccessDeniedException;
import io.github.edmaputra.iam.domain.exception.AuthenticationException;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException;

@RestControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(AuthenticationException.class)
	public ProblemDetail handleAuthentication(AuthenticationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler({ TenantNotFoundException.class, FacilityNotFoundException.class, ServiceUnitNotFoundException.class })
	public ProblemDetail handleNotFound(RuntimeException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler({ DuplicateTenantDisplayNameException.class, DuplicateFacilityCodeException.class, DuplicateServiceUnitCodeException.class })
	public ProblemDetail handleConflict(RuntimeException ex) {
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

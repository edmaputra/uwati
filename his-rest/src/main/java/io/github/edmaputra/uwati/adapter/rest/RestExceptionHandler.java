package io.github.edmaputra.uwati.adapter.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.edmaputra.iam.domain.exception.AccessDeniedException;
import io.github.edmaputra.iam.domain.exception.AuthenticationException;
import io.github.edmaputra.uwati.domain.organization.DuplicateFacilityCodeException;
import io.github.edmaputra.uwati.domain.organization.DuplicateServiceUnitCodeException;
import io.github.edmaputra.uwati.domain.organization.FacilityNotFoundException;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitNotFoundException;
import io.github.edmaputra.uwati.domain.tenancy.application.MissingTenantContextException;
import io.github.edmaputra.uwati.domain.tenancy.domain.DuplicateTenantDisplayNameException;
import io.github.edmaputra.uwati.domain.tenancy.domain.InvalidTenantSettingException;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException;

/**
 * Centralized REST exception handler translating domain, security, and validation exceptions
 * into RFC 9457 Problem Details responses.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@RestControllerAdvice
public class RestExceptionHandler {

	private static final String PROBLEM_BASE_URI = "https://api.uwati.example.com/problems/";

	/**
	 * Handles authentication failures returning 401 Unauthorized.
	 *
	 * @param ex the authentication exception
	 * @param request the HTTP servlet request
	 * @return RFC 9457 problem detail
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ProblemDetail handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
		problem.setTitle("Authentication Required");
		problem.setType(URI.create(PROBLEM_BASE_URI + "unauthorized"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	/**
	 * Handles access denial failures returning 403 Forbidden.
	 *
	 * @param ex the access denied exception
	 * @param request the HTTP servlet request
	 * @return RFC 9457 problem detail
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
		problem.setTitle("Access Denied");
		problem.setType(URI.create(PROBLEM_BASE_URI + "access-denied"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	/**
	 * Handles resource not found exceptions returning 404 Not Found.
	 *
	 * @param ex the runtime not found exception
	 * @param request the HTTP servlet request
	 * @return RFC 9457 problem detail
	 */
	@ExceptionHandler({ TenantNotFoundException.class, FacilityNotFoundException.class, ServiceUnitNotFoundException.class })
	public ProblemDetail handleNotFound(RuntimeException ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Resource Not Found");
		problem.setType(URI.create(PROBLEM_BASE_URI + "not-found"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	/**
	 * Handles domain uniqueness and conflict exceptions returning 409 Conflict.
	 *
	 * @param ex the runtime conflict exception
	 * @param request the HTTP servlet request
	 * @return RFC 9457 problem detail
	 */
	@ExceptionHandler({ DuplicateTenantDisplayNameException.class, DuplicateFacilityCodeException.class, DuplicateServiceUnitCodeException.class })
	public ProblemDetail handleConflict(RuntimeException ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Resource Conflict");
		problem.setType(URI.create(PROBLEM_BASE_URI + "conflict"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	/**
	 * Handles database optimistic locking concurrency failures returning 409 Conflict.
	 *
	 * @param ex the optimistic locking failure exception
	 * @param request the HTTP servlet request
	 * @return RFC 9457 problem detail
	 */
	@ExceptionHandler(OptimisticLockingFailureException.class)
	public ProblemDetail handleOptimisticLocking(OptimisticLockingFailureException ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.CONFLICT,
				"Resource was modified by another concurrent transaction. Please refresh and retry.");
		problem.setTitle("Concurrent Modification Conflict");
		problem.setType(URI.create(PROBLEM_BASE_URI + "concurrency-conflict"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	/**
	 * Handles Jakarta Bean Validation errors on request bodies returning 422 Unprocessable Entity
	 * with detailed field-level error mappings.
	 *
	 * @param ex the validation exception
	 * @param request the HTTP servlet request
	 * @return RFC 9457 problem detail with field errors extension
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		List<Map<String, Object>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> Map.<String, Object>of(
						"field", fe.getField(),
						"message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
						"rejectedValue", fe.getRejectedValue() != null ? fe.getRejectedValue() : "null"))
				.toList();

		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.UNPROCESSABLE_ENTITY,
				"Request contains %d validation errors.".formatted(fieldErrors.size()));
		problem.setTitle("Validation Failed");
		problem.setType(URI.create(PROBLEM_BASE_URI + "validation-error"));
		problem.setInstance(URI.create(request.getRequestURI()));
		problem.setProperty("errors", fieldErrors);
		return problem;
	}

	/**
	 * Handles malformed or unprocessable business inputs returning 400 Bad Request.
	 *
	 * @param ex the exception
	 * @param request the HTTP servlet request
	 * @return RFC 9457 problem detail
	 */
	@ExceptionHandler({
			InvalidTenantSettingException.class,
			MissingTenantContextException.class,
			IllegalArgumentException.class,
			IllegalStateException.class
	})
	public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setTitle("Bad Request");
		problem.setType(URI.create(PROBLEM_BASE_URI + "bad-request"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}
}

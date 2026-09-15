package io.github.edmaputra.uwati.adapter.rest.organization;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.edmaputra.iam.domain.security.annotation.RequirePermission;
import io.github.edmaputra.uwati.adapter.rest.OperationContextResolver;
import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityNotFoundException;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeFacilityStatusUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateFacilityUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.FindFacilityUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateFacilityUseCase;
import io.github.edmaputra.iam.domain.context.OperationContext;
import lombok.RequiredArgsConstructor;

/**
 * REST controller managing healthcare facilities under tenant scope.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@RestController
@RequestMapping("/api/v1/facilities")
@RequiredArgsConstructor
public class FacilityController {

	private final CreateFacilityUseCase createFacilityUseCase;
	private final UpdateFacilityUseCase updateFacilityUseCase;
	private final ChangeFacilityStatusUseCase changeFacilityStatusUseCase;
	private final FindFacilityUseCase findFacilityUseCase;

	/**
	 * Creates a new healthcare facility in the tenant.
	 *
	 * @param request validated creation request
	 * @param httpRequest HTTP servlet request
	 * @return 201 Created with Location header and FacilityResponse
	 */
	@PostMapping
	@RequirePermission("organization:facility:create")
	public ResponseEntity<FacilityResponse> create(
			@Valid @RequestBody CreateFacilityRequest request,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		Facility facility = createFacilityUseCase.execute(request.toCommand(), context);

		return ResponseEntity.created(URI.create("/api/v1/facilities/" + facility.id().value()))
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(FacilityResponse.from(facility));
	}

	/**
	 * Retrieves a facility by its unique identifier.
	 *
	 * @param id facility UUID
	 * @return facility response
	 */
	@GetMapping("/{id}")
	@RequirePermission("organization:facility:read")
	public ResponseEntity<FacilityResponse> getById(@PathVariable UUID id) {
		Facility facility = findFacilityUseCase.findById(new FacilityId(id))
				.orElseThrow(() -> new FacilityNotFoundException(new FacilityId(id)));
		return ResponseEntity.ok(FacilityResponse.from(facility));
	}

	/**
	 * Lists facilities filtered optionally by type and status.
	 *
	 * @param type optional facility type filter
	 * @param status optional facility status filter
	 * @return list of facility responses
	 */
	@GetMapping
	@RequirePermission("organization:facility:read")
	public ResponseEntity<List<FacilityResponse>> list(
			@RequestParam(required = false) FacilityType type,
			@RequestParam(required = false) FacilityStatus status) {
		List<Facility> facilities = findFacilityUseCase.findAll(type, status);
		return ResponseEntity.ok(facilities.stream().map(FacilityResponse::from).toList());
	}

	/**
	 * Updates facility details.
	 *
	 * @param id facility UUID
	 * @param request validated update request
	 * @param httpRequest HTTP servlet request
	 * @return updated facility response
	 */
	@PutMapping("/{id}")
	@RequirePermission("organization:facility:update")
	public ResponseEntity<FacilityResponse> update(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateFacilityRequest request,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		Facility updated = updateFacilityUseCase.execute(request.toCommand(new FacilityId(id)), context);

		return ResponseEntity.ok()
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(FacilityResponse.from(updated));
	}

	/**
	 * Changes the operational status of a facility.
	 *
	 * @param id facility UUID
	 * @param request validated status change request
	 * @param httpRequest HTTP servlet request
	 * @return updated facility response
	 */
	@PatchMapping("/{id}/status")
	@RequirePermission("organization:facility:status")
	public ResponseEntity<FacilityResponse> changeStatus(
			@PathVariable UUID id,
			@Valid @RequestBody ChangeFacilityStatusRequest request,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		Facility changed = changeFacilityStatusUseCase.execute(request.toCommand(new FacilityId(id)), context);

		return ResponseEntity.ok()
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(FacilityResponse.from(changed));
	}
}

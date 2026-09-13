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
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnit;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitId;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitNotFoundException;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitStatus;
import io.github.edmaputra.uwati.domain.organization.ServiceUnitType;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeServiceUnitStatusUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateServiceUnitUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.FindServiceUnitUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateServiceUnitUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import lombok.RequiredArgsConstructor;

/**
 * REST controller managing service units within healthcare facilities.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@RestController
@RequestMapping("/api/v1/service-units")
@RequiredArgsConstructor
public class ServiceUnitController {

	private final CreateServiceUnitUseCase createServiceUnitUseCase;
	private final UpdateServiceUnitUseCase updateServiceUnitUseCase;
	private final ChangeServiceUnitStatusUseCase changeServiceUnitStatusUseCase;
	private final FindServiceUnitUseCase findServiceUnitUseCase;

	/**
	 * Creates a new service unit within a facility.
	 *
	 * @param request validated creation request
	 * @param httpRequest HTTP servlet request
	 * @return 201 Created with Location header and ServiceUnitResponse
	 */
	@PostMapping
	@RequirePermission("organization:service-unit:create")
	public ResponseEntity<ServiceUnitResponse> create(
			@Valid @RequestBody CreateServiceUnitRequest request,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		ServiceUnit serviceUnit = createServiceUnitUseCase.execute(request.toCommand(), context);

		return ResponseEntity.created(URI.create("/api/v1/service-units/" + serviceUnit.id().value()))
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(ServiceUnitResponse.from(serviceUnit));
	}

	/**
	 * Retrieves a service unit by its unique identifier.
	 *
	 * @param id service unit UUID
	 * @return service unit response
	 */
	@GetMapping("/{id}")
	@RequirePermission("organization:service-unit:read")
	public ResponseEntity<ServiceUnitResponse> getById(@PathVariable UUID id) {
		ServiceUnit serviceUnit = findServiceUnitUseCase.findById(new ServiceUnitId(id))
				.orElseThrow(() -> new ServiceUnitNotFoundException(new ServiceUnitId(id)));
		return ResponseEntity.ok(ServiceUnitResponse.from(serviceUnit));
	}

	/**
	 * Lists service units belonging to a facility, optionally filtered by type and status.
	 *
	 * @param facilityId parent facility UUID
	 * @param type optional service unit type filter
	 * @param status optional service unit status filter
	 * @return list of service unit responses
	 */
	@GetMapping
	@RequirePermission("organization:service-unit:read")
	public ResponseEntity<List<ServiceUnitResponse>> list(
			@RequestParam UUID facilityId,
			@RequestParam(required = false) ServiceUnitType type,
			@RequestParam(required = false) ServiceUnitStatus status) {
		List<ServiceUnit> units = findServiceUnitUseCase.findByFacilityId(new FacilityId(facilityId), type, status);
		return ResponseEntity.ok(units.stream().map(ServiceUnitResponse::from).toList());
	}

	/**
	 * Updates service unit details.
	 *
	 * @param id service unit UUID
	 * @param request validated update request
	 * @param httpRequest HTTP servlet request
	 * @return updated service unit response
	 */
	@PutMapping("/{id}")
	@RequirePermission("organization:service-unit:update")
	public ResponseEntity<ServiceUnitResponse> update(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateServiceUnitRequest request,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		ServiceUnit updated = updateServiceUnitUseCase.execute(request.toCommand(new ServiceUnitId(id)), context);

		return ResponseEntity.ok()
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(ServiceUnitResponse.from(updated));
	}

	/**
	 * Changes the operational status of a service unit.
	 *
	 * @param id service unit UUID
	 * @param request validated status change request
	 * @param httpRequest HTTP servlet request
	 * @return updated service unit response
	 */
	@PatchMapping("/{id}/status")
	@RequirePermission("organization:service-unit:status")
	public ResponseEntity<ServiceUnitResponse> changeStatus(
			@PathVariable UUID id,
			@Valid @RequestBody ChangeServiceUnitStatusRequest request,
			HttpServletRequest httpRequest) {
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		ServiceUnit changed = changeServiceUnitStatusUseCase.execute(request.toCommand(new ServiceUnitId(id)), context);

		return ResponseEntity.ok()
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(ServiceUnitResponse.from(changed));
	}
}

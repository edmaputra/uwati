package io.github.edmaputra.uwati.adapter.rest.organization;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

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

@RestController
@RequestMapping("/api/v1/service-units")
@RequiredArgsConstructor
public class ServiceUnitController {

	private final CreateServiceUnitUseCase createServiceUnitUseCase;
	private final UpdateServiceUnitUseCase updateServiceUnitUseCase;
	private final ChangeServiceUnitStatusUseCase changeServiceUnitStatusUseCase;
	private final FindServiceUnitUseCase findServiceUnitUseCase;

	@PostMapping
	public ResponseEntity<ServiceUnitResponse> create(
			@RequestBody CreateServiceUnitRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		ServiceUnit serviceUnit = createServiceUnitUseCase.execute(request.toCommand(), context);

		return ResponseEntity.created(URI.create("/api/v1/service-units/" + serviceUnit.id().value()))
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(ServiceUnitResponse.from(serviceUnit));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ServiceUnitResponse> getById(@PathVariable UUID id) {
		ServiceUnit serviceUnit = findServiceUnitUseCase.findById(new ServiceUnitId(id))
				.orElseThrow(() -> new ServiceUnitNotFoundException(new ServiceUnitId(id)));
		return ResponseEntity.ok(ServiceUnitResponse.from(serviceUnit));
	}

	@GetMapping
	public ResponseEntity<List<ServiceUnitResponse>> list(
			@RequestParam UUID facilityId,
			@RequestParam(required = false) ServiceUnitType type,
			@RequestParam(required = false) ServiceUnitStatus status) {
		List<ServiceUnit> units = findServiceUnitUseCase.findByFacilityId(new FacilityId(facilityId), type, status);
		return ResponseEntity.ok(units.stream().map(ServiceUnitResponse::from).toList());
	}

	@PutMapping("/{id}")
	public ResponseEntity<ServiceUnitResponse> update(
			@PathVariable UUID id,
			@RequestBody UpdateServiceUnitRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		ServiceUnit updated = updateServiceUnitUseCase.execute(request.toCommand(new ServiceUnitId(id)), context);

		return ResponseEntity.ok()
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(ServiceUnitResponse.from(updated));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<ServiceUnitResponse> changeStatus(
			@PathVariable UUID id,
			@RequestBody ChangeServiceUnitStatusRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		ServiceUnit changed = changeServiceUnitStatusUseCase.execute(request.toCommand(new ServiceUnitId(id)), context);

		return ResponseEntity.ok()
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(ServiceUnitResponse.from(changed));
	}
}

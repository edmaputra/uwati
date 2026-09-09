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
import io.github.edmaputra.uwati.domain.organization.Facility;
import io.github.edmaputra.uwati.domain.organization.FacilityId;
import io.github.edmaputra.uwati.domain.organization.FacilityNotFoundException;
import io.github.edmaputra.uwati.domain.organization.FacilityStatus;
import io.github.edmaputra.uwati.domain.organization.FacilityType;
import io.github.edmaputra.uwati.domain.organization.port.in.ChangeFacilityStatusUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.CreateFacilityUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.FindFacilityUseCase;
import io.github.edmaputra.uwati.domain.organization.port.in.UpdateFacilityUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/facilities")
@RequiredArgsConstructor
public class FacilityController {

	private final CreateFacilityUseCase createFacilityUseCase;
	private final UpdateFacilityUseCase updateFacilityUseCase;
	private final ChangeFacilityStatusUseCase changeFacilityStatusUseCase;
	private final FindFacilityUseCase findFacilityUseCase;

	@PostMapping
	public ResponseEntity<FacilityResponse> create(
			@RequestBody CreateFacilityRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		Facility facility = createFacilityUseCase.execute(request.toCommand(), context);

		return ResponseEntity.created(URI.create("/api/v1/facilities/" + facility.id().value()))
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(FacilityResponse.from(facility));
	}

	@GetMapping("/{id}")
	public ResponseEntity<FacilityResponse> getById(@PathVariable UUID id) {
		Facility facility = findFacilityUseCase.findById(new FacilityId(id))
				.orElseThrow(() -> new FacilityNotFoundException(new FacilityId(id)));
		return ResponseEntity.ok(FacilityResponse.from(facility));
	}

	@GetMapping
	public ResponseEntity<List<FacilityResponse>> list(
			@RequestParam(required = false) FacilityType type,
			@RequestParam(required = false) FacilityStatus status) {
		List<Facility> facilities = findFacilityUseCase.findAll(type, status);
		return ResponseEntity.ok(facilities.stream().map(FacilityResponse::from).toList());
	}

	@PutMapping("/{id}")
	public ResponseEntity<FacilityResponse> update(
			@PathVariable UUID id,
			@RequestBody UpdateFacilityRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		Facility updated = updateFacilityUseCase.execute(request.toCommand(new FacilityId(id)), context);

		return ResponseEntity.ok()
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(FacilityResponse.from(updated));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<FacilityResponse> changeStatus(
			@PathVariable UUID id,
			@RequestBody ChangeFacilityStatusRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = OperationContextResolver.resolve(httpRequest);
		Facility changed = changeFacilityStatusUseCase.execute(request.toCommand(new FacilityId(id)), context);

		return ResponseEntity.ok()
				.header(OperationContextResolver.CORRELATION_ID_HEADER, context.correlationId())
				.body(FacilityResponse.from(changed));
	}
}

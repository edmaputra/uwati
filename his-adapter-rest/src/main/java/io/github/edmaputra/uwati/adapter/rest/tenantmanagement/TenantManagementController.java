package io.github.edmaputra.uwati.adapter.rest.tenantmanagement;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.github.edmaputra.uwati.core.tenancy.application.port.in.CreateTenantCommand;
import io.github.edmaputra.uwati.core.tenancy.application.port.in.CreateTenantUseCase;
import io.github.edmaputra.uwati.core.tenancy.domain.DuplicateTenantDisplayNameException;
import io.github.edmaputra.uwati.core.tenancy.domain.Tenant;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/platform/tenants")
@RequiredArgsConstructor
public class TenantManagementController {

	private final CreateTenantUseCase createTenantUseCase;

	@PostMapping
	public ResponseEntity<TenantResponse> createTenant(@RequestBody CreateTenantRequest request) {
		try {
			Tenant tenant =
					createTenantUseCase.execute(new CreateTenantCommand(request.legalName(), request.displayName()));
			return ResponseEntity.status(HttpStatus.CREATED).body(TenantResponse.from(tenant));
		}
		catch (DuplicateTenantDisplayNameException exception) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}

	public record CreateTenantRequest(String legalName, String displayName) {
	}

	public record TenantResponse(
			String id,
			String legalName,
			String displayName,
			String status,
			Instant createdAt,
			Instant updatedAt) {

		static TenantResponse from(Tenant tenant) {
			return new TenantResponse(
					tenant.id().toString(),
					tenant.legalName(),
					tenant.displayName(),
					tenant.status().name(),
					tenant.createdAt(),
					tenant.updatedAt());
		}
	}
}

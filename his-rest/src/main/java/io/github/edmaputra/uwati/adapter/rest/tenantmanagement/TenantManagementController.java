package io.github.edmaputra.uwati.adapter.rest.tenantmanagement;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand.SettingEntry;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.GetTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.domain.DuplicateTenantDisplayNameException;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantNotFoundException;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/platform/tenants")
@RequiredArgsConstructor
public class TenantManagementController {

	private final CreateTenantUseCase createTenantUseCase;
	private final ConfigureTenantSettingsUseCase configureTenantSettingsUseCase;
	private final GetTenantSettingsUseCase getTenantSettingsUseCase;

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

	@GetMapping("/{tenantId}/settings")
	public ResponseEntity<List<TenantSettingResponse>> getSettings(@PathVariable String tenantId) {
		try {
			List<TenantSetting> settings = getTenantSettingsUseCase.execute(TenantId.from(tenantId));
			return ResponseEntity.ok(settings.stream().map(TenantSettingResponse::from).toList());
		}
		catch (TenantNotFoundException exception) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}

	@PutMapping("/{tenantId}/settings")
	public ResponseEntity<List<TenantSettingResponse>> configureSettings(
			@PathVariable String tenantId,
			@RequestBody ConfigureTenantSettingsRequest request) {
		try {
			if (request == null || request.settings() == null) {
				throw new IllegalArgumentException("Settings list must not be null.");
			}
			List<SettingEntry> entries = request.settings().stream()
					.map(s -> new SettingEntry(s.key(), s.value()))
					.toList();
			List<TenantSetting> updated = configureTenantSettingsUseCase.execute(
					new ConfigureTenantSettingsCommand(TenantId.from(tenantId), entries));
			return ResponseEntity.ok(updated.stream().map(TenantSettingResponse::from).toList());
		}
		catch (TenantNotFoundException exception) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
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

	public record ConfigureTenantSettingsRequest(List<TenantSettingItem> settings) {
		public record TenantSettingItem(String key, String value) {
		}
	}

	public record TenantSettingResponse(String key, String value, int revision) {
		static TenantSettingResponse from(TenantSetting setting) {
			return new TenantSettingResponse(setting.key(), setting.value(), setting.revision());
		}
	}
}

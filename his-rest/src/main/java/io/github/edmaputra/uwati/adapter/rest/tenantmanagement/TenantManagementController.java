package io.github.edmaputra.uwati.adapter.rest.tenantmanagement;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.edmaputra.uwati.domain.tenancy.application.OperationContext;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsCommand.SettingEntry;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.ConfigureTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantCommand;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.CreateTenantUseCase;
import io.github.edmaputra.uwati.domain.tenancy.application.port.in.GetTenantSettingsUseCase;
import io.github.edmaputra.uwati.domain.tenancy.domain.Tenant;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.domain.tenancy.domain.TenantSetting;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/platform/tenants")
@RequiredArgsConstructor
public class TenantManagementController {

	public static final String ACTOR_HEADER = "X-Actor";
	public static final String ACTOR_ID_HEADER = "X-Actor-Id";
	public static final String USER_ID_HEADER = "X-User-Id";
	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	public static final String REQUEST_ID_HEADER = "X-Request-Id";

	private final CreateTenantUseCase createTenantUseCase;
	private final ConfigureTenantSettingsUseCase configureTenantSettingsUseCase;
	private final GetTenantSettingsUseCase getTenantSettingsUseCase;

	@PostMapping
	public ResponseEntity<TenantResponse> createTenant(
			@RequestBody CreateTenantRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			throw new IllegalArgumentException("Request body must not be null.");
		}
		OperationContext context = resolveContext(httpRequest);
		Tenant tenant =
				createTenantUseCase.execute(new CreateTenantCommand(request.legalName(), request.displayName()), context);
		return ResponseEntity.status(HttpStatus.CREATED)
				.header(CORRELATION_ID_HEADER, context.correlationId())
				.body(TenantResponse.from(tenant));
	}

	@GetMapping("/{tenantId}/settings")
	public ResponseEntity<List<TenantSettingResponse>> getSettings(@PathVariable String tenantId) {
		List<TenantSetting> settings = getTenantSettingsUseCase.execute(TenantId.from(tenantId));
		return ResponseEntity.ok(settings.stream().map(TenantSettingResponse::from).toList());
	}

	@PutMapping("/{tenantId}/settings")
	public ResponseEntity<List<TenantSettingResponse>> configureSettings(
			@PathVariable String tenantId,
			@RequestBody ConfigureTenantSettingsRequest request,
			HttpServletRequest httpRequest) {
		if (request == null || request.settings() == null) {
			throw new IllegalArgumentException("Settings list must not be null.");
		}
		OperationContext context = resolveContext(httpRequest);
		List<SettingEntry> entries = request.settings().stream()
				.map(s -> new SettingEntry(s.key(), s.value()))
				.toList();
		List<TenantSetting> updated = configureTenantSettingsUseCase.execute(
				new ConfigureTenantSettingsCommand(TenantId.from(tenantId), entries), context);
		return ResponseEntity.ok()
				.header(CORRELATION_ID_HEADER, context.correlationId())
				.body(updated.stream().map(TenantSettingResponse::from).toList());
	}

	private OperationContext resolveContext(HttpServletRequest request) {
		String actor = request.getHeader(ACTOR_ID_HEADER);
		if (actor == null || actor.isBlank()) {
			actor = request.getHeader(ACTOR_HEADER);
		}
		if (actor == null || actor.isBlank()) {
			actor = request.getHeader(USER_ID_HEADER);
		}
		if (actor == null || actor.isBlank()) {
			actor = "system";
		}

		String correlationId = request.getHeader(CORRELATION_ID_HEADER);
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = request.getHeader(REQUEST_ID_HEADER);
		}
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}

		return OperationContext.of(actor.trim(), correlationId.trim());
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

package io.github.edmaputra.uwati.adapter.rest.tenantmanagement;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import lombok.RequiredArgsConstructor;

/**
 * Controller for platform superadmin tenant lifecycle and configuration management.
 *
 * @author edmaputra
 * @since 0.0.1
 */
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

	/**
	 * Provisions a new tenant organization.
	 *
	 * @param request validated tenant creation request
	 * @param httpRequest HTTP servlet request
	 * @return 201 Created with Location header and TenantResponse
	 */
	@PostMapping
	public ResponseEntity<TenantResponse> createTenant(
			@Valid @RequestBody CreateTenantRequest request,
			HttpServletRequest httpRequest) {
		OperationContext context = resolveContext(httpRequest);
		Tenant tenant =
				createTenantUseCase.execute(new CreateTenantCommand(request.legalName(), request.displayName()), context);
		URI location = URI.create("/api/platform/tenants/" + tenant.id().value());
		return ResponseEntity.created(location)
				.header(CORRELATION_ID_HEADER, context.correlationId())
				.body(TenantResponse.from(tenant));
	}

	/**
	 * Retrieves all configured settings for a specific tenant.
	 *
	 * @param tenantId target tenant ID string
	 * @return list of tenant settings
	 */
	@GetMapping("/{tenantId}/settings")
	public ResponseEntity<List<TenantSettingResponse>> getSettings(@PathVariable String tenantId) {
		List<TenantSetting> settings = getTenantSettingsUseCase.execute(TenantId.from(tenantId));
		return ResponseEntity.ok(settings.stream().map(TenantSettingResponse::from).toList());
	}

	/**
	 * Configures or updates settings for a specific tenant.
	 *
	 * @param tenantId target tenant ID string
	 * @param request validated settings payload
	 * @param httpRequest HTTP servlet request
	 * @return updated tenant settings
	 */
	@PutMapping("/{tenantId}/settings")
	public ResponseEntity<List<TenantSettingResponse>> configureSettings(
			@PathVariable String tenantId,
			@Valid @RequestBody ConfigureTenantSettingsRequest request,
			HttpServletRequest httpRequest) {
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

	/**
	 * Request payload for creating a new tenant.
	 *
	 * @param legalName the registered legal name of the organization
	 * @param displayName the public-facing display name
	 * @author edmaputra
	 * @since 0.0.1
	 */
	public record CreateTenantRequest(
			@NotBlank(message = "Legal name is required")
			String legalName,

			@NotBlank(message = "Display name is required")
			String displayName) {
	}

	/**
	 * Response representing tenant details.
	 *
	 * @param id tenant ID string
	 * @param legalName registered legal name
	 * @param displayName display name
	 * @param status lifecycle status
	 * @param createdAt creation timestamp
	 * @param updatedAt last update timestamp
	 * @author edmaputra
	 * @since 0.0.1
	 */
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

	/**
	 * Request payload for configuring tenant settings.
	 *
	 * @param settings list of setting key-value pairs
	 * @author edmaputra
	 * @since 0.0.1
	 */
	public record ConfigureTenantSettingsRequest(
			@NotNull(message = "Settings list is required")
			@Valid
			List<TenantSettingItem> settings) {

		/**
		 * Individual key-value setting entry.
		 *
		 * @param key configuration key
		 * @param value configuration value
		 * @author edmaputra
		 * @since 0.0.1
		 */
		public record TenantSettingItem(
				@NotBlank(message = "Setting key is required")
				String key,

				@NotBlank(message = "Setting value is required")
				String value) {
		}
	}

	/**
	 * Response representing a tenant setting.
	 *
	 * @param key configuration key
	 * @param value configuration value
	 * @param revision optimistic concurrency revision number
	 * @author edmaputra
	 * @since 0.0.1
	 */
	public record TenantSettingResponse(String key, String value, int revision) {
		static TenantSettingResponse from(TenantSetting setting) {
			return new TenantSettingResponse(setting.key(), setting.value(), setting.revision());
		}
	}
}

package io.github.edmaputra.uwati.domain.security;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CurrentActor} default methods.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@DisplayName("CurrentActor Unit Tests")
class CurrentActorTests {

	record TestActor(
			UUID userId,
			String email,
			UUID tenantId,
			boolean isPlatformSuperAdmin,
			boolean isTenantWide,
			Set<String> groups,
			Set<String> roles,
			Set<String> permissions,
			Set<UUID> accessibleScopeNodeIds) implements CurrentActor {
	}

	@Test
	@DisplayName("hasPermission should return true for platform super admin regardless of permission set")
	void hasPermission_whenPlatformSuperAdmin_returnsTrue() {
		CurrentActor actor = new TestActor(
				UUID.randomUUID(), "super@admin.org", UUID.randomUUID(),
				true, false, Set.of(), Set.of(), Set.of(), Set.of());

		assertThat(actor.hasPermission("ANY_PERMISSION")).isTrue();
	}

	@Test
	@DisplayName("hasPermission should check permission set for regular actor")
	void hasPermission_whenRegularActor_checksPermissionSet() {
		CurrentActor actor = new TestActor(
				UUID.randomUUID(), "doctor@hospital.org", UUID.randomUUID(),
				false, false, Set.of(), Set.of(), Set.of("READ_PATIENT"), Set.of());

		assertThat(actor.hasPermission("READ_PATIENT")).isTrue();
		assertThat(actor.hasPermission("DELETE_PATIENT")).isFalse();
	}

	@Test
	@DisplayName("canAccessScope should return true for super admin or tenant-wide actor")
	void canAccessScope_whenSuperAdminOrTenantWide_returnsTrue() {
		CurrentActor superAdmin = new TestActor(
				UUID.randomUUID(), "super@admin.org", UUID.randomUUID(),
				true, false, Set.of(), Set.of(), Set.of(), Set.of());

		CurrentActor tenantWide = new TestActor(
				UUID.randomUUID(), "admin@hospital.org", UUID.randomUUID(),
				false, true, Set.of(), Set.of(), Set.of(), Set.of());

		UUID scopeId = UUID.randomUUID();
		assertThat(superAdmin.canAccessScope(scopeId)).isTrue();
		assertThat(tenantWide.canAccessScope(scopeId)).isTrue();
	}

	@Test
	@DisplayName("canAccessScope should verify targetScopeNodeId against accessibleScopeNodeIds")
	void canAccessScope_whenScopeConstrained_checksAccessibleScopeNodeIds() {
		UUID allowedScope = UUID.randomUUID();
		UUID deniedScope = UUID.randomUUID();

		CurrentActor actor = new TestActor(
				UUID.randomUUID(), "staff@hospital.org", UUID.randomUUID(),
				false, false, Set.of(), Set.of(), Set.of(), Set.of(allowedScope));

		assertThat(actor.canAccessScope(allowedScope)).isTrue();
		assertThat(actor.canAccessScope(deniedScope)).isFalse();
		assertThat(actor.canAccessScope(null)).isFalse();
	}
}

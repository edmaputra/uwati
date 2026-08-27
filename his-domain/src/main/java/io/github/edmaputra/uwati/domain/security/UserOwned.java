package io.github.edmaputra.uwati.domain.security;

import java.util.UUID;

/**
 * Indicates that an entity has an individual creator or single owner.
 */
public interface UserOwned {

	UUID ownerUserId();

	default boolean isOwnedBy(UUID userId) {
		return ownerUserId() != null && ownerUserId().equals(userId);
	}
}

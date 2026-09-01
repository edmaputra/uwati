package io.github.edmaputra.uwati.domain.security;

import java.util.UUID;

/**
 * Marks a domain entity or aggregate as owned by a specific individual user (e.g. private draft, doctor's personal notes).
 *
 * @author edmaputra
 */
public interface UserOwned {

	/**
	 * Returns the UUID of the owning user.
	 *
	 * @return user UUID
	 */
	UUID userId();
}

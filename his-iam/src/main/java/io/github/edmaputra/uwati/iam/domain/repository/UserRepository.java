package io.github.edmaputra.uwati.iam.domain.repository;

import java.util.List;
import java.util.Optional;

import io.github.edmaputra.uwati.domain.tenancy.domain.TenantId;
import io.github.edmaputra.uwati.iam.domain.model.User;
import io.github.edmaputra.uwati.iam.domain.model.UserId;

/**
 * Domain repository port for managing {@link User} entities.
 *
 * @author edmaputra
 */
public interface UserRepository {

	/**
	 * Finds a user by unique identifier.
	 *
	 * @param id the unique user ID
	 * @return optional containing the user if found
	 */
	Optional<User> findById(UserId id);

	/**
	 * Finds a user by unique email address (case-insensitive).
	 *
	 * @param email the user email address
	 * @return optional containing the user if found
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Checks if a user exists with the given email address (case-insensitive).
	 *
	 * @param email the email address to check
	 * @return true if an account exists with this email
	 */
	boolean existsByEmail(String email);

	/**
	 * Finds all users matching the specified user IDs.
	 *
	 * @param ids iterable of user IDs
	 * @return list of matching users
	 */
	List<User> findAllByIds(Iterable<UserId> ids);

	/**
	 * Finds all users in the system.
	 *
	 * @return list of all users
	 */
	List<User> findAll();

	/**
	 * Finds all users associated with a specific tenant.
	 *
	 * @param tenantId the tenant ID
	 * @return list of users in the tenant
	 */
	List<User> findAllByTenantId(TenantId tenantId);

	/**
	 * Saves or updates a user entity.
	 *
	 * @param user the user to persist
	 * @return the persisted user
	 */
	User save(User user);

	/**
	 * Deletes a user by unique identifier.
	 *
	 * @param id the unique user ID
	 */
	void delete(UserId id);
}


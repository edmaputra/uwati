package io.github.edmaputra.uwati.cache.port;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Outbound port interface for acquiring and releasing distributed locks across cluster nodes.
 */
public interface DistributedLockPort {

	/**
	 * Attempts to acquire a distributed lock atomically.
	 *
	 * @param lockKey the unique lock resource key
	 * @param lockValue unique ownership identifier (e.g. UUID)
	 * @param leaseTime maximum duration before the lock expires automatically
	 * @return {@code true} if the lock was acquired, {@code false} otherwise
	 */
	boolean acquire(String lockKey, String lockValue, Duration leaseTime);

	/**
	 * Releases a previously acquired distributed lock only if the ownership token matches.
	 *
	 * @param lockKey the unique lock resource key
	 * @param lockValue the ownership identifier that acquired the lock
	 * @return {@code true} if the lock was released, {@code false} otherwise
	 */
	boolean release(String lockKey, String lockValue);

	/**
	 * Executes a task within a distributed lock, acquiring and releasing it automatically.
	 *
	 * @param <T> the result type
	 * @param lockKey the unique lock resource key
	 * @param leaseTime maximum lock duration
	 * @param task the supplier to execute while holding the lock
	 * @return an {@link Optional} containing the result, or {@link Optional#empty()} if lock could not be acquired
	 */
	<T> Optional<T> executeWithLock(String lockKey, Duration leaseTime, Supplier<T> task);
}

package io.github.edmaputra.uwati.cache.adapter;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import io.github.edmaputra.uwati.cache.port.DistributedLockPort;

/**
 * Adapter implementing {@link DistributedLockPort} using atomic Redis commands and Lua scripting.
 * <p>
 * Implements safe distributed locking semantics:
 * <ul>
 *   <li><b>Atomic Acquisition:</b> Uses {@code SETNX} with an automatic expiration lease to avoid deadlocks.</li>
 *   <li><b>Safe Release:</b> Uses an atomic Lua script to ensure a client only releases a lock if it still holds
 *       the matching ownership token (value), preventing accidental release of locks renewed by other threads.</li>
 * </ul>
 */
@Component
public class RedisDistributedLockAdapter implements DistributedLockPort {

	private static final Logger log = LoggerFactory.getLogger(RedisDistributedLockAdapter.class);

	private static final String UNLOCK_LUA_SCRIPT =
			"if redis.call('get', KEYS[1]) == ARGV[1] then " +
			"    return redis.call('del', KEYS[1]) " +
			"else " +
			"    return 0 " +
			"end";

	private final StringRedisTemplate redisTemplate;
	private final RedisScript<Long> unlockScript;

	/**
	 * Constructs the distributed lock adapter with the specified Redis template.
	 *
	 * @param redisTemplate the StringRedisTemplate for lock state
	 */
	public RedisDistributedLockAdapter(StringRedisTemplate redisTemplate) {
		this.redisTemplate = Objects.requireNonNull(redisTemplate, "RedisTemplate must not be null.");
		this.unlockScript = new DefaultRedisScript<>(UNLOCK_LUA_SCRIPT, Long.class);
	}

	@Override
	public boolean acquire(String lockKey, String lockValue, Duration leaseTime) {
		Objects.requireNonNull(lockKey, "Lock key must not be null.");
		Objects.requireNonNull(lockValue, "Lock value must not be null.");
		Objects.requireNonNull(leaseTime, "Lease time must not be null.");

		try {
			Boolean acquired = redisTemplate.opsForValue()
					.setIfAbsent(lockKey, lockValue, leaseTime);
			return Boolean.TRUE.equals(acquired);
		}
		catch (Exception e) {
			log.warn("Failed to acquire distributed lock for key '{}': {}", lockKey, e.getMessage());
			return false;
		}
	}

	@Override
	public boolean release(String lockKey, String lockValue) {
		Objects.requireNonNull(lockKey, "Lock key must not be null.");
		Objects.requireNonNull(lockValue, "Lock value must not be null.");

		try {
			Long result = redisTemplate.execute(
					unlockScript,
					Collections.singletonList(lockKey),
					lockValue);
			return result != null && result > 0;
		}
		catch (Exception e) {
			log.warn("Failed to release distributed lock for key '{}': {}", lockKey, e.getMessage());
			return false;
		}
	}

	@Override
	public <T> Optional<T> executeWithLock(String lockKey, Duration leaseTime, Supplier<T> task) {
		String lockValue = UUID.randomUUID().toString();
		boolean acquired = acquire(lockKey, lockValue, leaseTime);
		if (!acquired) {
			return Optional.empty();
		}

		try {
			return Optional.ofNullable(task.get());
		}
		finally {
			release(lockKey, lockValue);
		}
	}
}

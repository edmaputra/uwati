package io.github.edmaputra.uwati.cache.port;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public interface DistributedLockPort {

	boolean acquire(String lockKey, String lockValue, Duration leaseTime);

	boolean release(String lockKey, String lockValue);

	<T> Optional<T> executeWithLock(String lockKey, Duration leaseTime, Supplier<T> task);
}

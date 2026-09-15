package io.github.edmaputra.uwati.adapter.persistence.audit;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for audit log entry persistence and querying.
 * <p>
 * Provides relational database access operations for audit entries in the
 * hexagonal architecture's outbound persistence adapter.
 *
 * @author edmaputra
 * @since 0.0.1
 */
public interface AuditEntryJpaRepository extends JpaRepository<AuditEntryEntity, Long> {

	/**
	 * Finds all audit entries for a specific entity name and ID, sorted by timestamp descending.
	 *
	 * @param entityName name of the domain entity
	 * @param entityId unique identifier of the entity
	 * @return list of audit entries sorted by occurrence timestamp descending
	 */
	List<AuditEntryEntity> findByEntityNameAndEntityIdOrderByOccurredAtDesc(String entityName, String entityId);

	/**
	 * Finds all audit entries for a given tenant ID, sorted by timestamp descending.
	 *
	 * @param tenantId unique identifier of the tenant
	 * @return list of audit entries for the tenant sorted by occurrence timestamp descending
	 */
	List<AuditEntryEntity> findByTenantIdOrderByOccurredAtDesc(UUID tenantId);

	/**
	 * Finds all audit entries matching a correlation ID, sorted by timestamp descending.
	 *
	 * @param correlationId correlation tracking identifier
	 * @return list of audit entries matching the correlation ID sorted descending
	 */
	List<AuditEntryEntity> findByCorrelationIdOrderByOccurredAtDesc(String correlationId);
}

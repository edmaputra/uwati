package io.github.edmaputra.uwati.adapter.persistence.audit;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEntryJpaRepository extends JpaRepository<AuditEntryEntity, Long> {

	List<AuditEntryEntity> findByEntityNameAndEntityIdOrderByOccurredAtDesc(String entityName, String entityId);

	List<AuditEntryEntity> findByTenantIdOrderByOccurredAtDesc(UUID tenantId);

	List<AuditEntryEntity> findByCorrelationIdOrderByOccurredAtDesc(String correlationId);
}

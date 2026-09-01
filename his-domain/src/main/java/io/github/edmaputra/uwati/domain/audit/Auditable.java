package io.github.edmaputra.uwati.domain.audit;

import java.util.Map;

/**
 * Interface implemented by domain models and aggregates to declare which
 * specific fields should be monitored and recorded in the audit trail.
 *
 * @author edmaputra
 */
public interface Auditable {

	/**
	 * Returns a map of field names to their values that should be monitored for audit changes.
	 * Only the fields returned by this method will be tracked and diffed.
	 *
	 * @return map of auditable field names to their current values
	 */
	Map<String, Object> auditableFields();
}

package io.github.edmaputra.uwati.iam.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Catalog of system permissions and permission groupings across Uwati HIS.
 *
 * @author edmaputra
 */
public final class Permissions {

	private Permissions() {}

	// IAM & Administration
	public static final String IAM_USER_READ = "IAM_USER_READ";
	public static final String IAM_USER_WRITE = "IAM_USER_WRITE";
	public static final String IAM_ROLE_READ = "IAM_ROLE_READ";
	public static final String IAM_ROLE_WRITE = "IAM_ROLE_WRITE";
	public static final String IAM_GROUP_READ = "IAM_GROUP_READ";
	public static final String IAM_GROUP_WRITE = "IAM_GROUP_WRITE";
	public static final String IAM_SCOPE_READ = "IAM_SCOPE_READ";
	public static final String IAM_SCOPE_WRITE = "IAM_SCOPE_WRITE";

	// Patient & Clinical
	public static final String PATIENT_READ = "PATIENT_READ";
	public static final String PATIENT_WRITE = "PATIENT_WRITE";
	public static final String CLINICAL_NOTE_READ = "CLINICAL_NOTE_READ";
	public static final String CLINICAL_NOTE_WRITE = "CLINICAL_NOTE_WRITE";
	public static final String PRESCRIPTION_CREATE = "PRESCRIPTION_CREATE";
	public static final String PRESCRIPTION_READ = "PRESCRIPTION_READ";

	// Pharmacy & Inventory
	public static final String PHARMACY_DISPENSE = "PHARMACY_DISPENSE";
	public static final String MEDICINE_READ = "MEDICINE_READ";
	public static final String MEDICINE_WRITE = "MEDICINE_WRITE";

	// Billing & Reports
	public static final String BILLING_READ = "BILLING_READ";
	public static final String BILLING_WRITE = "BILLING_WRITE";
	public static final String REPORT_READ = "REPORT_READ";

	private static final Set<String> ALL_PERMISSIONS = Set.of(
			IAM_USER_READ, IAM_USER_WRITE,
			IAM_ROLE_READ, IAM_ROLE_WRITE,
			IAM_GROUP_READ, IAM_GROUP_WRITE,
			IAM_SCOPE_READ, IAM_SCOPE_WRITE,
			PATIENT_READ, PATIENT_WRITE,
			CLINICAL_NOTE_READ, CLINICAL_NOTE_WRITE,
			PRESCRIPTION_CREATE, PRESCRIPTION_READ,
			PHARMACY_DISPENSE, MEDICINE_READ, MEDICINE_WRITE,
			BILLING_READ, BILLING_WRITE,
			REPORT_READ
	);

	private static final Map<String, List<String>> PERMISSIONS_BY_CATEGORY = Map.of(
			"IAM", List.of(IAM_USER_READ, IAM_USER_WRITE, IAM_ROLE_READ, IAM_ROLE_WRITE, IAM_GROUP_READ, IAM_GROUP_WRITE, IAM_SCOPE_READ, IAM_SCOPE_WRITE),
			"CLINICAL", List.of(PATIENT_READ, PATIENT_WRITE, CLINICAL_NOTE_READ, CLINICAL_NOTE_WRITE, PRESCRIPTION_CREATE, PRESCRIPTION_READ),
			"PHARMACY", List.of(PHARMACY_DISPENSE, MEDICINE_READ, MEDICINE_WRITE),
			"BILLING", List.of(BILLING_READ, BILLING_WRITE),
			"REPORTS", List.of(REPORT_READ)
	);

	/**
	 * Returns an unmodifiable set of all registered system permissions.
	 *
	 * @return set of all permissions
	 */
	public static Set<String> all() {
		return ALL_PERMISSIONS;
	}

	/**
	 * Returns an unmodifiable map of permissions grouped by category.
	 *
	 * @return map of category to permission list
	 */
	public static Map<String, List<String>> byCategory() {
		return Collections.unmodifiableMap(PERMISSIONS_BY_CATEGORY);
	}

	/**
	 * Checks if a permission string is a valid system permission.
	 *
	 * @param permission the permission code
	 * @return true if valid
	 */
	public static boolean isValid(String permission) {
		return permission != null && ALL_PERMISSIONS.contains(permission.trim().toUpperCase());
	}
}

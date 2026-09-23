package io.github.edmaputra.uwati.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HisDomain}.
 *
 * @author edmaputra
 * @since 0.0.1
 */
@DisplayName("HisDomain Unit Tests")
class HisDomainTests {

	@Test
	@DisplayName("values should return all defined bounded domains")
	void values_returnsAllBoundedDomains() {
		HisDomain[] domains = HisDomain.values();

		assertThat(domains).isNotEmpty();
		assertThat(HisDomain.valueOf("TENANCY")).isEqualTo(HisDomain.TENANCY);
		assertThat(HisDomain.valueOf("ORGANIZATION")).isEqualTo(HisDomain.ORGANIZATION);
	}
}

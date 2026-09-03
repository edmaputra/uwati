package io.github.edmaputra.uwati.iam;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.github.edmaputra.uwati.iam.adapter.rest.ScopeNodeController;
import io.github.edmaputra.uwati.iam.application.port.in.ManageScopeUseCase;
import io.github.edmaputra.uwati.iam.application.service.ScopeHierarchyService;
import io.github.edmaputra.uwati.iam.application.service.ScopeSubtreeResolver;
import io.github.edmaputra.uwati.iam.domain.repository.ScopeNodeRepository;

/**
 * Spring Boot auto-configuration for IAM hierarchical scope tree services and controllers.
 *
 * @author edmaputra
 */
@AutoConfiguration(after = IamJpaAutoConfiguration.class)
@Import(ScopeNodeController.class)
public class IamScopeAutoConfiguration {

	/**
	 * Registers the {@link ManageScopeUseCase} bean.
	 *
	 * @param scopeNodeRepository the scope node repository
	 * @param eventPublisher      the Spring application event publisher
	 * @return scope hierarchy management service
	 */
	@Bean
	@ConditionalOnMissingBean
	public ManageScopeUseCase manageScopeUseCase(
			ScopeNodeRepository scopeNodeRepository,
			ApplicationEventPublisher eventPublisher) {
		return new ScopeHierarchyService(scopeNodeRepository, eventPublisher);
	}

	/**
	 * Registers the {@link ScopeSubtreeResolver} bean.
	 *
	 * @param scopeNodeRepository the scope node repository
	 * @return scope subtree resolver engine
	 */
	@Bean
	@ConditionalOnMissingBean
	public ScopeSubtreeResolver scopeSubtreeResolver(ScopeNodeRepository scopeNodeRepository) {
		return new ScopeSubtreeResolver(scopeNodeRepository);
	}
}


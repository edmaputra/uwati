package io.github.edmaputra.uwati.iam;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.github.edmaputra.uwati.iam.adapter.persistence.adapter.ScopeNodeRepositoryAdapter;
import io.github.edmaputra.uwati.iam.adapter.persistence.repository.SpringDataScopeNodeRepository;
import io.github.edmaputra.uwati.iam.application.port.in.ManageScopeUseCase;
import io.github.edmaputra.uwati.iam.application.service.ScopeHierarchyService;
import io.github.edmaputra.uwati.iam.application.service.ScopeSubtreeResolver;
import io.github.edmaputra.uwati.iam.domain.repository.ScopeNodeRepository;

@AutoConfiguration
@EntityScan(basePackages = "io.github.edmaputra.uwati.iam.adapter.persistence.entity")
@EnableJpaRepositories(basePackages = "io.github.edmaputra.uwati.iam.adapter.persistence.repository")
public class IamScopeAutoConfiguration {

	@Bean
	public ScopeNodeRepository scopeNodeRepository(SpringDataScopeNodeRepository repository) {
		return new ScopeNodeRepositoryAdapter(repository);
	}

	@Bean
	public ManageScopeUseCase manageScopeUseCase(
			ScopeNodeRepository scopeNodeRepository,
			ApplicationEventPublisher eventPublisher) {
		return new ScopeHierarchyService(scopeNodeRepository, eventPublisher);
	}

	@Bean
	public ScopeSubtreeResolver scopeSubtreeResolver(ScopeNodeRepository scopeNodeRepository) {
		return new ScopeSubtreeResolver(scopeNodeRepository);
	}
}

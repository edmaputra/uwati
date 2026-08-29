package io.github.edmaputra.uwati.iam;

import javax.sql.DataSource;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@ConditionalOnClass(SpringLiquibase.class)
@ConditionalOnProperty(prefix = "spring.liquibase", name = "enabled", matchIfMissing = true)
public class IamLiquibaseAutoConfiguration {

	@Bean
	public SpringLiquibase iamLiquibase(DataSource dataSource) {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(dataSource);
		liquibase.setChangeLog("classpath:db/changelog/iam/db.changelog-iam.json");
		liquibase.setShouldRun(true);
		return liquibase;
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(EntityManagerFactoryDependsOnPostProcessor.class)
	static class IamLiquibaseEntityManagerDependencyConfiguration extends EntityManagerFactoryDependsOnPostProcessor {

		IamLiquibaseEntityManagerDependencyConfiguration() {
			super("iamLiquibase");
		}
	}

	@Configuration(proxyBeanMethods = false)
	static class IamLiquibaseDependencyConfiguration implements org.springframework.beans.factory.config.BeanFactoryPostProcessor {

		@Override
		public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) {
			if (beanFactory.containsBeanDefinition("iamLiquibase") && beanFactory.containsBeanDefinition("liquibase")) {
				org.springframework.beans.factory.config.BeanDefinition bd = beanFactory.getBeanDefinition("iamLiquibase");
				String[] existingDependsOn = bd.getDependsOn();
				if (existingDependsOn == null || existingDependsOn.length == 0) {
					bd.setDependsOn("liquibase");
				}
				else {
					String[] combined = java.util.Arrays.copyOf(existingDependsOn, existingDependsOn.length + 1);
					combined[existingDependsOn.length] = "liquibase";
					bd.setDependsOn(combined);
				}
			}
		}
	}
}


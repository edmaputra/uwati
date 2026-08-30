package io.github.edmaputra.uwati.bootstrap;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;

import liquibase.integration.spring.SpringLiquibase;

/**
 * Configuration for the core / master Liquibase database migrations.
 * <p>
 * Manages the execution of the base database changelog ({@code db.changelog-master.json})
 * which provisions fundamental schemas, tenancy structures, audit tables, and shared database objects.
 * Also configures JPA {@link jakarta.persistence.EntityManagerFactory} to depend on all
 * {@link SpringLiquibase} instances before initializing Hibernate.
 */
@Configuration
public class LiquibaseConfiguration {

	/**
	 * Configures the master {@link SpringLiquibase} migration runner.
	 *
	 * @param dataSource the shared application datasource
	 * @return configured master SpringLiquibase bean
	 */
	@Bean
	SpringLiquibase liquibase(DataSource dataSource) {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(dataSource);
		liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.json");
		return liquibase;
	}

	/**
	 * Ensures JPA {@link jakarta.persistence.EntityManagerFactory} initialization waits for all
	 * {@link SpringLiquibase} beans across all application modules to finish schema execution.
	 */
	@Configuration
	static class LiquibaseEntityManagerDependencyConfiguration extends EntityManagerFactoryDependsOnPostProcessor {

		LiquibaseEntityManagerDependencyConfiguration() {
			super(SpringLiquibase.class);
		}
	}
}

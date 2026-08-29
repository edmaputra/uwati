package io.github.edmaputra.uwati.bootstrap;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseConfiguration {

	@Bean
	SpringLiquibase liquibase(DataSource dataSource) {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(dataSource);
		liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.json");
		return liquibase;
	}

	@Configuration
	static class LiquibaseEntityManagerDependencyConfiguration extends EntityManagerFactoryDependsOnPostProcessor {

		LiquibaseEntityManagerDependencyConfiguration() {
			super(SpringLiquibase.class);
		}
	}
}

package org.example.lock.deadlock.config;

import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Profile("multi")
@Configuration
@EnableJpaRepositories(
    basePackages = "org.example.lock.deadlock.repository.lock",
    entityManagerFactoryRef = "lockEntityManagerFactory",
    transactionManagerRef = "lockTransactionManager"
)
public class LockPoolConfiguration {

    @Bean
    @ConfigurationProperties("spring.lock-datasource")
    public DataSourceProperties lockDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.lock-datasource.hikari")
    public DataSource lockDataSource() {
        return lockDataSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean lockEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
            .dataSource(lockDataSource())
            .packages("org.example.lock.deadlock.entity")
            .build();
    }

    @Bean
    public PlatformTransactionManager lockTransactionManager(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(Objects.requireNonNull(lockEntityManagerFactory(builder).getObject()));
    }
}

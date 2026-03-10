package org.example.lock.deadlock.config;

import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Profile("multi")
@Configuration
@EnableJpaRepositories(
    basePackages = "org.example.lock.deadlock.repository",
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "org.example.lock.deadlock.repository.lock.*"),
    entityManagerFactoryRef = "bizEntityManagerFactory",
    transactionManagerRef = "bizTransactionManager"
)

public class BizPoolConfiguration {

    @Bean
    @ConfigurationProperties("spring.biz-datasource")
    public DataSourceProperties bizDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.biz-datasource.hikari")
    public DataSource bizDataSource() {
        return bizDataSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean bizEntityManagerFactory(@Qualifier("bizDataSource") DataSource dataSource,
                                                                          EntityManagerFactoryBuilder builder) {
        return builder
            .dataSource(dataSource)
            .packages("org.example.lock.deadlock.entity")
            .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager bizTransactionManager(@Qualifier("bizEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {
        return new JpaTransactionManager(Objects.requireNonNull(factory.getObject()));
    }
}

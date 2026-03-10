package org.example.lock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class MultiCpTestDatabaseConfig {

    @Container
    public static final MySQLContainer<?> mysqlContainer;

    static {
        mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.33"))
            .withDatabaseName("lock_db")
            .withUsername("test")
            .withPassword("test");
        mysqlContainer.start();
    }

    @Bean
    MySQLContainer<?> mysqlContainer() {
        return mysqlContainer;
    }
}

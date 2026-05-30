package com.carddemo.transaction;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class FlywayMigrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("carddemo_transaction_db")
            .withUsername("carddemo")
            .withPassword("carddemo");

    @Test
    void flywayMigrationsApplyCleanly() {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();

        int migrations = flyway.migrate().migrationsExecuted;
        assertEquals(2, migrations, "Expected exactly 2 migrations to be applied");
    }
}

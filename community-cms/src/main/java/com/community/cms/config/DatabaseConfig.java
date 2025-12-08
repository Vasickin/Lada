package com.community.cms.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для миграций базы данных с использованием Flyway.
 *
 * @author Vasickin
 * @since 1.0
 */
@Configuration
public class DatabaseConfig {

    /**
     * Стратегия миграции Flyway.
     * Позволяет настраивать поведение миграций (например, очистку базы данных в dev-среде).
     *
     * @return Настроенная стратегия миграции
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // В продакшене миграции выполняются автоматически
            // В разработке можно настроить иначе, например, с очисткой базы
            flyway.migrate();
        };
    }
}

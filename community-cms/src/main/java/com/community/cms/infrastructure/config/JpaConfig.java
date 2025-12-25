package com.community.cms.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Конфигурация для включения JPA Auditing функциональности.
 *
 * <p>Аннотация {@link EnableJpaAuditing} активирует автоматическое заполнение
 * полей с аннотациями {@link org.springframework.data.annotation.CreatedDate}
 * и {@link org.springframework.data.annotation.LastModifiedDate}.</p>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see org.springframework.data.jpa.repository.config.EnableJpaAuditing
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // Конфигурация включается через аннотацию @EnableJpaAuditing
}

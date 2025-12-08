package com.community.cms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Конфигурация OpenAPI для автоматической генерации документации API.
 *
 * @author Vasickin
 * @since 1.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.name:Организация ЛАДА}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:REST API для системы управления проектами организации ЛАДА}")
    private String appDescription;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    /**
     * Конфигурация OpenAPI документации.
     *
     * @return Настроенный объект OpenAPI
     */
    @Bean
    public OpenAPI myOpenAPI() {
        Server server = new Server();
        server.setUrl(appUrl);
        server.setDescription("Сервер для окружения " + appUrl);

        Contact contact = new Contact();
        contact.setEmail("contact@lada.org");
        contact.setName("Организация ЛАДА");
        contact.setUrl("https://lada.org");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("API системы проектов")
                .version(appVersion)
                .contact(contact)
                .description(appDescription)
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}

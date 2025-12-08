package com.community.cms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Конфигурация для загрузки и отдачи файлов (изображений, документов и т.д.).
 *
 * @author Vasickin
 * @since 1.0
 */
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Добавляем обработчик ресурсов для раздачи загруженных файлов.
     *
     * @param registry Реестр обработчиков ресурсов
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Конвертируем путь загрузки в абсолютный
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadLocation = uploadPath.toUri().toString();

        // Регистрируем обработчик для URL, начинающихся с /uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation + "/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}

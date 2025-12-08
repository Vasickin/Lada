package com.community.cms.controller.admin.project;

import com.community.cms.dto.project.ProjectCreateRequest;
import com.community.cms.dto.project.ProjectDTO;
import com.community.cms.dto.project.ProjectFilter;
import com.community.cms.dto.project.ProjectUpdateRequest;
import com.community.cms.model.project.ProjectStatus;
import com.community.cms.service.project.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


//Описание файла:
//Административный REST-контроллер для управления проектами
//Аннотация @PreAuthorize для защиты доступа (требуются роли ADMIN или EDITOR)
//Расширенный набор эндпоинтов для админ-панели:
//CRUD операции над проектами
//Управление статусами проектов
//Проверка доступности slug
//Получение статистики проектов
//Клонирование проектов
//Экспорт в CSV
//Поиск проектов без обложки и с истекшими датами
//Массовое обновление SEO настроек
//Поддержка загрузки файлов (обложек проектов)
//Полная интеграция с OpenAPI/Swagger
//Логирование всех операций
//Обработка ошибок и валидация
//Гибкая пагинация и сортировка
//Особенности безопасности:
//Все эндпоинты требуют аутентификации
//Большинство операций доступны для ролей ADMIN и EDITOR
//Удаление проектов доступно только для роли ADMIN

/**
 * Административный контроллер для управления проектами организации "ЛАДА".
 * Предоставляет API для администраторов и редакторов для управления проектами.
 *
 * @author Vasickin
 * @since 1.0
 */
@RestController
@RequestMapping("/api/admin/projects")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
@Tag(name = "Проекты (административные)", description = "API для управления проектами организации (требуется аутентификация)")
public class AdminProjectController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProjectController.class);

    private final ProjectService projectService;

    /**
     * Конструктор контроллера
     *
     * @param projectService Сервис проектов
     */
    public AdminProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Получить список всех проектов (для админки) с пагинацией
     *
     * @param page Номер страницы (начиная с 0, по умолчанию 0)
     * @param size Размер страницы (по умолчанию 20)
     * @param sort Поле для сортировки (по умолчанию createdAt)
     * @param direction Направление сортировки (ASC или DESC, по умолчанию DESC)
     * @return Страница проектов
     */
    @Operation(
            summary = "Получить список всех проектов (админка)",
            description = "Возвращает пагинированный список всех проектов для админ-панели"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное получение списка проектов"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<Page<ProjectDTO>> getAllProjects(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Поле для сортировки", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sort,

            @Parameter(description = "Направление сортировки", example = "DESC")
            @RequestParam(defaultValue = "DESC") String direction) {

        logger.debug("Получение всех проектов (админка): page={}, size={}, sort={}, direction={}",
                page, size, sort, direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Direction.fromString(direction), sort
        ));

        Page<ProjectDTO> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Получить проекты с фильтрацией (для админки)
     *
     * @param filter Фильтр проектов
     * @param page Номер страницы (начиная с 0)
     * @param size Размер страницы
     * @return Страница отфильтрованных проектов
     */
    @Operation(
            summary = "Получить проекты с фильтрацией (админка)",
            description = "Возвращает проекты, отфильтрованные по категории, статусу и другим параметрам для админ-панели"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное получение отфильтрованных проектов"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры фильтрации"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/filter")
    public ResponseEntity<Page<ProjectDTO>> getProjectsWithFilter(
            @Parameter(description = "Параметры фильтрации")
            @ModelAttribute ProjectFilter filter,

            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        logger.debug("Получение проектов с фильтром (админка): {}, page={}, size={}", filter, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Direction.fromString(filter.getSortDirection()),
                filter.getSpringSortField()
        ));

        Page<ProjectDTO> projects = projectService.getProjects(filter, pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Получить проект по ID (для админки)
     *
     * @param id ID проекта
     * @return Проект
     */
    @Operation(
            summary = "Получить проект по ID (админка)",
            description = "Возвращает информацию о проекте по его идентификатору для админ-панели"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Проект найден"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Проект не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(
            @Parameter(description = "ID проекта", example = "1")
            @PathVariable Long id) {

        logger.debug("Получение проекта по ID (админка): {}", id);

        ProjectDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    /**
     * Создать новый проект
     *
     * @param request DTO запроса на создание проекта
     * @param coverImage Изображение обложки (опционально)
     * @return Созданный проект
     */
    @Operation(
            summary = "Создать новый проект",
            description = "Создает новый проект организации с возможностью загрузки обложки"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Проект успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные проекта"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "409", description = "Проект с таким URL уже существует"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectDTO> createProject(
            @Parameter(description = "Данные для создания проекта")
            @Valid @ModelAttribute ProjectCreateRequest request,

            @Parameter(description = "Изображение обложки проекта")
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {

        logger.info("Создание нового проекта: {}", request.getTitle());

        ProjectDTO createdProject = projectService.createProject(request, coverImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    /**
     * Обновить существующий проект
     *
     * @param id ID проекта
     * @param request DTO запроса на обновление проекта
     * @param coverImage Новое изображение обложки (опционально)
     * @return Обновленный проект
     */
    @Operation(
            summary = "Обновить проект",
            description = "Обновляет информацию о существующем проекте"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Проект успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные для обновления"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Проект не найден"),
            @ApiResponse(responseCode = "409", description = "Проект с таким URL уже существует"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectDTO> updateProject(
            @Parameter(description = "ID проекта", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Данные для обновления проекта")
            @Valid @ModelAttribute ProjectUpdateRequest request,

            @Parameter(description = "Новое изображение обложки проекта")
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {

        logger.info("Обновление проекта ID: {}", id);

        ProjectDTO updatedProject = projectService.updateProject(id, request, coverImage);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Удалить проект
     *
     * @param id ID проекта
     * @return Ответ без содержимого
     */
    @Operation(
            summary = "Удалить проект",
            description = "Удаляет проект и связанные с ним данные (видео, партнеры, связи с командой)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Проект успешно удален"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Проект не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "ID проекта", example = "1")
            @PathVariable Long id) {

        logger.info("Удаление проекта ID: {}", id);

        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Изменить статус проекта
     *
     * @param id ID проекта
     * @param status Новый статус проекта
     * @return Обновленный проект
     */
    @Operation(
            summary = "Изменить статус проекта",
            description = "Изменяет статус проекта (активный, архивный, планируется и т.д.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус проекта успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Некорректный статус"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Проект не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjectDTO> changeProjectStatus(
            @Parameter(description = "ID проекта", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Новый статус проекта", example = "ARCHIVE")
            @RequestParam ProjectStatus status) {

        logger.info("Изменение статуса проекта ID: {} на {}", id, status);

        ProjectDTO updatedProject = projectService.changeProjectStatus(id, status);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Проверить доступность URL-идентификатора (slug)
     *
     * @param slug URL-идентификатор для проверки
     * @return true, если slug доступен (не используется)
     */
    @Operation(
            summary = "Проверить доступность URL-идентификатора",
            description = "Проверяет, доступен ли указанный URL-идентификатор (slug) для использования"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результат проверки доступности"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/check-slug")
    public ResponseEntity<Boolean> checkSlugAvailability(
            @Parameter(description = "URL-идентификатор для проверки", example = "snegurochka-goda")
            @RequestParam String slug) {

        logger.debug("Проверка доступности slug: {}", slug);

        boolean available = !projectService.existsBySlug(slug);
        return ResponseEntity.ok(available);
    }

    /**
     * Получить статистику проектов
     *
     * @return Статистика проектов
     */
    @Operation(
            summary = "Получить статистику проектов",
            description = "Возвращает статистическую информацию о проектах организации"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статистика проектов получена"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/stats")
    public ResponseEntity<ProjectService.ProjectStats> getProjectStats() {
        logger.debug("Получение статистики проектов");

        ProjectService.ProjectStats stats = projectService.getProjectStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Клонировать проект
     *
     * @param id ID проекта для клонирования
     * @param newSlug Новый URL-идентификатор для клонированного проекта
     * @param copyContent Флаг копирования контента (описание, фото, видео и т.д.)
     * @return Клонированный проект
     */
    @Operation(
            summary = "Клонировать проект",
            description = "Создает копию существующего проекта с возможностью изменения URL-идентификатора"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Проект успешно клонирован"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры клонирования"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Исходный проект не найден"),
            @ApiResponse(responseCode = "409", description = "Проект с таким URL уже существует"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/{id}/clone")
    public ResponseEntity<ProjectDTO> cloneProject(
            @Parameter(description = "ID проекта для клонирования", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Новый URL-идентификатор для клонированного проекта", example = "snegurochka-goda-2024")
            @RequestParam String newSlug,

            @Parameter(description = "Копировать контент проекта (описание, фото и т.д.)", example = "true")
            @RequestParam(defaultValue = "true") boolean copyContent) {

        logger.info("Клонирование проекта ID: {} с новым slug: {}", id, newSlug);

        // TODO: Реализовать логику клонирования проекта
        // Пока возвращаем заглушку
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Экспортировать проекты в формате CSV
     *
     * @param filter Фильтр проектов для экспорта
     * @return CSV файл с данными проектов
     */
    @Operation(
            summary = "Экспортировать проекты в CSV",
            description = "Экспортирует отфильтрованные проекты в формате CSV"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV файл успешно создан"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportProjectsToCsv(
            @Parameter(description = "Параметры фильтрации для экспорта")
            @ModelAttribute ProjectFilter filter) {

        logger.info("Экспорт проектов в CSV с фильтром: {}", filter);

        // TODO: Реализовать экспорт в CSV
        // Пока возвращаем заглушку
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Получить проекты без обложки
     *
     * @param page Номер страницы (начиная с 0)
     * @param size Размер страницы
     * @return Страница проектов без обложки
     */
    @Operation(
            summary = "Получить проекты без обложки",
            description = "Возвращает проекты, у которых не установлено изображение обложки"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список проектов без обложки получен"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/without-cover")
    public ResponseEntity<Page<ProjectDTO>> getProjectsWithoutCover(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        logger.debug("Получение проектов без обложки: page={}, size={}", page, size);

        // TODO: Реализовать метод получения проектов без обложки
        // Пока возвращаем заглушку
        return ResponseEntity.ok(Page.empty());
    }

    /**
     * Получить проекты с истекшими датами
     *
     * @param page Номер страницы (начиная с 0)
     * @param size Размер страницы
     * @return Страница проектов с истекшими датами
     */
    @Operation(
            summary = "Получить проекты с истекшими датами",
            description = "Возвращает проекты, у которых дата окончания уже прошла, но статус еще не архивный"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список проектов с истекшими датами получен"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/expired")
    public ResponseEntity<Page<ProjectDTO>> getExpiredProjects(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        logger.debug("Получение проектов с истекшими датами: page={}, size={}", page, size);

        // TODO: Реализовать метод получения проектов с истекшими датами
        // Пока возвращаем заглушку
        return ResponseEntity.ok(Page.empty());
    }

    /**
     * Обновить SEO настройки для нескольких проектов
     *
     * @param projectIds Список ID проектов
     * @param metaTitle Новый SEO заголовок (опционально)
     * @param metaDescription Новое SEO описание (опционально)
     * @param metaKeywords Новые ключевые слова (опционально)
     * @return Количество обновленных проектов
     */
    @Operation(
            summary = "Обновить SEO настройки для нескольких проектов",
            description = "Массовое обновление SEO настроек для выбранных проектов"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SEO настройки успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PatchMapping("/batch/seo")
    public ResponseEntity<Integer> batchUpdateSeo(
            @Parameter(description = "Список ID проектов для обновления")
            @RequestParam List<Long> projectIds,

            @Parameter(description = "Новый SEO заголовок", example = "Лучший фестиваль года")
            @RequestParam(required = false) String metaTitle,

            @Parameter(description = "Новое SEO описание", example = "Участвуйте в нашем фестивале")
            @RequestParam(required = false) String metaDescription,

            @Parameter(description = "Новые ключевые слова", example = "фестиваль, конкурс, мероприятие")
            @RequestParam(required = false) String metaKeywords) {

        logger.info("Массовое обновление SEO для проектов: {}, count={}", projectIds, projectIds.size());

        // TODO: Реализовать массовое обновление SEO
        // Пока возвращаем заглушку
        return ResponseEntity.ok(0);
    }
}

package com.community.cms.controller.web.project;

import com.community.cms.dto.project.ProjectDTO;
import com.community.cms.dto.project.ProjectFilter;
import com.community.cms.model.project.ProjectCategory;
import com.community.cms.service.project.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//Описание файла:
//Публичный REST-контроллер для работы с проектами
//Аннотации Spring (@RestController, @RequestMapping)
//Интеграция с OpenAPI/Swagger для автоматической документации API
//Инъекция зависимостей через конструктор
//Полный набор эндпоинтов для работы с проектами:
//Получение списка проектов с пагинацией
//Фильтрация проектов по различным параметрам
//Получение проектов по категории, статусу, поисковому запросу
//     Получение активных, будущих и завершенных проектов
//Получение детальной информации о проекте по slug
//Получение похожих проектов
//Проверка существования проекта
//Использование ResponseEntity для гибкого управления HTTP-ответами
//Логирование всех операций
//Обработка ошибок и валидация параметров
//Полная документация OpenAPI для каждого эндпоинта
//Пагинация и сортировка результатов

/**
 * Публичный контроллер для работы с проектами организации "ЛАДА".
 * Предоставляет API для получения информации о проектах для обычных пользователей.
 *
 * @author Vasickin
 * @since 1.0
 */
@RestController
@RequestMapping("/api/projects")
@Tag(name = "Проекты (публичные)", description = "API для публичного доступа к проектам организации")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    /**
     * Конструктор контроллера
     *
     * @param projectService Сервис проектов
     */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Получить список всех проектов с пагинацией
     *
     * @param page Номер страницы (начиная с 0, по умолчанию 0)
     * @param size Размер страницы (по умолчанию 12)
     * @param sort Поле для сортировки (по умолчанию startDate)
     * @param direction Направление сортировки (ASC или DESC, по умолчанию DESC)
     * @return Страница проектов
     */
    @Operation(
            summary = "Получить список всех проектов",
            description = "Возвращает пагинированный список всех проектов организации"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное получение списка проектов"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<Page<ProjectDTO>> getAllProjects(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "12")
            @RequestParam(defaultValue = "12") int size,

            @Parameter(description = "Поле для сортировки", example = "startDate")
            @RequestParam(defaultValue = "startDate") String sort,

            @Parameter(description = "Направление сортировки", example = "DESC")
            @RequestParam(defaultValue = "DESC") String direction) {

        logger.debug("Получение всех проектов: page={}, size={}, sort={}, direction={}",
                page, size, sort, direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Direction.fromString(direction), sort
        ));

        Page<ProjectDTO> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Получить проекты с фильтрацией
     *
     * @param filter Фильтр проектов
     * @param page Номер страницы (начиная с 0)
     * @param size Размер страницы
     * @return Страница отфильтрованных проектов
     */
    @Operation(
            summary = "Получить проекты с фильтрацией",
            description = "Возвращает проекты, отфильтрованные по категории, статусу и другим параметрам"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное получение отфильтрованных проектов"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры фильтрации"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/filter")
    public ResponseEntity<Page<ProjectDTO>> getProjectsWithFilter(
            @Parameter(description = "Параметры фильтрации")
            @ModelAttribute ProjectFilter filter,

            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "12")
            @RequestParam(defaultValue = "12") int size) {

        logger.debug("Получение проектов с фильтром: {}, page={}, size={}", filter, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Direction.fromString(filter.getSortDirection()),
                filter.getSpringSortField()
        ));

        Page<ProjectDTO> projects = projectService.getProjects(filter, pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Получить проекты для публичного отображения
     *
     * @param category Категория проекта (опционально)
     * @param search Поисковый запрос (опционально)
     * @param page Номер страницы (начиная с 0)
     * @param size Размер страницы
     * @return Страница проектов для публичного отображения
     */
    @Operation(
            summary = "Получить проекты для публичного отображения",
            description = "Возвращает только те проекты, которые должны отображаться на сайте"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное получение публичных проектов"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/public")
    public ResponseEntity<Page<ProjectDTO>> getPublicProjects(
            @Parameter(description = "Категория проекта", example = "FESTIVAL")
            @RequestParam(required = false) ProjectCategory category,

            @Parameter(description = "Поисковый запрос", example = "фестиваль")
            @RequestParam(required = false) String search,

            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "12")
            @RequestParam(defaultValue = "12") int size) {

        logger.debug("Получение публичных проектов: category={}, search={}, page={}, size={}",
                category, search, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<ProjectDTO> projects = projectService.getPublicProjects(category, search, pageable);

        return ResponseEntity.ok(projects);
    }

    /**
     * Получить проект по ID
     *
     * @param id ID проекта
     * @return Проект
     */
    @Operation(
            summary = "Получить проект по ID",
            description = "Возвращает информацию о проекте по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Проект найден"),
            @ApiResponse(responseCode = "404", description = "Проект не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(
            @Parameter(description = "ID проекта", example = "1")
            @PathVariable Long id) {

        logger.debug("Получение проекта по ID: {}", id);

        ProjectDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    /**
     * Получить проект по URL-идентификатору (slug)
     *
     * @param slug URL-идентификатор проекта
     * @return Проект
     */
    @Operation(
            summary = "Получить проект по URL-идентификатору",
            description = "Возвращает информацию о проекте по его ЧПУ-ссылке"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Проект найден"),
            @ApiResponse(responseCode = "404", description = "Проект не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProjectDTO> getProjectBySlug(
            @Parameter(description = "URL-идентификатор проекта", example = "snegurochka-goda")
            @PathVariable String slug) {

        logger.debug("Получение проекта по slug: {}", slug);

        ProjectDTO project = projectService.getProjectBySlug(slug);
        return ResponseEntity.ok(project);
    }

    /**
     * Получить детальную информацию о проекте по slug
     *
     * @param slug URL-идентификатор проекта
     * @return Детальная информация о проекте
     */
    @Operation(
            summary = "Получить детальную информацию о проекте",
            description = "Возвращает полную информацию о проекте, включая видео, партнеров и команду"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Детальная информация о проекте найдена"),
            @ApiResponse(responseCode = "404", description = "Проект не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/slug/{slug}/details")
    public ResponseEntity<ProjectDTO> getProjectDetailsBySlug(
            @Parameter(description = "URL-идентификатор проекта", example = "snegurochka-goda")
            @PathVariable String slug) {

        logger.debug("Получение детальной информации о проекте по slug: {}", slug);

        ProjectDTO project = projectService.getProjectDetailsBySlug(slug);
        return ResponseEntity.ok(project);
    }

    /**
     * Получить активные проекты (которые проходят сейчас)
     *
     * @return Список активных проектов
     */
    @Operation(
            summary = "Получить активные проекты",
            description = "Возвращает список проектов, которые проходят в настоящее время"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список активных проектов получен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/active")
    public ResponseEntity<List<ProjectDTO>> getActiveProjects() {
        logger.debug("Получение активных проектов");

        List<ProjectDTO> projects = projectService.getActiveProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Получить будущие проекты (запланированные)
     *
     * @return Список будущих проектов
     */
    @Operation(
            summary = "Получить будущие проекты",
            description = "Возвращает список проектов, которые запланированы на будущее"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список будущих проектов получен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/upcoming")
    public ResponseEntity<List<ProjectDTO>> getUpcomingProjects() {
        logger.debug("Получение будущих проектов");

        List<ProjectDTO> projects = projectService.getUpcomingProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Получить завершенные проекты
     *
     * @return Список завершенных проектов
     */
    @Operation(
            summary = "Получить завершенные проекты",
            description = "Возвращает список проектов, которые уже завершились"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список завершенных проектов получен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/completed")
    public ResponseEntity<List<ProjectDTO>> getCompletedProjects() {
        logger.debug("Получение завершенных проектов");

        List<ProjectDTO> projects = projectService.getCompletedProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Получить похожие проекты
     *
     * @param projectId ID текущего проекта
     * @param limit Максимальное количество похожих проектов (по умолчанию 3)
     * @return Список похожих проектов
     */
    @Operation(
            summary = "Получить похожие проекты",
            description = "Возвращает проекты, похожие на указанный (по категории и статусу)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список похожих проектов получен"),
            @ApiResponse(responseCode = "404", description = "Исходный проект не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/{projectId}/related")
    public ResponseEntity<List<ProjectDTO>> getRelatedProjects(
            @Parameter(description = "ID текущего проекта", example = "1")
            @PathVariable Long projectId,

            @Parameter(description = "Максимальное количество похожих проектов", example = "3")
            @RequestParam(defaultValue = "3") int limit) {

        logger.debug("Получение похожих проектов для проекта ID: {}, limit: {}", projectId, limit);

        List<ProjectDTO> relatedProjects = projectService.getRelatedProjects(projectId, limit);
        return ResponseEntity.ok(relatedProjects);
    }

    /**
     * Проверить существование проекта по slug
     *
     * @param slug URL-идентификатор проекта
     * @return true, если проект существует
     */
    @Operation(
            summary = "Проверить существование проекта",
            description = "Проверяет, существует ли проект с указанным URL-идентификатором"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результат проверки получен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/exists/{slug}")
    public ResponseEntity<Boolean> checkProjectExists(
            @Parameter(description = "URL-идентификатор проекта", example = "snegurochka-goda")
            @PathVariable String slug) {

        logger.debug("Проверка существования проекта по slug: {}", slug);

        boolean exists = projectService.existsBySlug(slug);
        return ResponseEntity.ok(exists);
    }

    /**
     * Получить проекты по категории
     *
     * @param category Категория проекта
     * @param page Номер страницы (начиная с 0)
     * @param size Размер страницы
     * @return Страница проектов указанной категории
     */
    @Operation(
            summary = "Получить проекты по категории",
            description = "Возвращает проекты указанной категории"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список проектов категории получен"),
            @ApiResponse(responseCode = "400", description = "Некорректная категория"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProjectDTO>> getProjectsByCategory(
            @Parameter(description = "Категория проекта", example = "FESTIVAL")
            @PathVariable ProjectCategory category,

            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "12")
            @RequestParam(defaultValue = "12") int size) {

        logger.debug("Получение проектов по категории: {}, page={}, size={}", category, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        ProjectFilter filter = new ProjectFilter();
        filter.setCategory(category);

        Page<ProjectDTO> projects = projectService.getProjects(filter, pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Получить проекты по статусу
     *
     * @param status Статус проекта
     * @param page Номер страницы (начиная с 0)
     * @param size Размер страницы
     * @return Страница проектов указанного статуса
     */
    @Operation(
            summary = "Получить проекты по статусу",
            description = "Возвращает проекты указанного статуса"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список проектов статуса получен"),
            @ApiResponse(responseCode = "400", description = "Некорректный статус"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ProjectDTO>> getProjectsByStatus(
            @Parameter(description = "Статус проекта", example = "ACTIVE")
            @PathVariable String status,

            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "12")
            @RequestParam(defaultValue = "12") int size) {

        logger.debug("Получение проектов по статусу: {}, page={}, size={}", status, page, size);

        try {
            ProjectFilter filter = new ProjectFilter();
            filter.setStatus(com.community.cms.model.project.ProjectStatus.valueOf(status.toUpperCase()));

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
            Page<ProjectDTO> projects = projectService.getProjects(filter, pageable);

            return ResponseEntity.ok(projects);
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректный статус проекта: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Поиск проектов по названию или описанию
     *
     * @param query Поисковый запрос
     * @param page Номер страницы (начиная с 0)
     * @param size Размер страницы
     * @return Страница найденных проектов
     */
    @Operation(
            summary = "Поиск проектов",
            description = "Поиск проектов по названию или описанию"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результаты поиска получены"),
            @ApiResponse(responseCode = "400", description = "Пустой поисковый запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ProjectDTO>> searchProjects(
            @Parameter(description = "Поисковый запрос", example = "фестиваль")
            @RequestParam String query,

            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "12")
            @RequestParam(defaultValue = "12") int size) {

        logger.debug("Поиск проектов: query={}, page={}, size={}", query, page, size);

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        ProjectFilter filter = new ProjectFilter();
        filter.setSearch(query.trim());

        Page<ProjectDTO> projects = projectService.getProjects(filter, pageable);
        return ResponseEntity.ok(projects);
    }
}

package com.community.cms.repository.project;

import com.community.cms.model.project.Project;
import com.community.cms.model.project.ProjectCategory;
import com.community.cms.model.project.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

//Описание файла:
//Репозиторий для работы с сущностью Project
//Расширяет JpaRepository и JpaSpecificationExecutor для базовых операций CRUD и спецификаций
//Методы для поиска по slug, статусу, категории
//Сложные запросы с использованием @Query для нетривиальной логики
//Пагинация для списков проектов
//Фильтрация проектов для публичного доступа
//Поиск похожих проектов
//Статистические запросы (подсчет проектов)
//Оптимизированные запросы с JOIN FETCH для загрузки связанных сущностей
//Методы для карты сайта (sitemap)

/**
 * Репозиторий для работы с сущностью {@link Project}.
 * Предоставляет методы для доступа к данным проектов организации "ЛАДА".
 *
 * @author Vasickin
 * @since 1.0
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    /**
     * Найти проект по URL-идентификатору (slug)
     *
     * @param slug URL-идентификатор проекта
     * @return Optional с найденным проектом или пустой Optional
     */
    Optional<Project> findBySlug(String slug);

    /**
     * Проверить существование проекта по URL-идентификатору
     *
     * @param slug URL-идентификатор проекта
     * @return true, если проект с таким slug существует
     */
    boolean existsBySlug(String slug);

    /**
     * Найти все проекты с указанным статусом
     *
     * @param status Статус проекта
     * @return Список проектов с указанным статусом
     */
    List<Project> findByStatus(ProjectStatus status);

    /**
     * Найти все проекты с указанным статусом с пагинацией
     *
     * @param status Статус проекта
     * @param pageable Параметры пагинации
     * @return Страница проектов с указанным статусом
     */
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    /**
     * Найти все проекты с указанной категорией
     *
     * @param category Категория проекта
     * @return Список проектов с указанной категорией
     */
    List<Project> findByCategory(ProjectCategory category);

    /**
     * Найти все проекты с указанной категорией с пагинацией
     *
     * @param category Категория проекта
     * @param pageable Параметры пагинации
     * @return Страница проектов с указанной категорией
     */
    Page<Project> findByCategory(ProjectCategory category, Pageable pageable);

    /**
     * Найти активные проекты (статус ACTIVE) с пагинацией
     *
     * @param pageable Параметры пагинации
     * @return Страница активных проектов
     */
    Page<Project> findByStatusOrderByStartDateDesc(ProjectStatus status, Pageable pageable);

    /**
     * Найти проекты по статусу и категории
     *
     * @param status Статус проекта
     * @param category Категория проекта
     * @return Список проектов, удовлетворяющих критериям
     */
    List<Project> findByStatusAndCategory(ProjectStatus status, ProjectCategory category);

    /**
     * Найти проекты по статусу и категории с пагинацией
     *
     * @param status Статус проекта
     * @param category Категория проекта
     * @param pageable Параметры пагинации
     * @return Страница проектов, удовлетворяющих критериям
     */
    Page<Project> findByStatusAndCategory(ProjectStatus status, ProjectCategory category, Pageable pageable);

    /**
     * Найти проекты, у которых дата начала позже указанной даты
     *
     * @param date Дата для сравнения
     * @return Список проектов с датой начала после указанной
     */
    List<Project> findByStartDateAfter(LocalDate date);

    /**
     * Найти проекты, у которых дата окончания раньше указанной даты
     *
     * @param date Дата для сравнения
     * @return Список проектов с датой окончания до указанной
     */
    List<Project> findByEndDateBefore(LocalDate date);

    /**
     * Найти текущие активные проекты (в процессе проведения)
     *
     * @param currentDate Текущая дата
     * @return Список активных проектов, которые проходят в данный момент
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'ACTIVE' AND " +
            "p.startDate <= :currentDate AND " +
            "(p.endDate IS NULL OR p.endDate >= :currentDate) " +
            "ORDER BY p.startDate DESC")
    List<Project> findActiveProjects(@Param("currentDate") LocalDate currentDate);

    /**
     * Найти будущие проекты (запланированные)
     *
     * @param currentDate Текущая дата
     * @return Список проектов, которые начнутся в будущем
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'ACTIVE' AND " +
            "p.startDate > :currentDate " +
            "ORDER BY p.startDate ASC")
    List<Project> findUpcomingProjects(@Param("currentDate") LocalDate currentDate);

    /**
     * Найти завершенные проекты
     *
     * @param currentDate Текущая дата
     * @return Список завершенных проектов
     */
    @Query("SELECT p FROM Project p WHERE (p.status = 'ARCHIVE' OR " +
            "(p.status = 'ACTIVE' AND p.endDate < :currentDate)) " +
            "ORDER BY p.endDate DESC")
    List<Project> findCompletedProjects(@Param("currentDate") LocalDate currentDate);

    /**
     * Найти проекты по названию (поиск с учетом регистра)
     *
     * @param title Название проекта или его часть
     * @return Список проектов, название которых содержит указанную строку
     */
    List<Project> findByTitleContainingIgnoreCase(String title);

    /**
     * Найти проекты по названию с пагинацией
     *
     * @param title Название проекта или его часть
     * @param pageable Параметры пагинации
     * @return Страница проектов, название которых содержит указанную строку
     */
    Page<Project> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Найти проекты по части описания
     *
     * @param description Текст описания или его часть
     * @return Список проектов, описание которых содержит указанную строку
     */
    @Query("SELECT p FROM Project p WHERE " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :description, '%')) OR " +
            "LOWER(p.fullDescription) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<Project> findByDescriptionContaining(@Param("description") String description);

    /**
     * Найти проекты с фильтрацией по нескольким критериям
     *
     * @param category Категория проекта (может быть null)
     * @param status Статус проекта (может быть null)
     * @param search Поисковый запрос (может быть null)
     * @param pageable Параметры пагинации
     * @return Страница проектов, удовлетворяющих критериям
     */
    @Query("SELECT p FROM Project p WHERE " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:search IS NULL OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.fullDescription) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY p.startDate DESC, p.createdAt DESC")
    Page<Project> findByFilters(
            @Param("category") ProjectCategory category,
            @Param("status") ProjectStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Найти проекты с фильтрацией для публичного доступа
     * (только активные и не отмененные проекты)
     *
     * @param category Категория проекта (может быть null)
     * @param search Поисковый запрос (может быть null)
     * @param pageable Параметры пагинации
     * @return Страница проектов для публичного отображения
     */
    @Query("SELECT p FROM Project p WHERE " +
            "p.status IN ('ACTIVE', 'ANNUAL', 'PLANNED', 'ARCHIVE', 'PAUSED') AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:search IS NULL OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY " +
            "CASE WHEN p.status = 'ACTIVE' THEN 1 " +
            "     WHEN p.status = 'PLANNED' THEN 2 " +
            "     WHEN p.status = 'ANNUAL' THEN 3 " +
            "     WHEN p.status = 'PAUSED' THEN 4 " +
            "     WHEN p.status = 'ARCHIVE' THEN 5 END, " +
            "p.startDate DESC, p.createdAt DESC")
    Page<Project> findPublicProjects(
            @Param("category") ProjectCategory category,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Найти похожие проекты (по категории и статусу)
     *
     * @param projectId ID текущего проекта (исключить из результатов)
     * @param category Категория проекта
     * @param limit Максимальное количество результатов
     * @return Список похожих проектов
     */
    @Query(value = "SELECT * FROM projects p WHERE " +
            "p.id != :projectId AND " +
            "p.category = :category AND " +
            "p.status IN ('ACTIVE', 'ANNUAL', 'ARCHIVE') " +
            "ORDER BY " +
            "CASE WHEN p.status = 'ACTIVE' THEN 1 " +
            "     WHEN p.status = 'ANNUAL' THEN 2 " +
            "     WHEN p.status = 'ARCHIVE' THEN 3 END, " +
            "p.startDate DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Project> findRelatedProjects(
            @Param("projectId") Long projectId,
            @Param("category") String category,
            @Param("limit") int limit
    );

    /**
     * Найти проекты с галереей (имеют привязанную галерею)
     *
     * @return Список проектов с галереей
     */
    @Query("SELECT p FROM Project p WHERE p.gallery IS NOT NULL")
    List<Project> findProjectsWithGallery();

    /**
     * Найти проекты с видео (имеют хотя бы одно видео)
     *
     * @return Список проектов с видео
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.videos v WHERE SIZE(p.videos) > 0")
    List<Project> findProjectsWithVideos();

    /**
     * Найти проекты с партнерами (имеют хотя бы одного партнера)
     *
     * @return Список проектов с партнерами
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.partners pr WHERE SIZE(p.partners) > 0")
    List<Project> findProjectsWithPartners();

    /**
     * Найти проекты с командой (имеют хотя бы одного члена команды)
     *
     * @return Список проектов с командой
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.teamMembers tm WHERE SIZE(p.teamMembers) > 0")
    List<Project> findProjectsWithTeam();

    /**
     * Найти проекты по ID члена команды
     *
     * @param teamMemberId ID члена команды
     * @return Список проектов, в которых участвует указанный член команды
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.teamMembers tm WHERE KEY(tm).id = :teamMemberId")
    List<Project> findByTeamMemberId(@Param("teamMemberId") Long teamMemberId);

    /**
     * Найти проекты по ID члена команды с пагинацией
     *
     * @param teamMemberId ID члена команды
     * @param pageable Параметры пагинации
     * @return Страница проектов с участием указанного члена команды
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.teamMembers tm WHERE KEY(tm).id = :teamMemberId")
    Page<Project> findByTeamMemberId(@Param("teamMemberId") Long teamMemberId, Pageable pageable);

    /**
     * Найти проекты по ID галереи
     *
     * @param galleryId ID галереи
     * @return Проект, связанный с указанной галереей
     */
    Optional<Project> findByGalleryId(Long galleryId);

    /**
     * Получить количество проектов по статусу
     *
     * @param status Статус проекта
     * @return Количество проектов с указанным статусом
     */
    long countByStatus(ProjectStatus status);

    /**
     * Получить количество проектов по категории
     *
     * @param category Категория проекта
     * @return Количество проектов с указанной категорией
     */
    long countByCategory(ProjectCategory category);

    /**
     * Получить общее количество активных проектов
     *
     * @return Количество активных проектов
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status IN ('ACTIVE', 'ANNUAL', 'PLANNED')")
    long countActiveProjects();

    /**
     * Получить проекты для карты сайта (sitemap)
     *
     * @return Список проектов для карты сайта
     */
    @Query("SELECT p FROM Project p WHERE " +
            "p.status IN ('ACTIVE', 'ANNUAL', 'ARCHIVE') " +
            "ORDER BY p.updatedAt DESC")
    List<Project> findAllForSitemap();

    /**
     * Найти проект по ID с полной загрузкой связанных сущностей
     *
     * @param id ID проекта
     * @return Optional с найденным проектом или пустой Optional
     */
    @Query("SELECT p FROM Project p " +
            "LEFT JOIN FETCH p.videos " +
            "LEFT JOIN FETCH p.partners " +
            "LEFT JOIN FETCH p.teamMembers " +
            "WHERE p.id = :id")
    Optional<Project> findByIdWithDetails(@Param("id") Long id);

    /**
     * Найти проект по slug с полной загрузкой связанных сущностей
     *
     * @param slug URL-идентификатор проекта
     * @return Optional с найденным проектом или пустой Optional
     */
    @Query("SELECT p FROM Project p " +
            "LEFT JOIN FETCH p.videos " +
            "LEFT JOIN FETCH p.partners " +
            "LEFT JOIN FETCH p.teamMembers " +
            "WHERE p.slug = :slug")
    Optional<Project> findBySlugWithDetails(@Param("slug") String slug);
}

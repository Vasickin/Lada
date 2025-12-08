package com.community.cms.repository.project;

import com.community.cms.model.project.ProjectVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Описание файла:
//Репозиторий для работы с сущностью ProjectVideo
//Методы для поиска видео по проекту, URL и статусу (основное/не основное)
//Проверка на дублирование видео в проекте
//Статистические методы (количество видео)
//Методы с JOIN FETCH для загрузки связанного проекта
//Методы для управления порядком сортировки и основным видео
//Поиск по названию видео
//Методы для удаления видео по проекту или URL

/**
 * Репозиторий для работы с сущностью {@link ProjectVideo}.
 * Предоставляет методы для доступа к данным видео проектов организации "ЛАДА".
 *
 * @author Vasickin
 * @since 1.0
 */
@Repository
public interface ProjectVideoRepository extends JpaRepository<ProjectVideo, Long> {

    /**
     * Найти все видео указанного проекта
     *
     * @param projectId ID проекта
     * @return Список видео проекта, отсортированный по порядку
     */
    List<ProjectVideo> findByProjectIdOrderBySortOrderAsc(Long projectId);

    /**
     * Найти все видео указанного проекта, которые являются основными
     *
     * @param projectId ID проекта
     * @return Список основных видео проекта
     */
    List<ProjectVideo> findByProjectIdAndIsMainTrue(Long projectId);

    /**
     * Найти основное видео проекта
     *
     * @param projectId ID проекта
     * @return Optional с основным видео проекта или пустой Optional
     */
    Optional<ProjectVideo> findFirstByProjectIdAndIsMainTrue(Long projectId);

    /**
     * Найти первое видео проекта (по порядку сортировки)
     *
     * @param projectId ID проекта
     * @return Optional с первым видео проекта или пустой Optional
     */
    Optional<ProjectVideo> findFirstByProjectIdOrderBySortOrderAsc(Long projectId);

    /**
     * Найти видео по YouTube URL
     *
     * @param youtubeUrl URL YouTube видео
     * @return Список видео с указанным YouTube URL
     */
    List<ProjectVideo> findByYoutubeUrl(String youtubeUrl);

    /**
     * Найти видео по Vimeo URL
     *
     * @param vimeoUrl URL Vimeo видео
     * @return Список видео с указанным Vimeo URL
     */
    List<ProjectVideo> findByVimeoUrl(String vimeoUrl);

    /**
     * Проверить существование видео с указанным YouTube URL в проекте
     *
     * @param projectId ID проекта
     * @param youtubeUrl URL YouTube видео
     * @return true, если видео с таким URL уже существует в проекте
     */
    boolean existsByProjectIdAndYoutubeUrl(Long projectId, String youtubeUrl);

    /**
     * Проверить существование видео с указанным Vimeo URL в проекте
     *
     * @param projectId ID проекта
     * @param vimeoUrl URL Vimeo видео
     * @return true, если видео с таким URL уже существует в проекте
     */
    boolean existsByProjectIdAndVimeoUrl(Long projectId, String vimeoUrl);

    /**
     * Получить количество видео в проекте
     *
     * @param projectId ID проекта
     * @return Количество видео в проекте
     */
    long countByProjectId(Long projectId);

    /**
     * Получить количество основных видео в проекте
     *
     * @param projectId ID проекта
     * @return Количество основных видео в проекте
     */
    long countByProjectIdAndIsMainTrue(Long projectId);

    /**
     * Найти видео проекта по ID с загрузкой связанного проекта
     *
     * @param id ID видео
     * @return Optional с найденным видео или пустой Optional
     */
    @Query("SELECT v FROM ProjectVideo v JOIN FETCH v.project WHERE v.id = :id")
    Optional<ProjectVideo> findByIdWithProject(@Param("id") Long id);

    /**
     * Найти все видео проектов с загрузкой связанных проектов
     *
     * @return Список всех видео с загруженными проектами
     */
    @Query("SELECT v FROM ProjectVideo v JOIN FETCH v.project")
    List<ProjectVideo> findAllWithProjects();

    /**
     * Найти видео по ID проекта с загрузкой проекта
     *
     * @param projectId ID проекта
     * @return Список видео проекта с загруженным проектом
     */
    @Query("SELECT v FROM ProjectVideo v JOIN FETCH v.project WHERE v.project.id = :projectId ORDER BY v.sortOrder ASC")
    List<ProjectVideo> findByProjectIdWithProject(@Param("projectId") Long projectId);

    /**
     * Найти YouTube видео проектов
     *
     * @return Список видео, которые являются YouTube видео
     */
    @Query("SELECT v FROM ProjectVideo v WHERE v.youtubeUrl IS NOT NULL")
    List<ProjectVideo> findAllYouTubeVideos();

    /**
     * Найти Vimeo видео проектов
     *
     * @return Список видео, которые являются Vimeo видео
     */
    @Query("SELECT v FROM ProjectVideo v WHERE v.vimeoUrl IS NOT NULL")
    List<ProjectVideo> findAllVimeoVideos();

    /**
     * Найти видео, у которых есть миниатюра
     *
     * @return Список видео с загруженной миниатюрой
     */
    @Query("SELECT v FROM ProjectVideo v WHERE v.thumbnailPath IS NOT NULL")
    List<ProjectVideo> findVideosWithThumbnail();

    /**
     * Удалить все видео указанного проекта
     *
     * @param projectId ID проекта
     */
    void deleteByProjectId(Long projectId);

    /**
     * Удалить видео по URL (YouTube или Vimeo)
     *
     * @param url URL видео
     */
    void deleteByYoutubeUrlOrVimeoUrl(String url, String vimeoUrl);

    /**
     * Найти дубликаты видео в проекте (одинаковые URL)
     *
     * @param projectId ID проекта
     * @return Список дублирующихся видео
     */
    @Query("SELECT v FROM ProjectVideo v WHERE v.project.id = :projectId AND " +
            "(v.youtubeUrl IN (SELECT v2.youtubeUrl FROM ProjectVideo v2 WHERE v2.project.id = :projectId AND v2.youtubeUrl IS NOT NULL GROUP BY v2.youtubeUrl HAVING COUNT(v2) > 1) OR " +
            "v.vimeoUrl IN (SELECT v3.vimeoUrl FROM ProjectVideo v3 WHERE v3.project.id = :projectId AND v3.vimeoUrl IS NOT NULL GROUP BY v3.vimeoUrl HAVING COUNT(v3) > 1))")
    List<ProjectVideo> findDuplicateVideosInProject(@Param("projectId") Long projectId);

    /**
     * Обновить порядок сортировки видео
     *
     * @param videoId ID видео
     * @param newSortOrder Новый порядок сортировки
     */
    @Query("UPDATE ProjectVideo v SET v.sortOrder = :newSortOrder WHERE v.id = :videoId")
    void updateSortOrder(@Param("videoId") Long videoId, @Param("newSortOrder") Integer newSortOrder);

    /**
     * Сделать указанное видео основным, сбросив флаг isMain у остальных видео проекта
     *
     * @param videoId ID видео, которое нужно сделать основным
     * @param projectId ID проекта
     */
    @Query("UPDATE ProjectVideo v SET v.isMain = CASE WHEN v.id = :videoId THEN true ELSE false END WHERE v.project.id = :projectId")
    void setMainVideo(@Param("videoId") Long videoId, @Param("projectId") Long projectId);

    /**
     * Найти видео по части названия (без учета регистра)
     *
     * @param titlePart Часть названия видео
     * @return Список видео, название которых содержит указанную строку
     */
    @Query("SELECT v FROM ProjectVideo v WHERE LOWER(v.title) LIKE LOWER(CONCAT('%', :titlePart, '%'))")
    List<ProjectVideo> findByTitleContainingIgnoreCase(@Param("titlePart") String titlePart);

    /**
     * Найти видео по части названия в указанном проекте
     *
     * @param projectId ID проекта
     * @param titlePart Часть названия видео
     * @return Список видео проекта, название которых содержит указанную строку
     */
    @Query("SELECT v FROM ProjectVideo v WHERE v.project.id = :projectId AND LOWER(v.title) LIKE LOWER(CONCAT('%', :titlePart, '%'))")
    List<ProjectVideo> findByProjectIdAndTitleContainingIgnoreCase(@Param("projectId") Long projectId,
                                                                   @Param("titlePart") String titlePart);
}

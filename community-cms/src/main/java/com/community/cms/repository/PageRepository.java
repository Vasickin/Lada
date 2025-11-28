package com.community.cms.repository;

import com.community.cms.model.Page;
import com.community.cms.model.PageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Page в базе данных.
 * Предоставляет методы для выполнения CRUD операций и пользовательских запросов.
 *
 * <p>Расширен методами для работы с типами страниц и фильтрацией по статусу публикации.
 *
 * @author Vasickin
 * @version 1.2
 * @since 2025
 * @see Page
 * @see PageType
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    // СУЩЕСТВУЮЩИЕ МЕТОДЫ

    Optional<Page> findBySlug(String slug);
    List<Page> findByPublishedTrueOrderByCreatedAtDesc();
    List<Page> findByPublished(Boolean published);
    boolean existsBySlug(String slug);
    List<Page> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT p FROM Page p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    List<Page> findByContentContaining(@Param("content") String content);

    long countByPublished(Boolean published);
    List<Page> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT p FROM Page p ORDER BY p.createdAt DESC LIMIT :limit")
    List<Page> findRecentPages(@Param("limit") int limit);

    // НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ТИПАМИ СТРАНИЦ

    /**
     * Находит страницу по slug ТОЛЬКО если она опубликована.
     * Используется для публичного доступа к контенту.
     *
     * @param slug уникальный идентификатор страницы
     * @param published статус публикации (true для опубликованных)
     * @return Optional содержащий страницу если найдена и опубликована
     */
    Optional<Page> findBySlugAndPublished(String slug, Boolean published);

    /**
     * Находит страницы по типу и статусу публикации.
     *
     * @param pageType тип страницы для поиска
     * @param published статус публикации
     * @return список страниц с указанным типом и статусом
     */
    List<Page> findByPageTypeAndPublished(PageType pageType, Boolean published);

    /**
     * Находит ОДНУ страницу по типу ТОЛЬКО если она опубликована.
     * Используется для получения уникальных основных страниц сайта.
     *
     * @param pageType тип страницы
     * @param published статус публикации (true для опубликованных)
     * @return Optional содержащий страницу если найдена и опубликована
     */
    Optional<Page> findFirstByPageTypeAndPublished(PageType pageType, Boolean published);

    /**
     * Проверяет существование страницы определенного типа.
     *
     * @param pageType тип страницы для проверки
     * @return true если страница с таким типом существует, иначе false
     */
    boolean existsByPageType(PageType pageType);

    /**
     * Находит все страницы определенного типа.
     *
     * @param pageType тип страницы для поиска
     * @return список страниц указанного типа
     */
    List<Page> findByPageType(PageType pageType);

    /**
     * Находит все страницы определенного типа, отсортированные по дате создания.
     *
     * @param pageType тип страницы для поиска
     * @return список страниц указанного типа (сначала новые)
     */
    List<Page> findByPageTypeOrderByCreatedAtDesc(PageType pageType);

    /**
     * Находит все основные страницы сайта (исключая CUSTOM тип).
     * Использует оператор <> вместо != который не поддерживается в JPQL.
     *
     * @return список основных страниц сайта
     */
    @Query("SELECT p FROM Page p WHERE p.pageType <> com.community.cms.model.PageType.CUSTOM")
    List<Page> findAllSitePages();

    /**
     * Находит все опубликованные основные страницы сайта.
     * Использует оператор <> вместо != который не поддерживается в JPQL.
     *
     * @return список опубликованных основных страниц
     */
    @Query("SELECT p FROM Page p WHERE p.pageType <> com.community.cms.model.PageType.CUSTOM AND p.published = true")
    List<Page> findPublishedSitePages();

    /**
     * Подсчитывает количество страниц по типу.
     *
     * @param pageType тип страницы
     * @return количество страниц указанного типа
     */
    long countByPageType(PageType pageType);

    /**
     * Подсчитывает количество опубликованных страниц по типу.
     *
     * @param pageType тип страницы
     * @param published статус публикации
     * @return количество опубликованных страниц указанного типа
     */
    long countByPageTypeAndPublished(PageType pageType, Boolean published);
}
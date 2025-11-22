package com.community.cms.repository;

import com.community.cms.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Page entities in the database.
 * Provides methods for retrieving pages by various criteria.
 * Репозиторий для управления сущностями Page в базе данных.
 * Предоставляет методы для получения страниц по различным критериям.
 *
 * @author Vasickin
 * @version 2.0
 * @since 2024
 * @see Page
 */
@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    /**
     * Finds a page by its unique URL slug.
     * Used for retrieving pages by their URL identifier.
     * Находит страницу по её уникальному URL-идентификатору.
     * Используется для получения страниц по их URL-идентификатору.
     *
     * @param slug the URL slug to search for / URL-идентификатор для поиска
     * @return optional containing the page if found / опциональный объект со страницей, если найдена
     */
    Optional<Page> findBySlug(String slug);

    /**
     * Finds all published pages ordered by sort order.
     * Used for navigation menus and page listings.
     * Находит все опубликованные страницы, отсортированные по порядку.
     * Используется для навигационных меню и списков страниц.
     *
     * @return list of published pages / список опубликованных страниц
     */
    List<Page> findByIsPublishedTrueOrderBySortOrderAsc();

    /**
     * Checks if a page with the given slug exists.
     * Useful for validation when creating new pages.
     * Проверяет, существует ли страница с данным идентификатором.
     * Полезно для проверки при создании новых страниц.
     *
     * @param slug the URL slug to check / URL-идентификатор для проверки
     * @return true if page exists / true если страница существует
     */
    boolean existsBySlug(String slug);

    /**
     * Finds pages by slug containing the given string (case-insensitive).
     * Used for search functionality.
     * Находит страницы по идентификатору, содержащему данную строку (без учета регистра).
     * Используется для функциональности поиска.
     *
     * @param slug the slug fragment to search for / фрагмент идентификатора для поиска
     * @return list of matching pages / список соответствующих страниц
     */
    List<Page> findBySlugContainingIgnoreCase(String slug);

    // УДАЛЯЕМ старые методы, которые ссылаются на несуществующие поля:
    // - findByContentContaining
    // - findByTitleContaining
    // - findByTitle
}
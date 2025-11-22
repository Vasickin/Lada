package com.community.cms.repository;

import com.community.cms.model.Language;
import com.community.cms.model.PageTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing PageTranslation entities in the database.
 * Provides methods for retrieving page translations by various criteria.
 * Репозиторий для управления сущностями PageTranslation в базе данных.
 * Предоставляет методы для получения переводов страниц по различным критериям.
 *
 * @author Vasickin
 * @version 1.0
 * @since 2024
 * @see PageTranslation
 * @see Language
 */
@Repository
public interface PageTranslationRepository extends JpaRepository<PageTranslation, Long> {

    /**
     * Finds a page translation by page slug and language.
     * Useful for retrieving specific language version of a page by its URL identifier.
     * Находит перевод страницы по идентификатору страницы и языку.
     * Полезно для получения конкретной языковой версии страницы по её URL-идентификатору.
     *
     * @param slug the page URL slug / URL-идентификатор страницы
     * @param language the language of the translation / язык перевода
     * @return optional containing the translation if found / опциональный объект с переводом, если найден
     */
    @Query("SELECT pt FROM PageTranslation pt JOIN pt.page p WHERE p.slug = :slug AND pt.language = :language")
    Optional<PageTranslation> findByPageSlugAndLanguage(@Param("slug") String slug, @Param("language") Language language);

    /**
     * Finds all translations for a specific page by page ID.
     * Returns all language versions of a particular page.
     * Находит все переводы для конкретной страницы по ID страницы.
     * Возвращает все языковые версии определенной страницы.
     *
     * @param pageId the ID of the page / ID страницы
     * @return list of translations for the page / список переводов для страницы
     */
    List<PageTranslation> findByPageId(Long pageId);

    /**
     * Finds all published page translations for a specific language.
     * Only returns translations where the parent page is published.
     * Находит все опубликованные переводы страниц для конкретного языка.
     * Возвращает только переводы, где родительская страница опубликована.
     *
     * @param language the language to filter by / язык для фильтрации
     * @return list of published translations in specified language / список опубликованных переводов на указанном языке
     */
    @Query("SELECT pt FROM PageTranslation pt JOIN pt.page p WHERE p.isPublished = true AND pt.language = :language ORDER BY p.sortOrder ASC")
    List<PageTranslation> findPublishedByLanguage(@Param("language") Language language);

    /**
     * Checks if a translation exists for a specific page and language.
     * Useful for validation before creating new translations.
     * Проверяет, существует ли перевод для конкретной страницы и языка.
     * Полезно для проверки перед созданием новых переводов.
     *
     * @param pageId the ID of the page / ID страницы
     * @param language the language to check / язык для проверки
     * @return true if translation exists / true если перевод существует
     */
    boolean existsByPageIdAndLanguage(Long pageId, Language language);

    /**
     * Finds a translation by page ID and language.
     * Returns specific language version of a page.
     * Находит перевод по ID страницы и языку.
     * Возвращает конкретную языковую версию страницы.
     *
     * @param pageId the ID of the page / ID страницы
     * @param language the language of the translation / язык перевода
     * @return optional containing the translation if found / опциональный объект с переводом, если найден
     */
    Optional<PageTranslation> findByPageIdAndLanguage(Long pageId, Language language);

    /**
     * Deletes all translations for a specific page.
     * Useful when removing a page and all its associated translations.
     * Удаляет все переводы для конкретной страницы.
     * Полезно при удалении страницы и всех связанных с ней переводов.
     *
     * @param pageId the ID of the page to delete translations for / ID страницы, для которой удаляются переводы
     */
    void deleteByPageId(Long pageId);
}

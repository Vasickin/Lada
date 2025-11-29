package com.community.cms.repository;

import com.community.cms.model.GalleryItem;
import com.community.cms.model.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с элементами галереи в базе данных
 * Repository for working with gallery items in the database
 *
 * Предоставляет методы для выполнения CRUD операций и пользовательских запросов
 * Provides methods for performing CRUD operations and custom queries
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see GalleryItem
 * @see MediaType
 */
@Repository
public interface GalleryItemRepository extends JpaRepository<GalleryItem, Long> {

    /**
     * Находит все опубликованные элементы галереи, отсортированные по порядку
     * Finds all published gallery items sorted by order
     *
     * @return список опубликованных элементов / list of published items
     */
    List<GalleryItem> findByPublishedTrueOrderBySortOrderAsc();

    /**
     * Находит опубликованные элементы по году, отсортированные по порядку
     * Finds published items by year, sorted by order
     *
     * @param year год для фильтрации / year for filtering
     * @return список элементов за указанный год / list of items for specified year
     */
    List<GalleryItem> findByYearAndPublishedTrueOrderBySortOrderAsc(Integer year);

    /**
     * Находит опубликованные элементы по категории, отсортированные по порядку
     * Finds published items by category, sorted by order
     *
     * @param category категория для фильтрации / category for filtering
     * @return список элементов указанной категории / list of items for specified category
     */
    List<GalleryItem> findByCategoryAndPublishedTrueOrderBySortOrderAsc(String category);

    /**
     * Находит опубликованные элементы по типу медиа, отсортированные по порядку
     * Finds published items by media type, sorted by order
     *
     * @param mediaType тип медиа для фильтрации / media type for filtering
     * @return список элементов указанного типа / list of items for specified type
     */
    List<GalleryItem> findByMediaTypeAndPublishedTrueOrderBySortOrderAsc(MediaType mediaType);

    /**
     * Находит опубликованные элементы по году и категории, отсортированные по порядку
     * Finds published items by year and category, sorted by order
     *
     * @param year год для фильтрации / year for filtering
     * @param category категория для фильтрации / category for filtering
     * @return список элементов за указанный год и категорию / list of items for specified year and category
     */
    List<GalleryItem> findByYearAndCategoryAndPublishedTrueOrderBySortOrderAsc(Integer year, String category);

    /**
     * Находит опубликованные элементы по году и типу медиа, отсортированные по порядку
     * Finds published items by year and media type, sorted by order
     *
     * @param year год для фильтрации / year for filtering
     * @param mediaType тип медиа для фильтрации / media type for filtering
     * @return список элементов за указанный год и тип / list of items for specified year and type
     */
    List<GalleryItem> findByYearAndMediaTypeAndPublishedTrueOrderBySortOrderAsc(Integer year, MediaType mediaType);

    /**
     * Находит все элементы (включая неопубликованные) для админки
     * Finds all items (including unpublished) for admin panel
     *
     * @return список всех элементов / list of all items
     */
    List<GalleryItem> findAllByOrderBySortOrderAsc();

    /**
     * Находит элемент по ID только если он опубликован
     * Finds item by ID only if it's published
     *
     * @param id идентификатор элемента / item identifier
     * @return Optional с элементом если найден и опубликован / Optional with item if found and published
     */
    Optional<GalleryItem> findByIdAndPublishedTrue(Long id);

    /**
     * Проверяет существование элемента по названию
     * Checks if item exists by title
     *
     * @param title название элемента / item title
     * @return true если элемент существует / true if item exists
     */
    boolean existsByTitle(String title);

    /**
     * Находит элементы по части названия (без учета регистра)
     * Finds items by title part (case insensitive)
     *
     * @param title часть названия для поиска / title part for search
     * @return список найденных элементов / list of found items
     */
    List<GalleryItem> findByTitleContainingIgnoreCase(String title);

    /**
     * Находит уникальные года из опубликованных элементов (для фильтров)
     * Finds distinct years from published items (for filters)
     *
     * @return список уникальных годов по убыванию / list of distinct years in descending order
     */
    @Query("SELECT DISTINCT g.year FROM GalleryItem g WHERE g.published = true ORDER BY g.year DESC")
    List<Integer> findDistinctYears();

    /**
     * Находит уникальные категории из опубликованных элементов (для фильтров)
     * Finds distinct categories from published items (for filters)
     *
     * @return список уникальных категорий / list of distinct categories
     */
    @Query("SELECT DISTINCT g.category FROM GalleryItem g WHERE g.published = true ORDER BY g.category ASC")
    List<String> findDistinctCategories();

    /**
     * Подсчитывает количество элементов по статусу публикации
     * Counts items by publication status
     *
     * @param published статус публикации / publication status
     * @return количество элементов / items count
     */
    long countByPublished(Boolean published);

    /**
     * Находит элементы по диапазону годов
     * Finds items by year range
     *
     * @param startYear начальный год / start year
     * @param endYear конечный год / end year
     * @return список элементов в диапазоне / list of items in range
     */
    @Query("SELECT g FROM GalleryItem g WHERE g.year BETWEEN :startYear AND :endYear AND g.published = true ORDER BY g.year DESC, g.sortOrder ASC")
    List<GalleryItem> findByYearRangeAndPublishedTrue(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

    /**
     * Находит последние добавленные элементы (для главной страницы)
     * Finds recently added items (for homepage)
     *
     * @param limit ограничение количества / items limit
     * @return список последних элементов / list of recent items
     */
    @Query("SELECT g FROM GalleryItem g WHERE g.published = true ORDER BY g.createdAt DESC LIMIT :limit")
    List<GalleryItem> findRecentItems(@Param("limit") int limit);
}

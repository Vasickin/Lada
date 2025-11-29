package com.community.cms.service;

import com.community.cms.model.GalleryItem;
import com.community.cms.model.MediaType;
import com.community.cms.repository.GalleryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для бизнес-логики работы с галереей
 * Service for gallery business logic
 *
 * Обеспечивает взаимодействие между контроллерами и репозиторием
 * Provides interaction between controllers and repository
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see GalleryItemRepository
 * @see GalleryItem
 */
@Service
public class GalleryService {

    private final GalleryItemRepository galleryItemRepository;

    /**
     * Конструктор с внедрением зависимости репозитория
     * Constructor with repository dependency injection
     *
     * @param galleryItemRepository репозиторий элементов галереи / gallery items repository
     */
    @Autowired
    public GalleryService(GalleryItemRepository galleryItemRepository) {
        this.galleryItemRepository = galleryItemRepository;
    }

    /**
     * Сохраняет элемент галереи
     * Saves gallery item
     *
     * @param galleryItem элемент для сохранения / item to save
     * @return сохраненный элемент / saved item
     */
    public GalleryItem saveGalleryItem(GalleryItem galleryItem) {
        return galleryItemRepository.save(galleryItem);
    }

    /**
     * Находит элемент по ID
     * Finds item by ID
     *
     * @param id идентификатор элемента / item identifier
     * @return Optional с элементом если найден / Optional with item if found
     */
    public Optional<GalleryItem> findGalleryItemById(Long id) {
        return galleryItemRepository.findById(id);
    }

    /**
     * Находит все опубликованные элементы галереи
     * Finds all published gallery items
     *
     * @return список опубликованных элементов / list of published items
     */
    public List<GalleryItem> findAllPublishedItems() {
        return galleryItemRepository.findByPublishedTrueOrderBySortOrderAsc();
    }

    /**
     * Находит все элементы (для админки)
     * Finds all items (for admin panel)
     *
     * @return список всех элементов / list of all items
     */
    public List<GalleryItem> findAllItems() {
        return galleryItemRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * Находит опубликованные элементы по году
     * Finds published items by year
     *
     * @param year год для фильтрации / year for filtering
     * @return список элементов за указанный год / list of items for specified year
     */
    public List<GalleryItem> findPublishedItemsByYear(Integer year) {
        return galleryItemRepository.findByYearAndPublishedTrueOrderBySortOrderAsc(year);
    }

    /**
     * Находит опубликованные элементы по категории
     * Finds published items by category
     *
     * @param category категория для фильтрации / category for filtering
     * @return список элементов указанной категории / list of items for specified category
     */
    public List<GalleryItem> findPublishedItemsByCategory(String category) {
        return galleryItemRepository.findByCategoryAndPublishedTrueOrderBySortOrderAsc(category);
    }

    /**
     * Находит опубликованные элементы по типу медиа
     * Finds published items by media type
     *
     * @param mediaType тип медиа для фильтрации / media type for filtering
     * @return список элементов указанного типа / list of items for specified type
     */
    public List<GalleryItem> findPublishedItemsByMediaType(MediaType mediaType) {
        return galleryItemRepository.findByMediaTypeAndPublishedTrueOrderBySortOrderAsc(mediaType);
    }

    /**
     * Находит опубликованные элементы по году и категории
     * Finds published items by year and category
     *
     * @param year год для фильтрации / year for filtering
     * @param category категория для фильтрации / category for filtering
     * @return список элементов за указанный год и категорию / list of items for specified year and category
     */
    public List<GalleryItem> findPublishedItemsByYearAndCategory(Integer year, String category) {
        return galleryItemRepository.findByYearAndCategoryAndPublishedTrueOrderBySortOrderAsc(year, category);
    }

    /**
     * Находит опубликованные элементы по году и типу медиа
     * Finds published items by year and media type
     *
     * @param year год для фильтрации / year for filtering
     * @param mediaType тип медиа для фильтрации / media type for filtering
     * @return список элементов за указанный год и тип / list of items for specified year and type
     */
    public List<GalleryItem> findPublishedItemsByYearAndMediaType(Integer year, MediaType mediaType) {
        return galleryItemRepository.findByYearAndMediaTypeAndPublishedTrueOrderBySortOrderAsc(year, mediaType);
    }

    /**
     * Получает список доступных годов для фильтрации
     * Gets list of available years for filtering
     *
     * @return список уникальных годов / list of distinct years
     */
    public List<Integer> getAvailableYears() {
        return galleryItemRepository.findDistinctYears();
    }

    /**
     * Получает список доступных категорий для фильтрации
     * Gets list of available categories for filtering
     *
     * @return список уникальных категорий / list of distinct categories
     */
    public List<String> getAvailableCategories() {
        return galleryItemRepository.findDistinctCategories();
    }

    /**
     * Удаляет элемент галереи по ID
     * Deletes gallery item by ID
     *
     * @param id идентификатор элемента для удаления / item identifier to delete
     */
    public void deleteGalleryItem(Long id) {
        galleryItemRepository.deleteById(id);
    }

    /**
     * Публикует элемент галереи
     * Publishes gallery item
     *
     * @param id идентификатор элемента / item identifier
     * @return true если успешно опубликовано / true if successfully published
     */
    public boolean publishGalleryItem(Long id) {
        Optional<GalleryItem> itemOpt = galleryItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            GalleryItem item = itemOpt.get();
            item.setPublished(true);
            galleryItemRepository.save(item);
            return true;
        }
        return false;
    }

    /**
     * Снимает элемент галереи с публикации
     * Unpublishes gallery item
     *
     * @param id идентификатор элемента / item identifier
     * @return true если успешно снято с публикации / true if successfully unpublished
     */
    public boolean unpublishGalleryItem(Long id) {
        Optional<GalleryItem> itemOpt = galleryItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            GalleryItem item = itemOpt.get();
            item.setPublished(false);
            galleryItemRepository.save(item);
            return true;
        }
        return false;
    }

    /**
     * Получает статистику по галерее
     * Gets gallery statistics
     *
     * @return объект со статистикой / object with statistics
     */
    public GalleryStatistics getGalleryStatistics() {
        long totalItems = galleryItemRepository.count();
        long publishedCount = galleryItemRepository.countByPublished(true);
        long draftCount = galleryItemRepository.countByPublished(false);

        return new GalleryStatistics(totalItems, publishedCount, draftCount);
    }

    /**
     * Находит последние добавленные элементы
     * Finds recently added items
     *
     * @param limit ограничение количества / items limit
     * @return список последних элементов / list of recent items
     */
    public List<GalleryItem> findRecentItems(int limit) {
        return galleryItemRepository.findRecentItems(limit);
    }

    /**
     * Внутренний класс для статистики галереи
     * Inner class for gallery statistics
     */
    public static class GalleryStatistics {
        private final long totalItems;
        private final long publishedCount;
        private final long draftCount;

        public GalleryStatistics(long totalItems, long publishedCount, long draftCount) {
            this.totalItems = totalItems;
            this.publishedCount = publishedCount;
            this.draftCount = draftCount;
        }

        public long getTotalItems() { return totalItems; }
        public long getPublishedCount() { return publishedCount; }
        public long getDraftCount() { return draftCount; }
    }
}
package com.community.cms.service.gallery;

import com.community.cms.model.gallery.MediaFile;
import com.community.cms.model.gallery.PhotoGalleryItem;
import com.community.cms.model.gallery.PublicationCategory;
import com.community.cms.repository.gallery.MediaFileRepository;
import com.community.cms.repository.gallery.PhotoGalleryItemRepository;
import com.community.cms.repository.gallery.PublicationCategoryRepository;
import com.community.cms.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с фото-галереей.
 * Управляет элементами галереи, изображениями и категориями публикации.
 *
 * Service for working with photo gallery.
 * Manages gallery items, images and publication categories.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Service
@Transactional
public class PhotoGalleryService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoGalleryService.class);

    // Максимальное количество изображений на элемент
    private static final int MAX_IMAGES_PER_ITEM = 15;
    // Максимальный размер изображения (5MB)
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private final PhotoGalleryItemRepository photoGalleryItemRepository;
    private final MediaFileRepository mediaFileRepository;
    private final PublicationCategoryRepository publicationCategoryRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public PhotoGalleryService(PhotoGalleryItemRepository photoGalleryItemRepository,
                               MediaFileRepository mediaFileRepository,
                               PublicationCategoryRepository publicationCategoryRepository,
                               FileStorageService fileStorageService) {
        this.photoGalleryItemRepository = photoGalleryItemRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.publicationCategoryRepository = publicationCategoryRepository;
        this.fileStorageService = fileStorageService;
    }

    // ========== CRUD МЕТОДЫ ==========

    /**
     * Создает новый элемент фото-галереи.
     * Creates new photo gallery item.
     *
     * @param item элемент для создания / item to create
     * @return созданный элемент / created item
     */
    public PhotoGalleryItem createPhotoGalleryItem(PhotoGalleryItem item) {
        logger.info("Создание элемента фото-галереи: {}", item.getTitle());

        // Валидация бизнес-правил
        validatePhotoGalleryItem(item);

        // Сохраняем элемент
        PhotoGalleryItem savedItem = photoGalleryItemRepository.save(item);

        logger.info("Элемент создан успешно. ID: {}", savedItem.getId());
        return savedItem;
    }

    /**
     * Создает элемент фото-галереи с загрузкой изображений.
     * Creates photo gallery item with image upload.
     *
     * @param item элемент для создания / item to create
     * @param images массив изображений / array of images
     * @return созданный элемент / created item
     * @throws IOException если произошла ошибка при загрузке файлов / if file upload error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при сохранении файлов / if file storage error occurs
     */
    public PhotoGalleryItem createPhotoGalleryItemWithImages(PhotoGalleryItem item, MultipartFile[] images)
            throws IOException, FileStorageService.FileStorageException {
        logger.info("Создание элемента с {} изображениями", images != null ? images.length : 0);

        // Валидация количества изображений
        if (images != null && images.length > MAX_IMAGES_PER_ITEM) {
            throw new IllegalArgumentException(
                    String.format("Максимальное количество изображений: %d", MAX_IMAGES_PER_ITEM)
            );
        }

        // Сначала сохраняем элемент
        PhotoGalleryItem savedItem = createPhotoGalleryItem(item);

        // Затем добавляем изображения если есть
        if (images != null && images.length > 0) {
            addImagesToPhotoGalleryItem(savedItem.getId(), images);
            // Перезагружаем элемент с изображениями
            savedItem = getPhotoGalleryItemById(savedItem.getId());
        }

        return savedItem;
    }

    /**
     * Обновляет существующий элемент фото-галереи.
     * Updates existing photo gallery item.
     *
     * @param id ID элемента / item ID
     * @param item данные для обновления / data to update
     * @return обновленный элемент / updated item
     */
    public PhotoGalleryItem updatePhotoGalleryItem(Long id, PhotoGalleryItem item) {
        logger.info("Обновление элемента фото-галереи. ID: {}", id);

        // Проверяем существование элемента
        if (!photoGalleryItemRepository.existsById(id)) {
            throw new EntityNotFoundException("Элемент фото-галереи не найден. ID: " + id);
        }

        // Устанавливаем ID
        item.setId(id);

        // Валидация бизнес-правил
        validatePhotoGalleryItem(item);

        // Сохраняем обновленный элемент
        PhotoGalleryItem updatedItem = photoGalleryItemRepository.save(item);

        logger.info("Элемент обновлен успешно. ID: {}", updatedItem.getId());
        return updatedItem;
    }

    /**
     * Обновляет элемент фото-галереи с загрузкой новых изображений.
     * Updates photo gallery item with new image upload.
     *
     * @param id ID элемента / item ID
     * @param item данные для обновления / data to update
     * @param newImages новые изображения / new images
     * @return обновленный элемент / updated item
     * @throws IOException если произошла ошибка при загрузке файлов / if file upload error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при сохранении файлов / if file storage error occurs
     */
    public PhotoGalleryItem updatePhotoGalleryItemWithImages(Long id, PhotoGalleryItem item, MultipartFile[] newImages)
            throws IOException, FileStorageService.FileStorageException {
        logger.info("Обновление элемента с новыми изображениями. ID: {}", id);

        // Получаем текущий элемент
        PhotoGalleryItem existingItem = getPhotoGalleryItemById(id);

        // Проверяем общее количество изображений
        int currentImageCount = existingItem.getImagesCount();
        if (newImages != null && (currentImageCount + newImages.length) > MAX_IMAGES_PER_ITEM) {
            throw new IllegalArgumentException(
                    String.format("Превышено максимальное количество изображений. Текущее: %d, новые: %d, максимум: %d",
                            currentImageCount, newImages.length, MAX_IMAGES_PER_ITEM)
            );
        }

        // Обновляем элемент
        PhotoGalleryItem updatedItem = updatePhotoGalleryItem(id, item);

        // Добавляем новые изображения если есть
        if (newImages != null && newImages.length > 0) {
            addImagesToPhotoGalleryItem(id, newImages);
            // Перезагружаем элемент с изображениями
            updatedItem = getPhotoGalleryItemById(id);
        }

        return updatedItem;
    }

    /**
     * Получает элемент фото-галереи по ID.
     * Gets photo gallery item by ID.
     *
     * @param id ID элемента / item ID
     * @return элемент или null если не найден / item or null if not found
     */
    @Transactional(readOnly = true)
    public PhotoGalleryItem getPhotoGalleryItemById(Long id) {
        return photoGalleryItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Элемент фото-галереи не найден. ID: " + id));
    }

    /**
     * Получает все элементы фото-галереи.
     * Gets all photo gallery items.
     *
     * @return список всех элементов / list of all items
     */
    @Transactional(readOnly = true)
    public List<PhotoGalleryItem> getAllPhotoGalleryItems() {
        return photoGalleryItemRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Удаляет элемент фото-галереи.
     * Deletes photo gallery item.
     *
     * @param id ID элемента / item ID
     * @throws IOException если произошла ошибка при удалении файлов / if file deletion error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при удалении файлов / if file deletion error occurs
     */
    public void deletePhotoGalleryItem(Long id) throws IOException, FileStorageService.FileStorageException {
        logger.info("Удаление элемента фото-галереи. ID: {}", id);

        PhotoGalleryItem item = getPhotoGalleryItemById(id);

        // Удаляем файлы изображений
        deleteAllItemImages(item);

        // Удаляем элемент из базы данных
        photoGalleryItemRepository.deleteById(id);

        logger.info("Элемент удален успешно. ID: {}", id);
    }

    // ========== МЕТОДЫ РАБОТЫ С ИЗОБРАЖЕНИЯМИ ==========

    /**
     * Добавляет изображения к элементу фото-галереи.
     * Adds images to photo gallery item.
     *
     * @param itemId ID элемента / item ID
     * @param images массив изображений / array of images
     * @throws IOException если произошла ошибка при загрузке файлов / if file upload error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при сохранении файлов / if file storage error occurs
     */
    public void addImagesToPhotoGalleryItem(Long itemId, MultipartFile[] images)
            throws IOException, FileStorageService.FileStorageException {
        logger.info("Добавление изображений к элементу. ID: {}, количество: {}", itemId, images.length);

        PhotoGalleryItem item = getPhotoGalleryItemById(itemId);

        // Проверяем лимит изображений
        if (item.getImagesCount() + images.length > MAX_IMAGES_PER_ITEM) {
            throw new IllegalArgumentException(
                    String.format("Превышен лимит изображений. Текущее: %d, добавляемые: %d, максимум: %d",
                            item.getImagesCount(), images.length, MAX_IMAGES_PER_ITEM)
            );
        }

        // Загружаем файлы
        List<String> storedFilePaths = fileStorageService.storeFiles(images);

        List<MediaFile> mediaFiles = new ArrayList<>();

        for (int i = 0; i < images.length; i++) {
            MultipartFile image = images[i];
            String storedFilePath = storedFilePaths.get(i);

            // Валидация типа файла
            if (!isValidImageFile(image)) {
                throw new IllegalArgumentException("Файл не является изображением: " + image.getOriginalFilename());
            }

            // Валидация размера файла
            if (image.getSize() > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException(
                        String.format("Размер файла превышает лимит (%d MB): %s",
                                MAX_IMAGE_SIZE / (1024 * 1024), image.getOriginalFilename())
                );
            }

            // Создаем MediaFile
            MediaFile mediaFile = new MediaFile(
                    image.getOriginalFilename(),
                    storedFilePath,
                    image.getContentType(),
                    image.getSize()
            );

            // Устанавливаем порядок сортировки
            mediaFile.setSortOrder(item.getImagesCount() + i);

            // Если это первое изображение элемента, устанавливаем как основное
            if (item.getImagesCount() == 0 && i == 0) {
                mediaFile.setIsPrimary(true);
            }

            mediaFiles.add(mediaFile);
        }

        // Добавляем изображения к элементу
        item.addImages(mediaFiles);

        // Сохраняем элемент
        photoGalleryItemRepository.save(item);

        logger.info("Изображения успешно добавлены к элементу. ID: {}", itemId);
    }

    /**
     * Удаляет изображение из элемента фото-галереи.
     * Removes image from photo gallery item.
     *
     * @param itemId ID элемента / item ID
     * @param imageId ID изображения / image ID
     * @return true если изображение было удалено / true if image was removed
     * @throws IOException если произошла ошибка при удалении файла / if file deletion error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при удалении файла / if file deletion error occurs
     */
    public boolean removeImageFromPhotoGalleryItem(Long itemId, Long imageId)
            throws IOException, FileStorageService.FileStorageException {
        logger.info("Удаление изображения. Элемент ID: {}, изображение ID: {}", itemId, imageId);

        PhotoGalleryItem item = getPhotoGalleryItemById(itemId);
        MediaFile image = item.getImageById(imageId);

        if (image == null) {
            logger.warn("Изображение не найдено. Элемент ID: {}, изображение ID: {}", itemId, imageId);
            return false;
        }

        // Удаляем файл из файловой системы
        fileStorageService.deleteFile(image.getFilePath());

        // Удаляем изображение из элемента
        boolean removed = item.removeImageById(imageId);

        if (removed) {
            // Сохраняем изменения
            photoGalleryItemRepository.save(item);
            logger.info("Изображение удалено успешно. Элемент ID: {}, изображение ID: {}", itemId, imageId);
        }

        return removed;
    }

    /**
     * Устанавливает основное изображение элемента.
     * Sets primary image for item.
     *
     * @param itemId ID элемента / item ID
     * @param imageId ID изображения / image ID
     * @return true если операция успешна / true if operation successful
     */
    public boolean setPrimaryImage(Long itemId, Long imageId) {
        logger.info("Установка основного изображения. Элемент ID: {}, изображение ID: {}", itemId, imageId);

        PhotoGalleryItem item = getPhotoGalleryItemById(itemId);
        boolean success = item.setPrimaryImageById(imageId);

        if (success) {
            // Сохраняем изменения
            photoGalleryItemRepository.save(item);
            logger.info("Основное изображение установлено успешно. Элемент ID: {}, изображение ID: {}", itemId, imageId);
        } else {
            logger.warn("Не удалось установить основное изображение. Элемент ID: {}, изображение ID: {}", itemId, imageId);
        }

        return success;
    }

    /**
     * Удаляет все изображения элемента.
     * Deletes all item images.
     *
     * @param item элемент / item
     * @throws IOException если произошла ошибка при удалении файлов / if file deletion error occurs
     * @throws FileStorageService.FileStorageException если произошла ошибка при удалении файлов / if file storage error occurs
     */
    private void deleteAllItemImages(PhotoGalleryItem item)
            throws IOException, FileStorageService.FileStorageException {
        if (item.getImagesCount() == 0) {
            return;
        }

        for (MediaFile image : item.getImages()) {
            try {
                fileStorageService.deleteFile(image.getFilePath());
                logger.debug("Файл удален: {}", image.getFilePath());
            } catch (Exception e) {
                logger.error("Ошибка при удалении файла: {}", image.getFilePath(), e);
                // Продолжаем удаление других файлов
                // Continue deleting other files
            }
        }
    }

    // ========== МЕТОДЫ РАБОТЫ С КАТЕГОРИЯМИ ==========

    /**
     * Добавляет категорию к элементу фото-галереи.
     * Adds category to photo gallery item.
     *
     * @param itemId ID элемента / item ID
     * @param categoryId ID категории / category ID
     * @return обновленный элемент / updated item
     */
    public PhotoGalleryItem addCategoryToPhotoGalleryItem(Long itemId, Long categoryId) {
        PhotoGalleryItem item = getPhotoGalleryItemById(itemId);
        PublicationCategory category = publicationCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Категория не найдена. ID: " + categoryId));

        item.addCategory(category);
        return photoGalleryItemRepository.save(item);
    }

    /**
     * Удаляет категорию из элемента фото-галереи.
     * Removes category from photo gallery item.
     *
     * @param itemId ID элемента / item ID
     * @param categoryId ID категории / category ID
     * @return обновленный элемент / updated item
     */
    public PhotoGalleryItem removeCategoryFromPhotoGalleryItem(Long itemId, Long categoryId) {
        PhotoGalleryItem item = getPhotoGalleryItemById(itemId);
        item.removeCategoryById(categoryId);
        return photoGalleryItemRepository.save(item);
    }

    // ========== ПОИСК И ФИЛЬТРАЦИЯ ==========

    /**
     * Получает опубликованные элементы фото-галереи.
     * Gets published photo gallery items.
     *
     * @return список опубликованных элементов / list of published items
     */
    @Transactional(readOnly = true)
    public List<PhotoGalleryItem> getPublishedPhotoGalleryItems() {
        return photoGalleryItemRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    /**
     * Получает опубликованные элементы по году.
     * Gets published items by year.
     *
     * @param year год / year
     * @return список элементов за указанный год / list of items for specified year
     */
    @Transactional(readOnly = true)
    public List<PhotoGalleryItem> getPublishedPhotoGalleryItemsByYear(Integer year) {
        return photoGalleryItemRepository.findByYearAndPublishedTrueOrderByCreatedAtDesc(year);
    }

    /**
     * Получает опубликованные элементы по категории.
     * Gets published items by category.
     *
     * @param categoryName название категории / category name
     * @return список элементов указанной категории / list of items for specified category
     */
    @Transactional(readOnly = true)
    public List<PhotoGalleryItem> getPublishedPhotoGalleryItemsByCategory(String categoryName) {
        return photoGalleryItemRepository.findByCategoryNameAndPublishedTrue(categoryName);
    }

    /**
     * Получает элементы опубликованные на главной странице.
     * Gets items published on homepage.
     *
     * @return список элементов главной страницы / list of homepage items
     */
    @Transactional(readOnly = true)
    public List<PhotoGalleryItem> getHomepagePhotoGalleryItems() {
        return getPublishedPhotoGalleryItemsByCategory("Главная");
    }

    /**
     * Получает элементы опубликованные в галерее.
     * Gets items published in gallery.
     *
     * @return список элементов галереи / list of gallery items
     */
    @Transactional(readOnly = true)
    public List<PhotoGalleryItem> getGalleryPhotoGalleryItems() {
        return getPublishedPhotoGalleryItemsByCategory("Галерея");
    }

    /**
     * Поиск элементов по названию.
     * Searches items by title.
     *
     * @param query поисковый запрос / search query
     * @return список найденных элементов / list of found items
     */
    @Transactional(readOnly = true)
    public List<PhotoGalleryItem> searchPhotoGalleryItems(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPhotoGalleryItems();
        }
        return photoGalleryItemRepository.findByTitleContainingIgnoreCase(query.trim());
    }

    /**
     * Получает последние добавленные элементы.
     * Gets recently added items.
     *
     * @param limit ограничение количества / items limit
     * @return список последних элементов / list of recent items
     */
    @Transactional(readOnly = true)
    public List<PhotoGalleryItem> getRecentPhotoGalleryItems(int limit) {
        return photoGalleryItemRepository.findRecentItems(limit);
    }

    // ========== УПРАВЛЕНИЕ ПУБЛИКАЦИЕЙ ==========

    /**
     * Публикует элемент фото-галереи.
     * Publishes photo gallery item.
     *
     * @param id ID элемента / item ID
     * @return true если операция успешна / true if operation successful
     */
    public boolean publishPhotoGalleryItem(Long id) {
        PhotoGalleryItem item = getPhotoGalleryItemById(id);
        item.setPublished(true);
        photoGalleryItemRepository.save(item);

        logger.info("Элемент опубликован. ID: {}", id);
        return true;
    }

    /**
     * Снимает с публикации элемент фото-галереи.
     * Unpublishes photo gallery item.
     *
     * @param id ID элемента / item ID
     * @return true если операция успешна / true if operation successful
     */
    public boolean unpublishPhotoGalleryItem(Long id) {
        PhotoGalleryItem item = getPhotoGalleryItemById(id);
        item.setPublished(false);
        photoGalleryItemRepository.save(item);

        logger.info("Элемент снят с публикации. ID: {}", id);
        return true;
    }

    // ========== СТАТИСТИКА ==========

    /**
     * Получает статистику фото-галереи.
     * Gets photo gallery statistics.
     *
     * @return статистика галереи / gallery statistics
     */
    @Transactional(readOnly = true)
    public PhotoGalleryStatistics getPhotoGalleryStatistics() {
        long totalItems = photoGalleryItemRepository.count();
        long publishedCount = photoGalleryItemRepository.countByPublished(true);
        long draftCount = photoGalleryItemRepository.countByPublished(false);

        long totalImages = photoGalleryItemRepository.countTotalImages();
        long itemsWithoutImages = photoGalleryItemRepository.findItemsWithoutImages().size();
        long itemsWithoutCategories = photoGalleryItemRepository.findItemsWithoutCategories().size();

        return new PhotoGalleryStatistics(
                totalItems, publishedCount, draftCount,
                totalImages, itemsWithoutImages, itemsWithoutCategories
        );
    }

    /**
     * Получает список уникальных годов.
     * Gets list of distinct years.
     *
     * @return список уникальных годов / list of distinct years
     */
    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears() {
        return photoGalleryItemRepository.findDistinctYears();
    }

    /**
     * Получает элементы по диапазону годов.
     * Gets items by year range.
     *
     * @param startYear начальный год / start year
     * @param endYear конечный год / end year
     * @return список элементов в диапазоне / list of items in range
     */
    @Transactional(readOnly = true)
    public List<PhotoGalleryItem> getPhotoGalleryItemsByYearRange(Integer startYear, Integer endYear) {
        return photoGalleryItemRepository.findByYearRangeAndPublishedTrue(startYear, endYear);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Валидирует элемент фото-галереи.
     * Validates photo gallery item.
     *
     * @param item элемент для валидации / item to validate
     * @throws IllegalArgumentException если элемент не валиден / if item is not valid
     */
    private void validatePhotoGalleryItem(PhotoGalleryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Элемент не может быть null");
        }

        // Проверяем что все изображения являются именно изображениями
        for (MediaFile image : item.getImages()) {
            if (image != null && !image.isImage()) {
                throw new IllegalArgumentException("Все файлы должны быть изображениями");
            }
        }

        // Проверяем лимит изображений
        if (item.getImagesCount() > MAX_IMAGES_PER_ITEM) {
            throw new IllegalArgumentException(
                    String.format("Превышено максимальное количество изображений: %d", MAX_IMAGES_PER_ITEM)
            );
        }
    }

    /**
     * Проверяет является ли файл валидным изображением.
     * Checks if file is valid image.
     *
     * @param file файл для проверки / file to check
     * @return true если файл является изображением / true if file is image
     */
    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    // ========== КЛАСС СТАТИСТИКИ ==========

    /**
     * Класс статистики фото-галереи.
     * Photo gallery statistics class.
     */
    public static class PhotoGalleryStatistics {
        private final long totalItems;
        private final long publishedCount;
        private final long draftCount;
        private final long totalImages;
        private final long itemsWithoutImages;
        private final long itemsWithoutCategories;

        public PhotoGalleryStatistics(long totalItems, long publishedCount, long draftCount,
                                      long totalImages, long itemsWithoutImages, long itemsWithoutCategories) {
            this.totalItems = totalItems;
            this.publishedCount = publishedCount;
            this.draftCount = draftCount;
            this.totalImages = totalImages;
            this.itemsWithoutImages = itemsWithoutImages;
            this.itemsWithoutCategories = itemsWithoutCategories;
        }

        public long getTotalItems() { return totalItems; }
        public long getPublishedCount() { return publishedCount; }
        public long getDraftCount() { return draftCount; }
        public long getTotalImages() { return totalImages; }
        public long getItemsWithoutImages() { return itemsWithoutImages; }
        public long getItemsWithoutCategories() { return itemsWithoutCategories; }

        @Override
        public String toString() {
            return String.format(
                    "PhotoGalleryStatistics{totalItems=%d, published=%d, draft=%d, totalImages=%d, withoutImages=%d, withoutCategories=%d}",
                    totalItems, publishedCount, draftCount, totalImages, itemsWithoutImages, itemsWithoutCategories
            );
        }
    }
}
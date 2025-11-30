package com.community.cms.service;

import com.community.cms.model.GalleryItem;
import com.community.cms.model.GalleryMedia;
import com.community.cms.model.MediaType;
import com.community.cms.repository.GalleryItemRepository;
import com.community.cms.repository.GalleryMediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для бизнес-логики работы с галереей и медиафайлами
 * Service for gallery and media files business logic
 *
 * Расширен для поддержки множественных медиафайлов
 * Extended to support multiple media files
 *
 * @author Vasickin
 * @version 2.0
 * @since 2025
 * @see GalleryItemRepository
 * @see GalleryMediaRepository
 * @see FileStorageService
 */
@Service
@Transactional
public class GalleryService {

    private final GalleryItemRepository galleryItemRepository;
    private final GalleryMediaRepository galleryMediaRepository;
    private final FileStorageService fileStorageService;

    /**
     * Конструктор с внедрением зависимостей
     * Constructor with dependency injection
     *
     * @param galleryItemRepository репозиторий элементов галереи / gallery items repository
     * @param galleryMediaRepository репозиторий медиафайлов / media files repository
     * @param fileStorageService сервис файлового хранилища / file storage service
     */
    @Autowired
    public GalleryService(GalleryItemRepository galleryItemRepository,
                          GalleryMediaRepository galleryMediaRepository,
                          FileStorageService fileStorageService) {
        this.galleryItemRepository = galleryItemRepository;
        this.galleryMediaRepository = galleryMediaRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * СТАРЫЕ МЕТОДЫ ДЛЯ ОБРАТНОЙ СОВМЕСТИМОСТИ
     * OLD METHODS FOR BACKWARD COMPATIBILITY
     */

    /**
     * Сохраняет элемент галереи (старая версия)
     * Saves gallery item (old version)
     *
     * @param galleryItem элемент для сохранения / item to save
     * @return сохраненный элемент / saved item
     */
    public GalleryItem saveGalleryItem(GalleryItem galleryItem) {
        return galleryItemRepository.save(galleryItem);
    }

    /**
     * Находит элемент по ID (старая версия)
     * Finds item by ID (old version)
     *
     * @param id идентификатор элемента / item identifier
     * @return Optional с элементом если найден / Optional with item if found
     */
    @Transactional(readOnly = true)
    public Optional<GalleryItem> findGalleryItemById(Long id) {
        return galleryItemRepository.findById(id);
    }

    /**
     * Находит элемент по ID с полной загрузкой медиафайлов
     * Finds item by ID with full media files loading
     *
     * @param id идентификатор элемента / item identifier
     * @return Optional с элементом если найден / Optional with item if found
     */
    @Transactional(readOnly = true)
    public Optional<GalleryItem> findGalleryItemWithMediaFiles(Long id) {
        Optional<GalleryItem> itemOpt = galleryItemRepository.findById(id);

        // Явно загружаем медиафайлы для избежания LazyInitializationException
        // Explicitly load media files to avoid LazyInitializationException
        itemOpt.ifPresent(item -> {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size(); // Trigger lazy loading
            }
        });

        return itemOpt;
    }

    /**
     * Находит все опубликованные элементы галереи (старая версия)
     * Finds all published gallery items (old version)
     *
     * @return список опубликованных элементов / list of published items
     */
    @Transactional(readOnly = true)
    public List<GalleryItem> findAllPublishedItems() {
        List<GalleryItem> items = galleryItemRepository.findByPublishedTrueOrderBySortOrderAsc();

        // Загружаем медиафайлы для каждого элемента
        // Load media files for each item
        for (GalleryItem item : items) {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size(); // Trigger lazy loading
            }
        }

        return items;
    }

    /**
     * Находит все элементы (для админки) (старая версия)
     * Finds all items (for admin panel) (old version)
     *
     * @return список всех элементов / list of all items
     */
    @Transactional(readOnly = true)
    public List<GalleryItem> findAllItems() {
        List<GalleryItem> items = galleryItemRepository.findAllByOrderBySortOrderAsc();

        // Загружаем медиафайлы для каждого элемента
        // Load media files for each item
        for (GalleryItem item : items) {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size(); // Trigger lazy loading
            }
        }

        return items;
    }

    /**
     * НОВЫЕ МЕТОДЫ ДЛЯ МНОЖЕСТВЕННЫХ МЕДИАФАЙЛОВ
     * NEW METHODS FOR MULTIPLE MEDIA FILES
     */

    /**
     * Создает элемент галереи с медиафайлами
     * Creates gallery item with media files
     *
     * @param galleryItem элемент галереи / gallery item
     * @param files массив файлов для загрузки / array of files to upload
     * @return созданный элемент / created item
     * @throws IOException ошибка загрузки файлов / file upload error
     * @throws FileStorageService.FileStorageException ошибка валидации файлов / file validation error
     */
    public GalleryItem createGalleryItemWithFiles(GalleryItem galleryItem, MultipartFile[] files)
            throws IOException, FileStorageService.FileStorageException {

        // Сохраняем элемент
        // Save item
        GalleryItem savedItem = galleryItemRepository.save(galleryItem);

        // Добавляем медиафайлы если есть
        // Add media files if any
        if (files != null && files.length > 0) {
            addMediaFilesToItem(savedItem, files);
        }

        return savedItem;
    }

    /**
     * Обновляет элемент галереи с новыми файлами
     * Updates gallery item with new files
     *
     * @param itemId ID элемента / item ID
     * @param galleryItem обновленные данные / updated data
     * @param newFiles новые файлы для добавления / new files to add
     * @return обновленный элемент / updated item
     * @throws IOException ошибка загрузки файлов / file upload error
     * @throws FileStorageService.FileStorageException ошибка валидации файлов / file validation error
     */
    public GalleryItem updateGalleryItemWithFiles(Long itemId, GalleryItem galleryItem, MultipartFile[] newFiles)
            throws IOException, FileStorageService.FileStorageException {

        // Убеждаемся что ID сохранен
        // Ensure ID is preserved
        galleryItem.setId(itemId);

        // Сохраняем обновленный элемент
        // Save updated item
        GalleryItem updatedItem = galleryItemRepository.save(galleryItem);

        // Добавляем новые файлы если есть
        // Add new files if any
        if (newFiles != null && newFiles.length > 0) {
            addMediaFilesToItem(updatedItem, newFiles);
        }

        return updatedItem;
    }

    /**
     * Добавляет медиафайлы к элементу галереи
     * Adds media files to gallery item
     *
     * @param galleryItem элемент галереи / gallery item
     * @param files массив файлов / array of files
     * @throws IOException ошибка загрузки файлов / file upload error
     * @throws FileStorageService.FileStorageException ошибка валидации файлов / file validation error
     */
    public void addMediaFilesToItem(GalleryItem galleryItem, MultipartFile[] files)
            throws IOException, FileStorageService.FileStorageException {

        if (files == null || files.length == 0) {
            return;
        }

        List<String> storedFileNames = fileStorageService.storeFiles(files);

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String storedFileName = storedFileNames.get(i);

            GalleryMedia mediaFile = new GalleryMedia(
                    file.getOriginalFilename(),
                    storedFileName,
                    fileStorageService.getFileContentType(file),
                    fileStorageService.getFileSize(file),
                    fileStorageService.determineMediaType(file.getContentType()),
                    galleryItem
            );

            // Устанавливаем порядок сортировки
            // Set sort order
            mediaFile.setSortOrder(galleryItem.getMediaFilesCount() + i);

            galleryItem.addMediaFile(mediaFile);
        }

        // Сохраняем элемент с новыми файлами
        // Save item with new files
        galleryItemRepository.save(galleryItem);
    }

    /**
     * Удаляет медиафайл из элемента галереи
     * Removes media file from gallery item
     *
     * @param itemId ID элемента / item ID
     * @param mediaFileId ID медиафайла / media file ID
     * @return true если удаление успешно / true if deletion successful
     * @throws IOException ошибка удаления файла / file deletion error
     */
    public boolean removeMediaFileFromItem(Long itemId, Long mediaFileId) throws IOException {
        Optional<GalleryItem> itemOpt = galleryItemRepository.findById(itemId);

        if (itemOpt.isPresent()) {
            GalleryItem item = itemOpt.get();
            GalleryMedia mediaFile = item.getMediaFileById(mediaFileId);

            if (mediaFile != null) {
                // Удаляем файл из файловой системы
                // Delete file from file system
                fileStorageService.deleteFile(mediaFile.getFilePath());

                // Удаляем из элемента
                // Remove from item
                boolean removed = item.removeMediaFileById(mediaFileId);

                if (removed) {
                    galleryItemRepository.save(item);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Устанавливает основной медиафайл для элемента
     * Sets primary media file for item
     *
     * @param itemId ID элемента / item ID
     * @param mediaFileId ID медиафайла / media file ID
     * @return true если операция успешна / true if operation successful
     */
    public boolean setPrimaryMediaFile(Long itemId, Long mediaFileId) {
        Optional<GalleryItem> itemOpt = galleryItemRepository.findById(itemId);

        if (itemOpt.isPresent()) {
            GalleryItem item = itemOpt.get();
            boolean success = item.setPrimaryMediaById(mediaFileId);

            if (success) {
                galleryItemRepository.save(item);
                return true;
            }
        }

        return false;
    }

    /**
     * ОСТАЛЬНЫЕ СТАРЫЕ МЕТОДЫ ДЛЯ ОБРАТНОЙ СОВМЕСТИМОСТИ
     * OTHER OLD METHODS FOR BACKWARD COMPATIBILITY
     */

    @Transactional(readOnly = true)
    public List<GalleryItem> findPublishedItemsByYear(Integer year) {
        List<GalleryItem> items = galleryItemRepository.findByYearAndPublishedTrueOrderBySortOrderAsc(year);

        for (GalleryItem item : items) {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size();
            }
        }

        return items;
    }

    @Transactional(readOnly = true)
    public List<GalleryItem> findPublishedItemsByCategory(String category) {
        List<GalleryItem> items = galleryItemRepository.findByCategoryAndPublishedTrueOrderBySortOrderAsc(category);

        for (GalleryItem item : items) {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size();
            }
        }

        return items;
    }

    @Transactional(readOnly = true)
    public List<GalleryItem> findPublishedItemsByMediaType(MediaType mediaType) {
        List<GalleryItem> items = galleryItemRepository.findByMediaTypeAndPublishedTrueOrderBySortOrderAsc(mediaType);

        for (GalleryItem item : items) {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size();
            }
        }

        return items;
    }

    @Transactional(readOnly = true)
    public List<GalleryItem> findPublishedItemsByYearAndCategory(Integer year, String category) {
        List<GalleryItem> items = galleryItemRepository.findByYearAndCategoryAndPublishedTrueOrderBySortOrderAsc(year, category);

        for (GalleryItem item : items) {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size();
            }
        }

        return items;
    }

    @Transactional(readOnly = true)
    public List<GalleryItem> findPublishedItemsByYearAndMediaType(Integer year, MediaType mediaType) {
        List<GalleryItem> items = galleryItemRepository.findByYearAndMediaTypeAndPublishedTrueOrderBySortOrderAsc(year, mediaType);

        for (GalleryItem item : items) {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size();
            }
        }

        return items;
    }

    /**
     * Удаляет элемент галереи по ID со всеми медиафайлами
     * Deletes gallery item by ID with all media files
     *
     * @param id идентификатор элемента для удаления / item identifier to delete
     * @throws IOException ошибка удаления файлов / file deletion error
     */
    public void deleteGalleryItem(Long id) throws IOException {
        Optional<GalleryItem> itemOpt = galleryItemRepository.findById(id);

        if (itemOpt.isPresent()) {
            GalleryItem item = itemOpt.get();

            // Удаляем все файлы из файловой системы
            // Delete all files from file system
            if (item.getMediaFiles() != null) {
                for (GalleryMedia mediaFile : item.getMediaFiles()) {
                    fileStorageService.deleteFile(mediaFile.getFilePath());
                }
            }

            // Удаляем элемент из базы данных (каскадно удалит медиафайлы)
            // Delete item from database (will cascade delete media files)
            galleryItemRepository.deleteById(id);
        }
    }

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
    @Transactional(readOnly = true)
    public GalleryStatistics getGalleryStatistics() {
        long totalItems = galleryItemRepository.count();
        long publishedCount = galleryItemRepository.countByPublished(true);
        long draftCount = galleryItemRepository.countByPublished(false);

        // Статистика по медиафайлам
        // Media files statistics
        long totalMediaFiles = galleryMediaRepository.count();
        long totalPhotos = galleryMediaRepository.countByMediaType(MediaType.PHOTO);
        long totalVideos = galleryMediaRepository.countByMediaType(MediaType.VIDEO);

        return new GalleryStatistics(totalItems, publishedCount, draftCount, totalMediaFiles, totalPhotos, totalVideos);
    }

    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears() {
        return galleryItemRepository.findDistinctYears();
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableCategories() {
        return galleryItemRepository.findDistinctCategories();
    }

    @Transactional(readOnly = true)
    public List<GalleryItem> findRecentItems(int limit) {
        List<GalleryItem> items = galleryItemRepository.findRecentItems(limit);

        for (GalleryItem item : items) {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size();
            }
        }

        return items;
    }

    /**
     * Внутренний класс для статистики галереи
     * Inner class for gallery statistics
     */
    public static class GalleryStatistics {
        private final long totalItems;
        private final long publishedCount;
        private final long draftCount;
        private final long totalMediaFiles;
        private final long totalPhotos;
        private final long totalVideos;

        public GalleryStatistics(long totalItems, long publishedCount, long draftCount,
                                 long totalMediaFiles, long totalPhotos, long totalVideos) {
            this.totalItems = totalItems;
            this.publishedCount = publishedCount;
            this.draftCount = draftCount;
            this.totalMediaFiles = totalMediaFiles;
            this.totalPhotos = totalPhotos;
            this.totalVideos = totalVideos;
        }

        public long getTotalItems() { return totalItems; }
        public long getPublishedCount() { return publishedCount; }
        public long getDraftCount() { return draftCount; }
        public long getTotalMediaFiles() { return totalMediaFiles; }
        public long getTotalPhotos() { return totalPhotos; }
        public long getTotalVideos() { return totalVideos; }
    }
}
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GalleryService {

    private final GalleryItemRepository galleryItemRepository;
    private final GalleryMediaRepository galleryMediaRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public GalleryService(GalleryItemRepository galleryItemRepository,
                          GalleryMediaRepository galleryMediaRepository,
                          FileStorageService fileStorageService) {
        this.galleryItemRepository = galleryItemRepository;
        this.galleryMediaRepository = galleryMediaRepository;
        this.fileStorageService = fileStorageService;
    }

    // ========== ОСНОВНЫЕ МЕТОДЫ ==========

    public GalleryItem saveGalleryItem(GalleryItem galleryItem) {
        // Устанавливаем временные метки
        if (galleryItem.getCreatedAt() == null) {
            galleryItem.setCreatedAt(LocalDateTime.now());
        }
        galleryItem.setUpdatedAt(LocalDateTime.now());

        // Убеждаемся что обязательные поля не null
        ensureGalleryItemNotNull(galleryItem);

        // Убеждаемся что у всех медиафайлов установлена обратная ссылка
        ensureMediaFilesHaveBackReference(galleryItem);

        return galleryItemRepository.save(galleryItem);
    }

    @Transactional(readOnly = true)
    public Optional<GalleryItem> findGalleryItemById(Long id) {
        return galleryItemRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<GalleryItem> findGalleryItemWithMediaFiles(Long id) {
        Optional<GalleryItem> itemOpt = galleryItemRepository.findById(id);

        itemOpt.ifPresent(item -> {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size(); // Trigger lazy loading
            }
        });

        return itemOpt;
    }

    @Transactional(readOnly = true)
    public List<GalleryItem> findAllItems() {
        List<GalleryItem> items = galleryItemRepository.findAllByOrderBySortOrderAsc();

        for (GalleryItem item : items) {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size(); // Trigger lazy loading
            }
        }

        return items;
    }

    // ========== МЕТОДЫ С ФАЙЛАМИ ==========

    public GalleryItem createGalleryItemWithFiles(GalleryItem galleryItem, MultipartFile[] files)
            throws IOException, FileStorageService.FileStorageException {

        // Сначала сохраняем элемент
        GalleryItem savedItem = saveGalleryItem(galleryItem);

        // Потом добавляем файлы если есть
        if (files != null && files.length > 0) {
            addMediaFilesToItem(savedItem, files);
        }

        return savedItem;
    }

    public GalleryItem updateGalleryItemWithFiles(Long itemId, GalleryItem galleryItem, MultipartFile[] newFiles)
            throws IOException, FileStorageService.FileStorageException {

        // Устанавливаем ID
        galleryItem.setId(itemId);

        // Загружаем существующий элемент
        Optional<GalleryItem> existingItemOpt = galleryItemRepository.findById(itemId);

        if (existingItemOpt.isPresent()) {
            GalleryItem existingItem = existingItemOpt.get();

            // Сохраняем время создания
            galleryItem.setCreatedAt(existingItem.getCreatedAt());

            // Сохраняем существующие медиафайлы
            if (existingItem.getMediaFiles() != null && !existingItem.getMediaFiles().isEmpty()) {
                galleryItem.setMediaFiles(existingItem.getMediaFiles());
            }

            // Обеспечиваем обратную ссылку для всех медиафайлов
            ensureMediaFilesHaveBackReference(galleryItem);
        }

        // Обеспечиваем что поля не null
        ensureGalleryItemNotNull(galleryItem);

        // Устанавливаем время обновления
        galleryItem.setUpdatedAt(LocalDateTime.now());

        // Сохраняем обновленный элемент
        GalleryItem updatedItem = galleryItemRepository.save(galleryItem);

        // Добавляем новые файлы если есть
        if (newFiles != null && newFiles.length > 0) {
            addMediaFilesToItem(updatedItem, newFiles);
        }

        return updatedItem;
    }

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
            mediaFile.setSortOrder(galleryItem.getMediaFilesCount() + i);

            // Устанавливаем как основной, если это первый файл
            if (galleryItem.getMediaFilesCount() == 0 && i == 0) {
                mediaFile.setIsPrimary(true);
            }

            // Добавляем в коллекцию
            galleryItem.addMediaFile(mediaFile);
        }

        // Сохраняем элемент с новыми файлами
        galleryItemRepository.save(galleryItem);
    }

    public boolean removeMediaFileFromItem(Long itemId, Long mediaFileId) throws IOException {
        Optional<GalleryItem> itemOpt = galleryItemRepository.findById(itemId);

        if (itemOpt.isPresent()) {
            GalleryItem item = itemOpt.get();
            GalleryMedia mediaFile = item.getMediaFileById(mediaFileId);

            if (mediaFile != null) {
                // Удаляем файл из файловой системы
                try {
                    fileStorageService.deleteFile(mediaFile.getFilePath());
                } catch (Exception e) {
                    System.err.println("Failed to delete file from storage: " + e.getMessage());
                }

                // Удаляем из элемента
                boolean removed = item.removeMediaFileById(mediaFileId);

                if (removed) {
                    // Сохраняем изменения
                    galleryItemRepository.save(item);
                    return true;
                }
            }
        }

        return false;
    }

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

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    @Transactional(readOnly = true)
    public List<GalleryItem> findAllPublishedItems() {
        List<GalleryItem> items = galleryItemRepository.findByPublishedTrueOrderBySortOrderAsc();

        for (GalleryItem item : items) {
            if (item.getMediaFiles() != null) {
                item.getMediaFiles().size();
            }
        }

        return items;
    }

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

    public void deleteGalleryItem(Long id) throws IOException {
        Optional<GalleryItem> itemOpt = galleryItemRepository.findById(id);

        if (itemOpt.isPresent()) {
            GalleryItem item = itemOpt.get();

            // Удаляем все файлы из файловой системы
            if (item.getMediaFiles() != null) {
                for (GalleryMedia mediaFile : item.getMediaFiles()) {
                    try {
                        fileStorageService.deleteFile(mediaFile.getFilePath());
                    } catch (Exception e) {
                        System.err.println("Failed to delete file " + mediaFile.getFilePath() + ": " + e.getMessage());
                    }
                }
            }

            // Удаляем элемент из базы данных
            galleryItemRepository.deleteById(id);
        }
    }

    public boolean publishGalleryItem(Long id) {
        Optional<GalleryItem> itemOpt = galleryItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            GalleryItem item = itemOpt.get();
            item.setPublished(true);
            item.setUpdatedAt(LocalDateTime.now());
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
            item.setUpdatedAt(LocalDateTime.now());
            galleryItemRepository.save(item);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public GalleryStatistics getGalleryStatistics() {
        long totalItems = galleryItemRepository.count();
        long publishedCount = galleryItemRepository.countByPublished(true);
        long draftCount = galleryItemRepository.countByPublished(false);

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

    // ========== ПРИВАТНЫЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private void ensureGalleryItemNotNull(GalleryItem galleryItem) {
        if (galleryItem.getImageUrl() == null) {
            galleryItem.setImageUrl("");
        }
        if (galleryItem.getVideoUrl() == null) {
            galleryItem.setVideoUrl("");
        }
        if (galleryItem.getPublished() == null) {
            galleryItem.setPublished(true);
        }
        if (galleryItem.getSortOrder() == null) {
            galleryItem.setSortOrder(0);
        }
        if (galleryItem.getCreatedAt() == null) {
            galleryItem.setCreatedAt(LocalDateTime.now());
        }
    }

    private void ensureMediaFilesHaveBackReference(GalleryItem galleryItem) {
        if (galleryItem.getMediaFiles() != null) {
            for (GalleryMedia media : galleryItem.getMediaFiles()) {
                if (media != null && media.getGalleryItem() == null) {
                    media.setGalleryItem(galleryItem);
                }
            }
        }
    }

    // ========== КЛАСС СТАТИСТИКИ ==========

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
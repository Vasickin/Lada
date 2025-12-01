package com.community.cms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Сущность для элементов галереи с поддержкой множественных медиафайлов
 * Entity for gallery items with multiple media files support
 *
 * Расширенная система управления медиа-контентом с возможностью
 * загрузки нескольких файлов (фото и видео) для одного элемента
 *
 * Extended media content management system with ability to
 * upload multiple files (photos and videos) for single item
 *
 * @author Vasickin
 * @version 2.2 (Исправлены ошибки null-безопасности и обратных ссылок)
 * @since 2025
 */
@Entity
@Table(name = "gallery_items")
public class GalleryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название обязательно / Title is required")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // СТАРЫЕ ПОЛЯ ДЛЯ ОБРАТНОЙ СОВМЕСТИМОСТИ
    // OLD FIELDS FOR BACKWARD COMPATIBILITY
    @Column(name = "image_url", nullable = false)
    private String imageUrl = "";

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @NotNull(message = "Год обязателен / Year is required")
    @Column(nullable = false)
    private Integer year;

    @NotBlank(message = "Категория обязательна / Category is required")
    @Column(nullable = false)
    private String category; // education, volunteering, projects, events, community, ecology

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean published = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // НОВАЯ СВЯЗЬ С МНОЖЕСТВЕННЫМИ МЕДИАФАЙЛАМИ
    // NEW RELATIONSHIP WITH MULTIPLE MEDIA FILES
    @OneToMany(
            mappedBy = "galleryItem",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("isPrimary DESC, sortOrder ASC")
    private List<GalleryMedia> mediaFiles = new ArrayList<>();

    // Конструкторы / Constructors
    public GalleryItem() {
        this.createdAt = LocalDateTime.now();
        this.mediaFiles = new ArrayList<>();
        this.imageUrl = "";
        this.published = true;
        this.sortOrder = 0;
    }

    public GalleryItem(String title, String description, String imageUrl, Integer year,
                       String category, MediaType mediaType) {
        this();
        this.title = title;
        this.description = description;
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : "";
        this.year = year;
        this.category = category;
        this.mediaType = mediaType;
    }

    // Геттеры и сеттеры / Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    // СТАРЫЕ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ ОБРАТНОЙ СОВМЕСТИМОСТИ
    // OLD GETTERS AND SETTERS FOR BACKWARD COMPATIBILITY
    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : "";
    }

    public String getThumbnailUrl() {
        return thumbnailUrl != null ? thumbnailUrl : "";
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl != null ? thumbnailUrl : "";
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public void setCategory(String category) {
        this.category = category != null ? category.trim() : "";
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getVideoUrl() {
        return videoUrl != null ? videoUrl : "";
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl != null ? videoUrl : "";
    }

    public Integer getSortOrder() {
        return sortOrder != null ? sortOrder : 0;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public Boolean getPublished() {
        return published != null ? published : true;
    }

    public void setPublished(Boolean published) {
        this.published = published != null ? published : true;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt != null ? createdAt : LocalDateTime.now();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // НОВЫЕ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ МЕДИАФАЙЛОВ (БЕЗОПАСНЫЕ)
    // NEW GETTERS AND SETTERS FOR MEDIA FILES (SAFE)

    /**
     * Возвращает список медиафайлов (гарантированно не null)
     * Returns media files list (guaranteed not null)
     *
     * @return список медиафайлов / media files list
     */
    public List<GalleryMedia> getMediaFiles() {
        if (mediaFiles == null) {
            mediaFiles = new ArrayList<>();
        }
        return mediaFiles;
    }

    /**
     * Устанавливает список медиафайлов с безопасной обработкой
     * Sets media files list with safe handling
     *
     * @param mediaFiles список медиафайлов / media files list
     */
    public void setMediaFiles(List<GalleryMedia> mediaFiles) {
        if (mediaFiles == null) {
            this.mediaFiles = new ArrayList<>();
        } else {
            this.mediaFiles = new ArrayList<>(mediaFiles);
            // Устанавливаем обратную ссылку для всех файлов
            // Set back reference for all files
            for (GalleryMedia media : this.mediaFiles) {
                if (media != null) {
                    media.setGalleryItem(this);
                }
            }
        }
    }

    // Методы предварительной обработки / Pre-persist methods

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (mediaFiles == null) {
            mediaFiles = new ArrayList<>();
        }
        if (imageUrl == null) {
            imageUrl = "";
        }
        if (published == null) {
            published = true;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (title == null) {
            title = "";
        }
        if (category == null) {
            category = "";
        }
        if (mediaType == null) {
            mediaType = MediaType.PHOTO; // Значение по умолчанию
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Гарантируем что у всех файлов установлена обратная ссылка
        // Ensure all files have back reference
        if (mediaFiles != null) {
            for (GalleryMedia media : mediaFiles) {
                if (media != null && media.getGalleryItem() == null) {
                    media.setGalleryItem(this);
                }
            }
        }
    }

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С МЕДИАФАЙЛАМИ (БЕЗОПАСНЫЕ)
    // HELPER METHODS FOR WORKING WITH MEDIA FILES (SAFE)

    /**
     * Добавляет медиафайл к элементу галереи (безопасная версия)
     * Adds media file to gallery item (safe version)
     *
     * @param mediaFile медиафайл для добавления / media file to add
     */
    public void addMediaFile(GalleryMedia mediaFile) {
        List<GalleryMedia> files = getMediaFiles(); // Используем безопасный геттер

        if (mediaFile != null) {
            mediaFile.setGalleryItem(this);
            files.add(mediaFile);

            // Если это первый файл, устанавливаем его как основной
            // If this is first file, set it as primary
            if (files.size() == 1) {
                mediaFile.setIsPrimary(true);
            }
        }
    }

    /**
     * Добавляет несколько медиафайлов к элементу галереи (безопасная версия)
     * Adds multiple media files to gallery item (safe version)
     *
     * @param mediaFiles список медиафайлов / list of media files
     */
    public void addMediaFiles(List<GalleryMedia> mediaFiles) {
        if (mediaFiles != null) {
            for (GalleryMedia mediaFile : mediaFiles) {
                addMediaFile(mediaFile);
            }
        }
    }

    /**
     * Удаляет медиафайл из элемента галереи (безопасная версия)
     * Removes media file from gallery item (safe version)
     *
     * @param mediaFile медиафайл для удаления / media file to remove
     * @return true если файл был удален / true if file was removed
     */
    public boolean removeMediaFile(GalleryMedia mediaFile) {
        if (mediaFile != null && getMediaFiles().contains(mediaFile)) {
            boolean wasPrimary = Boolean.TRUE.equals(mediaFile.getIsPrimary());
            boolean removed = getMediaFiles().remove(mediaFile);

            if (removed) {
                mediaFile.setGalleryItem(null);

                // Если удалили основной файл и остались другие файлы
                // If primary file was removed and there are other files left
                if (wasPrimary && !getMediaFiles().isEmpty()) {
                    // Находим первый файл (не удаленный) и делаем его основным
                    // Find first file (not removed) and make it primary
                    GalleryMedia newPrimary = getMediaFiles().get(0);
                    if (newPrimary != null && !newPrimary.equals(mediaFile)) {
                        newPrimary.setIsPrimary(true);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Удаляет медиафайл по ID (безопасная версия)
     * Removes media file by ID (safe version)
     *
     * @param mediaFileId ID медиафайла / media file ID
     * @return true если файл был удален / true if file was removed
     */
    public boolean removeMediaFileById(Long mediaFileId) {
        if (mediaFileId != null) {
            List<GalleryMedia> files = getMediaFiles();

            for (GalleryMedia mediaFile : files) {
                if (mediaFile != null && mediaFile.getId() != null && mediaFile.getId().equals(mediaFileId)) {
                    boolean wasPrimary = Boolean.TRUE.equals(mediaFile.getIsPrimary());
                    boolean removed = files.remove(mediaFile);

                    if (removed) {
                        mediaFile.setGalleryItem(null);

                        // Если удалили основной файл и остались другие файлы
                        // If primary file was removed and there are other files left
                        if (wasPrimary && !files.isEmpty()) {
                            // Ищем подходящий файл для установки как основной
                            // Find suitable file to set as primary
                            for (GalleryMedia remainingFile : files) {
                                if (remainingFile != null && !remainingFile.getId().equals(mediaFileId)) {
                                    remainingFile.setIsPrimary(true);
                                    break;
                                }
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Очищает все медиафайлы элемента (безопасная версия)
     * Clears all media files of item (safe version)
     */
    public void clearMediaFiles() {
        List<GalleryMedia> files = getMediaFiles();
        for (GalleryMedia mediaFile : files) {
            if (mediaFile != null) {
                mediaFile.setGalleryItem(null);
            }
        }
        files.clear();
    }

    /**
     * Возвращает основной медиафайл элемента (безопасная версия)
     * Returns primary media file of item (safe version)
     *
     * @return основной медиафайл или null / primary media file or null
     */
    public GalleryMedia getPrimaryMedia() {
        List<GalleryMedia> files = getMediaFiles();

        if (files.isEmpty()) {
            return null;
        }

        // Сначала ищем явно помеченный как основной
        // First look for explicitly marked as primary
        for (GalleryMedia mediaFile : files) {
            if (mediaFile != null && Boolean.TRUE.equals(mediaFile.getIsPrimary())) {
                return mediaFile;
            }
        }

        // Если нет явно помеченного, возвращаем первый файл
        // If no explicitly marked, return first file
        return files.isEmpty() ? null : files.get(0);
    }

    /**
     * Устанавливает основной медиафайл (безопасная версия)
     * Sets primary media file (safe version)
     *
     * @param mediaFile медиафайл для установки как основной / media file to set as primary
     * @throws IllegalArgumentException если файл не принадлежит элементу / if file doesn't belong to item
     */
    public void setPrimaryMedia(GalleryMedia mediaFile) {
        if (mediaFile == null) {
            throw new IllegalArgumentException("Медиафайл не может быть null / Media file cannot be null");
        }

        List<GalleryMedia> files = getMediaFiles();

        if (!files.contains(mediaFile)) {
            throw new IllegalArgumentException("Медиафайл не принадлежит этому элементу галереи / Media file doesn't belong to this gallery item");
        }

        // Снимаем статус основного со всех файлов
        // Remove primary status from all files
        for (GalleryMedia file : files) {
            if (file != null) {
                file.setIsPrimary(false);
            }
        }

        // Устанавливаем новый основной файл
        // Set new primary file
        mediaFile.setIsPrimary(true);
    }

    /**
     * Устанавливает основной медиафайл по ID (безопасная версия)
     * Sets primary media file by ID (safe version)
     *
     * @param mediaFileId ID медиафайла / media file ID
     * @return true если операция успешна / true if operation successful
     */
    public boolean setPrimaryMediaById(Long mediaFileId) {
        if (mediaFileId != null) {
            List<GalleryMedia> files = getMediaFiles();

            for (GalleryMedia mediaFile : files) {
                if (mediaFile != null && mediaFile.getId() != null && mediaFile.getId().equals(mediaFileId)) {
                    setPrimaryMedia(mediaFile);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Проверяет содержит ли элемент фото
     * Checks if item contains photos
     *
     * @return true если есть фото / true if has photos
     */
    public boolean hasPhotos() {
        return getMediaFiles().stream()
                .filter(Objects::nonNull)
                .anyMatch(mediaFile -> mediaFile.getMediaType() == MediaType.PHOTO);
    }

    /**
     * Проверяет содержит ли элемент видео
     * Checks if item contains videos
     *
     * @return true если есть видео / true if has videos
     */
    public boolean hasVideos() {
        return getMediaFiles().stream()
                .filter(Objects::nonNull)
                .anyMatch(mediaFile -> mediaFile.getMediaType() == MediaType.VIDEO);
    }

    /**
     * Возвращает список всех фото элемента (безопасная версия)
     * Returns list of all photos of item (safe version)
     *
     * @return список фото / list of photos
     */
    public List<GalleryMedia> getPhotos() {
        return getMediaFiles().stream()
                .filter(Objects::nonNull)
                .filter(mediaFile -> mediaFile.getMediaType() == MediaType.PHOTO)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список всех видео элемента (безопасная версия)
     * Returns list of all videos of item (safe version)
     *
     * @return список видео / list of videos
     */
    public List<GalleryMedia> getVideos() {
        return getMediaFiles().stream()
                .filter(Objects::nonNull)
                .filter(mediaFile -> mediaFile.getMediaType() == MediaType.VIDEO)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает количество медиафайлов элемента
     * Returns count of item's media files
     *
     * @return количество медиафайлов / count of media files
     */
    public int getMediaFilesCount() {
        return getMediaFiles().size();
    }

    /**
     * Возвращает количество фото элемента
     * Returns count of item's photos
     *
     * @return количество фото / count of photos
     */
    public int getPhotosCount() {
        return getPhotos().size();
    }

    /**
     * Возвращает количество видео элемента
     * Returns count of item's videos
     *
     * @return количество видео / count of videos
     */
    public int getVideosCount() {
        return getVideos().size();
    }

    /**
     * Проверяет имеет ли элемент медиафайлы
     * Checks if item has media files
     *
     * @return true если есть медиафайлы / true if has media files
     */
    public boolean hasMediaFiles() {
        return !getMediaFiles().isEmpty();
    }

    /**
     * Получает медиафайл по ID (безопасная версия)
     * Gets media file by ID (safe version)
     *
     * @param mediaFileId ID медиафайла / media file ID
     * @return медиафайл или null / media file or null
     */
    public GalleryMedia getMediaFileById(Long mediaFileId) {
        if (mediaFileId != null) {
            return getMediaFiles().stream()
                    .filter(Objects::nonNull)
                    .filter(mediaFile -> mediaFile.getId() != null && mediaFile.getId().equals(mediaFileId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public String toString() {
        return "GalleryItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", year=" + year +
                ", category='" + category + '\'' +
                ", mediaType=" + mediaType +
                ", published=" + published +
                ", mediaFilesCount=" + getMediaFilesCount() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GalleryItem that = (GalleryItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
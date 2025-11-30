package com.community.cms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
 * @version 2.0
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
    @NotBlank(message = "URL изображения обязателен / Image URL is required")
    @Column(nullable = false)
    private String imageUrl;

    private String thumbnailUrl;

    @NotNull(message = "Год обязателен / Year is required")
    @Column(nullable = false)
    private Integer year;

    @NotBlank(message = "Категория обязательна / Category is required")
    @Column(nullable = false)
    private String category; // education, volunteering, projects, events, community, ecology

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    private String videoUrl;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean published = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

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
    }

    public GalleryItem(String title, String description, String imageUrl, Integer year, String category, MediaType mediaType) {
        this();
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
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
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // СТАРЫЕ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ ОБРАТНОЙ СОВМЕСТИМОСТИ
    // OLD GETTERS AND SETTERS FOR BACKWARD COMPATIBILITY
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // НОВЫЕ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ МЕДИАФАЙЛОВ
    // NEW GETTERS AND SETTERS FOR MEDIA FILES
    public List<GalleryMedia> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(List<GalleryMedia> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    // Методы предварительной обработки / Pre-persist methods

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (mediaFiles == null) {
            mediaFiles = new ArrayList<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С МЕДИАФАЙЛАМИ
    // HELPER METHODS FOR WORKING WITH MEDIA FILES

    /**
     * Добавляет медиафайл к элементу галереи
     * Adds media file to gallery item
     *
     * @param mediaFile медиафайл для добавления / media file to add
     */
    public void addMediaFile(GalleryMedia mediaFile) {
        if (mediaFiles == null) {
            mediaFiles = new ArrayList<>();
        }

        mediaFile.setGalleryItem(this);
        mediaFiles.add(mediaFile);

        // Если это первый файл, устанавливаем его как основной
        // If this is first file, set it as primary
        if (mediaFiles.size() == 1) {
            mediaFile.setIsPrimary(true);
        }
    }

    /**
     * Добавляет несколько медиафайлов к элементу галереи
     * Adds multiple media files to gallery item
     *
     * @param mediaFiles список медиафайлов / list of media files
     */
    public void addMediaFiles(List<GalleryMedia> mediaFiles) {
        if (this.mediaFiles == null) {
            this.mediaFiles = new ArrayList<>();
        }

        for (GalleryMedia mediaFile : mediaFiles) {
            addMediaFile(mediaFile);
        }
    }

    /**
     * Удаляет медиафайл из элемента галереи
     * Removes media file from gallery item
     *
     * @param mediaFile медиафайл для удаления / media file to remove
     */
    public void removeMediaFile(GalleryMedia mediaFile) {
        if (mediaFiles != null) {
            boolean wasPrimary = mediaFile.getIsPrimary();
            mediaFiles.remove(mediaFile);
            mediaFile.setGalleryItem(null);

            // Если удалили основной файл, устанавливаем новый основной
            // If primary file was removed, set new primary
            if (wasPrimary && !mediaFiles.isEmpty()) {
                mediaFiles.get(0).setIsPrimary(true);
            }
        }
    }

    /**
     * Удаляет медиафайл по ID
     * Removes media file by ID
     *
     * @param mediaFileId ID медиафайла / media file ID
     * @return true если файл был удален / true if file was removed
     */
    public boolean removeMediaFileById(Long mediaFileId) {
        if (mediaFiles != null) {
            return mediaFiles.removeIf(mediaFile -> {
                if (mediaFile.getId() != null && mediaFile.getId().equals(mediaFileId)) {
                    boolean wasPrimary = mediaFile.getIsPrimary();
                    mediaFile.setGalleryItem(null);

                    // Если удалили основной файл, устанавливаем новый основной
                    // If primary file was removed, set new primary
                    if (wasPrimary && !mediaFiles.isEmpty()) {
                        mediaFiles.get(0).setIsPrimary(true);
                    }
                    return true;
                }
                return false;
            });
        }
        return false;
    }

    /**
     * Очищает все медиафайлы элемента
     * Clears all media files of item
     */
    public void clearMediaFiles() {
        if (mediaFiles != null) {
            mediaFiles.forEach(mediaFile -> mediaFile.setGalleryItem(null));
            mediaFiles.clear();
        }
    }

    /**
     * Возвращает основной медиафайл элемента
     * Returns primary media file of item
     *
     * @return основной медиафайл или null / primary media file or null
     */
    public GalleryMedia getPrimaryMedia() {
        if (mediaFiles == null || mediaFiles.isEmpty()) {
            return null;
        }

        return mediaFiles.stream()
                .filter(GalleryMedia::getIsPrimary)
                .findFirst()
                .orElse(mediaFiles.get(0)); // Возвращаем первый если основной не установлен
    }

    /**
     * Устанавливает основной медиафайл
     * Sets primary media file
     *
     * @param mediaFile медиафайл для установки как основной / media file to set as primary
     * @throws IllegalArgumentException если файл не принадлежит элементу / if file doesn't belong to item
     */
    public void setPrimaryMedia(GalleryMedia mediaFile) {
        if (mediaFiles == null || !mediaFiles.contains(mediaFile)) {
            throw new IllegalArgumentException("Медиафайл не принадлежит этому элементу галереи / Media file doesn't belong to this gallery item");
        }

        // Снимаем статус основного со всех файлов
        // Remove primary status from all files
        mediaFiles.forEach(file -> file.setIsPrimary(false));

        // Устанавливаем новый основной файл
        // Set new primary file
        mediaFile.setIsPrimary(true);
    }

    /**
     * Устанавливает основной медиафайл по ID
     * Sets primary media file by ID
     *
     * @param mediaFileId ID медиафайла / media file ID
     * @return true если операция успешна / true if operation successful
     */
    public boolean setPrimaryMediaById(Long mediaFileId) {
        if (mediaFiles != null) {
            for (GalleryMedia mediaFile : mediaFiles) {
                if (mediaFile.getId() != null && mediaFile.getId().equals(mediaFileId)) {
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
        return mediaFiles != null && mediaFiles.stream()
                .anyMatch(mediaFile -> mediaFile.getMediaType() == MediaType.PHOTO);
    }

    /**
     * Проверяет содержит ли элемент видео
     * Checks if item contains videos
     *
     * @return true если есть видео / true if has videos
     */
    public boolean hasVideos() {
        return mediaFiles != null && mediaFiles.stream()
                .anyMatch(mediaFile -> mediaFile.getMediaType() == MediaType.VIDEO);
    }

    /**
     * Возвращает список всех фото элемента
     * Returns list of all photos of item
     *
     * @return список фото / list of photos
     */
    public List<GalleryMedia> getPhotos() {
        if (mediaFiles == null) {
            return new ArrayList<>();
        }

        return mediaFiles.stream()
                .filter(mediaFile -> mediaFile.getMediaType() == MediaType.PHOTO)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список всех видео элемента
     * Returns list of all videos of item
     *
     * @return список видео / list of videos
     */
    public List<GalleryMedia> getVideos() {
        if (mediaFiles == null) {
            return new ArrayList<>();
        }

        return mediaFiles.stream()
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
        return mediaFiles != null ? mediaFiles.size() : 0;
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
        return mediaFiles != null && !mediaFiles.isEmpty();
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

    /**
     * Получает медиафайл по ID
     * Gets media file by ID
     *
     * @param mediaFileId ID медиафайла / media file ID
     * @return медиафайл или null / media file or null
     */
    public GalleryMedia getMediaFileById(Long mediaFileId) {
        if (mediaFiles != null) {
            return mediaFiles.stream()
                    .filter(mediaFile -> mediaFile.getId() != null && mediaFile.getId().equals(mediaFileId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
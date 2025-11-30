package com.community.cms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Сущность для хранения медиафайлов галереи
 * Поддерживает множественную загрузку файлов для одного элемента галереи
 *
 * Entity for storing gallery media files
 * Supports multiple file uploads for single gallery item
 */
@Entity
@Table(name = "gallery_media")
public class GalleryMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Имя файла обязательно / File name is required")
    @Column(nullable = false)
    private String fileName;

    @NotNull(message = "Путь к файлу обязателен / File path is required")
    @Column(nullable = false)
    private String filePath;

    @NotNull(message = "Тип файла обязателен / File type is required")
    @Column(nullable = false)
    private String fileType; // image/jpeg, video/mp4, etc.

    @Column(name = "file_size")
    private Long fileSize; // размер в байтах / size in bytes

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType; // PHOTO, VIDEO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gallery_item_id", nullable = false)
    private GalleryItem galleryItem;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Конструкторы / Constructors
    public GalleryMedia() {
        this.createdAt = LocalDateTime.now();
    }

    public GalleryMedia(String fileName, String filePath, String fileType, Long fileSize, MediaType mediaType) {
        this();
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.mediaType = mediaType;
    }

    public GalleryMedia(String fileName, String filePath, String fileType, Long fileSize,
                        MediaType mediaType, GalleryItem galleryItem) {
        this(fileName, filePath, fileType, fileSize, mediaType);
        this.galleryItem = galleryItem;
    }

    // Геттеры и сеттеры / Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public GalleryItem getGalleryItem() {
        return galleryItem;
    }

    public void setGalleryItem(GalleryItem galleryItem) {
        this.galleryItem = galleryItem;
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

    // Методы предварительной обработки / Pre-persist methods

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Вспомогательные методы / Helper methods

    /**
     * Возвращает полный URL для доступа к файлу
     * Returns full URL for file access
     */
    public String getFileUrl() {
        return "/uploads/" + this.filePath;
    }

    /**
     * Возвращает читаемый размер файла
     * Returns human readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    /**
     * Проверяет является ли файл изображением
     * Checks if file is an image
     */
    public boolean isImage() {
        return mediaType == MediaType.PHOTO;
    }

    /**
     * Проверяет является ли файл видео
     * Checks if file is a video
     */
    public boolean isVideo() {
        return mediaType == MediaType.VIDEO;
    }

    /**
     * Возвращает расширение файла
     * Returns file extension
     */
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    @Override
    public String toString() {
        return "GalleryMedia{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", mediaType=" + mediaType +
                ", isPrimary=" + isPrimary +
                ", sortOrder=" + sortOrder +
                '}';
    }
}

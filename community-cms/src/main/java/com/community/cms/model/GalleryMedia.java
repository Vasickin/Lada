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

    @Column(name = "original_file_name")
    private String originalFileName;

    @NotNull(message = "Путь к файлу обязателен / File path is required")
    @Column(nullable = false)
    private String filePath;

    @Column(name = "file_url")
    private String fileUrl;

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
        this.originalFileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.mediaType = mediaType;
        this.fileUrl = "/uploads/" + filePath;
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

    public String getOriginalFileName() {
        return originalFileName != null ? originalFileName : fileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        if (filePath != null && !filePath.isEmpty()) {
            String name = filePath;
            if (name.contains("/")) {
                name = name.substring(name.lastIndexOf("/") + 1);
            }
            return "/" + name;
        }
        return "/images/default.jpg";
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
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
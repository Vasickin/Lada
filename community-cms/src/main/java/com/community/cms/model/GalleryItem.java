package com.community.cms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Сущность для элементов галереи (фото и видео)
 * Расширенная система управления медиа-контентом
 *
 * Entity for gallery items (photos and videos)
 * Extended media content management system
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
    private MediaType mediaType; // PHOTO, VIDEO

    private String videoUrl; // для видео / for videos

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean published = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Конструкторы / Constructors
    public GalleryItem() {
        this.createdAt = LocalDateTime.now();
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

    // Методы предварительной обработки / Pre-persist methods

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
                '}';
    }
}

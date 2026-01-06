package com.community.cms.web.mvc.dto.content;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Data Transfer Object для галереи (PhotoGalleryItem).
 * Используется для передачи данных о галерее на frontend.
 * Data Transfer Object for gallery (PhotoGalleryItem).
 * Used for transferring gallery data to frontend.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GalleryDTO {

    private Long id;
    private String title;
    private Integer year;
    private String description;
    private Integer photoCount;
    private Boolean published;

    /**
     * Конструктор по умолчанию.
     * Default constructor.
     */
    public GalleryDTO() {
    }

    /**
     * Конструктор с основными параметрами.
     * Constructor with main parameters.
     *
     * @param id идентификатор галереи / gallery ID
     * @param title название галереи / gallery title
     * @param year год галереи / gallery year
     * @param photoCount количество фото в галерее / number of photos in gallery
     */
    public GalleryDTO(Long id, String title, Integer year, Integer photoCount) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.photoCount = photoCount;
    }

    /**
     * Конструктор с полным набором параметров.
     * Constructor with full set of parameters.
     *
     * @param id идентификатор галереи / gallery ID
     * @param title название галереи / gallery title
     * @param year год галереи / gallery year
     * @param description описание галереи / gallery description
     * @param photoCount количество фото в галерее / number of photos in gallery
     * @param published статус публикации / publication status
     */
    public GalleryDTO(Long id, String title, Integer year, String description,
                      Integer photoCount, Boolean published) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.description = description;
        this.photoCount = photoCount;
        this.published = published;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    @Override
    public String toString() {
        return "GalleryDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", year=" + year +
                ", photoCount=" + photoCount +
                ", published=" + published +
                '}';
    }
}

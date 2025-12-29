package com.community.cms.web.mvc.dto.content;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Data Transfer Object для фото (MediaFile) с информацией о галерее.
 * Используется для передачи данных о фото на frontend.
 *
 * Data Transfer Object for photo (MediaFile) with gallery information.
 * Used for transferring photo data to frontend.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhotoDTO {

    private Long id;
    private String fileName;
    private String webPath;
    private String thumbnailPath;
    private String title;
    private Long galleryId;
    private String galleryTitle;
    private Integer galleryYear;
    private Boolean isPrimary;

    /**
     * Конструктор по умолчанию.
     * Default constructor.
     */
    public PhotoDTO() {
    }

    /**
     * Конструктор с основными параметрами.
     * Constructor with main parameters.
     *
     * @param id идентификатор фото / photo ID
     * @param fileName имя файла / file name
     * @param webPath веб-путь к фото / web path to photo
     * @param galleryId ID галереи / gallery ID
     * @param galleryTitle название галереи / gallery title
     */
    public PhotoDTO(Long id, String fileName, String webPath,
                    Long galleryId, String galleryTitle) {
        this.id = id;
        this.fileName = fileName;
        this.webPath = webPath;
        this.galleryId = galleryId;
        this.galleryTitle = galleryTitle;
    }

    /**
     * Конструктор с полным набором параметров.
     * Constructor with full set of parameters.
     *
     * @param id идентификатор фото / photo ID
     * @param fileName имя файла / file name
     * @param webPath веб-путь к фото / web path to photo
     * @param thumbnailPath путь к миниатюре / thumbnail path
     * @param title заголовок фото / photo title
     * @param galleryId ID галереи / gallery ID
     * @param galleryTitle название галереи / gallery title
     * @param galleryYear год галереи / gallery year
     * @param isPrimary является ли основным фото / is primary photo
     */
    public PhotoDTO(Long id, String fileName, String webPath, String thumbnailPath,
                    String title, Long galleryId, String galleryTitle,
                    Integer galleryYear, Boolean isPrimary) {
        this.id = id;
        this.fileName = fileName;
        this.webPath = webPath;
        this.thumbnailPath = thumbnailPath;
        this.title = title;
        this.galleryId = galleryId;
        this.galleryTitle = galleryTitle;
        this.galleryYear = galleryYear;
        this.isPrimary = isPrimary;
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

    public String getWebPath() {
        return webPath;
    }

    public void setWebPath(String webPath) {
        this.webPath = webPath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(Long galleryId) {
        this.galleryId = galleryId;
    }

    public String getGalleryTitle() {
        return galleryTitle;
    }

    public void setGalleryTitle(String galleryTitle) {
        this.galleryTitle = galleryTitle;
    }

    public Integer getGalleryYear() {
        return galleryYear;
    }

    public void setGalleryYear(Integer galleryYear) {
        this.galleryYear = galleryYear;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    /**
     * Получает отображаемое название фото.
     * Gets display name for photo.
     *
     * @return название для отображения / display name
     */
    public String getDisplayName() {
        if (title != null && !title.trim().isEmpty()) {
            return title;
        }
        return fileName;
    }

    /**
     * Получает полное название с информацией о галерее.
     * Gets full name with gallery information.
     *
     * @return полное название / full name
     */
    public String getFullName() {
        return String.format("%s (%s)", getDisplayName(), galleryTitle);
    }

    @Override
    public String toString() {
        return "PhotoDTO{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", galleryId=" + galleryId +
                ", galleryTitle='" + galleryTitle + '\'' +
                '}';
    }
}

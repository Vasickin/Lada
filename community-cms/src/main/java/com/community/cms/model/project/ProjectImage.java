package com.community.cms.model.project;

import com.community.cms.model.gallery.MediaFile;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Промежуточная сущность для связи проекта с медиафайлами.
 *
 * <p>Связывает проекты с изображениями через существующую систему MediaFile.
 * Позволяет добавлять изображения в галерею проекта с дополнительными
 * метаданными и категоризацией.</p>
 *
 * <p>Основные характеристики:
 * <ul>
 *   <li>Связь Project ↔ MediaFile через промежуточную таблицу</li>
 *   <li>Поддержка категорий изображений внутри проекта</li>
 *   <li>Флаг избранного изображения (isFeatured)</li>
 *   <li>Порядок сортировки в галерее проекта</li>
 *   <li>Подпись/описание для каждого изображения</li>
 * </ul>
 *
 * <p>Архитектура:
 * Project → ProjectImage → MediaFile
 * (Связь)   (Метаданные)   (Файл)
 * </p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "project_images")
public class ProjectImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================== СВЯЗИ С ДРУГИМИ СУЩНОСТЯМИ ==================

    /**
     * Проект, к которому принадлежит изображение.
     * Связь многие-к-одному: один проект может иметь много изображений.
     */
    @NotNull(message = "Проект обязателен / Project is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Ссылка на медиафайл в существующей системе.
     * Используем существующую сущность MediaFile без изменений.
     */
    @NotNull(message = "Медиафайл обязателен / Media file is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile mediaFile;

    // ================== МЕТАДАННЫЕ ИЗОБРАЖЕНИЯ ==================

    /**
     * Подпись/описание изображения.
     * Отображается под изображением в галерее.
     */
    @Column(length = 500)
    private String caption;

    /**
     * Альтернативный текст для доступности (alt text).
     * Важен для SEO и пользователей с ограниченными возможностями.
     */
    @Column(name = "alt_text", length = 255)
    private String altText;

    /**
     * Категория изображения внутри проекта.
     * Примеры: "Главная", "Участники", "Мероприятия", "Результаты".
     */
    @Column(length = 100)
    private String category;

    /**
     * Флаг избранного изображения.
     * Избранные изображения могут отображаться в превью или слайдерах.
     */
    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured = false;

    /**
     * Порядок сортировки в галерее проекта.
     * Меньшее значение = выше в списке.
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // ================== СИСТЕМНЫЕ ПОЛЯ ==================

    /**
     * Дата и время добавления изображения в проект.
     */
    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    /**
     * Конструктор по умолчанию.
     */
    public ProjectImage() {
        this.addedAt = LocalDateTime.now();
    }

    /**
     * Конструктор с основными параметрами.
     *
     * @param project проект
     * @param mediaFile медиафайл
     */
    public ProjectImage(Project project, MediaFile mediaFile) {
        this();
        this.project = project;
        this.mediaFile = mediaFile;
    }

    /**
     * Конструктор с полным набором параметров.
     *
     * @param project проект
     * @param mediaFile медиафайл
     * @param caption подпись
     * @param altText альтернативный текст
     * @param category категория
     * @param isFeatured флаг избранного
     * @param sortOrder порядок сортировки
     */
    public ProjectImage(Project project, MediaFile mediaFile, String caption,
                        String altText, String category, boolean isFeatured, Integer sortOrder) {
        this();
        this.project = project;
        this.mediaFile = mediaFile;
        this.caption = caption;
        this.altText = altText;
        this.category = category;
        this.isFeatured = isFeatured;
        this.sortOrder = sortOrder;
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Получает путь к файлу изображения.
     * Делегирует вызов к связанному MediaFile.
     *
     * @return путь к файлу изображения
     */
    public String getImagePath() {
        return mediaFile != null ? mediaFile.getFilePath() : null;
    }

    /**
     * Получает имя файла изображения.
     * Делегирует вызов к связанному MediaFile.
     *
     * @return имя файла изображения
     */
    public String getFileName() {
        return mediaFile != null ? mediaFile.getFileName() : null;
    }

    /**
     * Получает MIME тип изображения.
     * Делегирует вызов к связанному MediaFile.
     *
     * @return MIME тип изображения
     */
    public String getMimeType() {
        return mediaFile != null ? mediaFile.getMimeType() : null;
    }

    /**
     * Получает размер файла изображения.
     * Делегирует вызов к связанному MediaFile.
     *
     * @return размер файла в байтах
     */
    public Long getFileSize() {
        return mediaFile != null ? mediaFile.getFileSize() : null;
    }

    /**
     * Получает отформатированный размер файла.
     * Делегирует вызов к связанному MediaFile.
     *
     * @return отформатированный размер (например, "1.5 MB")
     */
    public String getFormattedFileSize() {
        return mediaFile != null ? mediaFile.getFormattedFileSize() : "0 B";
    }

    /**
     * Проверяет является ли файл изображением.
     * Делегирует вызов к связанному MediaFile.
     *
     * @return true если это изображение, иначе false
     */
    public boolean isImage() {
        return mediaFile != null && mediaFile.isImage();
    }

    /**
     * Получает веб-путь к изображению.
     * Используется для отображения в HTML.
     *
     * @return веб-путь к изображению
     */
    public String getWebPath() {
        return mediaFile != null ? mediaFile.getWebPath() : "";
    }

    /**
     * Получает альтернативный текст для изображения.
     * Если altText не задан, возвращает caption или имя файла.
     *
     * @return альтернативный текст для использования
     */
    public String getEffectiveAltText() {
        if (altText != null && !altText.trim().isEmpty()) {
            return altText;
        }
        if (caption != null && !caption.trim().isEmpty()) {
            return caption;
        }
        return getFileName() != null ? getFileName() : "Изображение проекта";
    }

    /**
     * Проверяет имеет ли изображение подпись.
     *
     * @return true если caption не пустой, иначе false
     */
    public boolean hasCaption() {
        return caption != null && !caption.trim().isEmpty();
    }

    /**
     * Проверяет имеет ли изображение категорию.
     *
     * @return true если category не пустая, иначе false
     */
    public boolean hasCategory() {
        return category != null && !category.trim().isEmpty();
    }

    /**
     * Получает ID проекта для быстрого доступа.
     *
     * @return ID проекта или null если проект не установлен
     */
    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    /**
     * Получает ID медиафайла для быстрого доступа.
     *
     * @return ID медиафайла или null если медиафайл не установлен
     */
    public Long getMediaFileId() {
        return mediaFile != null ? mediaFile.getId() : null;
    }

    @Override
    public String toString() {
        return "ProjectImage{" +
                "id=" + id +
                ", projectId=" + getProjectId() +
                ", mediaFileId=" + getMediaFileId() +
                ", fileName='" + getFileName() + '\'' +
                ", isFeatured=" + isFeatured +
                '}';
    }

    /**
     * Метод предварительной обработки перед сохранением.
     */
    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}

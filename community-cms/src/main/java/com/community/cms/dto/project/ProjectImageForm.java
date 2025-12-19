package com.community.cms.dto.project;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) для формы создания и редактирования изображений проектов.
 *
 * <p>Используется в административной панели для управления изображениями,
 * связанными с проектами. Позволяет добавлять метаданные к изображениям
 * и управлять их отображением в галерее проекта.</p>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Валидация входных данных</li>
 *   <li>Связывание медиафайлов с проектами</li>
 *   <li>Управление категориями и избранными изображениями</li>
 *   <li>Настройка подписей и альтернативного текста</li>
 *   <li>Управление порядком сортировки в галерее</li>
 * </ul>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see com.community.cms.model.project.ProjectImage
 */
public class ProjectImageForm {

    // ================== СИСТЕМНЫЕ ПОЛЯ ==================

    /**
     * ID связи изображения с проектом.
     * Используется только при редактировании существующей связи.
     */
    private Long id;

    // ================== СВЯЗИ С ДРУГИМИ СУЩНОСТЯМИ ==================

    /**
     * ID проекта, к которому относится изображение.
     * Обязательное поле.
     */
    @NotNull(message = "ID проекта обязателен / Project ID is required")
    private Long projectId;

    /**
     * ID медиафайла из существующей системы галереи.
     * Обязательное поле. Ссылается на существующий MediaFile.
     */
    @NotNull(message = "ID медиафайла обязателен / Media file ID is required")
    private Long mediaFileId;

    // ================== МЕТАДАННЫЕ ИЗОБРАЖЕНИЯ ==================

    /**
     * Подпись/описание изображения.
     * Максимум 500 символов. Отображается под изображением в галерее.
     */
    @Size(max = 500, message = "Подпись не должна превышать 500 символов / Caption must not exceed 500 characters")
    private String caption;

    /**
     * Альтернативный текст для доступности (alt text).
     * Максимум 255 символов. Важен для SEO и пользователей с ограниченными возможностями.
     */
    @Size(max = 255, message = "Альтернативный текст не должен превышать 255 символов / Alt text must not exceed 255 characters")
    private String altText;

    /**
     * Категория изображения внутри проекта.
     * Максимум 100 символов.
     * Примеры: "Главная", "Участники", "Мероприятия", "Результаты".
     */
    @Size(max = 100, message = "Категория не должна превышать 100 символов / Category must not exceed 100 characters")
    private String category;

    // ================== ФЛАГИ И НАСТРОЙКИ ==================

    /**
     * Флаг избранного изображения.
     * Избранные изображения могут отображаться в превью, слайдерах или как главные.
     */
    private boolean isFeatured = false;

    /**
     * Порядок сортировки в галерее проекта.
     * Меньшее значение = выше в списке.
     * По умолчанию: 0.
     */
    private Integer sortOrder = 0;

    // ================== КОНСТРУКТОРЫ ==================

    /**
     * Конструктор по умолчанию.
     * Инициализирует значения по умолчанию.
     */
    public ProjectImageForm() {
        this.isFeatured = false;
        this.sortOrder = 0;
    }

    /**
     * Конструктор с минимальным набором параметров.
     *
     * @param projectId ID проекта
     * @param mediaFileId ID медиафайла
     */
    public ProjectImageForm(Long projectId, Long mediaFileId) {
        this();
        this.projectId = projectId;
        this.mediaFileId = mediaFileId;
    }

    /**
     * Конструктор на основе существующей связи ProjectImage.
     * Используется для редактирования существующего изображения проекта.
     *
     * @param projectImage существующая связь изображения с проектом
     */
    public ProjectImageForm(com.community.cms.model.project.ProjectImage projectImage) {
        this();
        this.id = projectImage.getId();
        this.projectId = projectImage.getProject() != null ? projectImage.getProject().getId() : null;
        this.mediaFileId = projectImage.getMediaFile() != null ? projectImage.getMediaFile().getId() : null;
        this.caption = projectImage.getCaption();
        this.altText = projectImage.getAltText();
        this.category = projectImage.getCategory();
        this.isFeatured = projectImage.isFeatured();
        this.sortOrder = projectImage.getSortOrder();
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getMediaFileId() {
        return mediaFileId;
    }

    public void setMediaFileId(Long mediaFileId) {
        this.mediaFileId = mediaFileId;
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

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет, имеет ли изображение подпись.
     *
     * @return true если caption не пустой, иначе false
     */
    public boolean hasCaption() {
        return caption != null && !caption.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли изображение альтернативный текст.
     *
     * @return true если altText не пустой, иначе false
     */
    public boolean hasAltText() {
        return altText != null && !altText.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли изображение категорию.
     *
     * @return true если category не пустая, иначе false
     */
    public boolean hasCategory() {
        return category != null && !category.trim().isEmpty();
    }

    /**
     * Проверяет, является ли изображение избранным.
     *
     * @return true если isFeatured = true, иначе false
     */
    public boolean isImageFeatured() {
        return isFeatured;
    }

    /**
     * Получает эффективный альтернативный текст для изображения.
     * Если altText не задан, возвращает caption или имя файла.
     *
     * @param fileName имя файла изображения (опционально)
     * @return альтернативный текст для использования
     */
    public String getEffectiveAltText(String fileName) {
        if (hasAltText()) {
            return altText;
        }
        if (hasCaption()) {
            return caption;
        }
        return fileName != null ? fileName : "Изображение проекта";
    }

    /**
     * Получает отображаемую категорию.
     * Если категория не указана, возвращает "Без категории".
     *
     * @return отображаемое название категории
     */
    public String getDisplayCategory() {
        return hasCategory() ? category : "Без категории";
    }

    /**
     * Проверяет, является ли изображение новым (еще не сохранено в БД).
     *
     * @return true если id == null, иначе false
     */
    public boolean isNew() {
        return id == null;
    }

    /**
     * Проверяет, указаны ли все обязательные поля для сохранения.
     *
     * @return true если projectId и mediaFileId не null, иначе false
     */
    public boolean isValidForSave() {
        return projectId != null && mediaFileId != null;
    }

    /**
     * Проверяет, является ли изображение главным в проекте.
     * В текущей реализации главным считается первое избранное изображение.
     *
     * @return true если isFeatured = true и sortOrder = 0 (или минимальный), иначе false
     */
    public boolean isMainImage() {
        return isFeatured && (sortOrder == null || sortOrder <= 1);
    }

    /**
     * Получает рекомендуемую категорию на основе имени файла.
     * Используется при массовой загрузке изображений.
     *
     * @param fileName имя файла
     * @return предполагаемая категория или null
     */
    public static String suggestCategoryFromFileName(String fileName) {
        if (fileName == null) {
            return null;
        }

        fileName = fileName.toLowerCase();

        if (fileName.contains("main") || fileName.contains("cover") || fileName.contains("featured")) {
            return "Главная";
        } else if (fileName.contains("team") || fileName.contains("member") || fileName.contains("participant")) {
            return "Участники";
        } else if (fileName.contains("event") || fileName.contains("action") || fileName.contains("activity")) {
            return "Мероприятия";
        } else if (fileName.contains("result") || fileName.contains("achievement") || fileName.contains("success")) {
            return "Результаты";
        } else if (fileName.contains("logo") || fileName.contains("partner") || fileName.contains("sponsor")) {
            return "Партнеры";
        }

        return null;
    }

    /**
     * Создает заголовок из имени файла, если подпись не указана.
     * Удаляет расширение файла и заменяет подчеркивания пробелами.
     *
     * @param fileName имя файла
     * @return сгенерированный заголовок
     */
    public static String generateCaptionFromFileName(String fileName) {
        if (fileName == null) {
            return null;
        }

        // Удаляем расширение файла
        String nameWithoutExt = fileName.replaceAll("\\.[^.]+$", "");

        // Заменяем подчеркивания и дефисы пробелами
        String caption = nameWithoutExt.replaceAll("[_-]", " ");

        // Делаем первую букву заглавной
        if (!caption.isEmpty()) {
            caption = Character.toUpperCase(caption.charAt(0)) + caption.substring(1);
        }

        return caption;
    }

    /**
     * Преобразует ProjectImageForm в сущность ProjectImage.
     * Не заполняет связи с Project и MediaFile (нужно установить отдельно).
     *
     * @return сущность ProjectImage с заполненными базовыми полями
     */
    public com.community.cms.model.project.ProjectImage toEntity() {
        com.community.cms.model.project.ProjectImage projectImage = new com.community.cms.model.project.ProjectImage();
        projectImage.setId(this.id);
        projectImage.setCaption(this.caption);
        projectImage.setAltText(this.altText);
        projectImage.setCategory(this.category);
        projectImage.setFeatured(this.isFeatured);
        projectImage.setSortOrder(this.sortOrder != null ? this.sortOrder : 0);

        // Связи устанавливаются отдельно
        // projectImage.setProject(project);
        // projectImage.setMediaFile(mediaFile);

        return projectImage;
    }

    /**
     * Обновляет существующую сущность ProjectImage данными из формы.
     * Не обновляет связи с Project и MediaFile.
     *
     * @param projectImage сущность для обновления
     */
    public void updateEntity(com.community.cms.model.project.ProjectImage projectImage) {
        projectImage.setCaption(this.caption);
        projectImage.setAltText(this.altText);
        projectImage.setCategory(this.category);
        projectImage.setFeatured(this.isFeatured);
        projectImage.setSortOrder(this.sortOrder != null ? this.sortOrder : 0);
    }

    /**
     * Создает копию формы с другим projectId.
     * Используется для массового присвоения изображений другому проекту.
     *
     * @param newProjectId новый ID проекта
     * @return копия формы с измененным projectId
     */
    public ProjectImageForm copyWithNewProject(Long newProjectId) {
        ProjectImageForm copy = new ProjectImageForm();
        copy.setProjectId(newProjectId);
        copy.setMediaFileId(this.mediaFileId);
        copy.setCaption(this.caption);
        copy.setAltText(this.altText);
        copy.setCategory(this.category);
        copy.setFeatured(this.isFeatured);
        copy.setSortOrder(this.sortOrder);
        return copy;
    }

    @Override
    public String toString() {
        return "ProjectImageForm{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", mediaFileId=" + mediaFileId +
                ", category='" + category + '\'' +
                ", isFeatured=" + isFeatured +
                ", sortOrder=" + sortOrder +
                '}';
    }
}

package com.community.cms.dto.project;

import com.community.cms.model.project.ProjectCategory;
import com.community.cms.model.project.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


//Описание файла:
//DTO для запроса обновления проекта
//Все поля опциональны (обновляются только указанные)
//Аннотация @JsonInclude для исключения null полей из JSON
//Валидация только для полей, которые присутствуют в запросе
//Флаги для управления обложкой (removeCoverImage) и галереей (createGallery)
//Boolean поля для секций (позволяют явно установить true/false или не трогать поле)
//Вспомогательные методы для проверки обновления конкретных групп полей
//Метод для подсчета количества полей, которые будут обновлены
//Проверка на пустой запрос обновления
//Полная JavaDoc документация

/**
 * Data Transfer Object для запроса обновления проекта.
 * Используется для валидации и передачи данных при обновлении существующего проекта.
 * Все поля опциональны - обновляются только указанные поля.
 *
 * @author Vasickin
 * @since 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectUpdateRequest {

    @Size(min = 3, max = 200, message = "Название проекта должно содержать от 3 до 200 символов")
    private String title;

    @Size(min = 3, max = 200, message = "URL-идентификатор должен содержать от 3 до 200 символов")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "URL-идентификатор может содержать только латинские буквы в нижнем регистре, цифры и дефисы")
    private String slug;

    private ProjectCategory category;

    private ProjectStatus status;

    @Size(max = 500, message = "Краткое описание не должно превышать 500 символов")
    private String shortDescription;

    private String fullDescription;

    private String goals;

    @Size(max = 200, message = "Место проведения не должно превышать 200 символов")
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Size(max = 500, message = "Контакты куратора не должны превышать 500 символов")
    private String curatorContacts;

    private String participationInfo;

    // Настройки секций
    private Boolean showDescription;
    private Boolean showPhotos;
    private Boolean showVideos;
    private Boolean showTeam;
    private Boolean showParticipation;
    private Boolean showPartners;
    private Boolean showRelated;

    // Порядок секций (JSON строка или массив)
    private String sectionsOrder;

    // SEO настройки
    @Size(max = 200, message = "SEO заголовок не должен превышать 200 символов")
    private String metaTitle;

    @Size(max = 500, message = "SEO описание не должно превышать 500 символов")
    private String metaDescription;

    @Size(max = 500, message = "Ключевые слова не должны превышать 500 символов")
    private String metaKeywords;

    // Флаги для управления обложкой
    private Boolean removeCoverImage;
    private Boolean createGallery;

    // === КОНСТРУКТОРЫ ===

    /**
     * Конструктор по умолчанию
     */
    public ProjectUpdateRequest() {
    }

    // === ГЕТТЕРЫ И СЕТТЕРЫ ===

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public ProjectCategory getCategory() {
        return category;
    }

    public void setCategory(ProjectCategory category) {
        this.category = category;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getGoals() {
        return goals;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getCuratorContacts() {
        return curatorContacts;
    }

    public void setCuratorContacts(String curatorContacts) {
        this.curatorContacts = curatorContacts;
    }

    public String getParticipationInfo() {
        return participationInfo;
    }

    public void setParticipationInfo(String participationInfo) {
        this.participationInfo = participationInfo;
    }

    public Boolean getShowDescription() {
        return showDescription;
    }

    public void setShowDescription(Boolean showDescription) {
        this.showDescription = showDescription;
    }

    public Boolean getShowPhotos() {
        return showPhotos;
    }

    public void setShowPhotos(Boolean showPhotos) {
        this.showPhotos = showPhotos;
    }

    public Boolean getShowVideos() {
        return showVideos;
    }

    public void setShowVideos(Boolean showVideos) {
        this.showVideos = showVideos;
    }

    public Boolean getShowTeam() {
        return showTeam;
    }

    public void setShowTeam(Boolean showTeam) {
        this.showTeam = showTeam;
    }

    public Boolean getShowParticipation() {
        return showParticipation;
    }

    public void setShowParticipation(Boolean showParticipation) {
        this.showParticipation = showParticipation;
    }

    public Boolean getShowPartners() {
        return showPartners;
    }

    public void setShowPartners(Boolean showPartners) {
        this.showPartners = showPartners;
    }

    public Boolean getShowRelated() {
        return showRelated;
    }

    public void setShowRelated(Boolean showRelated) {
        this.showRelated = showRelated;
    }

    public String getSectionsOrder() {
        return sectionsOrder;
    }

    public void setSectionsOrder(String sectionsOrder) {
        this.sectionsOrder = sectionsOrder;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public Boolean isRemoveCoverImage() {
        return removeCoverImage;
    }

    public void setRemoveCoverImage(Boolean removeCoverImage) {
        this.removeCoverImage = removeCoverImage;
    }

    public Boolean isCreateGallery() {
        return createGallery;
    }

    public void setCreateGallery(Boolean createGallery) {
        this.createGallery = createGallery;
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    /**
     * Проверить, обновляется ли название проекта
     *
     * @return true, если поле title не null
     */
    public boolean hasTitleUpdate() {
        return title != null;
    }

    /**
     * Проверить, обновляется ли URL-идентификатор
     *
     * @return true, если поле slug не null
     */
    public boolean hasSlugUpdate() {
        return slug != null;
    }

    /**
     * Проверить, обновляется ли категория
     *
     * @return true, если поле category не null
     */
    public boolean hasCategoryUpdate() {
        return category != null;
    }

    /**
     * Проверить, обновляется ли статус
     *
     * @return true, если поле status не null
     */
    public boolean hasStatusUpdate() {
        return status != null;
    }

    /**
     * Проверить, обновляется ли описание проекта
     *
     * @return true, если хотя бы одно поле описания не null
     */
    public boolean hasDescriptionUpdate() {
        return shortDescription != null || fullDescription != null || goals != null;
    }

    /**
     * Проверить, обновляются ли даты проведения
     *
     * @return true, если хотя бы одно поле даты не null
     */
    public boolean hasDatesUpdate() {
        return startDate != null || endDate != null;
    }

    /**
     * Проверить корректность указанных дат (если обе указаны)
     *
     * @return true, если даты корректны (начало не позже окончания) или не указаны обе
     */
    public boolean areDatesValid() {
        if (startDate == null || endDate == null) {
            return true; // Если одна из дат не указана, считаем валидным
        }
        return !startDate.isAfter(endDate);
    }

    /**
     * Проверить, обновляется ли информация об участии
     *
     * @return true, если поле participationInfo не null
     */
    public boolean hasParticipationInfoUpdate() {
        return participationInfo != null;
    }

    /**
     * Проверить, обновляются ли настройки секций
     *
     * @return true, если хотя бы одно поле настройки секций не null
     */
    public boolean hasSectionsUpdate() {
        return showDescription != null || showPhotos != null || showVideos != null ||
                showTeam != null || showParticipation != null || showPartners != null ||
                showRelated != null || sectionsOrder != null;
    }

    /**
     * Проверить, обновляются ли SEO настройки
     *
     * @return true, если хотя бы одно SEO поле не null
     */
    public boolean hasSeoUpdate() {
        return metaTitle != null || metaDescription != null || metaKeywords != null;
    }

    /**
     * Проверить, требуется ли удаление обложки
     *
     * @return true, если флаг removeCoverImage установлен в true
     */
    public boolean shouldRemoveCoverImage() {
        return Boolean.TRUE.equals(removeCoverImage);
    }

    /**
     * Проверить, требуется ли создание галереи
     *
     * @return true, если флаг createGallery установлен в true
     */
    public boolean shouldCreateGallery() {
        return Boolean.TRUE.equals(createGallery);
    }

    /**
     * Получить количество полей, которые будут обновлены
     *
     * @return Количество не-null полей в запросе
     */
    public int getUpdateFieldCount() {
        int count = 0;
        if (title != null) count++;
        if (slug != null) count++;
        if (category != null) count++;
        if (status != null) count++;
        if (shortDescription != null) count++;
        if (fullDescription != null) count++;
        if (goals != null) count++;
        if (location != null) count++;
        if (startDate != null) count++;
        if (endDate != null) count++;
        if (curatorContacts != null) count++;
        if (participationInfo != null) count++;
        if (showDescription != null) count++;
        if (showPhotos != null) count++;
        if (showVideos != null) count++;
        if (showTeam != null) count++;
        if (showParticipation != null) count++;
        if (showPartners != null) count++;
        if (showRelated != null) count++;
        if (sectionsOrder != null) count++;
        if (metaTitle != null) count++;
        if (metaDescription != null) count++;
        if (metaKeywords != null) count++;
        if (removeCoverImage != null) count++;
        if (createGallery != null) count++;

        return count;
    }

    /**
     * Проверить, является ли запрос пустым (не содержит полей для обновления)
     *
     * @return true, если все поля null
     */
    public boolean isEmpty() {
        return getUpdateFieldCount() == 0;
    }

    @Override
    public String toString() {
        return "ProjectUpdateRequest{" +
                "title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", category=" + category +
                ", status=" + status +
                ", updateFieldCount=" + getUpdateFieldCount() +
                '}';
    }
}
package com.community.cms.dto.project;

import com.community.cms.model.project.ProjectCategory;
import com.community.cms.model.project.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

//Описание файла:
//DTO для запроса создания проекта с аннотациями валидации
//Обязательные поля: title, slug, category
//Опциональные поля с разумными значениями по умолчанию
//Валидация slug с использованием регулярного выражения
//Аннотации @JsonFormat для корректной десериализации дат
//Флаги для управления отображением секций
//SEO поля для оптимизации
//Флаг для автоматического создания галереи
//Вспомогательные методы для проверки дат, SEO настроек и секций

/**
 * Data Transfer Object для запроса создания проекта.
 * Используется для валидации и передачи данных при создании нового проекта.
 *
 * @author Vasickin
 * @since 1.0
 */
public class ProjectCreateRequest {

    @NotBlank(message = "Название проекта обязательно")
    @Size(min = 3, max = 200, message = "Название проекта должно содержать от 3 до 200 символов")
    private String title;

    @NotBlank(message = "URL-идентификатор обязателен")
    @Size(min = 3, max = 200, message = "URL-идентификатор должен содержать от 3 до 200 символов")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "URL-идентификатор может содержать только латинские буквы в нижнем регистре, цифры и дефисы")
    private String slug;

    @NotNull(message = "Категория проекта обязательна")
    private ProjectCategory category;

    private ProjectStatus status = ProjectStatus.ACTIVE;

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
    private boolean showDescription = true;
    private boolean showPhotos = true;
    private boolean showVideos = true;
    private boolean showTeam = true;
    private boolean showParticipation = true;
    private boolean showPartners = true;
    private boolean showRelated = true;

    // Порядок секций (JSON строка или массив)
    private String sectionsOrder = "[\"description\",\"photos\",\"videos\",\"team\",\"participation\",\"partners\",\"related\"]";

    // SEO настройки
    @Size(max = 200, message = "SEO заголовок не должен превышать 200 символов")
    private String metaTitle;

    @Size(max = 500, message = "SEO описание не должно превышать 500 символов")
    private String metaDescription;

    @Size(max = 500, message = "Ключевые слова не должны превышать 500 символов")
    private String metaKeywords;

    // Флаг для создания галереи
    private boolean createGallery = true;

    // === КОНСТРУКТОРЫ ===

    /**
     * Конструктор по умолчанию
     */
    public ProjectCreateRequest() {
    }

    /**
     * Конструктор с основными полями
     */
    public ProjectCreateRequest(String title, String slug, ProjectCategory category) {
        this.title = title;
        this.slug = slug;
        this.category = category;
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

    public boolean isShowDescription() {
        return showDescription;
    }

    public void setShowDescription(boolean showDescription) {
        this.showDescription = showDescription;
    }

    public boolean isShowPhotos() {
        return showPhotos;
    }

    public void setShowPhotos(boolean showPhotos) {
        this.showPhotos = showPhotos;
    }

    public boolean isShowVideos() {
        return showVideos;
    }

    public void setShowVideos(boolean showVideos) {
        this.showVideos = showVideos;
    }

    public boolean isShowTeam() {
        return showTeam;
    }

    public void setShowTeam(boolean showTeam) {
        this.showTeam = showTeam;
    }

    public boolean isShowParticipation() {
        return showParticipation;
    }

    public void setShowParticipation(boolean showParticipation) {
        this.showParticipation = showParticipation;
    }

    public boolean isShowPartners() {
        return showPartners;
    }

    public void setShowPartners(boolean showPartners) {
        this.showPartners = showPartners;
    }

    public boolean isShowRelated() {
        return showRelated;
    }

    public void setShowRelated(boolean showRelated) {
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

    public boolean isCreateGallery() {
        return createGallery;
    }

    public void setCreateGallery(boolean createGallery) {
        this.createGallery = createGallery;
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    /**
     * Проверить, указаны ли даты проведения проекта
     *
     * @return true, если обе даты указаны
     */
    public boolean hasDates() {
        return startDate != null && endDate != null;
    }

    /**
     * Проверить корректность указанных дат
     *
     * @return true, если даты корректны (начало раньше окончания)
     */
    public boolean areDatesValid() {
        if (!hasDates()) {
            return true; // Если даты не указаны, считаем валидным
        }
        return !startDate.isAfter(endDate);
    }

    /**
     * Проверить, является ли проект многодневным
     *
     * @return true, если проект длится более одного дня
     */
    public boolean isMultiDay() {
        if (!hasDates()) {
            return false;
        }
        return !startDate.equals(endDate);
    }

    /**
     * Получить продолжительность проекта в днях
     *
     * @return Продолжительность в днях или 0, если даты не указаны или некорректны
     */
    public int getDurationDays() {
        if (!hasDates() || !areDatesValid()) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Проверить, установлены ли SEO настройки
     *
     * @return true, если установлены хотя бы одно SEO поле
     */
    public boolean hasSeoSettings() {
        return (metaTitle != null && !metaTitle.isEmpty()) ||
                (metaDescription != null && !metaDescription.isEmpty()) ||
                (metaKeywords != null && !metaKeywords.isEmpty());
    }

    /**
     * Проверить, включены ли все секции
     *
     * @return true, если все секции включены
     */
    public boolean areAllSectionsEnabled() {
        return showDescription && showPhotos && showVideos && showTeam &&
                showParticipation && showPartners && showRelated;
    }

    /**
     * Проверить, отключены ли все секции
     *
     * @return true, если все секции отключены
     */
    public boolean areAllSectionsDisabled() {
        return !showDescription && !showPhotos && !showVideos && !showTeam &&
                !showParticipation && !showPartners && !showRelated;
    }

    /**
     * Получить список включенных секций
     *
     * @return Массив названий включенных секций
     */
    public String[] getEnabledSections() {
        java.util.List<String> enabled = new java.util.ArrayList<>();
        if (showDescription) enabled.add("description");
        if (showPhotos) enabled.add("photos");
        if (showVideos) enabled.add("videos");
        if (showTeam) enabled.add("team");
        if (showParticipation) enabled.add("participation");
        if (showPartners) enabled.add("partners");
        if (showRelated) enabled.add("related");
        return enabled.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return "ProjectCreateRequest{" +
                "title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", category=" + category +
                ", status=" + status +
                '}';
    }
}


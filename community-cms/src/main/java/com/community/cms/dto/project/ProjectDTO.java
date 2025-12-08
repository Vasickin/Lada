package com.community.cms.dto.project;

import com.community.cms.model.project.ProjectCategory;
import com.community.cms.model.project.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

//Описание файла:
//Основной DTO для передачи данных о проекте
//Аннотации Jackson для JSON сериализации
//Поля для основной информации о проекте
//Настройки отображения секций
//SEO поля
//Статистические поля (количество фото, видео и т.д.)
//Связанные данные (видео, партнеры, команда, похожие проекты)
//Вспомогательные методы с аннотациями @JsonProperty
//Вложенный DTO для членов команды в контексте проекта
//Методы для проверки наличия различных типов контента
//Форматирование дат

/**
 * Data Transfer Object для отображения информации о проекте.
 * Используется для передачи данных между слоями приложения и клиенту.
 *
 * @author Vasickin
 * @since 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDTO {

    private Long id;
    private String title;
    private String slug;
    private ProjectCategory category;
    private ProjectStatus status;
    private String shortDescription;
    private String fullDescription;
    private String goals;
    private String location;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;

    private String coverImageUrl;
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

    private List<String> sectionsOrder;

    // SEO информация
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    // Статистика
    private Integer photoCount;
    private Integer videoCount;
    private Integer partnerCount;
    private Integer teamMemberCount;

    // Связанные данные
    private List<ProjectVideoDTO> videos;
    private List<ProjectPartnerDTO> partners;
    private List<TeamMemberProjectDTO> teamMembers;
    private List<ProjectDTO> relatedProjects;

    // Технические поля
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    private LocalDateTime updatedAt;

    // === КОНСТРУКТОРЫ ===

    /**
     * Конструктор по умолчанию
     */
    public ProjectDTO() {
    }

    /**
     * Конструктор с основными полями
     */
    public ProjectDTO(Long id, String title, String slug, ProjectCategory category, ProjectStatus status) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.category = category;
        this.status = status;
    }

    // === ГЕТТЕРЫ И СЕТТЕРЫ ===

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

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
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

    public List<String> getSectionsOrder() {
        return sectionsOrder;
    }

    public void setSectionsOrder(List<String> sectionsOrder) {
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

    public Integer getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }

    public Integer getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(Integer videoCount) {
        this.videoCount = videoCount;
    }

    public Integer getPartnerCount() {
        return partnerCount;
    }

    public void setPartnerCount(Integer partnerCount) {
        this.partnerCount = partnerCount;
    }

    public Integer getTeamMemberCount() {
        return teamMemberCount;
    }

    public void setTeamMemberCount(Integer teamMemberCount) {
        this.teamMemberCount = teamMemberCount;
    }

    public List<ProjectVideoDTO> getVideos() {
        return videos;
    }

    public void setVideos(List<ProjectVideoDTO> videos) {
        this.videos = videos;
    }

    public List<ProjectPartnerDTO> getPartners() {
        return partners;
    }

    public void setPartners(List<ProjectPartnerDTO> partners) {
        this.partners = partners;
    }

    public List<TeamMemberProjectDTO> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<TeamMemberProjectDTO> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public List<ProjectDTO> getRelatedProjects() {
        return relatedProjects;
    }

    public void setRelatedProjects(List<ProjectDTO> relatedProjects) {
        this.relatedProjects = relatedProjects;
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

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    /**
     * Проверить, является ли проект активным в данный момент
     *
     * @return true, если проект активен и сегодня в диапазоне дат проведения
     */
    @JsonProperty("isCurrentlyActive")
    public boolean isCurrentlyActive() {
        if (status != ProjectStatus.ACTIVE || startDate == null || endDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Проверить, является ли проект завершенным
     *
     * @return true, если дата окончания в прошлом
     */
    @JsonProperty("isCompleted")
    public boolean isCompleted() {
        if (endDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return endDate.isBefore(today);
    }

    /**
     * Проверить, является ли проект будущим
     *
     * @return true, если дата начала в будущем
     */
    @JsonProperty("isUpcoming")
    public boolean isUpcoming() {
        if (startDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return startDate.isAfter(today);
    }

    /**
     * Получить отображаемое название категории
     *
     * @return Название категории на русском
     */
    @JsonProperty("categoryDisplayName")
    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : "";
    }

    /**
     * Получить отображаемое название статуса
     *
     * @return Название статуса на русском
     */
    @JsonProperty("statusDisplayName")
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    /**
     * Получить CSS класс для статуса (для Bootstrap)
     *
     * @return CSS класс
     */
    @JsonProperty("statusBadgeClass")
    public String getStatusBadgeClass() {
        return status != null ? status.getBadgeClass() : "badge bg-secondary";
    }

    /**
     * Получить продолжительность проекта в днях
     *
     * @return Продолжительность в днях или null, если даты не указаны
     */
    @JsonProperty("durationDays")
    public Integer getDurationDays() {
        if (startDate == null || endDate == null) {
            return null;
        }

        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Получить год проекта
     *
     * @return Год начала проекта или null, если дата не указана
     */
    @JsonProperty("year")
    public Integer getYear() {
        return startDate != null ? startDate.getYear() : null;
    }

    /**
     * Проверить, имеет ли проект обложку
     *
     * @return true, если URL обложки указан
     */
    @JsonProperty("hasCoverImage")
    public boolean hasCoverImage() {
        return coverImageUrl != null && !coverImageUrl.isEmpty();
    }

    /**
     * Проверить, имеет ли проект фотографии
     *
     * @return true, если есть фотографии
     */
    @JsonProperty("hasPhotos")
    public boolean hasPhotos() {
        return photoCount != null && photoCount > 0;
    }

    /**
     * Проверить, имеет ли проект видео
     *
     * @return true, если есть видео
     */
    @JsonProperty("hasVideos")
    public boolean hasVideos() {
        return videoCount != null && videoCount > 0;
    }

    /**
     * Проверить, имеет ли проект партнеров
     *
     * @return true, если есть партнеры
     */
    @JsonProperty("hasPartners")
    public boolean hasPartners() {
        return partnerCount != null && partnerCount > 0;
    }

    /**
     * Проверить, имеет ли проект команду
     *
     * @return true, если есть члены команды
     */
    @JsonProperty("hasTeam")
    public boolean hasTeam() {
        return teamMemberCount != null && teamMemberCount > 0;
    }

    @Override
    public String toString() {
        return "ProjectDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", category=" + category +
                ", status=" + status +
                '}';
    }

    /**
     * Вложенный DTO для члена команды в контексте проекта
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TeamMemberProjectDTO {
        private Long id;
        private String fullName;
        private String position;
        private String avatarUrl;
        private String projectRole;

        public TeamMemberProjectDTO() {
        }

        public TeamMemberProjectDTO(Long id, String fullName, String position, String avatarUrl, String projectRole) {
            this.id = id;
            this.fullName = fullName;
            this.position = position;
            this.avatarUrl = avatarUrl;
            this.projectRole = projectRole;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getProjectRole() {
            return projectRole;
        }

        public void setProjectRole(String projectRole) {
            this.projectRole = projectRole;
        }

        @Override
        public String toString() {
            return "TeamMemberProjectDTO{" +
                    "id=" + id +
                    ", fullName='" + fullName + '\'' +
                    ", projectRole='" + projectRole + '\'' +
                    '}';
        }
    }
}

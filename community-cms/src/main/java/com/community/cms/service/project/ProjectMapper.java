package com.community.cms.service.project;

import com.community.cms.dto.project.ProjectDTO;
import com.community.cms.model.gallery.MediaFile;
import com.community.cms.model.project.Project;
import com.community.cms.model.project.ProjectPartner;
import com.community.cms.model.project.ProjectVideo;
import com.community.cms.model.team.TeamMember;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Описание файлов:
//ProjectMapper:
//Компонент Spring для преобразования между сущностями и DTO
//Методы для базового и детализированного преобразования
//Поддержка преобразования списков
//Маппинг связанных сущностей (видео, партнеры, команда)
//Формирование URL для изображений
//ProjectVideoDTO:
//Отдельный DTO для видео проектов
//Поддержка YouTube и Vimeo
//Методы для получения ID видео, embed URL, миниатюр
//Форматирование длительности
//ProjectPartnerDTO:
//Отдельный DTO для партнеров проектов
//Поддержка различных типов партнерства
//Методы для проверки типа партнера
//Формирование CSS классов для отображения

/**
 * Маппер для преобразования между сущностью {@link Project} и DTO {@link ProjectDTO}.
 * Обеспечивает конвертацию данных между слоями приложения.
 *
 * @author Vasickin
 * @since 1.0
 */
@Component
public class ProjectMapper {

    /**
     * Преобразовать сущность Project в ProjectDTO (базовый вариант)
     *
     * @param project Сущность проекта
     * @return Базовый DTO проекта
     */
    public ProjectDTO toDTO(Project project) {
        if (project == null) {
            return null;
        }

        ProjectDTO dto = new ProjectDTO();
        mapBasicFields(project, dto);
        mapCountFields(project, dto);
        mapTechnicalFields(project, dto);

        return dto;
    }

    /**
     * Преобразовать сущность Project в детализированный ProjectDTO
     *
     * @param project Сущность проекта
     * @return Детализированный DTO проекта
     */
    public ProjectDTO toDetailedDTO(Project project) {
        if (project == null) {
            return null;
        }

        ProjectDTO dto = toDTO(project);
        mapRelatedEntities(project, dto);

        return dto;
    }

    /**
     * Преобразовать список сущностей Project в список ProjectDTO
     *
     * @param projects Список сущностей проектов
     * @return Список DTO проектов
     */
    public List<ProjectDTO> toDTOList(List<Project> projects) {
        if (projects == null) {
            return null;
        }

        List<ProjectDTO> dtos = new ArrayList<>(projects.size());
        for (Project project : projects) {
            dtos.add(toDTO(project));
        }
        return dtos;
    }

    /**
     * Преобразовать список сущностей Project в список детализированных ProjectDTO
     *
     * @param projects Список сущностей проектов
     * @return Список детализированных DTO проектов
     */
    public List<ProjectDTO> toDetailedDTOList(List<Project> projects) {
        if (projects == null) {
            return null;
        }

        List<ProjectDTO> dtos = new ArrayList<>(projects.size());
        for (Project project : projects) {
            dtos.add(toDetailedDTO(project));
        }
        return dtos;
    }

    /**
     * Маппинг базовых полей проекта
     *
     * @param project Сущность проекта
     * @param dto DTO проекта
     */
    private void mapBasicFields(Project project, ProjectDTO dto) {
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setSlug(project.getSlug());
        dto.setCategory(project.getCategory());
        dto.setStatus(project.getStatus());
        dto.setShortDescription(project.getShortDescription());
        dto.setFullDescription(project.getFullDescription());
        dto.setGoals(project.getGoals());
        dto.setLocation(project.getLocation());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setCuratorContacts(project.getCuratorContacts());
        dto.setParticipationInfo(project.getParticipationInfo());

        // Маппинг обложки
        if (project.getCoverImage() != null) {
            dto.setCoverImageUrl(getImageUrl(project.getCoverImage()));
        }

        // Маппинг настроек секций
        dto.setShowDescription(project.isShowDescription());
        dto.setShowPhotos(project.isShowPhotos());
        dto.setShowVideos(project.isShowVideos());
        dto.setShowTeam(project.isShowTeam());
        dto.setShowParticipation(project.isShowParticipation());
        dto.setShowPartners(project.isShowPartners());
        dto.setShowRelated(project.isShowRelated());

        // Маппинг порядка секций
        if (StringUtils.hasText(project.getSectionsOrder())) {
            dto.setSectionsOrder(project.getSectionsOrderList());
        }

        // Маппинг SEO полей
        dto.setMetaTitle(project.getMetaTitle());
        dto.setMetaDescription(project.getMetaDescription());
        dto.setMetaKeywords(project.getMetaKeywords());
    }

    /**
     * Маппинг счетчиков проекта
     *
     * @param project Сущность проекта
     * @param dto DTO проекта
     */
    private void mapCountFields(Project project, ProjectDTO dto) {
        dto.setPhotoCount(project.getPhotoCount());
        dto.setVideoCount(project.getVideoCount());
        dto.setPartnerCount(project.getPartnerCount());
        dto.setTeamMemberCount(project.getTeamMemberCount());
    }

    /**
     * Маппинг технических полей проекта
     *
     * @param project Сущность проекта
     * @param dto DTO проекта
     */
    private void mapTechnicalFields(Project project, ProjectDTO dto) {
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
    }

    /**
     * Маппинг связанных сущностей проекта
     *
     * @param project Сущность проекта
     * @param dto DTO проекта
     */
    private void mapRelatedEntities(Project project, ProjectDTO dto) {
        // Маппинг видео
        if (project.getVideos() != null && !project.getVideos().isEmpty()) {
            dto.setVideos(mapVideos(project.getVideos()));
        }

        // Маппинг партнеров
        if (project.getPartners() != null && !project.getPartners().isEmpty()) {
            dto.setPartners(mapPartners(project.getPartners()));
        }

        // Маппинг команды
        if (project.getTeamMembers() != null && !project.getTeamMembers().isEmpty()) {
            dto.setTeamMembers(mapTeamMembers(project.getTeamMembers()));
        }
    }

    /**
     * Маппинг списка видео проекта
     *
     * @param videos Список сущностей видео
     * @return Список DTO видео
     */
    private List<ProjectDTO.ProjectVideoDTO> mapVideos(List<ProjectVideo> videos) {
        List<ProjectDTO.ProjectVideoDTO> videoDTOs = new ArrayList<>();

        for (ProjectVideo video : videos) {
            ProjectDTO.ProjectVideoDTO videoDTO = new ProjectDTO.ProjectVideoDTO();
            videoDTO.setId(video.getId());
            videoDTO.setTitle(video.getTitle());
            videoDTO.setDescription(video.getDescription());
            videoDTO.setYoutubeUrl(video.getYoutubeUrl());
            videoDTO.setVimeoUrl(video.getVimeoUrl());
            videoDTO.setMain(video.isMain());
            videoDTO.setSortOrder(video.getSortOrder());
            videoDTO.setDurationSeconds(video.getDurationSeconds());
            videoDTO.setViewCount(video.getViewCount());
            videoDTO.setPublishedAt(video.getPublishedAt());
            videoDTO.setThumbnailPath(video.getThumbnailPath());
            videoDTO.setCreatedAt(video.getCreatedAt());

            videoDTOs.add(videoDTO);
        }

        return videoDTOs;
    }

    /**
     * Маппинг списка партнеров проекта
     *
     * @param partners Список сущностей партнеров
     * @return Список DTO партнеров
     */
    private List<ProjectDTO.ProjectPartnerDTO> mapPartners(List<ProjectPartner> partners) {
        List<ProjectDTO.ProjectPartnerDTO> partnerDTOs = new ArrayList<>();

        for (ProjectPartner partner : partners) {
            ProjectDTO.ProjectPartnerDTO partnerDTO = new ProjectDTO.ProjectPartnerDTO();
            partnerDTO.setId(partner.getId());
            partnerDTO.setName(partner.getName());
            partnerDTO.setDescription(partner.getDescription());
            partnerDTO.setPartnershipType(partner.getPartnershipType());

            // Маппинг логотипа
            if (partner.getLogo() != null) {
                partnerDTO.setLogoUrl(getImageUrl(partner.getLogo()));
            }

            partnerDTO.setWebsiteUrl(partner.getWebsiteUrl());
            partnerDTO.setEmail(partner.getEmail());
            partnerDTO.setPhone(partner.getPhone());
            partnerDTO.setContactPerson(partner.getContactPerson());
            partnerDTO.setAdditionalInfo(partner.getAdditionalInfo());
            partnerDTO.setSortOrder(partner.getSortOrder());
            partnerDTO.setActive(partner.isActive());
            partnerDTO.setMain(partner.isMain());
            partnerDTO.setCreatedAt(partner.getCreatedAt());

            partnerDTOs.add(partnerDTO);
        }

        return partnerDTOs;
    }

    /**
     * Маппинг членов команды проекта
     *
     * @param teamMembers Мапа членов команды с их ролями
     * @return Список DTO членов команды проекта
     */
    private List<ProjectDTO.TeamMemberProjectDTO> mapTeamMembers(Map<TeamMember, String> teamMembers) {
        List<ProjectDTO.TeamMemberProjectDTO> teamMemberDTOs = new ArrayList<>();

        for (Map.Entry<TeamMember, String> entry : teamMembers.entrySet()) {
            TeamMember member = entry.getKey();
            String role = entry.getValue();

            ProjectDTO.TeamMemberProjectDTO memberDTO = new ProjectDTO.TeamMemberProjectDTO();
            memberDTO.setId(member.getId());
            memberDTO.setFullName(member.getFullName());
            memberDTO.setPosition(member.getPosition());
            memberDTO.setProjectRole(role);

            // Маппинг аватара
            if (member.getAvatar() != null) {
                memberDTO.setAvatarUrl(getImageUrl(member.getAvatar()));
            }

            teamMemberDTOs.add(memberDTO);
        }

        return teamMemberDTOs;
    }

    /**
     * Получить URL изображения из MediaFile
     *
     * @param mediaFile Сущность MediaFile
     * @return URL изображения
     */
    private String getImageUrl(MediaFile mediaFile) {
        if (mediaFile == null || mediaFile.getFilePath() == null) {
            return null;
        }

        // Формируем URL в зависимости от типа файла
        // В реальном приложении здесь должна быть логика генерации URL
        return "/uploads/" + mediaFile.getFilePath();
    }

    /**
     * Вложенный DTO для видео проекта
     */
    public static class ProjectVideoDTO {
        private Long id;
        private String title;
        private String description;
        private String youtubeUrl;
        private String vimeoUrl;
        private boolean isMain;
        private Integer sortOrder;
        private Integer durationSeconds;
        private Long viewCount;
        private java.time.LocalDateTime publishedAt;
        private String thumbnailPath;
        private java.time.LocalDateTime createdAt;

        // Геттеры и сеттеры
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getYoutubeUrl() { return youtubeUrl; }
        public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }

        public String getVimeoUrl() { return vimeoUrl; }
        public void setVimeoUrl(String vimeoUrl) { this.vimeoUrl = vimeoUrl; }

        public boolean isMain() { return isMain; }
        public void setMain(boolean main) { isMain = main; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

        public Integer getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

        public Long getViewCount() { return viewCount; }
        public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

        public java.time.LocalDateTime getPublishedAt() { return publishedAt; }
        public void setPublishedAt(java.time.LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

        public String getThumbnailPath() { return thumbnailPath; }
        public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * Вложенный DTO для партнера проекта
     */
    public static class ProjectPartnerDTO {
        private Long id;
        private String name;
        private String description;
        private com.community.cms.model.project.ProjectPartner.PartnershipType partnershipType;
        private String logoUrl;
        private String websiteUrl;
        private String email;
        private String phone;
        private String contactPerson;
        private String additionalInfo;
        private Integer sortOrder;
        private boolean isActive;
        private boolean isMain;
        private java.time.LocalDateTime createdAt;

        // Геттеры и сеттеры
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public com.community.cms.model.project.ProjectPartner.PartnershipType getPartnershipType() { return partnershipType; }
        public void setPartnershipType(com.community.cms.model.project.ProjectPartner.PartnershipType partnershipType) { this.partnershipType = partnershipType; }

        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

        public String getWebsiteUrl() { return websiteUrl; }
        public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getContactPerson() { return contactPerson; }
        public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

        public String getAdditionalInfo() { return additionalInfo; }
        public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }

        public boolean isMain() { return isMain; }
        public void setMain(boolean main) { isMain = main; }

        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}

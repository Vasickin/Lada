package com.community.cms.model.project;

import com.community.cms.model.gallery.MediaFile;
import com.community.cms.model.gallery.PhotoGalleryItem;
import com.community.cms.model.team.TeamMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Основная сущность проекта организации "ЛАДА".
 * Представляет мероприятие, конкурс или активность с уникальным контентом.
 *
 * @author Vasickin
 * @since 1.0
 */
@Entity
@Table(name = "projects",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "slug", name = "uk_projects_slug")
        },
        indexes = {
                @Index(columnList = "slug", name = "idx_projects_slug"),
                @Index(columnList = "category", name = "idx_projects_category"),
                @Index(columnList = "status", name = "idx_projects_status"),
                @Index(columnList = "start_date", name = "idx_projects_start_date"),
                @Index(columnList = "created_at", name = "idx_projects_created_at")
        })
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название проекта (обязательное поле)
     */
    @NotBlank(message = "Название проекта не может быть пустым")
    @Size(min = 3, max = 200, message = "Название проекта должно содержать от 3 до 200 символов")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * URL-идентификатор проекта для ЧПУ
     */
    @NotBlank(message = "URL-идентификатор не может быть пустым")
    @Size(min = 3, max = 200, message = "URL-идентификатор должен содержать от 3 до 200 символов")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "URL-идентификатор может содержать только латинские буквы в нижнем регистре, цифры и дефисы")
    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;

    /**
     * Категория проекта
     */
    @NotNull(message = "Категория проекта обязательна")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ProjectCategory category;

    /**
     * Текущий статус проекта
     */
    @NotNull(message = "Статус проекта обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    /**
     * Краткое описание для карточек и списков
     */
    @Size(max = 500, message = "Краткое описание не должно превышать 500 символов")
    @Column(name = "short_description", length = 500)
    private String shortDescription;

    /**
     * Полное описание проекта с целями, этапами и деталями
     */
    @Lob
    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    /**
     * Цели проекта
     */
    @Lob
    @Column(name = "goals", columnDefinition = "TEXT")
    private String goals;

    /**
     * Место проведения проекта
     */
    @Size(max = 200, message = "Место проведения не должно превышать 200 символов")
    @Column(name = "location", length = 200)
    private String location;

    /**
     * Дата начала проекта
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * Дата окончания проекта
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Связь с существующей галереей фотографий
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gallery_id", foreignKey = @ForeignKey(name = "fk_projects_gallery"))
    private PhotoGalleryItem gallery;

    /**
     * Изображение обложки проекта
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_image_id", foreignKey = @ForeignKey(name = "fk_projects_cover_image"))
    private MediaFile coverImage;

    /**
     * Контакты куратора проекта
     */
    @Size(max = 500, message = "Контакты куратора не должны превышать 500 символов")
    @Column(name = "curator_contacts", length = 500)
    private String curatorContacts;

    /**
     * Информация о том, как принять участие
     */
    @Lob
    @Column(name = "participation_info", columnDefinition = "TEXT")
    private String participationInfo;

    // === НАСТРОЙКИ СЕКЦИЙ ===

    /**
     * Показывать секцию описания
     */
    @Column(name = "show_description", nullable = false)
    private boolean showDescription = true;

    /**
     * Показывать секцию фотогалереи
     */
    @Column(name = "show_photos", nullable = false)
    private boolean showPhotos = true;

    /**
     * Показывать секцию видео
     */
    @Column(name = "show_videos", nullable = false)
    private boolean showVideos = true;

    /**
     * Показывать секцию команды
     */
    @Column(name = "show_team", nullable = false)
    private boolean showTeam = true;

    /**
     * Показывать секцию участия
     */
    @Column(name = "show_participation", nullable = false)
    private boolean showParticipation = true;

    /**
     * Показывать секцию партнеров
     */
    @Column(name = "show_partners", nullable = false)
    private boolean showPartners = true;

    /**
     * Показывать секцию похожих проектов
     */
    @Column(name = "show_related", nullable = false)
    private boolean showRelated = true;

    /**
     * Порядок отображения секций (JSON массив)
     */
    @Column(name = "sections_order", columnDefinition = "TEXT")
    private String sectionsOrder = "[\"description\",\"photos\",\"videos\",\"team\",\"participation\",\"partners\",\"related\"]";

    // === ТЕХНИЧЕСКИЕ ПОЛЯ ===

    /**
     * SEO заголовок (meta title)
     */
    @Size(max = 200, message = "SEO заголовок не должен превышать 200 символов")
    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    /**
     * SEO описание (meta description)
     */
    @Size(max = 500, message = "SEO описание не должно превышать 500 символов")
    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    /**
     * Ключевые слова для SEO
     */
    @Size(max = 500, message = "Ключевые слова не должны превышать 500 символов")
    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // === СВЯЗИ С ДРУГИМИ СУЩНОСТЯМИ ===

    /**
     * Видео проекта (YouTube/Vimeo)
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProjectVideo> videos = new ArrayList<>();

    /**
     * Партнеры проекта
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProjectPartner> partners = new ArrayList<>();

    /**
     * Члены команды проекта с их ролями
     */
    @ManyToMany
    @JoinTable(
            name = "project_team_members",
            joinColumns = @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "fk_project_team_members_project")),
            inverseJoinColumns = @JoinColumn(name = "team_member_id", foreignKey = @ForeignKey(name = "fk_project_team_members_member"))
    )
    @MapKeyJoinColumn(name = "team_member_id")
    @Column(name = "project_role")
    private Map<TeamMember, String> teamMembers = new HashMap<>();

    // === КОНСТРУКТОРЫ ===

    /**
     * Конструктор по умолчанию (требуется JPA)
     */
    public Project() {
        // JPA требует пустого конструктора
    }

    /**
     * Конструктор с основными полями
     *
     * @param title Название проекта
     * @param slug URL-идентификатор
     * @param category Категория проекта
     * @param status Статус проекта
     */
    public Project(String title, String slug, ProjectCategory category, ProjectStatus status) {
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

    public PhotoGalleryItem getGallery() {
        return gallery;
    }

    public void setGallery(PhotoGalleryItem gallery) {
        this.gallery = gallery;
    }

    public MediaFile getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(MediaFile coverImage) {
        this.coverImage = coverImage;
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

    public List<ProjectVideo> getVideos() {
        return videos;
    }

    public void setVideos(List<ProjectVideo> videos) {
        this.videos = videos;
    }

    public List<ProjectPartner> getPartners() {
        return partners;
    }

    public void setPartners(List<ProjectPartner> partners) {
        this.partners = partners;
    }

    public Map<TeamMember, String> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(Map<TeamMember, String> teamMembers) {
        this.teamMembers = teamMembers;
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    /**
     * Добавить видео к проекту
     *
     * @param video Объект ProjectVideo
     */
    public void addVideo(ProjectVideo video) {
        videos.add(video);
        video.setProject(this);
    }

    /**
     * Удалить видео из проекта
     *
     * @param video Объект ProjectVideo
     */
    public void removeVideo(ProjectVideo video) {
        videos.remove(video);
        video.setProject(null);
    }

    /**
     * Добавить партнера к проекту
     *
     * @param partner Объект ProjectPartner
     */
    public void addPartner(ProjectPartner partner) {
        partners.add(partner);
        partner.setProject(this);
    }

    /**
     * Удалить партнера из проекта
     *
     * @param partner Объект ProjectPartner
     */
    public void removePartner(ProjectPartner partner) {
        partners.remove(partner);
        partner.setProject(null);
    }

    /**
     * Добавить члена команды с указанием роли
     *
     * @param member Член команды
     * @param role Роль в проекте
     */
    public void addTeamMember(TeamMember member, String role) {
        teamMembers.put(member, role);
    }

    /**
     * Удалить члена команды из проекта
     *
     * @param member Член команды
     */
    public void removeTeamMember(TeamMember member) {
        teamMembers.remove(member);
    }

    /**
     * Получить роль члена команды в этом проекте
     *
     * @param member Член команды
     * @return Роль в проекте или null, если член не участвует
     */
    public String getTeamMemberRole(TeamMember member) {
        return teamMembers.get(member);
    }

    /**
     * Проверить, является ли проект активным в данный момент
     *
     * @return true, если проект активен и сегодня в диапазоне дат проведения
     */
    public boolean isCurrentlyActive() {
        if (!status.isActive()) {
            return false;
        }

        if (startDate == null || endDate == null) {
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
    public boolean isCompleted() {
        if (endDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return endDate.isBefore(today);
    }

    /**
     * Получить порядок секций в виде списка
     *
     * @return Список идентификаторов секций
     */
    public List<String> getSectionsOrderList() {
        if (sectionsOrder == null || sectionsOrder.trim().isEmpty()) {
            return getDefaultSectionsOrder();
        }

        try {
            // Простой парсинг JSON массива
            String cleaned = sectionsOrder.trim()
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "");

            if (cleaned.isEmpty()) {
                return getDefaultSectionsOrder();
            }

            return Arrays.asList(cleaned.split(","));
        } catch (Exception e) {
            return getDefaultSectionsOrder();
        }
    }

    /**
     * Получить порядок секций по умолчанию
     *
     * @return Список идентификаторов секций по умолчанию
     */
    private List<String> getDefaultSectionsOrder() {
        return Arrays.asList("description", "photos", "videos", "team", "participation", "partners", "related");
    }

    /**
     * Получить количество активных видео
     *
     * @return Количество видео проекта
     */
    public int getVideoCount() {
        return videos != null ? videos.size() : 0;
    }

    /**
     * Получить количество партнеров
     *
     * @return Количество партнеров проекта
     */
    public int getPartnerCount() {
        return partners != null ? partners.size() : 0;
    }

    /**
     * Получить количество членов команды
     *
     * @return Количество членов команды проекта
     */
    public int getTeamMemberCount() {
        return teamMembers != null ? teamMembers.size() : 0;
    }

    /**
     * Получить количество фотографий в галерее
     *
     * @return Количество фотографий или 0, если галереи нет
     */
    public int getPhotoCount() {
        return (gallery != null && gallery.getMediaFiles() != null)
                ? gallery.getMediaFiles().size()
                : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id) &&
                Objects.equals(slug, project.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, slug);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", category=" + category +
                ", status=" + status +
                '}';
    }
}

package com.community.cms.model.project;

import com.community.cms.model.gallery.MediaFile;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

//Описание файла:
//Сущность для хранения информации о партнерах проекта
//Поддержка различных типов партнерства (спонсоры, медиа-партнеры и т.д.)
//Связь с существующей системой MediaFile для логотипов
//Валидация полей (URL, email, телефон)
//Методы для получения URL логотипов и веб-сайтов
//Методы для определения типа партнера (спонсор, медиа-партнер и т.д.)
//CSS классы и иконки для визуального отображения
//Полная контактная информация партнера
//Индексы для оптимизации запросов


/**
 * Сущность партнера проекта организации "ЛАДА".
 * Представляет организацию или компанию, которая поддерживает проект.
 *
 * @author Vasickin
 * @since 1.0
 */
@Entity
@Table(name = "project_partners",
        indexes = {
                @Index(columnList = "project_id", name = "idx_project_partners_project"),
                @Index(columnList = "name", name = "idx_project_partners_name"),
                @Index(columnList = "sort_order", name = "idx_project_partners_order"),
                @Index(columnList = "created_at", name = "idx_project_partners_created_at")
        })
public class ProjectPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Связь с проектом, к которому относится партнер
     */
    @NotNull(message = "Проект обязателен для партнера")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_project_partners_project"))
    private Project project;

    /**
     * Название партнера/организации
     */
    @NotBlank(message = "Название партнера не может быть пустым")
    @Size(min = 2, max = 200, message = "Название партнера должно содержать от 2 до 200 символов")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Описание партнера и его участия в проекте
     */
    @Size(max = 1000, message = "Описание партнера не должно превышать 1000 символов")
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Тип партнерства (генеральный, информационный, технический и т.д.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "partnership_type", length = 50)
    private PartnershipType partnershipType;

    /**
     * Логотип партнера
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo_id", foreignKey = @ForeignKey(name = "fk_project_partners_logo"))
    private MediaFile logo;

    /**
     * Ссылка на веб-сайт партнера
     */
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$",
            message = "URL веб-сайта должен быть корректной ссылкой")
    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    /**
     * Email для связи с партнером
     */
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$",
            message = "Email должен быть корректным адресом электронной почты")
    @Column(name = "email", length = 100)
    private String email;

    /**
     * Телефон для связи с партнером
     */
    @Size(max = 20, message = "Телефон не должен превышать 20 символов")
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Контактное лицо от партнера
     */
    @Size(max = 100, message = "Контактное лицо не должно превышать 100 символов")
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    /**
     * Дополнительная информация о партнере
     */
    @Lob
    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    /**
     * Порядок сортировки партнеров в списке
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * Активен ли партнер (отображается на сайте)
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Является ли партнер главным (отображается первым/крупнее)
     */
    @Column(name = "is_main", nullable = false)
    private boolean isMain = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // === ВНУТРЕННИЕ ПЕРЕЧИСЛЕНИЯ ===

    /**
     * Тип партнерства в проекте
     */
    public enum PartnershipType {
        GENERAL_SPONSOR("Генеральный спонсор", "general"),
        SPONSOR("Спонсор", "sponsor"),
        PARTNER("Партнер", "partner"),
        INFO_PARTNER("Информационный партнер", "info"),
        TECH_PARTNER("Технический партнер", "tech"),
        MEDIA_PARTNER("Медиа-партнер", "media"),
        VOLUNTEER("Волонтерская организация", "volunteer"),
        OTHER("Другое", "other");

        private final String displayName;
        private final String slug;

        PartnershipType(String displayName, String slug) {
            this.displayName = displayName;
            this.slug = slug;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getSlug() {
            return slug;
        }

        public static PartnershipType fromSlug(String slug) {
            for (PartnershipType type : values()) {
                if (type.getSlug().equalsIgnoreCase(slug)) {
                    return type;
                }
            }
            return OTHER;
        }
    }

    // === КОНСТРУКТОРЫ ===

    /**
     * Конструктор по умолчанию (требуется JPA)
     */
    public ProjectPartner() {
        // JPA требует пустого конструктора
    }

    /**
     * Конструктор с основными полями
     *
     * @param project Проект, к которому относится партнер
     * @param name Название партнера
     */
    public ProjectPartner(Project project, String name) {
        this.project = project;
        this.name = name;
    }

    /**
     * Конструктор с полным набором полей
     *
     * @param project Проект, к которому относится партнер
     * @param name Название партнера
     * @param partnershipType Тип партнерства
     * @param websiteUrl Ссылка на веб-сайт
     */
    public ProjectPartner(Project project, String name, PartnershipType partnershipType, String websiteUrl) {
        this.project = project;
        this.name = name;
        this.partnershipType = partnershipType;
        this.websiteUrl = websiteUrl;
    }

    // === ГЕТТЕРЫ И СЕТТЕРЫ ===

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PartnershipType getPartnershipType() {
        return partnershipType;
    }

    public void setPartnershipType(PartnershipType partnershipType) {
        this.partnershipType = partnershipType;
    }

    public MediaFile getLogo() {
        return logo;
    }

    public void setLogo(MediaFile logo) {
        this.logo = logo;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
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
     * Получить отображаемое название типа партнерства
     *
     * @return Название типа партнерства или пустая строка, если не указан
     */
    public String getPartnershipTypeDisplayName() {
        return partnershipType != null ? partnershipType.getDisplayName() : "";
    }

    /**
     * Получить URL логотипа партнера
     *
     * @return URL логотипа или null, если логотип не загружен
     */
    public String getLogoUrl() {
        if (logo != null && logo.getFilePath() != null) {
            return "/uploads/partners/logos/" + logo.getFileName();
        }
        return null;
    }

    /**
     * Получить абсолютный URL веб-сайта (с протоколом)
     *
     * @return Абсолютный URL или исходный URL, если уже содержит протокол
     */
    public String getAbsoluteWebsiteUrl() {
        if (websiteUrl == null || websiteUrl.isEmpty()) {
            return null;
        }

        if (websiteUrl.startsWith("http://") || websiteUrl.startsWith("https://")) {
            return websiteUrl;
        }

        return "https://" + websiteUrl;
    }

    /**
     * Получить домен веб-сайта партнера
     *
     * @return Домен или null, если URL не указан
     */
    public String getWebsiteDomain() {
        if (websiteUrl == null || websiteUrl.isEmpty()) {
            return null;
        }

        try {
            String url = getAbsoluteWebsiteUrl();
            // Удалить протокол
            String domain = url.replaceFirst("^(https?://)?(www\\.)?", "");
            // Удалить путь после домена
            int slashIndex = domain.indexOf('/');
            if (slashIndex != -1) {
                domain = domain.substring(0, slashIndex);
            }
            return domain;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Проверить, является ли партнер спонсором
     *
     * @return true, если тип партнерства SPONSOR или GENERAL_SPONSOR
     */
    public boolean isSponsor() {
        return partnershipType == PartnershipType.SPONSOR ||
                partnershipType == PartnershipType.GENERAL_SPONSOR;
    }

    /**
     * Проверить, является ли партнер медиа-партнером
     *
     * @return true, если тип партнерства MEDIA_PARTNER или INFO_PARTNER
     */
    public boolean isMediaPartner() {
        return partnershipType == PartnershipType.MEDIA_PARTNER ||
                partnershipType == PartnershipType.INFO_PARTNER;
    }

    /**
     * Проверить, является ли партнер техническим партнером
     *
     * @return true, если тип партнерства TECH_PARTNER
     */
    public boolean isTechPartner() {
        return partnershipType == PartnershipType.TECH_PARTNER;
    }

    /**
     * Получить CSS класс для стилизации карточки партнера
     *
     * @return CSS класс в зависимости от типа партнерства
     */
    public String getCardClass() {
        if (partnershipType == null) {
            return "partner-card";
        }

        switch (partnershipType) {
            case GENERAL_SPONSOR:
                return "partner-card sponsor-card main-sponsor";
            case SPONSOR:
                return "partner-card sponsor-card";
            case MEDIA_PARTNER:
            case INFO_PARTNER:
                return "partner-card media-partner";
            case TECH_PARTNER:
                return "partner-card tech-partner";
            default:
                return "partner-card";
        }
    }

    /**
     * Получить иконку для типа партнерства
     *
     * @return Название иконки FontAwesome или другое
     */
    public String getPartnershipIcon() {
        if (partnershipType == null) {
            return "fas fa-handshake";
        }

        switch (partnershipType) {
            case GENERAL_SPONSOR:
                return "fas fa-crown";
            case SPONSOR:
                return "fas fa-money-bill-wave";
            case MEDIA_PARTNER:
                return "fas fa-newspaper";
            case INFO_PARTNER:
                return "fas fa-bullhorn";
            case TECH_PARTNER:
                return "fas fa-cogs";
            case VOLUNTEER:
                return "fas fa-hands-helping";
            default:
                return "fas fa-handshake";
        }
    }

    /**
     * Проверить, имеет ли партнер контактную информацию
     *
     * @return true, если указан email, телефон или контактное лицо
     */
    public boolean hasContactInfo() {
        return (email != null && !email.isEmpty()) ||
                (phone != null && !phone.isEmpty()) ||
                (contactPerson != null && !contactPerson.isEmpty());
    }

    /**
     * Получить полную контактную информацию в виде строки
     *
     * @return Отформатированная строка с контактной информацией
     */
    public String getFullContactInfo() {
        StringBuilder sb = new StringBuilder();

        if (contactPerson != null && !contactPerson.isEmpty()) {
            sb.append(contactPerson);
        }

        if (email != null && !email.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("Email: ").append(email);
        }

        if (phone != null && !phone.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("Тел: ").append(phone);
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectPartner that = (ProjectPartner) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, project);
    }

    @Override
    public String toString() {
        return "ProjectPartner{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", partnershipType=" + partnershipType +
                ", websiteUrl='" + websiteUrl + '\'' +
                '}';
    }
}

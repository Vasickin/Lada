package com.community.cms.domain.model.people;

import com.community.cms.domain.model.content.Project;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сущность партнера проекта организации "ЛАДА".
 *
 * <p>Представляет организацию-партнера, участвующую в проекте.
 * Содержит информацию о партнере, логотип и ссылку на сайт.</p>
 *
 * <p>Основные характеристики:
 * <ul>
 *   <li>Название и описание партнера</li>
 *   <li>Логотип для отображения на сайте</li>
 *   <li>Ссылка на сайт партнера</li>
 *   <li>Тип партнерства (спонсор, информационный партнер и т.д.)</li>
 *   <li>Порядок отображения в списке партнеров</li>
 * </ul>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "project_partners")
public class Partner {

    /**
     * Типы партнерства для классификации партнеров.
     * Partner types for classification.
     */
    public enum PartnerType {
        SPONSOR("Спонсор", "Sponsor"),
        INFORMATION_PARTNER("Информационный партнер", "Information Partner"),
        ORGANIZATIONAL_PARTNER("Организационный партнер", "Organizational Partner"),
        TECHNICAL_PARTNER("Технический партнер", "Technical Partner"),
        GENERAL_PARTNER("Генеральный партнер", "General Partner"),
        OTHER("Другой", "Other");

        private final String nameRu;
        private final String nameEn;

        PartnerType(String nameRu, String nameEn) {
            this.nameRu = nameRu;
            this.nameEn = nameEn;
        }

        public String getNameRu() {
            return nameRu;
        }

        public String getNameEn() {
            return nameEn;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

// ================== СВЯЗИ ==================

    /**
     * Проект, к которому относится партнер (старая связь ManyToOne).
     * Для обратной совместимости.
     */
    @NotNull(message = "Проект обязателен / Project is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Проекты, в которых участвует партнер (новая связь ManyToMany).
     * Использует новую промежуточную таблицу project_partner_links.
     */
    @ManyToMany
    @JoinTable(
            name = "project_partner_links", // Новая таблица!
            joinColumns = @JoinColumn(name = "partner_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<Project> projects = new HashSet<>();

    // ================== ОСНОВНЫЕ ДАННЫЕ ==================

    /**
     * Название партнерской организации.
     */
    @NotBlank(message = "Название партнера обязательно / Partner name is required")
    @Size(min = 2, max = 255, message = "Название должно быть от 2 до 255 символов / Name must be between 2 and 255 characters")
    @Column(nullable = false)
    private String name;

    /**
     * Описание партнера (опционально).
     * Краткая информация о партнере и его участии в проекте.
     */
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов / Description must not exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Тип партнерства.
     * Определяет категорию участия партнера в проекте.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "partner_type", nullable = false, length = 50)
    private PartnerType partnerType = PartnerType.OTHER;

    /**
     * Путь к логотипу партнера.
     * Отображается на странице проекта.
     */
    @Column(name = "logo_path", length = 500)
    private String logoPath;

    /**
     * URL сайта партнера (опционально).
     */
    @Size(max = 500, message = "URL не должен превышать 500 символов / URL must not exceed 500 characters")
    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    /**
     * Контактный email партнера (опционально).
     */
    @Size(max = 100, message = "Email не должен превышать 100 символов / Email must not exceed 100 characters")
    private String contactEmail;

    /**
     * Контактный телефон партнера (опционально).
     */
    @Size(max = 50, message = "Телефон не должен превышать 50 символов / Phone must not exceed 50 characters")
    private String contactPhone;

    /**
     * Контактное лицо от партнера (опционально).
     */
    @Size(max = 100, message = "Контактное лицо не должно превышать 100 символов / Contact person must not exceed 100 characters")
    @Column(name = "contact_person")
    private String contactPerson;

    /**
     * Порядок сортировки в списке партнеров.
     * Меньшее значение = выше в списке.
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * Флаг активности партнера.
     * Неактивные партнеры не отображаются на сайте.
     */
    @Column(nullable = false)
    private boolean active = true;

    // ================== СИСТЕМНЫЕ ПОЛЯ ==================

    /**
     * Дата и время добавления партнера.
     */
    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    /**
     * Конструктор по умолчанию.
     */
    public Partner() {
        this.addedAt = LocalDateTime.now();
    }

    /**
     * Конструктор с основными параметрами.
     *
     * @param project проект
     * @param name название партнера
     * @param partnerType тип партнерства
     */
    public Partner(Project project, String name, PartnerType partnerType) {
        this();
        this.project = project;
        this.name = name;
        this.partnerType = partnerType;
        this.active = true;
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

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
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

    public PartnerType getPartnerType() {
        return partnerType;
    }

    public void setPartnerType(PartnerType partnerType) {
        this.partnerType = partnerType;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет имеет ли партнер логотип.
     *
     * @return true если logoPath не пустой, иначе false
     */
    public boolean hasLogo() {
        return logoPath != null && !logoPath.trim().isEmpty();
    }

    /**
     * Проверяет имеет ли партнер сайт.
     *
     * @return true если websiteUrl не пустой, иначе false
     */
    public boolean hasWebsite() {
        return websiteUrl != null && !websiteUrl.trim().isEmpty();
    }

    /**
     * Проверяет имеет ли партнер описание.
     *
     * @return true если description не пустой, иначе false
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    /**
     * Проверяет имеет ли партнер контактную информацию.
     *
     * @return true если есть email или телефон, иначе false
     */
    public boolean hasContactInfo() {
        return (contactEmail != null && !contactEmail.trim().isEmpty()) ||
                (contactPhone != null && !contactPhone.trim().isEmpty());
    }

    /**
     * Получает полную контактную информацию в виде строки.
     *
     * @return форматированная контактная информация
     */
    public String getFormattedContactInfo() {
        StringBuilder info = new StringBuilder();

        if (contactPerson != null && !contactPerson.trim().isEmpty()) {
            info.append(contactPerson);
        }

        if (contactEmail != null && !contactEmail.trim().isEmpty()) {
            if (info.length() > 0) info.append(", ");
            info.append("Email: ").append(contactEmail);
        }

        if (contactPhone != null && !contactPhone.trim().isEmpty()) {
            if (info.length() > 0) info.append(", ");
            info.append("Тел: ").append(contactPhone);
        }

        return info.toString();
    }

    /**
     * Получает URL сайта с протоколом.
     * Если websiteUrl не начинается с http:// или https://, добавляет https://.
     *
     * @return полный URL с протоколом
     */
    public String getFullWebsiteUrl() {
        if (websiteUrl == null || websiteUrl.trim().isEmpty()) {
            return null;
        }

        String url = websiteUrl.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }

        return url;
    }

    /**
     * Получает отображаемое имя типа партнерства на русском.
     *
     * @return русское название типа партнерства
     */
    public String getPartnerTypeDisplayNameRu() {
        return partnerType.getNameRu();
    }

    /**
     * Получает отображаемое имя типа партнерства на английском.
     *
     * @return английское название типа партнерства
     */
    public String getPartnerTypeDisplayNameEn() {
        return partnerType.getNameEn();
    }

    /**
     * Проверяет является ли партнер спонсором.
     *
     * @return true если partnerType == SPONSOR
     */
    public boolean isSponsor() {
        return partnerType == PartnerType.SPONSOR;
    }

    /**
     * Проверяет является ли партнер информационным партнером.
     *
     * @return true если partnerType == INFORMATION_PARTNER
     */
    public boolean isInformationPartner() {
        return partnerType == PartnerType.INFORMATION_PARTNER;
    }

    /**
     * Получает инициалы партнера (для заглушки логотипа).
     * Пример: "Русская Община" → "РО"
     *
     * @return инициалы (2 буквы)
     */
    public String getInitials() {
        if (name == null || name.trim().isEmpty()) {
            return "PP";
        }

        String[] words = name.split("\\s+");
        if (words.length >= 2) {
            // Берем первые буквы первых двух слов
            return (words[0].charAt(0) + "" + words[1].charAt(0)).toUpperCase();
        } else if (words.length == 1 && words[0].length() >= 2) {
            // Если только одно слово, берем первые две буквы
            return words[0].substring(0, 2).toUpperCase();
        }

        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    /**
     * Получает ID проекта для быстрого доступа.
     *
     * @return ID проекта или null если проект не установлен
     */
    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    @Override
    public String toString() {
        return "Partner{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", partnerType=" + partnerType +
                ", active=" + active +
                ", projectId=" + getProjectId() +
                '}';
    }

    /**
     * Метод предварительной обработки перед сохранением.
     * Синхронизирует старую и новую связи с проектами.
     */
    @PrePersist
    protected void onCreate() {
        // Синхронизация связей проект ↔ проекты
        synchronizeProjectLinks();

        // Инициализация полей по умолчанию
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (partnerType == null) {
            partnerType = PartnerType.OTHER;
        }
    }

    /**
     * Метод предварительной обработки перед обновлением.
     */
    @PreUpdate
    protected void onUpdate() {
        // Синхронизируем связи при обновлении
        synchronizeProjectLinks();
    }

    /**
     * Синхронизирует старую и новую связи с проектами.
     * Если есть projects, но нет project - устанавливаем первый проект как основной.
     * Если есть project, но нет в projects - добавляем его.
     */
    protected void synchronizeProjectLinks() {
        // Если есть новая связь (projects), но нет старой (project)
        if (projects != null && !projects.isEmpty() && project == null) {
            project = projects.iterator().next(); // Берем первый проект
        }

        // Если есть старая связь (project), но нет в новой (projects)
        if (project != null) {
            if (projects == null) {
                projects = new HashSet<>();
            }
            // Добавляем project в projects, если его там нет
            boolean projectExists = projects.stream()
                    .anyMatch(p -> p != null && p.getId() != null && p.getId().equals(project.getId()));
            if (!projectExists) {
                projects.add(project);
            }
        }


    }

    /**
     * Получает количество проектов партнера.
     *
     * @return количество проектов
     */
    public int getProjectsCount() {
        if (projects == null) {
            return 0;
        }
        return projects.size();
    }


    /**
     * Получает ID проектов.
     *
     * @return Set ID проектов или пустой Set если проектов нет
     */
    public Set<Long> getProjectIds() {
        if (projects == null || projects.isEmpty()) {
            return new HashSet<>();
        }
        return projects.stream()
                .map(Project::getId)
                .collect(Collectors.toSet());
    }
}
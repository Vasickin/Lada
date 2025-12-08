package com.community.cms.model.team;

import com.community.cms.model.gallery.MediaFile;
import com.community.cms.model.project.Project;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

//Описание файла:
//Сущность для хранения информации о членах команды организации
//Поддержка полного имени (фамилия, имя, отчество)
//Различные категории членов команды (руководство, координаторы, волонтеры и т.д.)
//Связь с существующей системой MediaFile для аватаров
//Поддержка социальных сетей с иконками FontAwesome
//Уровни доступа для системных пользователей
//Связь с проектами (обратная сторона связи из Project)
//Методы для форматирования телефона и получения URL аватаров
//Методы для проверки ролей и категорий
//Индексы для оптимизации запросов


/**
 * Сущность члена команды организации "ЛАДА".
 * Представляет сотрудника, волонтера или участника организации,
 * который может быть связан с различными проектами.
 *
 * @author Vasickin
 * @since 1.0
 */
@Entity
@Table(name = "team_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email", name = "uk_team_members_email")
        },
        indexes = {
                @Index(columnList = "last_name, first_name", name = "idx_team_members_name"),
                @Index(columnList = "position", name = "idx_team_members_position"),
                @Index(columnList = "is_active", name = "idx_team_members_active"),
                @Index(columnList = "sort_order", name = "idx_team_members_order"),
                @Index(columnList = "created_at", name = "idx_team_members_created_at")
        })
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя члена команды
     */
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /**
     * Фамилия члена команды
     */
    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(min = 2, max = 50, message = "Фамилия должна содержать от 2 до 50 символов")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * Отчество члена команды (необязательное)
     */
    @Size(max = 50, message = "Отчество не должно превышать 50 символов")
    @Column(name = "middle_name", length = 50)
    private String middleName;

    /**
     * Основная должность в организации
     */
    @NotBlank(message = "Должность не может быть пустой")
    @Size(min = 2, max = 100, message = "Должность должна содержать от 2 до 100 символов")
    @Column(name = "position", nullable = false, length = 100)
    private String position;

    /**
     * Краткая биография или описание
     */
    @Lob
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    /**
     * Образование или квалификация
     */
    @Size(max = 500, message = "Образование не должно превышать 500 символов")
    @Column(name = "education", length = 500)
    private String education;

    /**
     * Опыт работы
     */
    @Size(max = 1000, message = "Опыт работы не должен превышать 1000 символов")
    @Column(name = "experience", length = 1000)
    private String experience;

    /**
     * Достижения и награды
     */
    @Lob
    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements;

    /**
     * Email для связи
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть корректным адресом электронной почты")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Телефон для связи
     */
    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]+$",
            message = "Телефон должен содержать только цифры, пробелы, скобки и дефисы")
    @Size(max = 20, message = "Телефон не должен превышать 20 символов")
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Аватар члена команды
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", foreignKey = @ForeignKey(name = "fk_team_members_avatar"))
    private MediaFile avatar;

    /**
     * Ссылка на профиль в социальных сетях (основная)
     */
    @Size(max = 500, message = "Ссылка на соцсеть не должна превышать 500 символов")
    @Column(name = "social_link", length = 500)
    private String socialLink;

    /**
     * Тип социальной сети
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", length = 20)
    private SocialType socialType;

    /**
     * Дополнительные социальные сети (JSON формат)
     */
    @Lob
    @Column(name = "additional_socials", columnDefinition = "TEXT")
    private String additionalSocials;

    /**
     * Категория члена команды (руководство, волонтер и т.д.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private TeamMemberCategory category = TeamMemberCategory.TEAM;

    /**
     * Уровень доступа в системе (если член команды - пользователь)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", length = 20)
    private AccessLevel accessLevel;

    /**
     * Порядок сортировки в списке команды
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * Активен ли член команды (отображается на сайте)
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Показывать ли контактную информацию публично
     */
    @Column(name = "show_contacts", nullable = false)
    private boolean showContacts = true;

    /**
     * Показывать ли в списке "Наша команда"
     */
    @Column(name = "show_in_team_list", nullable = false)
    private boolean showInTeamList = true;

    /**
     * Примечания или внутренняя информация
     */
    @Lob
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Связь с проектами (обратная сторона связи из Project)
     */
    @ManyToMany(mappedBy = "teamMembers")
    private Set<Project> projects = new HashSet<>();

    // === ВНУТРЕННИЕ ПЕРЕЧИСЛЕНИЯ ===

    /**
     * Категория члена команды
     */
    public enum TeamMemberCategory {
        MANAGEMENT("Руководство", "management", 1),
        COORDINATOR("Координатор", "coordinator", 2),
        TEAM("Команда", "team", 3),
        VOLUNTEER("Волонтер", "volunteer", 4),
        PARTNER("Партнер", "partner", 5),
        CONSULTANT("Консультант", "consultant", 6);

        private final String displayName;
        private final String slug;
        private final int sortOrder;

        TeamMemberCategory(String displayName, String slug, int sortOrder) {
            this.displayName = displayName;
            this.slug = slug;
            this.sortOrder = sortOrder;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getSlug() {
            return slug;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        public static TeamMemberCategory fromSlug(String slug) {
            for (TeamMemberCategory category : values()) {
                if (category.getSlug().equalsIgnoreCase(slug)) {
                    return category;
                }
            }
            return TEAM;
        }
    }

    /**
     * Тип социальной сети
     */
    public enum SocialType {
        FACEBOOK("Facebook", "fab fa-facebook"),
        VK("ВКонтакте", "fab fa-vk"),
        INSTAGRAM("Instagram", "fab fa-instagram"),
        TELEGRAM("Telegram", "fab fa-telegram"),
        WHATSAPP("WhatsApp", "fab fa-whatsapp"),
        LINKEDIN("LinkedIn", "fab fa-linkedin"),
        TWITTER("Twitter", "fab fa-twitter"),
        YOUTUBE("YouTube", "fab fa-youtube"),
        OTHER("Другое", "fas fa-share-alt");

        private final String displayName;
        private final String iconClass;

        SocialType(String displayName, String iconClass) {
            this.displayName = displayName;
            this.iconClass = iconClass;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIconClass() {
            return iconClass;
        }
    }

    /**
     * Уровень доступа в системе
     */
    public enum AccessLevel {
        NONE("Нет доступа"),
        VIEWER("Просмотр"),
        EDITOR("Редактор"),
        MANAGER("Менеджер"),
        ADMIN("Администратор");

        private final String displayName;

        AccessLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // === КОНСТРУКТОРЫ ===

    /**
     * Конструктор по умолчанию (требуется JPA)
     */
    public TeamMember() {
        // JPA требует пустого конструктора
    }

    /**
     * Конструктор с основными полями
     *
     * @param firstName Имя
     * @param lastName Фамилия
     * @param position Должность
     * @param email Email
     */
    public TeamMember(String firstName, String lastName, String position, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.email = email;
    }

    /**
     * Конструктор с полным именем
     *
     * @param firstName Имя
     * @param lastName Фамилия
     * @param middleName Отчество
     * @param position Должность
     * @param email Email
     */
    public TeamMember(String firstName, String lastName, String middleName, String position, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.position = position;
        this.email = email;
    }

    // === ГЕТТЕРЫ И СЕТТЕРЫ ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
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

    public MediaFile getAvatar() {
        return avatar;
    }

    public void setAvatar(MediaFile avatar) {
        this.avatar = avatar;
    }

    public String getSocialLink() {
        return socialLink;
    }

    public void setSocialLink(String socialLink) {
        this.socialLink = socialLink;
    }

    public SocialType getSocialType() {
        return socialType;
    }

    public void setSocialType(SocialType socialType) {
        this.socialType = socialType;
    }

    public String getAdditionalSocials() {
        return additionalSocials;
    }

    public void setAdditionalSocials(String additionalSocials) {
        this.additionalSocials = additionalSocials;
    }

    public TeamMemberCategory getCategory() {
        return category;
    }

    public void setCategory(TeamMemberCategory category) {
        this.category = category;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
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

    public boolean isShowContacts() {
        return showContacts;
    }

    public void setShowContacts(boolean showContacts) {
        this.showContacts = showContacts;
    }

    public boolean isShowInTeamList() {
        return showInTeamList;
    }

    public void setShowInTeamList(boolean showInTeamList) {
        this.showInTeamList = showInTeamList;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    /**
     * Получить полное имя (Фамилия Имя Отчество)
     *
     * @return Полное имя
     */
    public String getFullName() {
        if (middleName != null && !middleName.isEmpty()) {
            return lastName + " " + firstName + " " + middleName;
        }
        return lastName + " " + firstName;
    }

    /**
     * Получить имя и фамилию (Имя Фамилия)
     *
     * @return Имя и фамилия
     */
    public String getShortName() {
        return firstName + " " + lastName;
    }

    /**
     * Получить инициалы (Фамилия И.О.)
     *
     * @return Инициалы
     */
    public String getInitials() {
        StringBuilder sb = new StringBuilder(lastName);

        if (firstName != null && !firstName.isEmpty()) {
            sb.append(" ").append(firstName.charAt(0)).append(".");
        }

        if (middleName != null && !middleName.isEmpty()) {
            sb.append(middleName.charAt(0)).append(".");
        }

        return sb.toString();
    }

    /**
     * Получить URL аватара
     *
     * @return URL аватара или null, если аватар не загружен
     */
    public String getAvatarUrl() {
        if (avatar != null && avatar.getFilePath() != null) {
            return "/uploads/team/avatars/" + avatar.getFileName();
        }
        return null;
    }

    /**
     * Получить URL аватара по умолчанию
     *
     * @return URL стандартного аватара
     */
    public String getDefaultAvatarUrl() {
        return "/images/default-avatar.png";
    }

    /**
     * Получить отображаемый аватар (загруженный или по умолчанию)
     *
     * @return URL аватара
     */
    public String getDisplayAvatarUrl() {
        String avatarUrl = getAvatarUrl();
        return avatarUrl != null ? avatarUrl : getDefaultAvatarUrl();
    }

    /**
     * Получить иконку социальной сети
     *
     * @return CSS класс иконки или стандартная иконка
     */
    public String getSocialIconClass() {
        return socialType != null ? socialType.getIconClass() : "fas fa-share-alt";
    }

    /**
     * Получить название социальной сети
     *
     * @return Название соцсети или "Соцсеть"
     */
    public String getSocialDisplayName() {
        return socialType != null ? socialType.getDisplayName() : "Соцсеть";
    }

    /**
     * Проверить, является ли член команды руководителем
     *
     * @return true, если категория MANAGEMENT
     */
    public boolean isManagement() {
        return category == TeamMemberCategory.MANAGEMENT;
    }

    /**
     * Проверить, является ли член команды волонтером
     *
     * @return true, если категория VOLUNTEER
     */
    public boolean isVolunteer() {
        return category == TeamMemberCategory.VOLUNTEER;
    }

    /**
     * Проверить, является ли член команды координатором
     *
     * @return true, если категория COORDINATOR
     */
    public boolean isCoordinator() {
        return category == TeamMemberCategory.COORDINATOR;
    }

    /**
     * Получить количество проектов, в которых участвует член команды
     *
     * @return Количество проектов
     */
    public int getProjectCount() {
        return projects != null ? projects.size() : 0;
    }

    /**
     * Получить активные проекты члена команды
     *
     * @return Набор активных проектов
     */
    public Set<Project> getActiveProjects() {
        Set<Project> activeProjects = new HashSet<>();
        if (projects != null) {
            for (Project project : projects) {
                if (project.getStatus().isActive()) {
                    activeProjects.add(project);
                }
            }
        }
        return activeProjects;
    }

    /**
     * Получить форматированный телефон
     *
     * @return Отформатированный телефон или исходный, если форматирование невозможно
     */
    public String getFormattedPhone() {
        if (phone == null || phone.isEmpty()) {
            return null;
        }

        try {
            // Удаляем все нецифровые символы
            String digits = phone.replaceAll("[^0-9]", "");

            if (digits.length() == 11) {
                // Формат: +7 (XXX) XXX-XX-XX
                return "+7 (" + digits.substring(1, 4) + ") " +
                        digits.substring(4, 7) + "-" +
                        digits.substring(7, 9) + "-" +
                        digits.substring(9);
            } else if (digits.length() == 10) {
                // Формат: (XXX) XXX-XX-XX
                return "(" + digits.substring(0, 3) + ") " +
                        digits.substring(3, 6) + "-" +
                        digits.substring(6, 8) + "-" +
                        digits.substring(8);
            } else {
                return phone;
            }
        } catch (Exception e) {
            return phone;
        }
    }

    /**
     * Проверить, имеет ли член команды аватар
     *
     * @return true, если аватар загружен
     */
    public boolean hasAvatar() {
        return avatar != null && avatar.getFilePath() != null;
    }

    /**
     * Проверить, имеет ли член команды биографию
     *
     * @return true, если биография не пуста
     */
    public boolean hasBio() {
        return bio != null && !bio.trim().isEmpty();
    }

    /**
     * Проверить, имеет ли член команды контактную информацию
     *
     * @return true, если есть телефон или email
     */
    public boolean hasContactInfo() {
        return (phone != null && !phone.isEmpty()) ||
                (email != null && !email.isEmpty());
    }

    /**
     * Получить CSS класс для карточки члена команды
     *
     * @return CSS класс в зависимости от категории
     */
    public String getCardClass() {
        switch (category) {
            case MANAGEMENT:
                return "team-card management-card";
            case COORDINATOR:
                return "team-card coordinator-card";
            case VOLUNTEER:
                return "team-card volunteer-card";
            case PARTNER:
                return "team-card partner-card";
            default:
                return "team-card";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMember that = (TeamMember) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "TeamMember{" +
                "id=" + id +
                ", fullName='" + getFullName() + '\'' +
                ", position='" + position + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

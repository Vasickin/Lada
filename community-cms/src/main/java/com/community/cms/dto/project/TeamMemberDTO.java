package com.community.cms.dto.project;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Data Transfer Object (DTO) для отображения информации о членах команды.
 *
 * <p>Используется для передачи данных в представления (шаблоны) без раскрытия
 * внутренней структуры сущностей. Содержит только те данные, которые необходимы
 * для отображения на сайте.</p>
 *
 * <p>Основные преимущества:
 * <ul>
 *   <li>Изоляция представления от модели данных</li>
 *   <li>Контроль над отображаемыми полями</li>
 *   <li>Оптимизация передачи данных (без лишних полей)</li>
 *   <li>Безопасность (не передаются служебные поля)</li>
 * </ul>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see com.community.cms.model.project.TeamMember
 * @see TeamMemberForm
 */
public class TeamMemberDTO {

    // ================== ОСНОВНАЯ ИНФОРМАЦИЯ ==================

    /**
     * ID члена команды.
     */
    private Long id;

    /**
     * Полное имя члена команды.
     */
    private String fullName;

    /**
     * Основная должность в организации.
     */
    private String position;

    /**
     * Биография члена команды (очищенная от HTML тегов для превью).
     */
    private String bio;

    /**
     * Краткая биография (первые 200 символов).
     */
    private String shortBio;

    /**
     * Путь к аватарке (фотографии) члена команды.
     */
    private String avatarPath;

    // ================== КОНТАКТНАЯ ИНФОРМАЦИЯ ==================

    /**
     * Электронная почта для контактов (только если разрешено отображать).
     */
    private String email;

    /**
     * Телефон для контактов (только если разрешено отображать).
     */
    private String phone;

    /**
     * Социальные сети в виде Map (ключ - название сети, значение - ссылка).
     */
    private Map<String, String> socialLinks;

    // ================== УЧАСТИЕ В ПРОЕКТАХ ==================

    /**
     * ID проектов, в которых участвует член команды.
     */
    private Set<Long> projectIds;

    /**
     * Количество проектов, в которых участвует член команды.
     */
    private Integer projectsCount;

    /**
     * Названия проектов, в которых участвует член команды (для отображения).
     */
    private Set<String> projectNames;

    // ================== НАСТРОЙКИ ОТОБРАЖЕНИЯ ==================

    /**
     * Порядок сортировки в списке команды.
     */
    private Integer sortOrder;

    /**
     * Флаг активности члена команды.
     */
    private boolean active;

    /**
     * Инициалы члена команды (для аватарок по умолчанию).
     */
    private String initials;

    // ================== ДАТЫ ==================

    /**
     * Дата и время создания записи (форматированная строка).
     */
    private String createdAtFormatted;

    /**
     * Дата и время последнего обновления (форматированная строка).
     */
    private String updatedAtFormatted;

    // ================== КОНСТРУКТОРЫ ==================

    /**
     * Конструктор по умолчанию.
     */
    public TeamMemberDTO() {
    }

    /**
     * Конструктор на основе сущности TeamMember.
     *
     * @param teamMember сущность члена команды
     */
    public TeamMemberDTO(com.community.cms.model.project.TeamMember teamMember) {
        this.id = teamMember.getId();
        this.fullName = teamMember.getFullName();
        this.position = teamMember.getPosition();
        this.bio = teamMember.getBio();
        this.avatarPath = teamMember.getAvatarPath();
        this.email = teamMember.getEmail();
        this.phone = teamMember.getPhone();
        this.sortOrder = teamMember.getSortOrder();
        this.active = teamMember.isActive();
        this.initials = teamMember.getInitials();
        this.projectsCount = teamMember.getProjectsCount();

        // Парсим социальные ссылки из JSON
        if (teamMember.getSocialLinks() != null && !teamMember.getSocialLinks().trim().isEmpty()) {
            this.socialLinks = TeamMemberForm.parseSocialLinks(teamMember.getSocialLinks());
        }

        // Собираем ID проектов
        if (teamMember.getProjects() != null) {
            this.projectIds = new java.util.HashSet<>();
            this.projectNames = new java.util.HashSet<>();
            teamMember.getProjects().forEach(project -> {
                this.projectIds.add(project.getId());
                this.projectNames.add(project.getTitle());
            });
        }

        // Форматируем даты
        this.createdAtFormatted = formatDateTime(teamMember.getCreatedAt());
        this.updatedAtFormatted = formatDateTime(teamMember.getUpdatedAt());

        // Создаем короткую биографию
        this.shortBio = createShortBio(teamMember.getBio());
    }

    /**
     * Конструктор на основе формы TeamMemberForm.
     *
     * @param form форма члена команды
     */
    public TeamMemberDTO(TeamMemberForm form) {
        this.id = form.getId();
        this.fullName = form.getFullName();
        this.position = form.getPosition();
        this.bio = form.getBio();
        this.avatarPath = form.getAvatarPath();
        this.email = form.getEmail();
        this.phone = form.getPhone();
        this.sortOrder = form.getSortOrder();
        this.active = form.isActive();
        this.initials = form.getInitials();
        this.projectIds = form.getProjectIds();
        this.projectsCount = form.getProjectsCount();

        // Парсим социальные ссылки
        if (form.getSocialLinks() != null && !form.getSocialLinks().trim().isEmpty()) {
            this.socialLinks = TeamMemberForm.parseSocialLinks(form.getSocialLinks());
        }

        // Создаем короткую биографию
        this.shortBio = createShortBio(form.getBio());
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
        // Обновляем короткую биографию при изменении основной
        this.shortBio = createShortBio(bio);
    }

    public String getShortBio() {
        return shortBio;
    }

    public void setShortBio(String shortBio) {
        this.shortBio = shortBio;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
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

    public Map<String, String> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(Map<String, String> socialLinks) {
        this.socialLinks = socialLinks;
    }

    public Set<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(Set<Long> projectIds) {
        this.projectIds = projectIds;
        this.projectsCount = projectIds != null ? projectIds.size() : 0;
    }

    public Integer getProjectsCount() {
        return projectsCount;
    }

    public void setProjectsCount(Integer projectsCount) {
        this.projectsCount = projectsCount;
    }

    public Set<String> getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(Set<String> projectNames) {
        this.projectNames = projectNames;
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

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getCreatedAtFormatted() {
        return createdAtFormatted;
    }

    public void setCreatedAtFormatted(String createdAtFormatted) {
        this.createdAtFormatted = createdAtFormatted;
    }

    public String getUpdatedAtFormatted() {
        return updatedAtFormatted;
    }

    public void setUpdatedAtFormatted(String updatedAtFormatted) {
        this.updatedAtFormatted = updatedAtFormatted;
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет, имеет ли член команды аватарку.
     *
     * @return true если avatarPath не пустой, иначе false
     */
    public boolean hasAvatar() {
        return avatarPath != null && !avatarPath.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли член команды биографию.
     *
     * @return true если bio не пустой, иначе false
     */
    public boolean hasBio() {
        return bio != null && !bio.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли член команды короткую биографию.
     *
     * @return true если shortBio не пустой, иначе false
     */
    public boolean hasShortBio() {
        return shortBio != null && !shortBio.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли член команды email.
     *
     * @return true если email не пустой, иначе false
     */
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли член команды телефон.
     *
     * @return true если phone не пустой, иначе false
     */
    public boolean hasPhone() {
        return phone != null && !phone.trim().isEmpty();
    }

    /**
     * Проверяет, имеет ли член команды социальные ссылки.
     *
     * @return true если есть хотя бы одна социальная ссылка, иначе false
     */
    public boolean hasSocialLinks() {
        return socialLinks != null && !socialLinks.isEmpty();
    }

    /**
     * Проверяет, участвует ли член команды в каком-либо проекте.
     *
     * @return true если projectsCount > 0, иначе false
     */
    public boolean hasProjects() {
        return projectsCount != null && projectsCount > 0;
    }

    /**
     * Проверяет, является ли член команды активным.
     *
     * @return true если active = true, иначе false
     */
    public boolean isMemberActive() {
        return active;
    }

    /**
     * Получает URL аватарки для отображения.
     * Если аватарка отсутствует, возвращает null.
     *
     * @return URL аватарки или null
     */
    public String getAvatarUrl() {
        if (!hasAvatar()) {
            return null;
        }
        // Здесь можно добавить логику для преобразования пути в полный URL
        // Например, добавление префикса /uploads/
        return avatarPath.startsWith("/") ? avatarPath : "/uploads/" + avatarPath;
    }

    /**
     * Получает первую социальную ссылку для отображения.
     * Приоритет: Facebook → Instagram → VK → Telegram → первая доступная.
     *
     * @return Map.Entry с названием сети и ссылкой, или null
     */
    public Map.Entry<String, String> getPrimarySocialLink() {
        if (!hasSocialLinks()) {
            return null;
        }

        // Проверяем в порядке приоритета
        if (socialLinks.containsKey("facebook")) {
            return new java.util.AbstractMap.SimpleEntry<>("facebook", socialLinks.get("facebook"));
        }
        if (socialLinks.containsKey("instagram")) {
            return new java.util.AbstractMap.SimpleEntry<>("instagram", socialLinks.get("instagram"));
        }
        if (socialLinks.containsKey("vk")) {
            return new java.util.AbstractMap.SimpleEntry<>("vk", socialLinks.get("vk"));
        }
        if (socialLinks.containsKey("telegram")) {
            return new java.util.AbstractMap.SimpleEntry<>("telegram", socialLinks.get("telegram"));
        }

        // Возвращаем первую доступную ссылку
        return socialLinks.entrySet().iterator().next();
    }

    /**
     * Получает CSS класс для цвета аватарки по умолчанию.
     * Используется, когда нет реальной фотографии.
     *
     * @return CSS класс цвета
     */
    public String getAvatarColorClass() {
        if (initials == null || initials.isEmpty()) {
            return "avatar-color-1";
        }

        // Генерируем детерминированный цвет на основе инициалов
        int hash = initials.hashCode();
        int colorIndex = Math.abs(hash % 8) + 1; // 8 различных цветов
        return "avatar-color-" + colorIndex;
    }

    /**
     * Получает подсказку для email (скрывает часть для защиты от спама).
     *
     * @return замаскированный email или оригинальный если не нужно маскировать
     */
    public String getEmailHint() {
        if (!hasEmail()) {
            return null;
        }

        // Простая маскировка: user@domain.com → use***@domain.com
        int atIndex = email.indexOf('@');
        if (atIndex > 3) {
            String localPart = email.substring(0, atIndex);
            String domain = email.substring(atIndex);
            String maskedLocal = localPart.substring(0, 3) + "***";
            return maskedLocal + domain;
        }

        return email;
    }

    // ================== ПРИВАТНЫЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Создает короткую биографию из полной.
     * Удаляет HTML теги и обрезает до 200 символов.
     *
     * @param bio полная биография
     * @return короткая биография
     */
    private String createShortBio(String bio) {
        if (bio == null || bio.trim().isEmpty()) {
            return "";
        }

        // Удаляем HTML теги
        String plainBio = bio.replaceAll("<[^>]*>", "").trim();

        // Обрезаем до 200 символов
        if (plainBio.length() > 200) {
            return plainBio.substring(0, 197) + "...";
        }

        return plainBio;
    }

    /**
     * Форматирует дату и время для отображения.
     *
     * @param dateTime дата и время
     * @return отформатированная строка
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        try {
            // Используем стандартный формат даты
            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            return dateTime.format(formatter);
        } catch (Exception e) {
            return dateTime.toString();
        }
    }

    @Override
    public String toString() {
        return "TeamMemberDTO{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", position='" + position + '\'' +
                ", active=" + active +
                ", projectsCount=" + projectsCount +
                '}';
    }
}

package com.community.cms.model.project;

/**
 * Перечисление статусов проектов организации "ЛАДА".
 * Определяет текущее состояние проекта в жизненном цикле.
 *
 * @author Vasickin
 * @since 1.0
 */
public enum ProjectStatus {

    /**
     * Активный проект - в настоящее время проходит
     * или готовится к проведению
     */
    ACTIVE("Активный", "active", "success"),

    /**
     * Ежегодный проект - проводится регулярно каждый год,
     * в настоящее время может быть не активен
     */
    ANNUAL("Ежегодный", "annual", "info"),

    /**
     * Планируемый проект - находится в стадии планирования,
     * даты проведения еще не определены
     */
    PLANNED("Планируется", "planned", "warning"),

    /**
     * Архивный проект - мероприятие уже прошло
     * и больше не проводится
     */
    ARCHIVE("Архивный", "archive", "secondary"),

    /**
     * Проект на паузе - временно не проводится,
     * но может быть возобновлен
     */
    PAUSED("На паузе", "paused", "secondary"),

    /**
     * Отмененный проект - мероприятие отменено
     * и не будет проводиться
     */
    CANCELLED("Отменен", "cancelled", "danger");

    private final String displayName;
    private final String slug;
    private final String bootstrapColor;

    /**
     * Конструктор статуса проекта
     *
     * @param displayName Отображаемое название на русском
     * @param slug URL-идентификатор статуса
     * @param bootstrapColor Цвет Bootstrap для отображения бейджа
     */
    ProjectStatus(String displayName, String slug, String bootstrapColor) {
        this.displayName = displayName;
        this.slug = slug;
        this.bootstrapColor = bootstrapColor;
    }

    /**
     * Получить отображаемое название статуса
     *
     * @return Название статуса на русском языке
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Получить URL-идентификатор статуса
     *
     * @return URL-совместимый идентификатор
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Получить цвет Bootstrap для отображения бейджа
     *
     * @return Название класса цвета Bootstrap (success, info, warning и т.д.)
     */
    public String getBootstrapColor() {
        return bootstrapColor;
    }

    /**
     * Получить CSS класс для стилизации бейджа
     *
     * @return Полный CSS класс для бейджа (например, "badge bg-success")
     */
    public String getBadgeClass() {
        return "badge bg-" + bootstrapColor;
    }

    /**
     * Проверить, является ли статус активным (проект сейчас проводится)
     *
     * @return true, если статус ACTIVE
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Проверить, является ли статус архивным (проект завершен)
     *
     * @return true, если статус ARCHIVE
     */
    public boolean isArchive() {
        return this == ARCHIVE;
    }

    /**
     * Проверить, виден ли проект обычным пользователям
     * (не отмененные проекты видны)
     *
     * @return true, если проект должен отображаться на сайте
     */
    public boolean isVisible() {
        return this != CANCELLED;
    }

    /**
     * Проверить, является ли статус планируемым
     *
     * @return true, если статус PLANNED
     */
    public boolean isPlanned() {
        return this == PLANNED;
    }

    /**
     * Поиск статуса по URL-идентификатору
     *
     * @param slug URL-идентификатор статуса
     * @return Объект статуса или null, если не найден
     */
    public static ProjectStatus fromSlug(String slug) {
        if (slug == null) {
            return null;
        }
        for (ProjectStatus status : values()) {
            if (status.getSlug().equalsIgnoreCase(slug)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Поиск статуса по отображаемому имени
     *
     * @param displayName Отображаемое имя на русском
     * @return Объект статуса или null, если не найден
     */
    public static ProjectStatus fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }
        for (ProjectStatus status : values()) {
            if (status.getDisplayName().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Получить массив статусов, которые отображаются на сайте
     * (все, кроме CANCELLED)
     *
     * @return Массив видимых статусов
     */
    public static ProjectStatus[] getVisibleStatuses() {
        return new ProjectStatus[]{ACTIVE, ANNUAL, PLANNED, ARCHIVE, PAUSED};
    }

    /**
     * Получить массив статусов для фильтрации в админке
     * (все статусы)
     *
     * @return Массив всех статусов
     */
    public static ProjectStatus[] getAllStatuses() {
        return values();
    }

    @Override
    public String toString() {
        return displayName;
    }
}

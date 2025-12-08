package com.community.cms.model.project;

/**
 * Перечисление категорий проектов организации "ЛАДА".
 * Определяет тип мероприятия или активности проекта.
 *
 * @author Vasickin
 * @since 1.0
 */
public enum ProjectCategory {

    /**
     * Фестивали - масштабные мероприятия с участием
     * множества коллективов и гостей
     */
    FESTIVAL("Фестиваль", "festival"),

    /**
     * Конкурсы - соревновательные мероприятия
     * с оценкой участников жюри
     */
    CONTEST("Конкурс", "contest"),

    /**
     * Благотворительные акции - мероприятия
     * для сбора помощи нуждающимся
     */
    CHARITY("Благотворительность", "charity"),

    /**
     * Театральные постановки и спектакли
     */
    THEATER("Спектакль", "theater"),

    /**
     * Музыкальные мероприятия и концерты
     */
    MUSIC("Музыкальный вечер", "music"),

    /**
     * Литературные мероприятия
     */
    LITERARY("Литературный бал", "literary"),

    /**
     * Выставки и экспозиции
     */
    EXHIBITION("Выставка", "exhibition"),

    /**
     * Детские праздники и мероприятия
     */
    CHILDREN("Детский праздник", "children"),

    /**
     * Традиционные народные праздники
     */
    FOLK("Народный праздник", "folk"),

    /**
     * Международные мероприятия
     */
    INTERNATIONAL("Международный", "international"),

    /**
     * Прочие мероприятия, не вошедшие в основные категории
     */
    OTHER("Другое", "other");

    private final String displayName;
    private final String slug;

    /**
     * Конструктор категории проекта
     *
     * @param displayName Отображаемое название на русском
     * @param slug URL-идентификатор категории
     */
    ProjectCategory(String displayName, String slug) {
        this.displayName = displayName;
        this.slug = slug;
    }

    /**
     * Получить отображаемое название категории
     *
     * @return Название категории на русском языке
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Получить URL-идентификатор категории
     *
     * @return URL-совместимый идентификатор
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Поиск категории по URL-идентификатору
     *
     * @param slug URL-идентификатор категории
     * @return Объект категории или null, если не найден
     */
    public static ProjectCategory fromSlug(String slug) {
        for (ProjectCategory category : values()) {
            if (category.getSlug().equalsIgnoreCase(slug)) {
                return category;
            }
        }
        return null;
    }

    /**
     * Поиск категории по отображаемому имени
     *
     * @param displayName Отображаемое имя на русском
     * @return Объект категории или null, если не найден
     */
    public static ProjectCategory fromDisplayName(String displayName) {
        for (ProjectCategory category : values()) {
            if (category.getDisplayName().equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        return null;
    }

    /**
     * Получить массив всех отображаемых названий категорий
     *
     * @return Массив названий на русском языке
     */
    public static String[] getAllDisplayNames() {
        ProjectCategory[] categories = values();
        String[] names = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            names[i] = categories[i].getDisplayName();
        }
        return names;
    }

    /**
     * Проверить, является ли категория активным типом мероприятия
     * (не OTHER)
     *
     * @return true, если это основная категория, false для OTHER
     */
    public boolean isMainCategory() {
        return this != OTHER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
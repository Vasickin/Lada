package com.community.cms.web.mvc.dto.people;

import com.community.cms.domain.model.people.Partner;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) для формы создания и редактирования партнера проекта.
 *
 * <p>Используется в административной панели для управления партнерами проектов.
 * Поддерживает различные типы партнерства (спонсоры, информационные партнеры и т.д.).</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see Partner
 */
public class PartnerForm {

    private Long id;

    /**
     * Название партнерской организации.
     * Обязательное поле, от 2 до 255 символов.
     */
    @NotBlank(message = "Название партнера обязательно")
    @Size(min = 2, max = 255, message = "Название должно быть от 2 до 255 символов")
    private String name;

    /**
     * Описание партнера (опционально).
     * Краткая информация о партнере и его участии в проекте.
     */
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    /**
     * Тип партнерства.
     * Определяет категорию участия партнера в проекте.
     */
    @NotNull(message = "Тип партнерства обязателен")
    private Partner.PartnerType partnerType = Partner.PartnerType.OTHER;

    /**
     * Путь к логотипу партнера.
     * Отображается на странице проекта.
     */
    private String logoPath;

    /**
     * URL сайта партнера.
     * Должен быть валидным URL (начинаться с http:// или https://).
     */
    @Pattern(
            regexp = "^(https?://)?([\\w\\-]+\\.)+[\\w\\-]{2,}(/.*)?$",
            message = "Некорректный формат URL"
    )
    @Size(max = 500, message = "URL не должен превышать 500 символов")
    private String websiteUrl;

    /**
     * Контактный email партнера (опционально).
     */
    @Size(max = 100, message = "Email не должен превышать 100 символов")
    private String contactEmail;

    /**
     * Контактный телефон партнера (опционально).
     */
    @Size(max = 50, message = "Телефон не должен превышать 50 символов")
    private String contactPhone;

    /**
     * Контактное лицо от партнера (опционально).
     */
    @Size(max = 100, message = "Контактное лицо не должно превышать 100 символов")
    private String contactPerson;

    /**
     * ID проекта, к которому относится партнер.
     * Используется для привязки партнера к проекту.
     */
    @NotNull(message = "Проект обязателен")
    private Long projectId;

    /**
     * Порядок сортировки в списке партнеров.
     * Меньшее значение = выше в списке.
     */
    @NotNull(message = "Порядок сортировки обязателен")
    private Integer sortOrder = 0;

    /**
     * Флаг активности партнера.
     * Неактивные партнеры не отображаются на сайте.
     */
    private boolean active = true;

    // ================== КОНСТРУКТОРЫ ==================

    /**
     * Конструктор по умолчанию.
     * Инициализирует значения по умолчанию.
     */
    public PartnerForm() {
        this.partnerType = Partner.PartnerType.OTHER;
        this.sortOrder = 0;
        this.active = true;
    }

    /**
     * Конструктор на основе существующего партнера проекта.
     * Используется для редактирования партнера.
     *
     * @param partner существующий партнер проекта
     */
    public PartnerForm(Partner partner) {
        this();
        this.id = partner.getId();
        this.name = partner.getName();
        this.description = partner.getDescription();
        this.partnerType = partner.getPartnerType();
        this.logoPath = partner.getLogoPath();
        this.websiteUrl = partner.getWebsiteUrl();
        this.contactEmail = partner.getContactEmail();
        this.contactPhone = partner.getContactPhone();
        this.contactPerson = partner.getContactPerson();
        this.projectId = partner.getProject() != null ? partner.getProject().getId() : null;
        this.sortOrder = partner.getSortOrder();
        this.active = partner.isActive();
    }

    // ================== ГЕТТЕРЫ И СЕТТЕРЫ ==================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Partner.PartnerType getPartnerType() {
        return partnerType;
    }

    public void setPartnerType(Partner.PartnerType partnerType) {
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
        return partnerType != null ? partnerType.getNameRu() : "";
    }

    /**
     * Получает отображаемое имя типа партнерства на английском.
     *
     * @return английское название типа партнерства
     */
    public String getPartnerTypeDisplayNameEn() {
        return partnerType != null ? partnerType.getNameEn() : "";
    }

    /**
     * Проверяет является ли партнер спонсором.
     *
     * @return true если partnerType == SPONSOR
     */
    public boolean isSponsor() {
        return partnerType == Partner.PartnerType.SPONSOR;
    }

    /**
     * Проверяет является ли партнер информационным партнером.
     *
     * @return true если partnerType == INFORMATION_PARTNER
     */
    public boolean isInformationPartner() {
        return partnerType == Partner.PartnerType.INFORMATION_PARTNER;
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
     * Преобразует PartnerForm в сущность Partner.
     * Проект не устанавливается (только projectId).
     *
     * @return сущность Partner с заполненными базовыми полями
     */
    public Partner toEntity() {
        Partner partner = new Partner();
        partner.setId(this.id);
        partner.setName(this.name);
        partner.setDescription(this.description);
        partner.setPartnerType(this.partnerType);
        partner.setLogoPath(this.logoPath);
        partner.setWebsiteUrl(this.websiteUrl);
        partner.setContactEmail(this.contactEmail);
        partner.setContactPhone(this.contactPhone);
        partner.setContactPerson(this.contactPerson);
        partner.setSortOrder(this.sortOrder);
        partner.setActive(this.active);

        // Проект устанавливается отдельно в сервисе по projectId

        return partner;
    }

    /**
     * Обновляет существующую сущность Partner данными из формы.
     *
     * @param partner сущность для обновления
     */
    public void updateEntity(Partner partner) {
        partner.setName(this.name);
        partner.setDescription(this.description);
        partner.setPartnerType(this.partnerType);
        partner.setLogoPath(this.logoPath);
        partner.setWebsiteUrl(this.websiteUrl);
        partner.setContactEmail(this.contactEmail);
        partner.setContactPhone(this.contactPhone);
        partner.setContactPerson(this.contactPerson);
        partner.setSortOrder(this.sortOrder);
        partner.setActive(this.active);

        // Проект обновляется отдельно в сервисе по projectId
    }

    @Override
    public String toString() {
        return "PartnerForm{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", partnerType=" + partnerType +
                ", active=" + active +
                ", projectId=" + projectId +
                '}';
    }
}

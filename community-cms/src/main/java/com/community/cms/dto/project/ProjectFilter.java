package com.community.cms.dto.project;

import com.community.cms.model.project.ProjectCategory;
import com.community.cms.model.project.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

//Описание файла:
//DTO для фильтрации проектов с множеством опциональных параметров
//Поддержка фильтрации по категории, статусу, датам, наличию контента
//Поиск по тексту в названии и описании
//Фильтры активности (активные, предстоящие, завершенные, ежегодные)
//Фильтрация по местоположению
//Минимальные счетчики контента (фото, видео, партнеры, команда)
//Параметры сортировки для использования с Pageable
//Вспомогательные методы для проверки применения различных фильтров
//Метод для создания JPA Specification на основе фильтра
//Проверка корректности диапазона дат
//Методы для преобразования параметров в формат Spring Data
//Аннотации Jackson для корректной сериализации/десериализации
//Полная JavaDoc документация

/**
 * Data Transfer Object для фильтрации проектов.
 * Используется для передачи параметров фильтрации при поиске проектов.
 * Все поля опциональны - фильтрация применяется только по указанным полям.
 *
 * @author Vasickin
 * @since 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectFilter {

    private ProjectCategory category;
    private ProjectStatus status;
    private String search;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;

    private Boolean hasPhotos;
    private Boolean hasVideos;
    private Boolean hasPartners;
    private Boolean hasTeam;

    private Boolean isActive;
    private Boolean isUpcoming;
    private Boolean isCompleted;
    private Boolean isAnnual;

    private String location;
    private Boolean showOnlyWithLocation;

    private Integer minPhotoCount;
    private Integer minVideoCount;
    private Integer minPartnerCount;
    private Integer minTeamCount;

    // Сортировка (можно использовать в сочетании с Pageable)
    private String sortBy = "startDate";
    private String sortDirection = "DESC";

    // === КОНСТРУКТОРЫ ===

    /**
     * Конструктор по умолчанию
     */
    public ProjectFilter() {
    }

    /**
     * Конструктор с основными параметрами фильтрации
     */
    public ProjectFilter(ProjectCategory category, ProjectStatus status, String search) {
        this.category = category;
        this.status = status;
        this.search = search;
    }

    // === ГЕТТЕРЫ И СЕТТЕРЫ ===

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

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public Boolean getHasPhotos() {
        return hasPhotos;
    }

    public void setHasPhotos(Boolean hasPhotos) {
        this.hasPhotos = hasPhotos;
    }

    public Boolean getHasVideos() {
        return hasVideos;
    }

    public void setHasVideos(Boolean hasVideos) {
        this.hasVideos = hasVideos;
    }

    public Boolean getHasPartners() {
        return hasPartners;
    }

    public void setHasPartners(Boolean hasPartners) {
        this.hasPartners = hasPartners;
    }

    public Boolean getHasTeam() {
        return hasTeam;
    }

    public void setHasTeam(Boolean hasTeam) {
        this.hasTeam = hasTeam;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsUpcoming() {
        return isUpcoming;
    }

    public void setIsUpcoming(Boolean isUpcoming) {
        this.isUpcoming = isUpcoming;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Boolean getIsAnnual() {
        return isAnnual;
    }

    public void setIsAnnual(Boolean isAnnual) {
        this.isAnnual = isAnnual;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getShowOnlyWithLocation() {
        return showOnlyWithLocation;
    }

    public void setShowOnlyWithLocation(Boolean showOnlyWithLocation) {
        this.showOnlyWithLocation = showOnlyWithLocation;
    }

    public Integer getMinPhotoCount() {
        return minPhotoCount;
    }

    public void setMinPhotoCount(Integer minPhotoCount) {
        this.minPhotoCount = minPhotoCount;
    }

    public Integer getMinVideoCount() {
        return minVideoCount;
    }

    public void setMinVideoCount(Integer minVideoCount) {
        this.minVideoCount = minVideoCount;
    }

    public Integer getMinPartnerCount() {
        return minPartnerCount;
    }

    public void setMinPartnerCount(Integer minPartnerCount) {
        this.minPartnerCount = minPartnerCount;
    }

    public Integer getMinTeamCount() {
        return minTeamCount;
    }

    public void setMinTeamCount(Integer minTeamCount) {
        this.minTeamCount = minTeamCount;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    /**
     * Проверить, применяется ли фильтр по категории
     *
     * @return true, если категория указана
     */
    public boolean hasCategoryFilter() {
        return category != null;
    }

    /**
     * Проверить, применяется ли фильтр по статусу
     *
     * @return true, если статус указан
     */
    public boolean hasStatusFilter() {
        return status != null;
    }

    /**
     * Проверить, применяется ли поисковый фильтр
     *
     * @return true, если поисковый запрос указан и не пустой
     */
    public boolean hasSearchFilter() {
        return search != null && !search.trim().isEmpty();
    }

    /**
     * Проверить, применяется ли фильтр по датам
     *
     * @return true, если указана хотя бы одна дата
     */
    public boolean hasDateFilter() {
        return dateFrom != null || dateTo != null;
    }

    /**
     * Проверить, применяется ли фильтр по наличию контента
     *
     * @return true, если указан хотя бы один фильтр наличия
     */
    public boolean hasContentFilter() {
        return hasPhotos != null || hasVideos != null ||
                hasPartners != null || hasTeam != null;
    }

    /**
     * Проверить, применяется ли фильтр по активности
     *
     * @return true, если указан фильтр активности, предстоящих или завершенных проектов
     */
    public boolean hasActivityFilter() {
        return isActive != null || isUpcoming != null ||
                isCompleted != null || isAnnual != null;
    }

    /**
     * Проверить, применяется ли фильтр по местоположению
     *
     * @return true, если указано местоположение или флаг показа только с местоположением
     */
    public boolean hasLocationFilter() {
        return location != null || showOnlyWithLocation != null;
    }

    /**
     * Проверить, применяется ли фильтр по минимальному количеству
     *
     * @return true, если указан хотя бы один минимальный счетчик
     */
    public boolean hasMinCountFilter() {
        return minPhotoCount != null || minVideoCount != null ||
                minPartnerCount != null || minTeamCount != null;
    }

    /**
     * Проверить, является ли фильтр сложным (содержит несколько условий)
     *
     * @return true, если применено более одного фильтра
     */
    public boolean isComplexFilter() {
        int filterCount = 0;
        if (hasCategoryFilter()) filterCount++;
        if (hasStatusFilter()) filterCount++;
        if (hasSearchFilter()) filterCount++;
        if (hasDateFilter()) filterCount++;
        if (hasContentFilter()) filterCount++;
        if (hasActivityFilter()) filterCount++;
        if (hasLocationFilter()) filterCount++;
        if (hasMinCountFilter()) filterCount++;

        return filterCount > 1;
    }

    /**
     * Проверить, является ли фильтр пустым (не содержит условий)
     *
     * @return true, если не применено ни одного фильтра
     */
    public boolean isEmpty() {
        return !hasCategoryFilter() && !hasStatusFilter() && !hasSearchFilter() &&
                !hasDateFilter() && !hasContentFilter() && !hasActivityFilter() &&
                !hasLocationFilter() && !hasMinCountFilter();
    }

    /**
     * Получить статус проекта на основе фильтров активности
     *
     * @return ProjectStatus или null, если статус не может быть определен по активности
     */
    public ProjectStatus getStatusFromActivityFilters() {
        if (isActive != null && isActive) {
            return ProjectStatus.ACTIVE;
        } else if (isUpcoming != null && isUpcoming) {
            return ProjectStatus.PLANNED;
        } else if (isCompleted != null && isCompleted) {
            return ProjectStatus.ARCHIVE;
        } else if (isAnnual != null && isAnnual) {
            return ProjectStatus.ANNUAL;
        }
        return null;
    }

    /**
     * Получить отформатированную строку поиска для использования в запросах
     *
     * @return Поисковая строка или null
     */
    public String getFormattedSearch() {
        if (!hasSearchFilter()) {
            return null;
        }
        return search.trim();
    }

    /**
     * Проверить корректность диапазона дат
     *
     * @return true, если диапазон дат корректен (from <= to) или не указан полностью
     */
    public boolean isDateRangeValid() {
        if (dateFrom == null || dateTo == null) {
            return true; // Если одна из дат не указана, считаем валидным
        }
        return !dateFrom.isAfter(dateTo);
    }

    /**
     * Получить SQL-подобное условие для местоположения
     *
     * @return LIKE условие для местоположения или null
     */
    public String getLocationLikeCondition() {
        if (location == null || location.trim().isEmpty()) {
            return null;
        }
        return "%" + location.trim() + "%";
    }

    /**
     * Получить направление сортировки в формате Spring Data
     *
     * @return org.springframework.data.domain.Sort.Direction
     */
    public org.springframework.data.domain.Sort.Direction getSpringSortDirection() {
        if ("ASC".equalsIgnoreCase(sortDirection)) {
            return org.springframework.data.domain.Sort.Direction.ASC;
        }
        return org.springframework.data.domain.Sort.Direction.DESC;
    }

    /**
     * Получить поле для сортировки в формате Spring Data
     *
     * @return Название поля для сортировки
     */
    public String getSpringSortField() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "startDate";
        }

        // Маппинг пользовательских названий полей на реальные имена полей сущности
        switch (sortBy.toLowerCase()) {
            case "title":
                return "title";
            case "category":
                return "category";
            case "status":
                return "status";
            case "location":
                return "location";
            case "created":
            case "createdat":
                return "createdAt";
            case "updated":
            case "updatedat":
                return "updatedAt";
            case "startdate":
            case "start":
                return "startDate";
            case "enddate":
            case "end":
                return "endDate";
            default:
                return "startDate";
        }
    }

    /**
     * Создать спецификацию для фильтрации (для использования в JPA Specifications)
     *
     * @return Спецификация фильтрации
     */
    public org.springframework.data.jpa.domain.Specification<com.community.cms.model.project.Project> toSpecification() {
        return (root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // Фильтр по категории
            if (hasCategoryFilter()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            // Фильтр по статусу
            if (hasStatusFilter()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Фильтр по поиску
            if (hasSearchFilter()) {
                String searchPattern = "%" + getFormattedSearch().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("shortDescription")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullDescription")), searchPattern)
                ));
            }

            // Фильтр по датам
            if (dateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), dateTo));
            }

            // Фильтр по местоположению
            if (location != null && !location.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("location")),
                        "%" + location.trim().toLowerCase() + "%"));
            }

            // Фильтр "только с местоположением"
            if (showOnlyWithLocation != null && showOnlyWithLocation) {
                predicates.add(criteriaBuilder.isNotNull(root.get("location")));
                predicates.add(criteriaBuilder.notEqual(root.get("location"), ""));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    @Override
    public String toString() {
        return "ProjectFilter{" +
                "category=" + category +
                ", status=" + status +
                ", search='" + search + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", isComplexFilter=" + isComplexFilter() +
                '}';
    }
}

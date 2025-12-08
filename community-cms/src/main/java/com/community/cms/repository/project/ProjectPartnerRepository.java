package com.community.cms.repository.project;

import com.community.cms.model.project.ProjectPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


//Описание файла:
//Репозиторий для работы с сущностью ProjectPartner
//Методы для поиска партнеров по проекту, активности, типу партнерства
//Проверка на уникальность названия партнера в рамках проекта
//Статистические методы (количество партнеров, спонсоров, медиа-партнеров)
//Методы с JOIN FETCH для загрузки связанного проекта
//Поиск по названию партнера
//Методы для управления порядком сортировки и главным партнером
//Поиск партнеров с логотипом, веб-сайтом, контактной информацией
//Методы для удаления партнеров по проекту

/**
 * Репозиторий для работы с сущностью {@link ProjectPartner}.
 * Предоставляет методы для доступа к данным партнеров проектов организации "ЛАДА".
 *
 * @author Vasickin
 * @since 1.0
 */
@Repository
public interface ProjectPartnerRepository extends JpaRepository<ProjectPartner, Long> {

    /**
     * Найти всех партнеров указанного проекта
     *
     * @param projectId ID проекта
     * @return Список партнеров проекта, отсортированный по порядку
     */
    List<ProjectPartner> findByProjectIdOrderBySortOrderAsc(Long projectId);

    /**
     * Найти активных партнеров указанного проекта
     *
     * @param projectId ID проекта
     * @return Список активных партнеров проекта
     */
    List<ProjectPartner> findByProjectIdAndIsActiveTrueOrderBySortOrderAsc(Long projectId);

    /**
     * Найти главных партнеров проекта (isMain = true)
     *
     * @param projectId ID проекта
     * @return Список главных партнеров проекта
     */
    List<ProjectPartner> findByProjectIdAndIsMainTrueOrderBySortOrderAsc(Long projectId);

    /**
     * Найти партнера по названию (без учета регистра) в рамках проекта
     *
     * @param projectId ID проекта
     * @param name Название партнера
     * @return Optional с найденным партнером или пустой Optional
     */
    Optional<ProjectPartner> findByProjectIdAndNameIgnoreCase(Long projectId, String name);

    /**
     * Проверить существование партнера с указанным названием в проекте
     *
     * @param projectId ID проекта
     * @param name Название партнера
     * @return true, если партнер с таким названием уже существует в проекте
     */
    boolean existsByProjectIdAndNameIgnoreCase(Long projectId, String name);

    /**
     * Найти партнеров по типу партнерства
     *
     * @param partnershipType Тип партнерства
     * @return Список партнеров указанного типа
     */
    List<ProjectPartner> findByPartnershipType(ProjectPartner.PartnershipType partnershipType);

    /**
     * Найти партнеров по типу партнерства в рамках проекта
     *
     * @param projectId ID проекта
     * @param partnershipType Тип партнерства
     * @return Список партнеров проекта указанного типа
     */
    List<ProjectPartner> findByProjectIdAndPartnershipType(Long projectId, ProjectPartner.PartnershipType partnershipType);

    /**
     * Получить количество партнеров в проекте
     *
     * @param projectId ID проекта
     * @return Количество партнеров в проекте
     */
    long countByProjectId(Long projectId);

    /**
     * Получить количество активных партнеров в проекте
     *
     * @param projectId ID проекта
     * @return Количество активных партнеров в проекте
     */
    long countByProjectIdAndIsActiveTrue(Long projectId);

    /**
     * Получить количество партнеров по типу партнерства
     *
     * @param partnershipType Тип партнерства
     * @return Количество партнеров указанного типа
     */
    long countByPartnershipType(ProjectPartner.PartnershipType partnershipType);

    /**
     * Найти партнера по ID с загрузкой связанного проекта
     *
     * @param id ID партнера
     * @return Optional с найденным партнером или пустой Optional
     */
    @Query("SELECT p FROM ProjectPartner p JOIN FETCH p.project WHERE p.id = :id")
    Optional<ProjectPartner> findByIdWithProject(@Param("id") Long id);

    /**
     * Найти всех партнеров с загрузкой связанных проектов
     *
     * @return Список всех партнеров с загруженными проектами
     */
    @Query("SELECT p FROM ProjectPartner p JOIN FETCH p.project")
    List<ProjectPartner> findAllWithProjects();

    /**
     * Найти партнеров по ID проекта с загрузкой проекта
     *
     * @param projectId ID проекта
     * @return Список партнеров проекта с загруженным проектом
     */
    @Query("SELECT p FROM ProjectPartner p JOIN FETCH p.project WHERE p.project.id = :projectId ORDER BY p.sortOrder ASC")
    List<ProjectPartner> findByProjectIdWithProject(@Param("projectId") Long projectId);

    /**
     * Найти партнеров, у которых есть логотип
     *
     * @return Список партнеров с загруженным логотипом
     */
    @Query("SELECT p FROM ProjectPartner p WHERE p.logo IS NOT NULL")
    List<ProjectPartner> findPartnersWithLogo();

    /**
     * Найти партнеров, у которых указан веб-сайт
     *
     * @return Список партнеров с указанным веб-сайтом
     */
    @Query("SELECT p FROM ProjectPartner p WHERE p.websiteUrl IS NOT NULL AND p.websiteUrl <> ''")
    List<ProjectPartner> findPartnersWithWebsite();

    /**
     * Найти партнеров по домену веб-сайта
     *
     * @param domain Домен веб-сайта
     * @return Список партнеров, чей веб-сайт содержит указанный домен
     */
    @Query("SELECT p FROM ProjectPartner p WHERE p.websiteUrl LIKE %:domain%")
    List<ProjectPartner> findByWebsiteDomain(@Param("domain") String domain);

    /**
     * Удалить всех партнеров указанного проекта
     *
     * @param projectId ID проекта
     */
    void deleteByProjectId(Long projectId);

    /**
     * Найти партнеров по части названия (без учета регистра)
     *
     * @param namePart Часть названия партнера
     * @return Список партнеров, название которых содержит указанную строку
     */
    @Query("SELECT p FROM ProjectPartner p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    List<ProjectPartner> findByNameContainingIgnoreCase(@Param("namePart") String namePart);

    /**
     * Найти партнеров по части названия в указанном проекте
     *
     * @param projectId ID проекта
     * @param namePart Часть названия партнера
     * @return Список партнеров проекта, название которых содержит указанную строку
     */
    @Query("SELECT p FROM ProjectPartner p WHERE p.project.id = :projectId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    List<ProjectPartner> findByProjectIdAndNameContainingIgnoreCase(@Param("projectId") Long projectId,
                                                                    @Param("namePart") String namePart);

    /**
     * Найти партнеров, которые являются спонсорами
     *
     * @return Список партнеров-спонсоров
     */
    @Query("SELECT p FROM ProjectPartner p WHERE p.partnershipType IN ('GENERAL_SPONSOR', 'SPONSOR')")
    List<ProjectPartner> findSponsors();

    /**
     * Найти партнеров, которые являются медиа-партнерами
     *
     * @return Список медиа-партнеров
     */
    @Query("SELECT p FROM ProjectPartner p WHERE p.partnershipType IN ('MEDIA_PARTNER', 'INFO_PARTNER')")
    List<ProjectPartner> findMediaPartners();

    /**
     * Найти партнеров, которые являются техническими партнерами
     *
     * @return Список технических партнеров
     */
    @Query("SELECT p FROM ProjectPartner p WHERE p.partnershipType = 'TECH_PARTNER'")
    List<ProjectPartner> findTechPartners();

    /**
     * Обновить порядок сортировки партнера
     *
     * @param partnerId ID партнера
     * @param newSortOrder Новый порядок сортировки
     */
    @Query("UPDATE ProjectPartner p SET p.sortOrder = :newSortOrder WHERE p.id = :partnerId")
    void updateSortOrder(@Param("partnerId") Long partnerId, @Param("newSortOrder") Integer newSortOrder);

    /**
     * Сделать указанного партнера главным, сбросив флаг isMain у остальных партнеров проекта
     *
     * @param partnerId ID партнера, который должен стать главным
     * @param projectId ID проекта
     */
    @Query("UPDATE ProjectPartner p SET p.isMain = CASE WHEN p.id = :partnerId THEN true ELSE false END WHERE p.project.id = :projectId")
    void setMainPartner(@Param("partnerId") Long partnerId, @Param("projectId") Long projectId);

    /**
     * Найти партнеров с контактной информацией (email или телефон)
     *
     * @return Список партнеров с указанным email или телефоном
     */
    @Query("SELECT p FROM ProjectPartner p WHERE (p.email IS NOT NULL AND p.email <> '') OR (p.phone IS NOT NULL AND p.phone <> '')")
    List<ProjectPartner> findPartnersWithContactInfo();

    /**
     * Найти дубликаты партнеров в проекте (одинаковые названия)
     *
     * @param projectId ID проекта
     * @return Список дублирующихся партнеров
     */
    @Query("SELECT p FROM ProjectPartner p WHERE p.project.id = :projectId AND " +
            "LOWER(p.name) IN (SELECT LOWER(p2.name) FROM ProjectPartner p2 WHERE p2.project.id = :projectId GROUP BY LOWER(p2.name) HAVING COUNT(p2) > 1)")
    List<ProjectPartner> findDuplicatePartnersInProject(@Param("projectId") Long projectId);
}

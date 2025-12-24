package com.community.cms.repository.project;

import com.community.cms.domain.model.people.Partner;
import com.community.cms.model.project.Project;
import com.community.cms.domain.model.people.Partner.PartnerType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectPartnerRepository extends JpaRepository<Partner, Long> {

    // ================== ПОИСК ПО ПРОЕКТУ ==================

    List<Partner> findByProject(Project project);
    List<Partner> findByProjectOrderBySortOrderAsc(Project project);
    List<Partner> findByProjectOrderByNameAsc(Project project);

    // ================== ПОИСК ПО ТИПУ ПАРТНЕРСТВА ==================

    List<Partner> findByProjectAndPartnerType(Project project, PartnerType partnerType);
    List<Partner> findByProjectAndPartnerTypeOrderBySortOrderAsc(Project project, PartnerType partnerType);

    // ================== ПОИСК ПО АКТИВНОСТИ ==================

    List<Partner> findByProjectAndActiveTrue(Project project);
    List<Partner> findByProjectAndActiveTrueOrderBySortOrderAsc(Project project);
    List<Partner> findByProjectAndActiveFalse(Project project);

    // ================== ПОИСК ПО НАЗВАНИЮ И КОНТАКТАМ ==================

    List<Partner> findByNameContainingIgnoreCase(String name);
    List<Partner> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    List<Partner> findByContactEmail(String email);
    List<Partner> findByContactPersonContainingIgnoreCase(String contactPerson);

    // ================== ПОИСК ПО ЛОГОТИПУ И САЙТУ ==================

    List<Partner> findByLogoPathIsNotNull();
    List<Partner> findByLogoPathIsNull();
    List<Partner> findByWebsiteUrlIsNotNull();
    List<Partner> findByWebsiteUrlIsNull();
    boolean existsByWebsiteUrl(String websiteUrl);
    boolean existsByContactEmail(String email);

    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    long countByProject(Project project);
    long countByProjectAndActiveTrue(Project project);
    long countByProjectAndPartnerType(Project project, PartnerType partnerType);

    @Query("SELECT DISTINCT pp.partnerType FROM Partner pp WHERE pp.project = :project")
    List<PartnerType> findDistinctPartnerTypesByProject(@Param("project") Project project);

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    default List<Partner> findSponsorsByProject(Project project) {
        return findByProjectAndPartnerType(project, PartnerType.SPONSOR);
    }

    default List<Partner> findInformationPartnersByProject(Project project) {
        return findByProjectAndPartnerType(project, PartnerType.INFORMATION_PARTNER);
    }

    // Метод с Pageable вместо int limit
    @Query("SELECT pp FROM Partner pp WHERE pp.project = :project AND pp.active = true ORDER BY pp.sortOrder ASC")
    List<Partner> findFirstNByProject(@Param("project") Project project, Pageable pageable);

    // Удобный метод с limit
    default List<Partner> findFirstNByProject(Project project, int limit) {
        return findFirstNByProject(project, PageRequest.of(0, limit));
    }

    @Query("SELECT pp FROM Partner pp WHERE pp.project = :project AND " +
            "(pp.description IS NULL OR TRIM(pp.description) = '')")
    List<Partner> findWithoutDescriptionByProject(@Param("project") Project project);

    @Query("SELECT pp FROM Partner pp WHERE pp.project = :project AND " +
            "(pp.contactEmail IS NULL OR TRIM(pp.contactEmail) = '') AND " +
            "(pp.contactPhone IS NULL OR TRIM(pp.contactPhone) = '')")
    List<Partner> findWithoutContactInfoByProject(@Param("project") Project project);

    @Query("SELECT pp FROM Partner pp WHERE pp.name IN (" +
            "SELECT pp2.name FROM Partner pp2 GROUP BY pp2.name HAVING COUNT(DISTINCT pp2.project) >= :minProjects)")
    List<Partner> findPartnersInMultipleProjects(@Param("minProjects") int minProjects);

    // ================== УДАЛЕНИЕ ==================

    void deleteByProject(Project project);
    void deleteByProjectAndPartnerType(Project project, PartnerType partnerType);
}
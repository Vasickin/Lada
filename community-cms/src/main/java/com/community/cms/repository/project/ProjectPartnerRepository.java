package com.community.cms.repository.project;

import com.community.cms.model.project.Project;
import com.community.cms.model.project.ProjectPartner;
import com.community.cms.model.project.ProjectPartner.PartnerType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectPartnerRepository extends JpaRepository<ProjectPartner, Long> {

    // ================== ПОИСК ПО ПРОЕКТУ ==================

    List<ProjectPartner> findByProject(Project project);
    List<ProjectPartner> findByProjectOrderBySortOrderAsc(Project project);
    List<ProjectPartner> findByProjectOrderByNameAsc(Project project);

    // ================== ПОИСК ПО ТИПУ ПАРТНЕРСТВА ==================

    List<ProjectPartner> findByProjectAndPartnerType(Project project, PartnerType partnerType);
    List<ProjectPartner> findByProjectAndPartnerTypeOrderBySortOrderAsc(Project project, PartnerType partnerType);

    // ================== ПОИСК ПО АКТИВНОСТИ ==================

    List<ProjectPartner> findByProjectAndActiveTrue(Project project);
    List<ProjectPartner> findByProjectAndActiveTrueOrderBySortOrderAsc(Project project);
    List<ProjectPartner> findByProjectAndActiveFalse(Project project);

    // ================== ПОИСК ПО НАЗВАНИЮ И КОНТАКТАМ ==================

    List<ProjectPartner> findByNameContainingIgnoreCase(String name);
    List<ProjectPartner> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    List<ProjectPartner> findByContactEmail(String email);
    List<ProjectPartner> findByContactPersonContainingIgnoreCase(String contactPerson);

    // ================== ПОИСК ПО ЛОГОТИПУ И САЙТУ ==================

    List<ProjectPartner> findByLogoPathIsNotNull();
    List<ProjectPartner> findByLogoPathIsNull();
    List<ProjectPartner> findByWebsiteUrlIsNotNull();
    List<ProjectPartner> findByWebsiteUrlIsNull();
    boolean existsByWebsiteUrl(String websiteUrl);
    boolean existsByContactEmail(String email);

    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    long countByProject(Project project);
    long countByProjectAndActiveTrue(Project project);
    long countByProjectAndPartnerType(Project project, PartnerType partnerType);

    @Query("SELECT DISTINCT pp.partnerType FROM ProjectPartner pp WHERE pp.project = :project")
    List<PartnerType> findDistinctPartnerTypesByProject(@Param("project") Project project);

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    default List<ProjectPartner> findSponsorsByProject(Project project) {
        return findByProjectAndPartnerType(project, PartnerType.SPONSOR);
    }

    default List<ProjectPartner> findInformationPartnersByProject(Project project) {
        return findByProjectAndPartnerType(project, PartnerType.INFORMATION_PARTNER);
    }

    // Метод с Pageable вместо int limit
    @Query("SELECT pp FROM ProjectPartner pp WHERE pp.project = :project AND pp.active = true ORDER BY pp.sortOrder ASC")
    List<ProjectPartner> findFirstNByProject(@Param("project") Project project, Pageable pageable);

    // Удобный метод с limit
    default List<ProjectPartner> findFirstNByProject(Project project, int limit) {
        return findFirstNByProject(project, PageRequest.of(0, limit));
    }

    @Query("SELECT pp FROM ProjectPartner pp WHERE pp.project = :project AND " +
            "(pp.description IS NULL OR TRIM(pp.description) = '')")
    List<ProjectPartner> findWithoutDescriptionByProject(@Param("project") Project project);

    @Query("SELECT pp FROM ProjectPartner pp WHERE pp.project = :project AND " +
            "(pp.contactEmail IS NULL OR TRIM(pp.contactEmail) = '') AND " +
            "(pp.contactPhone IS NULL OR TRIM(pp.contactPhone) = '')")
    List<ProjectPartner> findWithoutContactInfoByProject(@Param("project") Project project);

    @Query("SELECT pp FROM ProjectPartner pp WHERE pp.name IN (" +
            "SELECT pp2.name FROM ProjectPartner pp2 GROUP BY pp2.name HAVING COUNT(DISTINCT pp2.project) >= :minProjects)")
    List<ProjectPartner> findPartnersInMultipleProjects(@Param("minProjects") int minProjects);

    // ================== УДАЛЕНИЕ ==================

    void deleteByProject(Project project);
    void deleteByProjectAndPartnerType(Project project, PartnerType partnerType);
}
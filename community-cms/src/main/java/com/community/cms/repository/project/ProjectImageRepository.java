package com.community.cms.repository.project;

import com.community.cms.domain.model.content.Project;
import com.community.cms.model.project.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {

    // ================== ПОИСК ПО ПРОЕКТУ ==================

    List<ProjectImage> findByProject(Project project);
    List<ProjectImage> findByProjectOrderBySortOrderAsc(Project project);
    List<ProjectImage> findByProjectOrderByAddedAtDesc(Project project);

    // ВМЕСТО findByProjectId - используем @Query
    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project.id = :projectId")
    List<ProjectImage> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project.id = :projectId ORDER BY pi.sortOrder ASC")
    List<ProjectImage> findByProjectIdOrderBySortOrderAsc(@Param("projectId") Long projectId);

    // ================== ПОИСК ПО КАТЕГОРИИ ==================

    List<ProjectImage> findByProjectAndCategory(Project project, String category);
    List<ProjectImage> findByProjectAndCategoryOrderBySortOrderAsc(Project project, String category);

    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project.id = :projectId AND pi.category = :category")
    List<ProjectImage> findByProjectIdAndCategory(@Param("projectId") Long projectId, @Param("category") String category);

    // ================== ПОИСК ПО ФЛАГАМ ==================

    List<ProjectImage> findByProjectAndIsFeaturedTrue(Project project);
    List<ProjectImage> findByProjectAndIsFeaturedTrueOrderBySortOrderAsc(Project project);

    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project.id = :projectId AND pi.isFeatured = true")
    List<ProjectImage> findByProjectIdAndIsFeaturedTrue(@Param("projectId") Long projectId);

    List<ProjectImage> findByProjectAndIsFeaturedFalse(Project project);

    // ================== ПОИСК ПО МЕДИАФАЙЛУ ==================

    // ВАРИАНТ 1: Если в сущности есть поле Long mediaFileId
    // List<ProjectImage> findByMediaFileId(Long mediaFileId);

    // ВАРИАНТ 2: Если есть связь @ManyToOne MediaFile mediaFile
    @Query("SELECT pi FROM ProjectImage pi WHERE pi.mediaFile.id = :mediaFileId")
    List<ProjectImage> findByMediaFileId(@Param("mediaFileId") Long mediaFileId);

    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project = :project AND pi.mediaFile.id = :mediaFileId")
    Optional<ProjectImage> findByProjectAndMediaFileId(@Param("project") Project project, @Param("mediaFileId") Long mediaFileId);

    @Query("SELECT CASE WHEN COUNT(pi) > 0 THEN true ELSE false END FROM ProjectImage pi WHERE pi.project.id = :projectId AND pi.mediaFile.id = :mediaFileId")
    boolean existsByProjectIdAndMediaFileId(@Param("projectId") Long projectId, @Param("mediaFileId") Long mediaFileId);

    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    long countByProject(Project project);

    @Query("SELECT COUNT(pi) FROM ProjectImage pi WHERE pi.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    long countByProjectAndIsFeaturedTrue(Project project);
    long countByProjectAndCategory(Project project, String category);

    @Query("SELECT DISTINCT pi.category FROM ProjectImage pi WHERE pi.project = :project AND pi.category IS NOT NULL")
    List<String> findDistinctCategoriesByProject(@Param("project") Project project);

    @Query("SELECT DISTINCT pi.category FROM ProjectImage pi WHERE pi.project.id = :projectId AND pi.category IS NOT NULL")
    List<String> findDistinctCategoriesByProjectId(@Param("projectId") Long projectId);

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    // УДАЛИТЬ ЭТОТ МЕТОД (пока) - он вызывает ошибку
    // @Query("SELECT pi FROM ProjectImage pi WHERE pi.project = :project ORDER BY pi.sortOrder ASC")
    // List<ProjectImage> findFirstNByProject(@Param("project") Project project, @Param("limit") int limit);

    // Вместо этого используем:
    default List<ProjectImage> findFirstNByProject(Project project, int limit) {
        return findByProjectOrderBySortOrderAsc(project).stream()
                .limit(limit)
                .toList();
    }

    List<ProjectImage> findByProjectAndCategoryIsNull(Project project);

    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project = :project AND (pi.caption IS NULL OR TRIM(pi.caption) = '')")
    List<ProjectImage> findWithoutCaptionByProject(@Param("project") Project project);

    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project = :project AND (pi.altText IS NULL OR TRIM(pi.altText) = '')")
    List<ProjectImage> findWithoutAltTextByProject(@Param("project") Project project);

    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project = :project AND pi.isFeatured = true ORDER BY pi.sortOrder ASC")
    Optional<ProjectImage> findMainImageByProject(@Param("project") Project project);

    // УДАЛИТЬ ЭТОТ МЕТОД (пока)
    // @Query("SELECT pi FROM ProjectImage pi WHERE pi.project = :project AND pi.isFeatured = true ORDER BY pi.sortOrder ASC")
    // List<ProjectImage> findSliderImagesByProject(@Param("project") Project project, @Param("limit") int limit);

    // Вместо этого:
    default List<ProjectImage> findSliderImagesByProject(Project project, int limit) {
        return findByProjectAndIsFeaturedTrueOrderBySortOrderAsc(project).stream()
                .limit(limit)
                .toList();
    }

    // ================== УДАЛЕНИЕ ==================

    void deleteByProject(Project project);

    @Modifying
    @Query("DELETE FROM ProjectImage pi WHERE pi.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    void deleteByProjectAndCategory(Project project, String category);
}
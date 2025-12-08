package com.community.cms.repository.project;

import com.community.cms.model.project.Project;
import com.community.cms.model.project.ProjectArticle;
import com.community.cms.model.project.ProjectArticle.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectArticleRepository extends JpaRepository<ProjectArticle, Long> {

    // ================== ПОИСК ПО ПРОЕКТУ ==================

    List<ProjectArticle> findByProject(Project project);
    List<ProjectArticle> findByProjectOrderByPublishedDateDesc(Project project);
    List<ProjectArticle> findByProjectOrderBySortOrderAsc(Project project);

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.project.id = :projectId")
    List<ProjectArticle> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.project.id = :projectId ORDER BY pa.publishedDate DESC")
    List<ProjectArticle> findByProjectIdOrderByPublishedDateDesc(@Param("projectId") Long projectId);

    // ================== ПОИСК ПО СТАТУСУ ==================

    List<ProjectArticle> findByProjectAndStatus(Project project, ArticleStatus status);
    List<ProjectArticle> findByProjectAndStatusOrderByPublishedDateDesc(Project project, ArticleStatus status);

    default List<ProjectArticle> findPublishedByProject(Project project) {
        return findByProjectAndStatusOrderByPublishedDateDesc(project, ArticleStatus.PUBLISHED);
    }

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.project.id = :projectId AND pa.status = :status")
    List<ProjectArticle> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") ArticleStatus status);

    // ================== ПОИСК ОПУБЛИКОВАННЫХ СТАТЕЙ ==================

    List<ProjectArticle> findByStatusOrderByPublishedDateDesc(ArticleStatus status);

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    List<ProjectArticle> findPublishedArticles();

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.project = :project AND pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    List<ProjectArticle> findPublishedArticlesByProject(@Param("project") Project project);

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.project.id = :projectId AND pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    List<ProjectArticle> findPublishedArticlesByProjectId(@Param("projectId") Long projectId);

    // ================== ПОИСК ПО ЗАГОЛОВКУ И СОДЕРЖИМОМУ ==================

    List<ProjectArticle> findByTitleContainingIgnoreCase(String title);
    List<ProjectArticle> findByTitleContainingIgnoreCaseAndStatus(String title, ArticleStatus status);

    @Query("SELECT pa FROM ProjectArticle pa WHERE LOWER(pa.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    List<ProjectArticle> findByContentContaining(@Param("content") String content);

    @Query("SELECT pa FROM ProjectArticle pa WHERE " +
            "LOWER(pa.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(pa.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(pa.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ProjectArticle> searchByTitleOrContent(@Param("searchTerm") String searchTerm);

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.status = 'PUBLISHED' AND (" +
            "LOWER(pa.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(pa.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(pa.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ProjectArticle> searchPublishedByTitleOrContent(@Param("searchTerm") String searchTerm);

    // ================== ПОИСК ПО АВТОРУ И SLUG ==================

    List<ProjectArticle> findByAuthorContainingIgnoreCase(String author);
    Optional<ProjectArticle> findBySlug(String slug);
    Optional<ProjectArticle> findBySlugAndStatus(String slug, ArticleStatus status);
    boolean existsBySlug(String slug);

    // ================== ПАГИНАЦИЯ ==================

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    Page<ProjectArticle> findPublishedArticles(Pageable pageable);

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.project = :project AND pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    Page<ProjectArticle> findPublishedArticlesByProject(@Param("project") Project project, Pageable pageable);

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.project.id = :projectId AND pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    Page<ProjectArticle> findPublishedArticlesByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    Page<ProjectArticle> findAll(Pageable pageable);
    Page<ProjectArticle> findByStatus(ArticleStatus status, Pageable pageable);

    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    long countByProject(Project project);

    @Query("SELECT COUNT(pa) FROM ProjectArticle pa WHERE pa.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    long countByProjectAndStatus(Project project, ArticleStatus status);

    @Query("SELECT COUNT(pa) FROM ProjectArticle pa WHERE pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP)")
    long countPublishedArticles();

    long countByStatus(ArticleStatus status);

    @Query("SELECT SUM(pa.viewCount) FROM ProjectArticle pa")
    Long sumViewCount();

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.status = 'PUBLISHED' ORDER BY pa.viewCount DESC")
    Optional<ProjectArticle> findMostPopularArticle();

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    Page<ProjectArticle> findRecentArticles(Pageable pageable);

    default List<ProjectArticle> findRecentArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedDate"));
        return findRecentArticles(pageable).getContent();
    }

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.project = :project AND pa.status = 'PUBLISHED' AND " +
            "(pa.publishedDate IS NULL OR pa.publishedDate <= CURRENT_TIMESTAMP) " +
            "ORDER BY pa.publishedDate DESC")
    Page<ProjectArticle> findRecentArticlesByProject(@Param("project") Project project, Pageable pageable);

    default List<ProjectArticle> findRecentArticlesByProject(Project project, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedDate"));
        return findRecentArticlesByProject(project, pageable).getContent();
    }

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.status = 'PUBLISHED' AND pa.publishedDate > :currentTime")
    List<ProjectArticle> findScheduledArticles(@Param("currentTime") LocalDateTime currentTime);

    List<ProjectArticle> findByFeaturedImagePathIsNull();

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.shortDescription IS NULL OR TRIM(pa.shortDescription) = ''")
    List<ProjectArticle> findWithoutShortDescription();

    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.metaDescription IS NULL OR TRIM(pa.metaDescription) = ''")
    List<ProjectArticle> findWithoutMetaDescription();

    // ИСПРАВЛЕНИЕ: Заменяем != на <> в JPQL
    @Query("SELECT pa FROM ProjectArticle pa WHERE pa.project = :project AND pa.id <> :excludeId AND " +
            "pa.status = 'PUBLISHED' AND (" +
            "LOWER(pa.title) LIKE LOWER(CONCAT('%', :searchTerms, '%')) OR " +
            "LOWER(pa.content) LIKE LOWER(CONCAT('%', :searchTerms, '%'))) " +
            "ORDER BY pa.publishedDate DESC")
    Page<ProjectArticle> findSimilarArticles(@Param("project") Project project,
                                             @Param("excludeId") Long excludeId,
                                             @Param("searchTerms") String searchTerms,
                                             Pageable pageable);

    default List<ProjectArticle> findSimilarArticles(Project project, Long excludeId, String searchTerms, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return findSimilarArticles(project, excludeId, searchTerms, pageable).getContent();
    }

    // ================== УДАЛЕНИЕ ==================

    void deleteByProject(Project project);

    @Modifying
    @Query("DELETE FROM ProjectArticle pa WHERE pa.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    void deleteByProjectAndStatus(Project project, ArticleStatus status);
}
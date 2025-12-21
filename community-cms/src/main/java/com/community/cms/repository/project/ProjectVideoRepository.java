package com.community.cms.repository.project;

import com.community.cms.model.project.Project;
import com.community.cms.model.project.ProjectVideo;
import com.community.cms.model.project.ProjectVideo.VideoType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectVideoRepository extends JpaRepository<ProjectVideo, Long> {

    // ================== ПОИСК ПО ПРОЕКТУ ==================

    List<ProjectVideo> findByProject(Project project);
    List<ProjectVideo> findByProjectOrderBySortOrderAsc(Project project);
    List<ProjectVideo> findByProjectOrderByAddedAtDesc(Project project);

    @Query("SELECT pv FROM ProjectVideo pv WHERE pv.project.id = :projectId")
    List<ProjectVideo> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT pv FROM ProjectVideo pv WHERE pv.project.id = :projectId ORDER BY pv.sortOrder ASC")
    List<ProjectVideo> findByProjectIdOrderBySortOrderAsc(@Param("projectId") Long projectId);

    // ================== ПОИСК ПО ТИПУ ВИДЕО ==================

    List<ProjectVideo> findByProjectAndVideoType(Project project, VideoType videoType);
    List<ProjectVideo> findByProjectAndVideoTypeOrderBySortOrderAsc(Project project, VideoType videoType);

    @Query("SELECT pv FROM ProjectVideo pv WHERE pv.project.id = :projectId AND pv.videoType = :videoType")
    List<ProjectVideo> findByProjectIdAndVideoType(@Param("projectId") Long projectId, @Param("videoType") VideoType videoType);

    // ================== ПОИСК ПО ФЛАГАМ ==================

    Optional<ProjectVideo> findByProjectAndIsMainTrue(Project project);

    @Query("SELECT pv FROM ProjectVideo pv WHERE pv.project.id = :projectId AND pv.isMain = true")
    Optional<ProjectVideo> findByProjectIdAndIsMainTrue(@Param("projectId") Long projectId);

    List<ProjectVideo> findByProjectAndIsMainFalse(Project project);
    List<ProjectVideo> findByProjectAndIsMainFalseOrderBySortOrderAsc(Project project);

    // ================== ПОИСК ПО URL И ID ВИДЕО ==================

    Optional<ProjectVideo> findByVideoUrl(String videoUrl);
    List<ProjectVideo> findByVideoId(String videoId);
    Optional<ProjectVideo> findByProjectAndVideoId(Project project, String videoId);

    @Query("SELECT CASE WHEN COUNT(pv) > 0 THEN true ELSE false END FROM ProjectVideo pv WHERE pv.project.id = :projectId AND pv.videoUrl = :videoUrl")
    boolean existsByProjectIdAndVideoUrl(@Param("projectId") Long projectId, @Param("videoUrl") String videoUrl);

    @Query("SELECT CASE WHEN COUNT(pv) > 0 THEN true ELSE false END FROM ProjectVideo pv WHERE pv.project.id = :projectId AND pv.videoId = :videoId")
    boolean existsByProjectIdAndVideoId(@Param("projectId") Long projectId, @Param("videoId") String videoId);

    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    long countByProject(Project project);

    @Query("SELECT COUNT(pv) FROM ProjectVideo pv WHERE pv.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    long countByProjectAndVideoType(Project project, VideoType videoType);
    long countByProjectAndIsMainTrue(Project project);

    default long countYouTubeVideosByProject(Project project) {
        return countByProjectAndVideoType(project, VideoType.YOUTUBE);
    }

    default long countVimeoVideosByProject(Project project) {
        return countByProjectAndVideoType(project, VideoType.VIMEO);
    }

    default long countRutubeVideosByProject(Project project) {
        return countByProjectAndVideoType(project, VideoType.RUTUBE);
    }

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    @Query("SELECT pv FROM ProjectVideo pv WHERE pv.project = :project ORDER BY pv.sortOrder ASC")
    List<ProjectVideo> findFirstNByProject(@Param("project") Project project, Pageable pageable);

    default List<ProjectVideo> findFirstNByProject(Project project, int limit) {
        return findFirstNByProject(project, PageRequest.of(0, limit));
    }

    @Query("SELECT pv FROM ProjectVideo pv WHERE pv.project.id = :projectId ORDER BY pv.sortOrder ASC")
    List<ProjectVideo> findFirstNByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    default List<ProjectVideo> findFirstNByProjectId(Long projectId, int limit) {
        return findFirstNByProjectId(projectId, PageRequest.of(0, limit));
    }

    @Query("SELECT pv FROM ProjectVideo pv WHERE pv.project = :project AND " +
            "(pv.description IS NULL OR TRIM(pv.description) = '')")
    List<ProjectVideo> findWithoutDescriptionByProject(@Param("project") Project project);

    List<ProjectVideo> findByProjectAndDurationSecondsIsNotNull(Project project);
    List<ProjectVideo> findByProjectAndDurationSecondsIsNull(Project project);

    @Query("SELECT DISTINCT pv.project FROM ProjectVideo pv")
    List<Project> findProjectsWithVideos();

    @Query("SELECT pv FROM ProjectVideo pv ORDER BY pv.addedAt DESC")
    List<ProjectVideo> findRecentVideos(Pageable pageable);

    default List<ProjectVideo> findRecentVideos(int limit) {
        return findRecentVideos(PageRequest.of(0, limit));
    }

    @Query("SELECT pv FROM ProjectVideo pv WHERE pv.videoType = :videoType ORDER BY pv.addedAt DESC")
    List<ProjectVideo> findRecentVideosByType(@Param("videoType") VideoType videoType, Pageable pageable);

    default List<ProjectVideo> findRecentVideosByType(VideoType videoType, int limit) {
        return findRecentVideosByType(videoType, PageRequest.of(0, limit));
    }

    // ================== УПРАВЛЕНИЕ ОСНОВНЫМ ВИДЕО ==================

    @Modifying
    @Query("UPDATE ProjectVideo pv SET pv.isMain = false WHERE pv.project = :project")
    void resetMainVideoForProject(@Param("project") Project project);

    @Modifying
    @Query("UPDATE ProjectVideo pv SET pv.isMain = false WHERE pv.project.id = :projectId")
    void resetMainVideoForProjectId(@Param("projectId") Long projectId);

    // ================== УДАЛЕНИЕ ==================

    void deleteByProject(Project project);

    @Modifying
    @Query("DELETE FROM ProjectVideo pv WHERE pv.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    void deleteByProjectAndVideoType(Project project, VideoType videoType);
}
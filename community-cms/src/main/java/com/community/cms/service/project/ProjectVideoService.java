package com.community.cms.service.project;

import com.community.cms.model.project.Project;
import com.community.cms.model.project.ProjectVideo;
import com.community.cms.model.project.ProjectVideo.VideoType;
import com.community.cms.repository.project.ProjectVideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления видео проектов.
 *
 * <p>Предоставляет бизнес-логику для работы с видео проектов.
 * Видео хранятся только как ссылки на внешние видеохостинги (YouTube, Vimeo).</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see ProjectVideo
 * @see ProjectVideoRepository
 */
@Service
@Transactional
public class ProjectVideoService {

    private final ProjectVideoRepository projectVideoRepository;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param projectVideoRepository репозиторий для работы с видео проектов
     */
    @Autowired
    public ProjectVideoService(ProjectVideoRepository projectVideoRepository) {
        this.projectVideoRepository = projectVideoRepository;
    }

    // ================== CRUD ОПЕРАЦИИ ==================

    /**
     * Сохраняет видео проекта.
     *
     * @param projectVideo видео проекта для сохранения
     * @return сохраненное видео проекта
     */
    public ProjectVideo save(ProjectVideo projectVideo) {
        validateProjectVideo(projectVideo);
        return projectVideoRepository.save(projectVideo);
    }

    /**
     * Обновляет существующее видео проекта.
     *
     * @param projectVideo видео проекта для обновления
     * @return обновленное видео проекта
     */
    public ProjectVideo update(ProjectVideo projectVideo) {
        validateProjectVideo(projectVideo);
        return projectVideoRepository.save(projectVideo);
    }

    /**
     * Находит видео проекта по ID.
     *
     * @param id идентификатор видео проекта
     * @return Optional с видео проекта, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<ProjectVideo> findById(Long id) {
        return projectVideoRepository.findById(id);
    }

    /**
     * Удаляет видео проекта по ID.
     *
     * @param id идентификатор видео проекта для удаления
     */
    public void deleteById(Long id) {
        projectVideoRepository.deleteById(id);
    }

    // ================== ПОИСК ПО ПРОЕКТУ ==================

    /**
     * Находит все видео указанного проекта.
     *
     * @param project проект
     * @return список видео проекта
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findByProject(Project project) {
        return projectVideoRepository.findByProject(project);
    }

    /**
     * Находит все видео проекта по ID проекта.
     *
     * @param projectId ID проекта
     * @return список видео проекта
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findByProjectId(Long projectId) {
        return projectVideoRepository.findByProjectId(projectId);
    }

    /**
     * Находит все видео проекта, отсортированные по порядку сортировки.
     *
     * @param project проект
     * @return список видео проекта (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findByProjectOrderBySortOrder(Project project) {
        return projectVideoRepository.findByProjectOrderBySortOrderAsc(project);
    }

    /**
     * Находит все видео проекта по ID проекта, отсортированные по порядку сортировки.
     *
     * @param projectId ID проекта
     * @return список видео проекта (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findByProjectIdOrderBySortOrder(Long projectId) {
        return projectVideoRepository.findByProjectIdOrderBySortOrderAsc(projectId);
    }

    // ================== ПОИСК ПО ТИПУ ВИДЕО ==================

    /**
     * Находит видео проекта по типу видеохостинга.
     *
     * @param project проект
     * @param videoType тип видеохостинга
     * @return список видео проекта указанного типа
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findByProjectAndVideoType(Project project, VideoType videoType) {
        return projectVideoRepository.findByProjectAndVideoType(project, videoType);
    }

    /**
     * Находит видео проекта по ID проекта и типу видеохостинга.
     *
     * @param projectId ID проекта
     * @param videoType тип видеохостинга
     * @return список видео проекта указанного типа
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findByProjectIdAndVideoType(Long projectId, VideoType videoType) {
        return projectVideoRepository.findByProjectIdAndVideoType(projectId, videoType);
    }

    /**
     * Находит видео проекта по типу видеохостинга, отсортированные по порядку сортировки.
     *
     * @param project проект
     * @param videoType тип видеохостинга
     * @return список видео проекта указанного типа (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findByProjectAndVideoTypeOrderBySortOrder(Project project, VideoType videoType) {
        return projectVideoRepository.findByProjectAndVideoTypeOrderBySortOrderAsc(project, videoType);
    }

    // ================== ПОИСК ПО ФЛАГАМ ==================

    /**
     * Находит основное видео проекта.
     *
     * @param project проект
     * @return Optional с основным видео проекта, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<ProjectVideo> findMainVideoByProject(Project project) {
        return projectVideoRepository.findByProjectAndIsMainTrue(project);
    }

    /**
     * Находит основное видео проекта по ID проекта.
     *
     * @param projectId ID проекта
     * @return Optional с основным видео проекта, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<ProjectVideo> findMainVideoByProjectId(Long projectId) {
        return projectVideoRepository.findByProjectIdAndIsMainTrue(projectId);
    }

    /**
     * Находит не основные видео проекта.
     *
     * @param project проект
     * @return список не основных видео проекта
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findNonMainVideosByProject(Project project) {
        return projectVideoRepository.findByProjectAndIsMainFalse(project);
    }

    /**
     * Находит не основные видео проекта, отсортированные по порядку сортировки.
     *
     * @param project проект
     * @return список не основных видео проекта (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findNonMainVideosByProjectOrderBySortOrder(Project project) {
        return projectVideoRepository.findByProjectAndIsMainFalseOrderBySortOrderAsc(project);
    }

    // ================== ПОИСК ПО URL И ID ВИДЕО ==================

    /**
     * Находит видео по URL.
     *
     * @param youtubeUrl URL видео
     * @return Optional с видео, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<ProjectVideo> findByYoutubeUrl(String youtubeUrl) {
        return projectVideoRepository.findByYoutubeUrl(youtubeUrl);
    }

    /**
     * Находит видео по ID видео на внешнем хостинге.
     *
     * @param videoId ID видео на внешнем хостинге
     * @return список видео с указанным ID
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findByVideoId(String videoId) {
        return projectVideoRepository.findByVideoId(videoId);
    }

    /**
     * Находит видео проекта по проекту и ID видео на внешнем хостинге.
     *
     * @param project проект
     * @param videoId ID видео на внешнем хостинге
     * @return Optional с видео проекта, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<ProjectVideo> findByProjectAndVideoId(Project project, String videoId) {
        return projectVideoRepository.findByProjectAndVideoId(project, videoId);
    }

    /**
     * Проверяет существование видео по ID проекта и URL.
     *
     * @param projectId ID проекта
     * @param youtubeUrl URL видео
     * @return true если видео существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsByProjectIdAndYoutubeUrl(Long projectId, String youtubeUrl) {
        return projectVideoRepository.existsByProjectIdAndYoutubeUrl(projectId, youtubeUrl);
    }

    /**
     * Проверяет существование видео по ID проекта и ID видео на внешнем хостинге.
     *
     * @param projectId ID проекта
     * @param videoId ID видео на внешнем хостинге
     * @return true если видео существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsByProjectIdAndVideoId(Long projectId, String videoId) {
        return projectVideoRepository.existsByProjectIdAndVideoId(projectId, videoId);
    }

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    /**
     * Находит первые N видео проекта.
     *
     * @param project проект
     * @param limit количество видео
     * @return список первых N видео проекта
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findFirstNByProject(Project project, int limit) {
        return projectVideoRepository.findFirstNByProject(project, limit);
    }

    /**
     * Находит первые N видео проекта по ID проекта.
     *
     * @param projectId ID проекта
     * @param limit количество видео
     * @return список первых N видео проекта
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findFirstNByProjectId(Long projectId, int limit) {
        return projectVideoRepository.findFirstNByProjectId(projectId, limit);
    }

    /**
     * Находит видео проекта без описания.
     *
     * @param project проект
     * @return список видео проекта без описания
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findWithoutDescriptionByProject(Project project) {
        return projectVideoRepository.findWithoutDescriptionByProject(project);
    }

    /**
     * Находит видео проекта с указанной длительностью.
     *
     * @param project проект
     * @return список видео проекта с указанной длительностью
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findWithDurationByProject(Project project) {
        return projectVideoRepository.findByProjectAndDurationSecondsIsNotNull(project);
    }

    /**
     * Находит видео проекта без указанной длительности.
     *
     * @param project проект
     * @return список видео проекта без указанной длительности
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findWithoutDurationByProject(Project project) {
        return projectVideoRepository.findByProjectAndDurationSecondsIsNull(project);
    }

    /**
     * Находит последние N добавленных видео.
     *
     * @param limit количество видео
     * @return список последних добавленных видео
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findRecentVideos(int limit) {
        return projectVideoRepository.findRecentVideos(limit);
    }

    /**
     * Находит последние N добавленных видео по типу видеохостинга.
     *
     * @param videoType тип видеохостинга
     * @param limit количество видео
     * @return список последних добавленных видео указанного типа
     */
    @Transactional(readOnly = true)
    public List<ProjectVideo> findRecentVideosByType(VideoType videoType, int limit) {
        return projectVideoRepository.findRecentVideosByType(videoType, limit);
    }

    /**
     * Находит проекты, у которых есть видео.
     *
     * @return список проектов с видео
     */
    @Transactional(readOnly = true)
    public List<Project> findProjectsWithVideos() {
        return projectVideoRepository.findProjectsWithVideos();
    }

    // ================== СТАТИСТИКА ==================

    /**
     * Подсчитывает количество видео проекта.
     *
     * @param project проект
     * @return количество видео проекта
     */
    @Transactional(readOnly = true)
    public long countByProject(Project project) {
        return projectVideoRepository.countByProject(project);
    }

    /**
     * Подсчитывает количество видео по ID проекта.
     *
     * @param projectId ID проекта
     * @return количество видео проекта
     */
    @Transactional(readOnly = true)
    public long countByProjectId(Long projectId) {
        return projectVideoRepository.countByProjectId(projectId);
    }

    /**
     * Подсчитывает количество видео проекта по типу видеохостинга.
     *
     * @param project проект
     * @param videoType тип видеохостинга
     * @return количество видео проекта указанного типа
     */
    @Transactional(readOnly = true)
    public long countByProjectAndVideoType(Project project, VideoType videoType) {
        return projectVideoRepository.countByProjectAndVideoType(project, videoType);
    }

    /**
     * Подсчитывает количество основных видео проекта.
     *
     * @param project проект
     * @return количество основных видео проекта
     */
    @Transactional(readOnly = true)
    public long countMainVideosByProject(Project project) {
        return projectVideoRepository.countByProjectAndIsMainTrue(project);
    }

    /**
     * Подсчитывает количество YouTube видео проекта.
     *
     * @param project проект
     * @return количество YouTube видео проекта
     */
    @Transactional(readOnly = true)
    public long countYouTubeVideosByProject(Project project) {
        return projectVideoRepository.countYouTubeVideosByProject(project);
    }

    /**
     * Подсчитывает количество Vimeo видео проекта.
     *
     * @param project проект
     * @return количество Vimeo видео проекта
     */
    @Transactional(readOnly = true)
    public long countVimeoVideosByProject(Project project) {
        return projectVideoRepository.countVimeoVideosByProject(project);
    }

    // ================== УПРАВЛЕНИЕ ОСНОВНЫМ ВИДЕО ==================

    /**
     * Устанавливает видео как основное для проекта.
     * Сбрасывает флаг основного видео с других видео проекта.
     *
     * @param projectVideo видео для установки как основного
     * @return обновленное видео проекта
     */
    public ProjectVideo setAsMainVideo(ProjectVideo projectVideo) {
        // Сбрасываем флаг основного видео со всех видео проекта
        projectVideoRepository.resetMainVideoForProject(projectVideo.getProject());

        // Устанавливаем флаг основного видео для указанного видео
        projectVideo.setMain(true);
        return projectVideoRepository.save(projectVideo);
    }

    /**
     * Устанавливает видео как основное для проекта по ID видео.
     *
     * @param videoId ID видео
     * @return обновленное видео проекта
     * @throws IllegalArgumentException если видео не найдено
     */
    public ProjectVideo setAsMainVideoById(Long videoId) {
        ProjectVideo projectVideo = projectVideoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Видео с ID " + videoId + " не найдено"));
        return setAsMainVideo(projectVideo);
    }

    /**
     * Сбрасывает флаг основного видео с указанного видео.
     *
     * @param projectVideo видео для сброса флага
     * @return обновленное видео проекта
     */
    public ProjectVideo removeMainVideo(ProjectVideo projectVideo) {
        projectVideo.setMain(false);
        return projectVideoRepository.save(projectVideo);
    }

    /**
     * Сбрасывает флаг основного видео со всех видео проекта.
     *
     * @param project проект
     */
    public void resetMainVideoForProject(Project project) {
        projectVideoRepository.resetMainVideoForProject(project);
    }

    /**
     * Сбрасывает флаг основного видео со всех видео проекта по ID проекта.
     *
     * @param projectId ID проекта
     */
    public void resetMainVideoForProjectId(Long projectId) {
        projectVideoRepository.resetMainVideoForProjectId(projectId);
    }

    // ================== УДАЛЕНИЕ ПО СВЯЗЯМ ==================

    /**
     * Удаляет все видео указанного проекта.
     *
     * @param project проект
     */
    public void deleteByProject(Project project) {
        projectVideoRepository.deleteByProject(project);
    }

    /**
     * Удаляет все видео по ID проекта.
     *
     * @param projectId ID проекта
     */
    public void deleteByProjectId(Long projectId) {
        projectVideoRepository.deleteByProjectId(projectId);
    }

    /**
     * Удаляет видео проекта по типу видеохостинга.
     *
     * @param project проект
     * @param videoType тип видеохостинга
     */
    public void deleteByProjectAndVideoType(Project project, VideoType videoType) {
        projectVideoRepository.deleteByProjectAndVideoType(project, videoType);
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет бизнес-правила для видео проекта.
     *
     * @param projectVideo видео проекта для валидации
     * @throws IllegalArgumentException если видео проекта невалидно
     */
    private void validateProjectVideo(ProjectVideo projectVideo) {
        if (projectVideo == null) {
            throw new IllegalArgumentException("Видео проекта не может быть null");
        }

        if (projectVideo.getProject() == null) {
            throw new IllegalArgumentException("Видео должно быть привязано к проекту");
        }

        if (projectVideo.getTitle() == null || projectVideo.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Название видео обязательно");
        }

        if (projectVideo.getYoutubeUrl() == null || projectVideo.getYoutubeUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("URL видео обязателен");
        }

        // Автоматическое определение типа и ID видео при сохранении
        projectVideo.setYoutubeUrl(projectVideo.getYoutubeUrl());

        if (projectVideo.getVideoType() == null) {
            throw new IllegalArgumentException("Не удалось определить тип видеохостинга. Поддерживаются только YouTube и Vimeo");
        }

        if (projectVideo.getVideoId() == null || projectVideo.getVideoId().trim().isEmpty()) {
            throw new IllegalArgumentException("Не удалось извлечь ID видео из URL");
        }

        if (projectVideo.getSortOrder() == null) {
            projectVideo.setSortOrder(0);
        }
    }

    /**
     * Создает новое видео проекта.
     *
     * @param project проект
     * @param title название видео
     * @param youtubeUrl URL видео
     * @param description описание (опционально)
     * @param isMain флаг основного видео
     * @param durationSeconds длительность в секундах (опционально)
     * @param sortOrder порядок сортировки
     * @return созданное видео проекта
     */
    public ProjectVideo createProjectVideo(Project project, String title, String youtubeUrl,
                                           String description, boolean isMain,
                                           Integer durationSeconds, Integer sortOrder) {
        ProjectVideo projectVideo = new ProjectVideo(project, title, youtubeUrl);
        projectVideo.setDescription(description);
        projectVideo.setMain(isMain);
        projectVideo.setDurationSeconds(durationSeconds);
        projectVideo.setSortOrder(sortOrder != null ? sortOrder : 0);

        return save(projectVideo);
    }

    /**
     * Проверяет валидность URL видео.
     *
     * @param youtubeUrl URL видео для проверки
     * @return true если URL валиден и поддерживается, иначе false
     */
    public boolean isValidVideoUrl(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.trim().isEmpty()) {
            return false;
        }

        VideoType videoType = VideoType.fromUrl(youtubeUrl);
        if (videoType == null) {
            return false;
        }

        String videoId = VideoType.extractVideoId(youtubeUrl);
        return videoId != null && !videoId.trim().isEmpty();
    }

    /**
     * Получает embed код для вставки видео в HTML.
     *
     * @param projectVideo видео проекта
     * @return HTML embed код
     */
    public String getEmbedCode(ProjectVideo projectVideo) {
        return projectVideo.getEmbedCode();
    }

    /**
     * Получает URL превью видео (миниатюры).
     *
     * @param projectVideo видео проекта
     * @return URL превью видео
     */
    public String getThumbnailUrl(ProjectVideo projectVideo) {
        return projectVideo.getThumbnailUrl();
    }

    /**
     * Обновляет порядок сортировки видео проекта.
     *
     * @param projectVideos список видео с обновленными sortOrder
     * @return список обновленных видео
     */
    public List<ProjectVideo> updateSortOrder(List<ProjectVideo> projectVideos) {
        return projectVideoRepository.saveAll(projectVideos);
    }
}

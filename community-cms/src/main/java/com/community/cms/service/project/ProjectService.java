package com.community.cms.service.project;

import com.community.cms.dto.project.ProjectCreateRequest;
import com.community.cms.dto.project.ProjectDTO;
import com.community.cms.dto.project.ProjectFilter;
import com.community.cms.dto.project.ProjectUpdateRequest;
import com.community.cms.exception.ResourceNotFoundException;
import com.community.cms.exception.ValidationException;
import com.community.cms.model.gallery.MediaFile;
import com.community.cms.model.gallery.PhotoGalleryItem;
import com.community.cms.model.project.Project;
import com.community.cms.model.project.ProjectCategory;
import com.community.cms.model.project.ProjectStatus;
import com.community.cms.repository.project.ProjectRepository;
import com.community.cms.service.FileStorageService;
import com.community.cms.service.gallery.PhotoGalleryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;


//Основной сервис для работы с проектами
//Аннотации @Service и @Transactional для управления транзакциями
//Инъекция зависимостей через конструктор
//Логирование с помощью SLF4J
//Методы CRUD для проектов с обработкой исключений
//Поддержка фильтрации и пагинации
//Управление изображениями обложек через FileStorageService
//Интеграция с существующей системой галереи
//Методы для получения активных, будущих и завершенных проектов
//Статистика проектов
//Вспомогательные методы для загрузки и удаления изображений

/**
 * Сервис для управления проектами организации "ЛАДА".
 * Обеспечивает бизнес-логику работы с проектами.
 *
 * @author Vasickin
 * @since 1.0
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final FileStorageService fileStorageService;
    private final PhotoGalleryService photoGalleryService;
    private final ProjectMapper projectMapper;

    /**
     * Конструктор сервиса проектов
     *
     * @param projectRepository Репозиторий проектов
     * @param fileStorageService Сервис хранения файлов
     * @param photoGalleryService Сервис фотогалерей
     * @param projectMapper Маппер проектов
     */
    public ProjectService(
            ProjectRepository projectRepository,
            FileStorageService fileStorageService,
            PhotoGalleryService photoGalleryService,
            ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.fileStorageService = fileStorageService;
        this.photoGalleryService = photoGalleryService;
        this.projectMapper = projectMapper;
    }

    /**
     * Получить все проекты с пагинацией
     *
     * @param pageable Параметры пагинации
     * @return Страница проектов
     */
    @Transactional(readOnly = true)
    public Page<ProjectDTO> getAllProjects(Pageable pageable) {
        logger.debug("Получение всех проектов с пагинацией: {}", pageable);
        Page<Project> projects = projectRepository.findAll(pageable);
        return projects.map(projectMapper::toDTO);
    }

    /**
     * Получить проекты с фильтрацией
     *
     * @param filter Фильтр проектов
     * @param pageable Параметры пагинации
     * @return Страница отфильтрованных проектов
     */
    @Transactional(readOnly = true)
    public Page<ProjectDTO> getProjects(ProjectFilter filter, Pageable pageable) {
        logger.debug("Получение проектов с фильтром: {}, пагинацией: {}", filter, pageable);
        Page<Project> projects = projectRepository.findByFilters(
                filter.getCategory(),
                filter.getStatus(),
                filter.getSearch(),
                pageable
        );
        return projects.map(projectMapper::toDTO);
    }

    /**
     * Получить проекты для публичного доступа
     *
     * @param category Категория проекта (может быть null)
     * @param search Поисковый запрос (может быть null)
     * @param pageable Параметры пагинации
     * @return Страница проектов для публичного отображения
     */
    @Transactional(readOnly = true)
    public Page<ProjectDTO> getPublicProjects(ProjectCategory category, String search, Pageable pageable) {
        logger.debug("Получение публичных проектов: категория={}, поиск={}", category, search);
        Page<Project> projects = projectRepository.findPublicProjects(category, search, pageable);
        return projects.map(projectMapper::toDTO);
    }

    /**
     * Получить проект по ID
     *
     * @param id ID проекта
     * @return DTO проекта
     * @throws ResourceNotFoundException если проект не найден
     */
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) {
        logger.debug("Получение проекта по ID: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект с ID " + id + " не найден"));
        return projectMapper.toDTO(project);
    }

    /**
     * Получить проект по URL-идентификатору (slug)
     *
     * @param slug URL-идентификатор проекта
     * @return DTO проекта
     * @throws ResourceNotFoundException если проект не найден
     */
    @Transactional(readOnly = true)
    public ProjectDTO getProjectBySlug(String slug) {
        logger.debug("Получение проекта по slug: {}", slug);
        Project project = projectRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Проект '" + slug + "' не найден"));
        return projectMapper.toDTO(project);
    }

    /**
     * Получить полную информацию о проекте по slug (с деталями)
     *
     * @param slug URL-идентификатор проекта
     * @return Полное DTO проекта
     * @throws ResourceNotFoundException если проект не найден
     */
    @Transactional(readOnly = true)
    public ProjectDTO getProjectDetailsBySlug(String slug) {
        logger.debug("Получение детальной информации о проекте по slug: {}", slug);
        Project project = projectRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Проект '" + slug + "' не найден"));
        return projectMapper.toDetailedDTO(project);
    }

    /**
     * Создать новый проект
     *
     * @param request DTO запроса на создание проекта
     * @param coverImage Изображение обложки (может быть null)
     * @return Созданный проект в формате DTO
     * @throws ValidationException если данные невалидны
     */
    public ProjectDTO createProject(ProjectCreateRequest request, MultipartFile coverImage) {
        logger.info("Создание нового проекта: {}", request.getTitle());

        // Проверка уникальности slug
        if (projectRepository.existsBySlug(request.getSlug())) {
            throw new ValidationException("Проект с URL '" + request.getSlug() + "' уже существует");
        }

        // Создание проекта
        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setSlug(request.getSlug());
        project.setCategory(request.getCategory());
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.ACTIVE);
        project.setShortDescription(request.getShortDescription());
        project.setFullDescription(request.getFullDescription());
        project.setGoals(request.getGoals());
        project.setLocation(request.getLocation());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setCuratorContacts(request.getCuratorContacts());
        project.setParticipationInfo(request.getParticipationInfo());

        // Настройки секций
        project.setShowDescription(request.isShowDescription());
        project.setShowPhotos(request.isShowPhotos());
        project.setShowVideos(request.isShowVideos());
        project.setShowTeam(request.isShowTeam());
        project.setShowParticipation(request.isShowParticipation());
        project.setShowPartners(request.isShowPartners());
        project.setShowRelated(request.isShowRelated());

        // SEO поля
        project.setMetaTitle(request.getMetaTitle());
        project.setMetaDescription(request.getMetaDescription());
        project.setMetaKeywords(request.getMetaKeywords());

        // Загрузка обложки
        if (coverImage != null && !coverImage.isEmpty()) {
            MediaFile cover = uploadCoverImage(coverImage);
            project.setCoverImage(cover);
        }

        // Создание галереи для проекта
        if (request.isCreateGallery()) {
            PhotoGallery gallery = createGalleryForProject(request.getTitle());
            project.setGallery(gallery);
        }

        // Сохранение проекта
        Project savedProject = projectRepository.save(project);
        logger.info("Проект создан: {} (ID: {})", savedProject.getTitle(), savedProject.getId());

        return projectMapper.toDTO(savedProject);
    }

    /**
     * Обновить существующий проект
     *
     * @param id ID проекта
     * @param request DTO запроса на обновление проекта
     * @param coverImage Новое изображение обложки (может быть null)
     * @return Обновленный проект в формате DTO
     * @throws ResourceNotFoundException если проект не найден
     * @throws ValidationException если данные невалидны
     */
    public ProjectDTO updateProject(Long id, ProjectUpdateRequest request, MultipartFile coverImage) {
        logger.info("Обновление проекта ID: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект с ID " + id + " не найден"));

        // Проверка уникальности slug, если он изменился
        if (request.getSlug() != null && !request.getSlug().equals(project.getSlug())) {
            if (projectRepository.existsBySlug(request.getSlug())) {
                throw new ValidationException("Проект с URL '" + request.getSlug() + "' уже существует");
            }
            project.setSlug(request.getSlug());
        }

        // Обновление полей проекта
        if (request.getTitle() != null) {
            project.setTitle(request.getTitle());
        }
        if (request.getCategory() != null) {
            project.setCategory(request.getCategory());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getShortDescription() != null) {
            project.setShortDescription(request.getShortDescription());
        }
        if (request.getFullDescription() != null) {
            project.setFullDescription(request.getFullDescription());
        }
        if (request.getGoals() != null) {
            project.setGoals(request.getGoals());
        }
        if (request.getLocation() != null) {
            project.setLocation(request.getLocation());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getCuratorContacts() != null) {
            project.setCuratorContacts(request.getCuratorContacts());
        }
        if (request.getParticipationInfo() != null) {
            project.setParticipationInfo(request.getParticipationInfo());
        }

        // Обновление настроек секций
        if (request.getShowDescription() != null) {
            project.setShowDescription(request.getShowDescription());
        }
        if (request.getShowPhotos() != null) {
            project.setShowPhotos(request.getShowPhotos());
        }
        if (request.getShowVideos() != null) {
            project.setShowVideos(request.getShowVideos());
        }
        if (request.getShowTeam() != null) {
            project.setShowTeam(request.getShowTeam());
        }
        if (request.getShowParticipation() != null) {
            project.setShowParticipation(request.isShowParticipation());
        }
        if (request.getShowPartners() != null) {
            project.setShowPartners(request.isShowPartners());
        }
        if (request.getShowRelated() != null) {
            project.setShowRelated(request.isShowRelated());
        }

        // Обновление SEO полей
        if (request.getMetaTitle() != null) {
            project.setMetaTitle(request.getMetaTitle());
        }
        if (request.getMetaDescription() != null) {
            project.setMetaDescription(request.getMetaDescription());
        }
        if (request.getMetaKeywords() != null) {
            project.setMetaKeywords(request.getMetaKeywords());
        }

        // Обновление обложки
        if (coverImage != null && !coverImage.isEmpty()) {
            // Удаление старой обложки, если есть
            if (project.getCoverImage() != null) {
                deleteCoverImage(project.getCoverImage());
            }
            // Загрузка новой обложки
            MediaFile newCover = uploadCoverImage(coverImage);
            project.setCoverImage(newCover);
        } else if (request.isRemoveCoverImage() && project.getCoverImage() != null) {
            // Удаление обложки по запросу
            deleteCoverImage(project.getCoverImage());
            project.setCoverImage(null);
        }

        // Обновление галереи
        if (request.isCreateGallery() && project.getGallery() == null) {
            PhotoGallery gallery = createGalleryForProject(project.getTitle());
            project.setGallery(gallery);
        }

        Project updatedProject = projectRepository.save(project);
        logger.info("Проект обновлен: {} (ID: {})", updatedProject.getTitle(), updatedProject.getId());

        return projectMapper.toDTO(updatedProject);
    }

    /**
     * Удалить проект
     *
     * @param id ID проекта
     * @throws ResourceNotFoundException если проект не найден
     */
    public void deleteProject(Long id) {
        logger.info("Удаление проекта ID: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект с ID " + id + " не найден"));

        // Удаление обложки
        if (project.getCoverImage() != null) {
            deleteCoverImage(project.getCoverImage());
        }

        // Удаление проекта (каскадно удалятся видео, партнеры и связи с командой)
        projectRepository.delete(project);
        logger.info("Проект удален: {} (ID: {})", project.getTitle(), id);
    }

    /**
     * Изменить статус проекта
     *
     * @param id ID проекта
     * @param status Новый статус
     * @return Обновленный проект
     * @throws ResourceNotFoundException если проект не найден
     */
    public ProjectDTO changeProjectStatus(Long id, ProjectStatus status) {
        logger.info("Изменение статуса проекта ID: {} на {}", id, status);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект с ID " + id + " не найден"));

        project.setStatus(status);
        Project updatedProject = projectRepository.save(project);

        logger.info("Статус проекта изменен: {} -> {}", project.getTitle(), status);
        return projectMapper.toDTO(updatedProject);
    }

    /**
     * Получить активные проекты (которые проходят сейчас)
     *
     * @return Список активных проектов
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getActiveProjects() {
        logger.debug("Получение активных проектов");
        List<Project> projects = projectRepository.findActiveProjects(LocalDate.now());
        return projectMapper.toDTOList(projects);
    }

    /**
     * Получить будущие проекты (запланированные)
     *
     * @return Список будущих проектов
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getUpcomingProjects() {
        logger.debug("Получение будущих проектов");
        List<Project> projects = projectRepository.findUpcomingProjects(LocalDate.now());
        return projectMapper.toDTOList(projects);
    }

    /**
     * Получить завершенные проекты
     *
     * @return Список завершенных проектов
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getCompletedProjects() {
        logger.debug("Получение завершенных проектов");
        List<Project> projects = projectRepository.findCompletedProjects(LocalDate.now());
        return projectMapper.toDTOList(projects);
    }

    /**
     * Получить похожие проекты
     *
     * @param projectId ID текущего проекта
     * @param limit Максимальное количество похожих проектов
     * @return Список похожих проектов
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getRelatedProjects(Long projectId, int limit) {
        logger.debug("Получение похожих проектов для проекта ID: {}, limit: {}", projectId, limit);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Проект с ID " + projectId + " не найден"));

        List<Project> relatedProjects = projectRepository.findRelatedProjects(
                projectId,
                project.getCategory().name(),
                limit
        );

        return projectMapper.toDTOList(relatedProjects);
    }

    /**
     * Проверить существование проекта по slug
     *
     * @param slug URL-идентификатор проекта
     * @return true, если проект существует
     */
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return projectRepository.existsBySlug(slug);
    }

    /**
     * Получить статистику проектов
     *
     * @return Статистика проектов
     */
    @Transactional(readOnly = true)
    public ProjectStats getProjectStats() {
        logger.debug("Получение статистики проектов");

        ProjectStats stats = new ProjectStats();
        stats.setTotalProjects(projectRepository.count());
        stats.setActiveProjects(projectRepository.countActiveProjects());
        stats.setArchiveProjects(projectRepository.countByStatus(ProjectStatus.ARCHIVE));

        // Подсчет по категориям
        for (ProjectCategory category : ProjectCategory.values()) {
            long count = projectRepository.countByCategory(category);
            stats.addCategoryCount(category.getDisplayName(), count);
        }

        return stats;
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    /**
     * Загрузить изображение обложки
     *
     * @param coverImage Файл изображения
     * @return Сохраненный MediaFile
     */
    private MediaFile uploadCoverImage(MultipartFile coverImage) {
        try {
            String fileName = fileStorageService.storeFile(coverImage, "projects/covers");
            MediaFile mediaFile = new MediaFile();
            mediaFile.setFileName(fileName);
            mediaFile.setFilePath("projects/covers/" + fileName);
            mediaFile.setMimeType(coverImage.getContentType());
            mediaFile.setSize(coverImage.getSize());
            // Здесь должен быть сохранен MediaFile через репозиторий,
            // но в рамках существующей системы предполагается использование сервиса
            return mediaFile;
        } catch (Exception e) {
            logger.error("Ошибка при загрузке обложки проекта", e);
            throw new ValidationException("Не удалось загрузить изображение обложки: " + e.getMessage());
        }
    }

    /**
     * Удалить изображение обложки
     *
     * @param coverImage Обложка для удаления
     */
    private void deleteCoverImage(MediaFile coverImage) {
        try {
            fileStorageService.deleteFile(coverImage.getFilePath());
            // Здесь должна быть логика удаления MediaFile из базы
        } catch (Exception e) {
            logger.warn("Не удалось удалить изображение обложки: {}", coverImage.getFilePath(), e);
        }
    }

    /**
     * Создать галерею для проекта
     *
     * @param projectTitle Название проекта
     * @return Созданная галерея
     */
    private PhotoGalleryItem createGalleryForProject(String projectTitle) {
        PhotoGallery gallery = new PhotoGallery();
        gallery.setTitle("Галерея проекта: " + projectTitle);
        gallery.setDescription("Фотографии проекта " + projectTitle);
        // Используем существующий сервис для сохранения галереи
        return photoGalleryService.save(gallery);
    }

    /**
     * Внутренний класс для статистики проектов
     */
    public static class ProjectStats {
        private long totalProjects;
        private long activeProjects;
        private long archiveProjects;
        private java.util.Map<String, Long> categoryCounts = new java.util.HashMap<>();

        public long getTotalProjects() {
            return totalProjects;
        }

        public void setTotalProjects(long totalProjects) {
            this.totalProjects = totalProjects;
        }

        public long getActiveProjects() {
            return activeProjects;
        }

        public void setActiveProjects(long activeProjects) {
            this.activeProjects = activeProjects;
        }

        public long getArchiveProjects() {
            return archiveProjects;
        }

        public void setArchiveProjects(long archiveProjects) {
            this.archiveProjects = archiveProjects;
        }

        public java.util.Map<String, Long> getCategoryCounts() {
            return categoryCounts;
        }

        public void setCategoryCounts(java.util.Map<String, Long> categoryCounts) {
            this.categoryCounts = categoryCounts;
        }

        public void addCategoryCount(String category, long count) {
            this.categoryCounts.put(category, count);
        }

        @Override
        public String toString() {
            return "ProjectStats{" +
                    "totalProjects=" + totalProjects +
                    ", activeProjects=" + activeProjects +
                    ", archiveProjects=" + archiveProjects +
                    ", categoryCounts=" + categoryCounts +
                    '}';
        }
    }
}

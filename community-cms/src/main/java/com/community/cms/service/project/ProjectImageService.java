package com.community.cms.service.project;

import com.community.cms.model.gallery.MediaFile;
import com.community.cms.model.project.Project;
import com.community.cms.model.project.ProjectImage;
import com.community.cms.repository.project.ProjectImageRepository;
import com.community.cms.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления изображениями проектов.
 *
 * <p>Предоставляет бизнес-логику для работы с изображениями проектов,
 * связывая проекты с медиафайлами через промежуточную сущность ProjectImage.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see ProjectImage
 * @see ProjectImageRepository
 */
@Service
@Transactional
public class ProjectImageService {

    private final ProjectImageRepository projectImageRepository;
    private final FileStorageService fileStorageService;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param projectImageRepository репозиторий для работы с изображениями проектов
     * @param fileStorageService сервис для работы с файлами
     */
    @Autowired
    public ProjectImageService(ProjectImageRepository projectImageRepository,
                               FileStorageService fileStorageService) {
        this.projectImageRepository = projectImageRepository;
        this.fileStorageService = fileStorageService;
    }

    // ================== CRUD ОПЕРАЦИИ ==================

    /**
     * Сохраняет изображение проекта.
     *
     * @param projectImage изображение проекта для сохранения
     * @return сохраненное изображение проекта
     */
    public ProjectImage save(ProjectImage projectImage) {
        validateProjectImage(projectImage);
        return projectImageRepository.save(projectImage);
    }

    /**
     * Обновляет существующее изображение проекта.
     *
     * @param projectImage изображение проекта для обновления
     * @return обновленное изображение проекта
     */
    public ProjectImage update(ProjectImage projectImage) {
        validateProjectImage(projectImage);
        return projectImageRepository.save(projectImage);
    }

    /**
     * Находит изображение проекта по ID.
     *
     * @param id идентификатор изображения проекта
     * @return Optional с изображением проекта, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<ProjectImage> findById(Long id) {
        return projectImageRepository.findById(id);
    }

    /**
     * Удаляет изображение проекта по ID.
     * Удаляет только связь, сам медиафайл остается в системе.
     *
     * @param id идентификатор изображения проекта для удаления
     */
    public void deleteById(Long id) {
        projectImageRepository.deleteById(id);
    }

    // ================== ПОИСК ПО ПРОЕКТУ ==================

    /**
     * Находит все изображения указанного проекта.
     *
     * @param project проект
     * @return список изображений проекта
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findByProject(Project project) {
        return projectImageRepository.findByProject(project);
    }

    /**
     * Находит все изображения проекта по ID проекта.
     *
     * @param projectId ID проекта
     * @return список изображений проекта
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findByProjectId(Long projectId) {
        return projectImageRepository.findByProjectId(projectId);
    }

    /**
     * Находит все изображения проекта, отсортированные по порядку сортировки.
     *
     * @param project проект
     * @return список изображений проекта (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findByProjectOrderBySortOrder(Project project) {
        return projectImageRepository.findByProjectOrderBySortOrderAsc(project);
    }

    /**
     * Находит все изображения проекта по ID проекта, отсортированные по порядку сортировки.
     *
     * @param projectId ID проекта
     * @return список изображений проекта (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findByProjectIdOrderBySortOrder(Long projectId) {
        return projectImageRepository.findByProjectIdOrderBySortOrderAsc(projectId);
    }

    // ================== ПОИСК ПО КАТЕГОРИИ ==================

    /**
     * Находит изображения проекта по категории.
     *
     * @param project проект
     * @param category категория изображений
     * @return список изображений проекта указанной категории
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findByProjectAndCategory(Project project, String category) {
        return projectImageRepository.findByProjectAndCategory(project, category);
    }

    /**
     * Находит изображения проекта по ID проекта и категории.
     *
     * @param projectId ID проекта
     * @param category категория изображений
     * @return список изображений проекта указанной категории
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findByProjectIdAndCategory(Long projectId, String category) {
        return projectImageRepository.findByProjectIdAndCategory(projectId, category);
    }

    /**
     * Находит изображения проекта по категории, отсортированные по порядку сортировки.
     *
     * @param project проект
     * @param category категория изображений
     * @return список изображений проекта указанной категории (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findByProjectAndCategoryOrderBySortOrder(Project project, String category) {
        return projectImageRepository.findByProjectAndCategoryOrderBySortOrderAsc(project, category);
    }

    // ================== ПОИСК ПО ФЛАГАМ ==================

    /**
     * Находит избранные изображения проекта.
     *
     * @param project проект
     * @return список избранных изображений проекта
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findFeaturedByProject(Project project) {
        return projectImageRepository.findByProjectAndIsFeaturedTrue(project);
    }

    /**
     * Находит избранные изображения проекта по ID проекта.
     *
     * @param projectId ID проекта
     * @return список избранных изображений проекта
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findFeaturedByProjectId(Long projectId) {
        return projectImageRepository.findByProjectIdAndIsFeaturedTrue(projectId);
    }

    /**
     * Находит избранные изображения проекта, отсортированные по порядку сортировки.
     *
     * @param project проект
     * @return список избранных изображений проекта (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findFeaturedByProjectOrderBySortOrder(Project project) {
        return projectImageRepository.findByProjectAndIsFeaturedTrueOrderBySortOrderAsc(project);
    }

    /**
     * Находит не избранные изображения проекта.
     *
     * @param project проект
     * @return список не избранных изображений проекта
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findNonFeaturedByProject(Project project) {
        return projectImageRepository.findByProjectAndIsFeaturedFalse(project);
    }

    // ================== ПОИСК ПО МЕДИАФАЙЛУ ==================

    /**
     * Находит изображения проекта по ID медиафайла.
     *
     * @param mediaFileId ID медиафайла
     * @return список изображений проекта, связанных с указанным медиафайлом
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findByMediaFileId(Long mediaFileId) {
        return projectImageRepository.findByMediaFileId(mediaFileId);
    }

    /**
     * Находит изображение проекта по проекту и ID медиафайла.
     *
     * @param project проект
     * @param mediaFileId ID медиафайла
     * @return Optional с изображением проекта, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<ProjectImage> findByProjectAndMediaFileId(Project project, Long mediaFileId) {
        return projectImageRepository.findByProjectAndMediaFileId(project, mediaFileId);
    }

    /**
     * Проверяет существование изображения проекта по ID проекта и ID медиафайла.
     *
     * @param projectId ID проекта
     * @param mediaFileId ID медиафайла
     * @return true если связь существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsByProjectIdAndMediaFileId(Long projectId, Long mediaFileId) {
        return projectImageRepository.existsByProjectIdAndMediaFileId(projectId, mediaFileId);
    }

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    /**
     * Находит главное изображение проекта (первое избранное).
     *
     * @param project проект
     * @return Optional с главным изображением проекта, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<ProjectImage> findMainImageByProject(Project project) {
        return projectImageRepository.findMainImageByProject(project);
    }

    /**
     * Находит первые N изображений проекта для слайдера.
     *
     * @param project проект
     * @param limit количество изображений
     * @return список изображений для слайдера
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findSliderImagesByProject(Project project, int limit) {
        return projectImageRepository.findSliderImagesByProject(project, limit);
    }

    /**
     * Находит первые N изображений проекта.
     *
     * @param project проект
     * @param limit количество изображений
     * @return список первых N изображений проекта
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findFirstNByProject(Project project, int limit) {
        return projectImageRepository.findFirstNByProject(project, limit);
    }

    /**
     * Находит изображения проекта без категории.
     *
     * @param project проект
     * @return список изображений проекта без категории
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findWithoutCategoryByProject(Project project) {
        return projectImageRepository.findByProjectAndCategoryIsNull(project);
    }

    /**
     * Находит изображения проекта без подписи.
     *
     * @param project проект
     * @return список изображений проекта без подписи
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findWithoutCaptionByProject(Project project) {
        return projectImageRepository.findWithoutCaptionByProject(project);
    }

    /**
     * Находит изображения проекта без альтернативного текста.
     *
     * @param project проект
     * @return список изображений проекта без alt text
     */
    @Transactional(readOnly = true)
    public List<ProjectImage> findWithoutAltTextByProject(Project project) {
        return projectImageRepository.findWithoutAltTextByProject(project);
    }

    // ================== СТАТИСТИКА ==================

    /**
     * Подсчитывает количество изображений проекта.
     *
     * @param project проект
     * @return количество изображений проекта
     */
    @Transactional(readOnly = true)
    public long countByProject(Project project) {
        return projectImageRepository.countByProject(project);
    }

    /**
     * Подсчитывает количество изображений по ID проекта.
     *
     * @param projectId ID проекта
     * @return количество изображений проекта
     */
    @Transactional(readOnly = true)
    public long countByProjectId(Long projectId) {
        return projectImageRepository.countByProjectId(projectId);
    }

    /**
     * Подсчитывает количество избранных изображений проекта.
     *
     * @param project проект
     * @return количество избранных изображений проекта
     */
    @Transactional(readOnly = true)
    public long countFeaturedByProject(Project project) {
        return projectImageRepository.countByProjectAndIsFeaturedTrue(project);
    }

    /**
     * Подсчитывает количество изображений проекта по категории.
     *
     * @param project проект
     * @param category категория изображений
     * @return количество изображений проекта указанной категории
     */
    @Transactional(readOnly = true)
    public long countByProjectAndCategory(Project project, String category) {
        return projectImageRepository.countByProjectAndCategory(project, category);
    }

    /**
     * Находит все уникальные категории изображений проекта.
     *
     * @param project проект
     * @return список уникальных категорий изображений проекта
     */
    @Transactional(readOnly = true)
    public List<String> findDistinctCategoriesByProject(Project project) {
        return projectImageRepository.findDistinctCategoriesByProject(project);
    }

    /**
     * Находит все уникальные категории изображений по ID проекта.
     *
     * @param projectId ID проекта
     * @return список уникальных категорий изображений проекта
     */
    @Transactional(readOnly = true)
    public List<String> findDistinctCategoriesByProjectId(Long projectId) {
        return projectImageRepository.findDistinctCategoriesByProjectId(projectId);
    }

    // ================== УДАЛЕНИЕ ПО СВЯЗЯМ ==================

    /**
     * Удаляет все изображения указанного проекта.
     *
     * @param project проект
     */
    public void deleteByProject(Project project) {
        projectImageRepository.deleteByProject(project);
    }

    /**
     * Удаляет все изображения по ID проекта.
     *
     * @param projectId ID проекта
     */
    public void deleteByProjectId(Long projectId) {
        projectImageRepository.deleteByProjectId(projectId);
    }

    /**
     * Удаляет изображения проекта по категории.
     *
     * @param project проект
     * @param category категория изображений
     */
    public void deleteByProjectAndCategory(Project project, String category) {
        projectImageRepository.deleteByProjectAndCategory(project, category);
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет бизнес-правила для изображения проекта.
     *
     * @param projectImage изображение проекта для валидации
     * @throws IllegalArgumentException если изображение проекта невалидно
     */
    private void validateProjectImage(ProjectImage projectImage) {
        if (projectImage == null) {
            throw new IllegalArgumentException("Изображение проекта не может быть null");
        }

        if (projectImage.getProject() == null) {
            throw new IllegalArgumentException("Изображение должно быть привязано к проекту");
        }

        if (projectImage.getMediaFile() == null) {
            throw new IllegalArgumentException("Изображение должно быть связано с медиафайлом");
        }

        if (projectImage.getSortOrder() == null) {
            projectImage.setSortOrder(0);
        }
    }

    /**
     * Создает новое изображение проекта.
     *
     * @param project проект
     * @param mediaFile медиафайл
     * @param caption подпись (опционально)
     * @param altText альтернативный текст (опционально)
     * @param category категория (опционально)
     * @param isFeatured флаг избранного изображения
     * @param sortOrder порядок сортировки
     * @return созданное изображение проекта
     */
    public ProjectImage createProjectImage(Project project, MediaFile mediaFile,
                                           String caption, String altText, String category,
                                           boolean isFeatured, Integer sortOrder) {
        ProjectImage projectImage = new ProjectImage(project, mediaFile, caption,
                altText, category, isFeatured, sortOrder);
        return save(projectImage);
    }

    /**
     * Делает изображение избранным и снимает флаг избранности с других изображений проекта.
     *
     * @param projectImage изображение для пометки как избранное
     * @return обновленное изображение проекта
     */
    public ProjectImage setAsFeatured(ProjectImage projectImage) {
        // Снимаем флаг избранности со всех изображений проекта
        List<ProjectImage> featuredImages = findFeaturedByProject(projectImage.getProject());
        for (ProjectImage featuredImage : featuredImages) {
            if (!featuredImage.getId().equals(projectImage.getId())) {
                featuredImage.setFeatured(false);
                projectImageRepository.save(featuredImage);
            }
        }

        // Устанавливаем флаг избранности для указанного изображения
        projectImage.setFeatured(true);
        return projectImageRepository.save(projectImage);
    }

    /**
     * Снимает флаг избранности с изображения.
     *
     * @param projectImage изображение для снятия флага избранности
     * @return обновленное изображение проекта
     */
    public ProjectImage removeFeatured(ProjectImage projectImage) {
        projectImage.setFeatured(false);
        return projectImageRepository.save(projectImage);
    }

    /**
     * Обновляет порядок сортировки изображений проекта.
     *
     * @param projectImages список изображений с обновленными sortOrder
     * @return список обновленных изображений
     */
    public List<ProjectImage> updateSortOrder(List<ProjectImage> projectImages) {
        return projectImageRepository.saveAll(projectImages);
    }
}
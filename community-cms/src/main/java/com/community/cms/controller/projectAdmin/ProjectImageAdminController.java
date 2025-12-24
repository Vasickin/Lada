package com.community.cms.controller.projectAdmin;

import com.community.cms.domain.model.media.MediaFile;
import com.community.cms.model.project.Project;
import com.community.cms.model.project.ProjectImage;
import com.community.cms.service.FileStorageService;
import com.community.cms.service.project.ProjectImageService;
import com.community.cms.service.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Контроллер для админ-панели управления изображениями проектов.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Controller
@RequestMapping("/admin/projects/{projectId}/images")
public class ProjectImageAdminController {

    private final ProjectImageService projectImageService;
    private final ProjectService projectService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ProjectImageAdminController(ProjectImageService projectImageService,
                                       ProjectService projectService,
                                       FileStorageService fileStorageService) {
        this.projectImageService = projectImageService;
        this.projectService = projectService;
        this.fileStorageService = fileStorageService;
    }

    // ================== СПИСОК ИЗОБРАЖЕНИЙ ПРОЕКТА ==================

    /**
     * Отображает список изображений проекта.
     */
    @GetMapping
    public String listImages(@PathVariable Long projectId,
                             Model model) {

        Project project = getProjectOrThrow(projectId);
        List<ProjectImage> images = projectImageService.findByProjectOrderBySortOrder(project);
        List<String> categories = projectImageService.findDistinctCategoriesByProject(project);

        model.addAttribute("project", project);
        model.addAttribute("images", images);
        model.addAttribute("categories", categories);

        return "admin/projects/images/list";
    }

    // ================== ДОБАВЛЕНИЕ ИЗОБРАЖЕНИЙ ==================

    /**
     * Отображает форму добавления изображений.
     */
    @GetMapping("/add")
    public String showAddForm(@PathVariable Long projectId,
                              Model model) {

        Project project = getProjectOrThrow(projectId);

        model.addAttribute("project", project);
        model.addAttribute("categories", getExistingCategories(projectId));

        return "admin/projects/images/add";
    }

    /**
     * Обрабатывает загрузку изображений.
     */
    @PostMapping("/upload")
    public String uploadImages(@PathVariable Long projectId,
                               @RequestParam("files") List<MultipartFile> files,
                               @RequestParam(value = "category", required = false) String category,
                               @RequestParam(value = "isFeatured", defaultValue = "false") boolean isFeatured,
                               RedirectAttributes redirectAttributes) {

        Project project = getProjectOrThrow(projectId);
        int uploadedCount = 0;
        int errorCount = 0;

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    // Сохраняем файл через существующий FileStorageService
                    String filePath = fileStorageService.storeFile(file);

                    // Создаем объект MediaFile
                    MediaFile mediaFile = new MediaFile(
                            file.getOriginalFilename(),
                            filePath,
                            file.getContentType(),
                            file.getSize()
                    );

                    // Создаем связь с проектом
                    ProjectImage projectImage = new ProjectImage();
                    projectImage.setProject(project);
                    projectImage.setMediaFile(mediaFile);
                    projectImage.setCategory(category);
                    projectImage.setFeatured(isFeatured);

                    // Устанавливаем порядок сортировки (последний)
                    int maxSortOrder = projectImageService.findByProject(project).size();
                    projectImage.setSortOrder(maxSortOrder + 1);

                    // Нужно сохранить сначала MediaFile, затем ProjectImage
                    // Предполагается, что у вас есть сервис для MediaFile
                    // mediaFileService.save(mediaFile);
                    projectImageService.save(projectImage);

                    uploadedCount++;

                } catch (Exception e) {
                    errorCount++;
                    // Логируйте ошибку для отладки
                    e.printStackTrace();
                }
            }
        }

        if (uploadedCount > 0) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Загружено изображений: " + uploadedCount);
        }
        if (errorCount > 0) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибок при загрузке: " + errorCount);
        }

        return "redirect:/admin/projects/" + projectId + "/images";
    }

    // ================== РЕДАКТИРОВАНИЕ ИЗОБРАЖЕНИЯ ==================

    /**
     * Отображает форму редактирования изображения.
     */
    @GetMapping("/edit/{imageId}")
    public String showEditForm(@PathVariable Long projectId,
                               @PathVariable Long imageId,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        Project project = getProjectOrThrow(projectId);

        return projectImageService.findById(imageId)
                .filter(image -> image.getProject().getId().equals(projectId))
                .map(image -> {
                    model.addAttribute("project", project);
                    model.addAttribute("image", image);
                    model.addAttribute("categories", getExistingCategories(projectId));
                    return "admin/projects/images/edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Изображение не найдено");
                    return "redirect:/admin/projects/" + projectId + "/images";
                });
    }

    /**
     * Обрабатывает обновление изображения.
     */
    @PostMapping("/update/{imageId}")
    public String updateImage(@PathVariable Long projectId,
                              @PathVariable Long imageId,
                              @ModelAttribute ProjectImage imageData,
                              RedirectAttributes redirectAttributes) {

        return projectImageService.findById(imageId)
                .filter(image -> image.getProject().getId().equals(projectId))
                .map(image -> {
                    try {
                        // Обновляем данные
                        image.setCaption(imageData.getCaption());
                        image.setAltText(imageData.getAltText());
                        image.setCategory(imageData.getCategory());
                        image.setSortOrder(imageData.getSortOrder());

                        projectImageService.update(image);
                        redirectAttributes.addFlashAttribute("successMessage", "Изображение обновлено");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
                    }
                    return "redirect:/admin/projects/" + projectId + "/images";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Изображение не найдено");
                    return "redirect:/admin/projects/" + projectId + "/images";
                });
    }

    // ================== УПРАВЛЕНИЕ ИЗБРАННЫМИ ИЗОБРАЖЕНИЯМИ ==================

    /**
     * Устанавливает изображение как избранное.
     */
    @PostMapping("/set-featured/{imageId}")
    public String setAsFeatured(@PathVariable Long projectId,
                                @PathVariable Long imageId,
                                RedirectAttributes redirectAttributes) {

        return projectImageService.findById(imageId)
                .filter(image -> image.getProject().getId().equals(projectId))
                .map(image -> {
                    try {
                        projectImageService.setAsFeatured(image);
                        redirectAttributes.addFlashAttribute("successMessage", "Изображение установлено как избранное");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
                    }
                    return "redirect:/admin/projects/" + projectId + "/images";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Изображение не найдено");
                    return "redirect:/admin/projects/" + projectId + "/images";
                });
    }

    /**
     * Снимает флаг избранного с изображения.
     */
    @PostMapping("/remove-featured/{imageId}")
    public String removeFeatured(@PathVariable Long projectId,
                                 @PathVariable Long imageId,
                                 RedirectAttributes redirectAttributes) {

        return projectImageService.findById(imageId)
                .filter(image -> image.getProject().getId().equals(projectId))
                .map(image -> {
                    try {
                        projectImageService.removeFeatured(image);
                        redirectAttributes.addFlashAttribute("successMessage", "Флаг избранного снят");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
                    }
                    return "redirect:/admin/projects/" + projectId + "/images";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Изображение не найдено");
                    return "redirect:/admin/projects/" + projectId + "/images";
                });
    }

    // ================== УДАЛЕНИЕ ИЗОБРАЖЕНИЯ ==================

    /**
     * Удаляет изображение.
     */
    @PostMapping("/delete/{imageId}")
    public String deleteImage(@PathVariable Long projectId,
                              @PathVariable Long imageId,
                              RedirectAttributes redirectAttributes) {

        try {
            // Проверяем что изображение принадлежит проекту
            boolean imageBelongsToProject = projectImageService.findById(imageId)
                    .filter(image -> image.getProject().getId().equals(projectId))
                    .isPresent();

            if (!imageBelongsToProject) {
                redirectAttributes.addFlashAttribute("errorMessage", "Изображение не найдено или не принадлежит проекту");
                return "redirect:/admin/projects/" + projectId + "/images";
            }

            projectImageService.deleteById(imageId);
            redirectAttributes.addFlashAttribute("successMessage", "Изображение удалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении: " + e.getMessage());
        }

        return "redirect:/admin/projects/" + projectId + "/images";
    }

    // ================== УПРАВЛЕНИЕ ПОРЯДКОМ СОРТИРОВКИ ==================

    /**
     * Обновляет порядок сортировки изображений.
     */
    @PostMapping("/update-order")
    public String updateImageOrder(@PathVariable Long projectId,
                                   @RequestParam("imageIds") List<Long> imageIds,
                                   RedirectAttributes redirectAttributes) {

        Project project = getProjectOrThrow(projectId);

        try {
            // Получаем все изображения проекта
            List<ProjectImage> images = projectImageService.findByProject(project);

            // Создаем Map для быстрого поиска по ID
            Map<Long, ProjectImage> imageMap = images.stream()
                    .collect(Collectors.toMap(ProjectImage::getId, img -> img));

            // Обновляем порядок сортировки
            for (int i = 0; i < imageIds.size(); i++) {
                Long imageId = imageIds.get(i);
                ProjectImage image = imageMap.get(imageId);
                if (image != null) {
                    image.setSortOrder(i + 1);
                }
            }

            // Сохраняем изменения
            projectImageService.updateSortOrder(images);
            redirectAttributes.addFlashAttribute("successMessage", "Порядок изображений обновлен");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении порядка: " + e.getMessage());
        }

        return "redirect:/admin/projects/" + projectId + "/images";
    }

    // ================== МАССОВЫЕ ОПЕРАЦИИ ==================

    /**
     * Обрабатывает массовые операции с изображениями.
     */
    @PostMapping("/batch")
    public String batchOperation(@PathVariable Long projectId,
                                 @RequestParam String action,
                                 @RequestParam(value = "ids", required = false) List<Long> ids,
                                 @RequestParam(value = "category", required = false) String category,
                                 RedirectAttributes redirectAttributes) {

        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не выбрано ни одного изображения");
            return "redirect:/admin/projects/" + projectId + "/images";
        }

        Project project = getProjectOrThrow(projectId);
        int successCount = 0;
        int errorCount = 0;

        for (Long imageId : ids) {
            try {
                // Проверяем что изображение принадлежит проекту
                boolean imageBelongsToProject = projectImageService.findById(imageId)
                        .filter(image -> image.getProject().getId().equals(projectId))
                        .isPresent();

                if (!imageBelongsToProject) {
                    errorCount++;
                    continue;
                }

                switch (action) {
                    case "delete":
                        projectImageService.deleteById(imageId);
                        successCount++;
                        break;
                    case "set-featured":
                        projectImageService.findById(imageId)
                                .ifPresent(projectImageService::setAsFeatured);
                        successCount++;
                        break;
                    case "remove-featured":
                        projectImageService.findById(imageId)
                                .ifPresent(projectImageService::removeFeatured);
                        successCount++;
                        break;
                    case "update-category":
                        projectImageService.findById(imageId)
                                .ifPresent(image -> {
                                    image.setCategory(category);
                                    projectImageService.update(image);
                                });
                        successCount++;
                        break;
                    default:
                        errorCount++;
                }
            } catch (Exception e) {
                errorCount++;
            }
        }

        if (successCount > 0) {
            String message = switch (action) {
                case "delete" -> "Удалено изображений: " + successCount;
                case "set-featured" -> "Установлено как избранное: " + successCount;
                case "remove-featured" -> "Снято флагов избранного: " + successCount;
                case "update-category" -> "Обновлена категория у изображений: " + successCount;
                default -> "Выполнено операций: " + successCount;
            };
            redirectAttributes.addFlashAttribute("successMessage", message);
        }

        if (errorCount > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибок: " + errorCount);
        }

        return "redirect:/admin/projects/" + projectId + "/images";
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    private Project getProjectOrThrow(Long projectId) {
        return projectService.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Проект с ID " + projectId + " не найден"));
    }

    private List<String> getExistingCategories(Long projectId) {
        Project project = getProjectOrThrow(projectId);
        List<String> categories = projectImageService.findDistinctCategoriesByProject(project);

        // Добавляем стандартные категории
        categories.add(0, "Главная");
        categories.add("Участники");
        categories.add("Мероприятия");
        categories.add("Результаты");
        categories.add("Другое");

        return categories.stream()
                .distinct()
                .toList();
    }
}

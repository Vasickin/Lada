package com.community.cms.controller.projectAdmin;

import com.community.cms.model.project.Project;
import com.community.cms.service.project.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Контроллер для админ-панели управления проектами.
 *
 * <p>Предоставляет CRUD интерфейс для управления проектами организации "ЛАДА"
 * через админ-панель. Все методы требуют аутентификации и соответствующих прав.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Controller
@RequestMapping("/admin/projects")
public class ProjectAdminController {

    private final ProjectService projectService;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param projectService сервис для работы с проектами
     */
    @Autowired
    public ProjectAdminController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // ================== СПИСОК ПРОЕКТОВ ==================

    /**
     * Отображает список всех проектов с пагинацией.
     *
     * @param model модель для передачи данных в шаблон
     * @param pageable параметры пагинации
     * @param status фильтр по статусу (опционально)
     * @param category фильтр по категории (опционально)
     * @param search поисковый запрос (опционально)
     * @return имя шаблона для отображения
     */
    @GetMapping
    public String listProjects(Model model,
                               @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) String search) {

        Page<Project> projectsPage;

        if (search != null && !search.trim().isEmpty()) {
            // Поиск проектов
            List<Project> projects = projectService.search(search);
            // Создаем Page из List для совместимости с пагинацией
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), projects.size());
            projectsPage = new PageImpl<>(
                    projects.subList(start, end),
                    pageable,
                    projects.size()
            );
            model.addAttribute("search", search);
        } else if (status != null && !status.trim().isEmpty() && category != null && !category.trim().isEmpty()) {
            // Фильтрация по статусу и категории - фильтруем вручную
            try {
                Project.ProjectStatus projectStatus = Project.ProjectStatus.valueOf(status.toUpperCase());
                // Сначала получаем по статусу, потом фильтруем по категории
                Page<Project> statusPage = projectService.findByStatus(projectStatus, pageable);
                List<Project> filtered = statusPage.getContent().stream()
                        .filter(p -> category.equals(p.getCategory()))
                        .toList();

                projectsPage = new PageImpl<>(
                        filtered,
                        pageable,
                        filtered.size()
                );
                model.addAttribute("status", status);
                model.addAttribute("category", category);
            } catch (IllegalArgumentException e) {
                projectsPage = projectService.findAll(pageable);
            }
        } else if (status != null && !status.trim().isEmpty()) {
            // Фильтрация только по статусу
            try {
                Project.ProjectStatus projectStatus = Project.ProjectStatus.valueOf(status.toUpperCase());
                projectsPage = projectService.findByStatus(projectStatus, pageable);
                model.addAttribute("status", status);
            } catch (IllegalArgumentException e) {
                projectsPage = projectService.findAll(pageable);
            }
        } else if (category != null && !category.trim().isEmpty()) {
            // Фильтрация только по категории
            // В сервисе нет метода findByCategory с Pageable, фильтруем вручную
            List<Project> allProjects = projectService.findAll();
            List<Project> filtered = allProjects.stream()
                    .filter(p -> category.equals(p.getCategory()))
                    .toList();

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filtered.size());
            projectsPage = new PageImpl<>(
                    filtered.subList(start, end),
                    pageable,
                    filtered.size()
            );
            model.addAttribute("category", category);
        } else {
            // Все проекты без фильтрации
            projectsPage = projectService.findAll(pageable);
        }

        model.addAttribute("projectsPage", projectsPage);
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", Project.ProjectStatus.values());

        return "admin/projects/list";
    }

    // ================== СОЗДАНИЕ ПРОЕКТА ==================

    /**
     * Отображает форму создания нового проекта.
     *
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона для отображения
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new Project());
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", Project.ProjectStatus.values());
        return "admin/projects/create";
    }

    /**
     * Обрабатывает создание нового проекта.
     *
     * @param project данные проекта из формы
     * @param bindingResult результат валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу списка или форму с ошибками
     */
    @PostMapping("/create")
    public String createProject(@Valid @ModelAttribute("project") Project project,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/projects/create";
        }

        // Проверка уникальности slug
        if (projectService.existsBySlug(project.getSlug())) {
            bindingResult.rejectValue("slug", "error.project", "Проект с таким URL уже существует");
            return "admin/projects/create";
        }

        try {
            projectService.save(project);
            redirectAttributes.addFlashAttribute("successMessage", "Проект успешно создан");
            return "redirect:/admin/projects";
        } catch (Exception e) {
            bindingResult.reject("error.project", "Ошибка при создании проекта: " + e.getMessage());
            return "admin/projects/create";
        }
    }

    // ================== РЕДАКТИРОВАНИЕ ПРОЕКТА ==================

    /**
     * Отображает форму редактирования проекта.
     *
     * @param id идентификатор проекта
     * @param model модель для передачи данных в шаблон
     * @param redirectAttributes атрибуты для редиректа
     * @return имя шаблона для отображения или редирект
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return projectService.findById(id)
                .map(project -> {
                    model.addAttribute("project", project);
                    model.addAttribute("categories", projectService.findAllDistinctCategories());
                    model.addAttribute("statuses", Project.ProjectStatus.values());
                    return "admin/projects/edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
                    return "redirect:/admin/projects";
                });
    }

    /**
     * Обрабатывает обновление проекта.
     *
     * @param id идентификатор проекта
     * @param project обновленные данные проекта
     * @param bindingResult результат валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу списка или форму с ошибками
     */
    @PostMapping("/edit/{id}")
    public String updateProject(@PathVariable Long id,
                                @Valid @ModelAttribute("project") Project project,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/projects/edit";
        }

        // Проверка что проект с таким ID существует
        if (!projectService.findById(id).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
            return "redirect:/admin/projects";
        }

        // Проверка уникальности slug (исключая текущий проект)
        projectService.findBySlug(project.getSlug())
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    bindingResult.rejectValue("slug", "error.project", "Проект с таким URL уже существует");
                });

        if (bindingResult.hasErrors()) {
            return "admin/projects/edit";
        }

        try {
            project.setId(id);
            projectService.update(project);
            redirectAttributes.addFlashAttribute("successMessage", "Проект успешно обновлен");
            return "redirect:/admin/projects";
        } catch (Exception e) {
            bindingResult.reject("error.project", "Ошибка при обновлении проекта: " + e.getMessage());
            return "admin/projects/edit";
        }
    }

    // ================== ПРОСМОТР ПРОЕКТА ==================

    /**
     * Отображает детальную информацию о проекте.
     *
     * @param id идентификатор проекта
     * @param model модель для передачи данных в шаблон
     * @param redirectAttributes атрибуты для редиректа
     * @return имя шаблона для отображения или редирект
     */
    @GetMapping("/view/{id}")
    public String viewProject(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return projectService.findById(id)
                .map(project -> {
                    model.addAttribute("project", project);
                    return "admin/projects/view";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
                    return "redirect:/admin/projects";
                });
    }

    // ================== УДАЛЕНИЕ ПРОЕКТА ==================

    /**
     * Удаляет проект.
     *
     * @param id идентификатор проекта
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу списка
     */
    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            projectService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Проект успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении проекта: " + e.getMessage());
        }
        return "redirect:/admin/projects";
    }

    // ================== УПРАВЛЕНИЕ СТАТУСОМ ==================

    /**
     * Активирует проект.
     *
     * @param id идентификатор проекта
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу списка
     */
    @PostMapping("/activate/{id}")
    public String activateProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return projectService.findById(id)
                .map(project -> {
                    try {
                        projectService.activate(project);
                        redirectAttributes.addFlashAttribute("successMessage", "Проект активирован");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при активации проекта: " + e.getMessage());
                    }
                    return "redirect:/admin/projects";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
                    return "redirect:/admin/projects";
                });
    }

    /**
     * Архивирует проект.
     *
     * @param id идентификатор проекта
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу списка
     */
    @PostMapping("/archive/{id}")
    public String archiveProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return projectService.findById(id)
                .map(project -> {
                    try {
                        projectService.archive(project);
                        redirectAttributes.addFlashAttribute("successMessage", "Проект архивирован");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при архивации проекта: " + e.getMessage());
                    }
                    return "redirect:/admin/projects";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
                    return "redirect:/admin/projects";
                });
    }

    /**
     * Помечает проект как ежегодный.
     *
     * @param id идентификатор проекта
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу списка
     */
    @PostMapping("/mark-annual/{id}")
    public String markAsAnnual(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return projectService.findById(id)
                .map(project -> {
                    try {
                        projectService.markAsAnnual(project);
                        redirectAttributes.addFlashAttribute("successMessage", "Проект помечен как ежегодный");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
                    }
                    return "redirect:/admin/projects";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
                    return "redirect:/admin/projects";
                });
    }

    // ================== МАССОВЫЕ ОПЕРАЦИИ ==================

    /**
     * Обрабатывает массовые операции с проектами.
     *
     * @param action действие (activate, archive, delete)
     * @param ids массив идентификаторов проектов
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу списка
     */
    @PostMapping("/batch")
    public String batchOperation(@RequestParam String action,
                                 @RequestParam("ids") List<Long> ids,
                                 RedirectAttributes redirectAttributes) {

        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не выбрано ни одного проекта");
            return "redirect:/admin/projects";
        }

        int successCount = 0;
        int errorCount = 0;

        for (Long id : ids) {
            try {
                switch (action) {
                    case "activate":
                        projectService.findById(id).ifPresent(projectService::activate);
                        successCount++;
                        break;
                    case "archive":
                        projectService.findById(id).ifPresent(projectService::archive);
                        successCount++;
                        break;
                    case "delete":
                        projectService.deleteById(id);
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
                case "activate" -> "Активировано проектов: " + successCount;
                case "archive" -> "Архивировано проектов: " + successCount;
                case "delete" -> "Удалено проектов: " + successCount;
                default -> "Выполнено операций: " + successCount;
            };
            redirectAttributes.addFlashAttribute("successMessage", message);
        }

        if (errorCount > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибок: " + errorCount);
        }

        return "redirect:/admin/projects";
    }

    // ================== ОЧИСТКА КЭША ==================

    /**
     * Очищает кэш проектов.
     *
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу списка
     */
    @PostMapping("/clear-cache")
    public String clearCache(RedirectAttributes redirectAttributes) {
        try {
            projectService.clearAllCache();
            redirectAttributes.addFlashAttribute("successMessage", "Кэш проектов очищен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при очистке кэша: " + e.getMessage());
        }
        return "redirect:/admin/projects";
    }
}

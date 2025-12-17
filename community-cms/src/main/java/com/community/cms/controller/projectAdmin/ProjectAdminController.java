package com.community.cms.controller.projectAdmin;

import com.community.cms.model.project.Project;
import com.community.cms.model.project.TeamMember;
import com.community.cms.service.project.ProjectService;
import com.community.cms.service.project.TeamMemberService;
import jakarta.persistence.EntityNotFoundException;
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


import java.util.*;
import java.util.stream.Collectors;

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
    private final TeamMemberService teamMemberService;


    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param projectService сервис для работы с проектами
     */
    @Autowired
    public ProjectAdminController(ProjectService projectService, TeamMemberService teamMemberService) {
        this.projectService = projectService;
        this.teamMemberService = teamMemberService;
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
        model.addAttribute("project", new Project()); // Только новый объект, без сохранения!
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", Project.ProjectStatus.values());
        model.addAttribute("allTeamMembers", teamMemberService.findAllActiveOrderBySortOrder());
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
                                RedirectAttributes redirectAttributes,
                                Model model,
                                @RequestParam(value = "newCategoryName", required = false) String newCategoryName,
                                @RequestParam(value = "selectedTeamMemberIds", required = false) String selectedTeamMemberIds) { // ← ДОБАВИТЬ ЭТОТ ПАРАМЕТР

        System.out.println("=== DEBUG CREATE PROJECT ===");
        System.out.println("Title: " + project.getTitle());
        System.out.println("Slug: " + project.getSlug());
        System.out.println("Category from select: " + project.getCategory());
        System.out.println("New category name: " + newCategoryName);
        System.out.println("Selected team member IDs: " + selectedTeamMemberIds); // ← ДОБАВИТЬ
        System.out.println("Has errors? " + bindingResult.hasErrors());

        // Восстанавливаем списки для формы (на случай ошибки)
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", Project.ProjectStatus.values());
        model.addAttribute("allTeamMembers", teamMemberService.findAllActiveOrderBySortOrder()); // ← ДОБАВИТЬ

        // ===== ОБРАБОТКА КАТЕГОРИИ =====
        // Если пользователь выбрал "Добавить новую категорию"
        if ("__NEW__".equals(project.getCategory())) {
            System.out.println("User selected: CREATE NEW CATEGORY");

            // Проверяем, что название новой категории указано
            if (newCategoryName == null || newCategoryName.trim().isEmpty()) {
                System.out.println("ERROR: New category name is empty!");
                bindingResult.rejectValue("category", "error.project",
                        "Введите название новой категории");
                return "admin/projects/create";
            } else {
                // Устанавливаем новую категорию в проект
                String cleanedCategory = newCategoryName.trim();
                System.out.println("Setting new category: " + cleanedCategory);
                project.setCategory(cleanedCategory);

                // ===== ПРОВЕРКА УНИКАЛЬНОСТИ КАТЕГОРИИ =====
                List<String> allCategories = projectService.findAllDistinctCategories();
                boolean alreadyExists = false;
                String existingCategory = null;

                for (String cat : allCategories) {
                    if (cat != null && cleanedCategory != null) {
                        // Нормализуем для сравнения (без регистра, без лишних пробелов)
                        String normalizedExisting = cat.trim().toLowerCase().replaceAll("\\s+", " ");
                        String normalizedNew = cleanedCategory.trim().toLowerCase().replaceAll("\\s+", " ");

                        if (normalizedExisting.equals(normalizedNew)) {
                            alreadyExists = true;
                            existingCategory = cat; // Запоминаем оригинальное написание
                            break;
                        }
                    }
                }

                if (alreadyExists) {
                    System.out.println("ERROR: Category already exists: " + existingCategory);
                    bindingResult.rejectValue("category", "error.project",
                            "Категория \"" + existingCategory + "\" уже существует. " +
                                    "Используйте существующую или введите другое название.");
                    return "admin/projects/create";
                }
                // ===== КОНЕЦ ПРОВЕРКИ УНИКАЛЬНОСТИ =====
            }
        }
// Если категория не выбрана вообще
        else if (project.getCategory() == null || project.getCategory().trim().isEmpty()) {
            System.out.println("ERROR: No category selected!");
            bindingResult.rejectValue("category", "error.project",
                    "Выберите категорию проекта");
            return "admin/projects/create";
        }

        if (bindingResult.hasErrors()) {
            System.out.println("Errors: " + bindingResult.getAllErrors());
            return "admin/projects/create";
        }

        // Проверка уникальности slug
        if (projectService.existsBySlug(project.getSlug())) {
            System.out.println("Slug already exists: " + project.getSlug());
            bindingResult.rejectValue("slug", "error.project", "Проект с таким URL уже существует");
            return "admin/projects/create";
        }

        try {
            System.out.println("Saving project with category: " + project.getCategory());
            Project savedProject = projectService.save(project); // Сохраняем проект

            // ===== ОБРАБОТКА ВЫБРАННЫХ ЧЛЕНОВ КОМАНДЫ =====
            if (selectedTeamMemberIds != null && !selectedTeamMemberIds.trim().isEmpty()) {
                System.out.println("Processing team members: " + selectedTeamMemberIds);
                String[] ids = selectedTeamMemberIds.split(",");

                for (String idStr : ids) {
                    try {
                        Long memberId = Long.parseLong(idStr.trim());
                        teamMemberService.findById(memberId).ifPresent(member -> {
                            // Добавляем проект к члену команды
                            if (member.getProjects() == null) {
                                member.setProjects(new HashSet<>());
                            }
                            member.getProjects().add(savedProject);
                            teamMemberService.save(member);
                            System.out.println("Added team member: " + member.getFullName() + " (ID: " + memberId + ")");
                        });
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid member ID format: " + idStr);
                    }
                }
            } else {
                System.out.println("No team members selected for project");
            }
            // ===== КОНЕЦ ОБРАБОТКИ КОМАНДЫ =====

            System.out.println("Project saved with ID: " + savedProject.getId());

            redirectAttributes.addFlashAttribute("successMessage", "Проект успешно создан" +
                    (selectedTeamMemberIds != null && !selectedTeamMemberIds.trim().isEmpty() ?
                            " с командой из " + selectedTeamMemberIds.split(",").length + " человек" : ""));
            return "redirect:/admin/projects";

        } catch (Exception e) {
            System.out.println("ERROR saving project: " + e.getMessage());
            e.printStackTrace();

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
                    // Получаем всех членов команды
                    List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();

                    // Получаем членов команды, уже входящих в этот проект
                    List<TeamMember> projectTeamMembers = teamMemberService.findByProject(project);

                    // Получаем членов команды, НЕ входящих в проект
                    List<TeamMember> availableMembers = allTeamMembers.stream()
                            .filter(member -> !projectTeamMembers.contains(member))
                            .collect(Collectors.toList());

                    model.addAttribute("project", project);
                    model.addAttribute("categories", projectService.findAllDistinctCategories());
                    model.addAttribute("statuses", Project.ProjectStatus.values());
                    model.addAttribute("allTeamMembers", allTeamMembers);
                    model.addAttribute("projectTeamMembers", projectTeamMembers); // ← ДОБАВИТЬ
                    model.addAttribute("availableMembers", availableMembers); // ← ДОБАВИТЬ

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
                                RedirectAttributes redirectAttributes,
                                @RequestParam(value = "selectedTeamMemberIds", required = false) String selectedTeamMemberIds) { // ← ДОБАВИТЬ ПАРАМЕТР

        if (bindingResult.hasErrors()) {
            return "admin/projects/edit";
        }

        // Проверка что проект с таким ID существует
        Optional<Project> existingProjectOpt = projectService.findById(id);
        if (!existingProjectOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Проект не найден");
            return "redirect:/admin/projects";
        }

        Project existingProject = existingProjectOpt.get();

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
            // Обновляем основные данные проекта
            project.setId(id);
            Project updatedProject = projectService.update(project);

            // ===== ОБРАБОТКА ОБНОВЛЕНИЯ КОМАНДЫ =====
            if (selectedTeamMemberIds != null) {
                System.out.println("Updating team members for project " + id + ": " + selectedTeamMemberIds);

                // Получаем текущих членов команды
                List<TeamMember> currentMembers = teamMemberService.findByProject(existingProject);

                // Удаляем всех текущих членов из проекта
                for (TeamMember member : currentMembers) {
                    member.getProjects().remove(existingProject);
                    teamMemberService.save(member);
                }

                // Добавляем новых членов (если есть)
                if (!selectedTeamMemberIds.trim().isEmpty()) {
                    String[] ids = selectedTeamMemberIds.split(",");
                    for (String idStr : ids) {
                        try {
                            Long memberId = Long.parseLong(idStr.trim());
                            teamMemberService.findById(memberId).ifPresent(member -> {
                                if (member.getProjects() == null) {
                                    member.setProjects(new HashSet<>());
                                }
                                member.getProjects().add(updatedProject);
                                teamMemberService.save(member);
                                System.out.println("Added team member to project: " + member.getFullName());
                            });
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid member ID format: " + idStr);
                        }
                    }
                }
            }
            // ===== КОНЕЦ ОБРАБОТКИ КОМАНДЫ =====

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

    // ProjectAdminController.java - добавьте эти методы:

    @GetMapping("/{id}/team-management")
    public String manageProjectTeam(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        // Получаем всех активных членов команды
        List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();

        // Получаем членов команды, уже входящих в этот проект
        List<TeamMember> projectTeamMembers = teamMemberService.findByProject(project);

        // Получаем членов команды, НЕ входящих в проект
        List<TeamMember> availableTeamMembers = allTeamMembers.stream()
                .filter(member -> !projectTeamMembers.contains(member))
                .collect(Collectors.toList());

        model.addAttribute("project", project);
        model.addAttribute("availableMembers", availableTeamMembers);
        model.addAttribute("projectMembers", projectTeamMembers);
        model.addAttribute("allTeamMembers", allTeamMembers);
        return "admin/projects/project-team-management";
    }

    @PostMapping("/{id}/team-management/update")
    public String updateProjectTeam(@PathVariable Long id,
                                    @RequestParam(value = "teamMemberIds", required = false) List<Long> teamMemberIds,
                                    RedirectAttributes redirectAttributes) {
        try {
            Project project = projectService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found"));

            // Получаем текущих членов команды проекта
            List<TeamMember> currentMembers = teamMemberService.findByProject(project);

            // Удаляем всех текущих членов из проекта
            for (TeamMember member : currentMembers) {
                member.getProjects().remove(project);
                teamMemberService.save(member);
            }

            // Добавляем новых членов (если есть)
            if (teamMemberIds != null && !teamMemberIds.isEmpty()) {
                for (Long memberId : teamMemberIds) {
                    TeamMember member = teamMemberService.findById(memberId)
                            .orElseThrow(() -> new EntityNotFoundException("TeamMember not found"));
                    member.getProjects().add(project);
                    teamMemberService.save(member);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Команда проекта успешно обновлена!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении команды: " + e.getMessage());
        }

        return "redirect:/admin/projects/" + id + "/team-management";
    }


}

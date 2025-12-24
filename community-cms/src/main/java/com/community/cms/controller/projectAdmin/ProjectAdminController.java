package com.community.cms.controller.projectAdmin;

import com.community.cms.domain.model.content.PhotoGallery;
import com.community.cms.dto.gallery.GalleryDTO;
import com.community.cms.dto.gallery.PhotoDTO;
import com.community.cms.domain.model.media.MediaFile;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.domain.repository.content.ProjectRepository;
import com.community.cms.service.gallery.PhotoGalleryService;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.service.project.TeamMemberService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
    private final ProjectRepository projectRepository;
    @Autowired
    private PhotoGalleryService photoGalleryService;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param projectService сервис для работы с проектами
     */
    @Autowired
    public ProjectAdminController(ProjectService projectService, TeamMemberService teamMemberService, ProjectRepository projectRepository) {
        this.projectService = projectService;
        this.teamMemberService = teamMemberService;
        this.projectRepository = projectRepository;
    }

    // ================== СПИСОК ПРОЕКТОВ ==================
    /**
     * Отображает список проектов с поддержкой фильтрации, поиска и пагинации.
     *
     * <p>Метод поддерживает следующие фильтры:
     * <ul>
     *   <li><strong>Поиск (search)</strong>: поиск по названию проекта (без учета регистра)</li>
     *   <li><strong>Категория (category)</strong>: фильтрация по категории проекта</li>
     *   <li><strong>Статус (status)</strong>: фильтрация по статусу проекта (ACTIVE, ANNUAL, ARCHIVED)</li>
     *   <li><strong>Год события (year)</strong>: фильтрация по году даты события проекта</li>
     * </ul>
     *
     * <p>Фильтры могут применяться как отдельно, так и в комбинации.
     * Все фильтры сохраняются при пагинации.</p>
     *
     * @param model модель для передачи данных в шаблон
     * @param pageable параметры пагинации (размер, номер страницы, сортировка)
     * @param status фильтр по статусу проекта (опционально)
     * @param category фильтр по категории проекта (опционально)
     * @param search поисковый запрос по названию проекта (опционально)
     * @param year фильтр по году события (опционально)
     * @return имя шаблона для отображения списка проектов
     */
    @GetMapping
    public String listProjects(Model model,
                               @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) Integer year) {

        Page<Project> projectsPage;

        // Логирование для отладки фильтров
        System.out.println("=== ПАРАМЕТРЫ ФИЛЬТРАЦИИ ===");
        System.out.println("Статус: " + status);
        System.out.println("Категория: " + category);
        System.out.println("Поиск: '" + search + "'");
        System.out.println("Год: " + year);

        try {
            // ================== СПЕЦИАЛЬНАЯ ОБРАБОТКА ПОИСКА ==================

            if (search != null && !search.trim().isEmpty()) {
                System.out.println("=== ВЫПОЛНЯЕМ ПОИСК ПО НАЗВАНИЮ ===");

                // ВРЕМЕННО: Используем простой поиск по названию вместо сломанного комплексного
                // Это решит проблему Hibernate 6 без изменения репозитория
                List<Project> searchResults = projectRepository.findByTitleContainingIgnoreCase(search.trim());
                System.out.println("Найдено проектов по названию '" + search + "': " + searchResults.size());

                // Для отладки: показываем найденные проекты
                if (searchResults.isEmpty()) {
                    System.out.println("НИЧЕГО не найдено по запросу: '" + search + "'");

                    // Проверим все проекты для отладки
                    List<Project> allProjects = projectRepository.findAll();
                    System.out.println("Всего проектов в базе: " + allProjects.size());
                    for (Project p : allProjects) {
                        System.out.println("Проект: '" + p.getTitle() + "' содержит '" + search + "': " +
                                p.getTitle().toLowerCase().contains(search.toLowerCase()));
                    }
                } else {
                    for (Project p : searchResults) {
                        System.out.println("Найден: " + p.getTitle() + " (ID: " + p.getId() + ")");
                    }
                }

                // Дополнительная фильтрация поисковых результатов
                List<Project> filteredProjects = new ArrayList<>(searchResults);

                // ФИЛЬТРАЦИЯ ПО СТАТУСУ (если указан)
                if (status != null && !status.trim().isEmpty() && !filteredProjects.isEmpty()) {
                    try {
                        Project.ProjectStatus projectStatus = Project.ProjectStatus.valueOf(status.toUpperCase());
                        filteredProjects = filteredProjects.stream()
                                .filter(p -> p.getStatus() == projectStatus)
                                .collect(Collectors.toList());
                        System.out.println("После фильтра статуса: " + filteredProjects.size() + " проектов");
                    } catch (IllegalArgumentException e) {
                        System.err.println("Некорректный статус: " + status);
                    }
                }

                // ФИЛЬТРАЦИЯ ПО КАТЕГОРИИ (если указана)
                if (category != null && !category.trim().isEmpty() && !category.equals("Все категории") && !filteredProjects.isEmpty()) {
                    filteredProjects = filteredProjects.stream()
                            .filter(p -> category.equals(p.getCategory()))
                            .collect(Collectors.toList());
                    System.out.println("После фильтра категории: " + filteredProjects.size() + " проектов");
                }

                // ФИЛЬТРАЦИЯ ПО ГОДУ СОБЫТИЯ (если указан)
                if (year != null && !filteredProjects.isEmpty()) {
                    filteredProjects = filteredProjects.stream()
                            .filter(p -> p.getEventDate() != null && p.getEventDate().getYear() == year)
                            .collect(Collectors.toList());
                    System.out.println("После фильтра года: " + filteredProjects.size() + " проектов");
                }

                // ПАГИНАЦИЯ для поисковых результатов
                filteredProjects.sort(Comparator.comparing(Project::getCreatedAt).reversed());

                int start = (int) pageable.getOffset();
                int totalItems = filteredProjects.size();

                if (start > totalItems) {
                    start = 0;
                }

                int end = Math.min((start + pageable.getPageSize()), totalItems);

                List<Project> pageContent;
                if (start >= totalItems || filteredProjects.isEmpty()) {
                    pageContent = Collections.emptyList();
                } else {
                    pageContent = filteredProjects.subList(start, end);
                }

                projectsPage = new PageImpl<>(pageContent, pageable, totalItems);

            } else {
                // ================== БЕЗ ПОИСКА ==================

                System.out.println("=== БЕЗ ПОИСКА - ФИЛЬТРАЦИЯ ВСЕХ ПРОЕКТОВ ===");

                List<Project> allProjects = projectRepository.findAll();
                System.out.println("Всего проектов: " + allProjects.size());

                List<Project> filteredProjects = new ArrayList<>(allProjects);

                // ФИЛЬТРАЦИЯ ПО СТАТУСУ
                if (status != null && !status.trim().isEmpty()) {
                    try {
                        Project.ProjectStatus projectStatus = Project.ProjectStatus.valueOf(status.toUpperCase());
                        filteredProjects = filteredProjects.stream()
                                .filter(p -> p.getStatus() == projectStatus)
                                .collect(Collectors.toList());
                        System.out.println("После статуса: " + filteredProjects.size() + " проектов");
                    } catch (IllegalArgumentException e) {
                        System.err.println("Некорректный статус: " + status);
                    }
                }

                // ФИЛЬТРАЦИЯ ПО КАТЕГОРИИ
                if (category != null && !category.trim().isEmpty() && !category.equals("Все категории")) {
                    filteredProjects = filteredProjects.stream()
                            .filter(p -> category.equals(p.getCategory()))
                            .collect(Collectors.toList());
                    System.out.println("После категории: " + filteredProjects.size() + " проектов");
                }

                // ФИЛЬТРАЦИЯ ПО ГОДУ СОБЫТИЯ
                if (year != null) {
                    filteredProjects = filteredProjects.stream()
                            .filter(p -> p.getEventDate() != null && p.getEventDate().getYear() == year)
                            .collect(Collectors.toList());
                    System.out.println("После года: " + filteredProjects.size() + " проектов");
                }

                // ПАГИНАЦИЯ
                filteredProjects.sort(Comparator.comparing(Project::getCreatedAt).reversed());

                int start = (int) pageable.getOffset();
                int totalItems = filteredProjects.size();

                if (start > totalItems) {
                    start = 0;
                }

                int end = Math.min((start + pageable.getPageSize()), totalItems);

                List<Project> pageContent;
                if (start >= totalItems || filteredProjects.isEmpty()) {
                    pageContent = Collections.emptyList();
                } else {
                    pageContent = filteredProjects.subList(start, end);
                }

                projectsPage = new PageImpl<>(pageContent, pageable, totalItems);
            }

            System.out.println("Итог: " + projectsPage.getTotalElements() + " проектов, " +
                    projectsPage.getTotalPages() + " страниц");

        } catch (Exception e) {
            System.err.println("ОШИБКА при фильтрации проектов: " + e.getMessage());
            e.printStackTrace();

            projectsPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            model.addAttribute("errorMessage", "Ошибка при поиске: " + e.getMessage());
        }

        // ================== ПОДГОТОВКА ДАННЫХ ДЛЯ ШАБЛОНА ==================

        model.addAttribute("projectsPage", projectsPage);
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", Project.ProjectStatus.values());

        // Годы для фильтра
        List<Integer> years = projectRepository.findAll().stream()
                .filter(p -> p.getEventDate() != null)
                .map(p -> p.getEventDate().getYear())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        model.addAttribute("years", years);

        // Сохраняем выбранные фильтры
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedSearch", search);

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
    @Transactional
    public String createProject(@Valid @ModelAttribute("project") Project project,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                @RequestParam(value = "newCategoryName", required = false) String newCategoryName,
                                @RequestParam(value = "selectedTeamMemberIds", required = false) String selectedTeamMemberIds,
                                @RequestParam(value = "selectedPhotoIds", required = false) String selectedPhotoIds,
                                // ↓ ДОБАВЛЯЕМ ЭТОТ ПАРАМЕТР ↓
                                @RequestParam(value = "videoUrl", required = false) String videoUrl) {

        // ===== ВАЛИДАЦИЯ ДАТ =====
        if (project.getStartDate() != null && project.getEndDate() != null) {
            if (project.getStartDate().isAfter(project.getEndDate())) {
                bindingResult.rejectValue("startDate", "error.project",
                        "Дата начала не может быть позже даты окончания");
            }
        }

        if (project.getEventDate() != null && project.getStartDate() != null && project.getEndDate() != null) {
            if (project.getEventDate().isBefore(project.getStartDate()) ||
                    project.getEventDate().isAfter(project.getEndDate())) {
                bindingResult.rejectValue("eventDate", "error.project",
                        "Дата события должна быть в рамках проекта");
            }
        }

        // ===== ЛОГИРОВАНИЕ =====
        System.out.println("=== CREATE PROJECT ===");
        System.out.println("Title: " + project.getTitle());
        System.out.println("New category: " + newCategoryName);
        System.out.println("Team IDs: " + selectedTeamMemberIds);
        System.out.println("Photo IDs: " + selectedPhotoIds);
        System.out.println("Video URL: " + videoUrl); // ← ДОБАВЛЯЕМ ЛОГ

        // Восстанавливаем списки для формы
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", Project.ProjectStatus.values());
        model.addAttribute("allTeamMembers", teamMemberService.findAllActiveOrderBySortOrder());

        // ===== ОБРАБОТКА КАТЕГОРИИ =====
        if ("__NEW__".equals(project.getCategory())) {
            if (newCategoryName == null || newCategoryName.trim().isEmpty()) {
                bindingResult.rejectValue("category", "error.project",
                        "Введите название новой категории");
                return "admin/projects/create";
            } else {
                String cleanedCategory = newCategoryName.trim();
                project.setCategory(cleanedCategory);

                // Проверка уникальности
                List<String> allCategories = projectRepository.findAllDistinctCategories();
                for (String cat : allCategories) {
                    if (cat != null && cleanedCategory != null) {
                        String normalizedExisting = cat.trim().toLowerCase().replaceAll("\\s+", " ");
                        String normalizedNew = cleanedCategory.trim().toLowerCase().replaceAll("\\s+", " ");
                        if (normalizedExisting.equals(normalizedNew)) {
                            bindingResult.rejectValue("category", "error.project",
                                    "Категория \"" + cat + "\" уже существует");
                            return "admin/projects/create";
                        }
                    }
                }
            }
        } else if (project.getCategory() == null || project.getCategory().trim().isEmpty()) {
            bindingResult.rejectValue("category", "error.project",
                    "Выберите категорию проекта");
            return "admin/projects/create";
        }

        if (bindingResult.hasErrors()) {
            return "admin/projects/create";
        }

        // Проверка уникальности slug
        if (projectService.existsBySlug(project.getSlug())) {
            bindingResult.rejectValue("slug", "error.project", "Проект с таким URL уже существует");
            return "admin/projects/create";
        }

        // ===== ДОБАВЛЯЕМ ОБРАБОТКУ ВИДЕО URL =====
        if (videoUrl != null && !videoUrl.trim().isEmpty()) {
            project.setVideoUrl(videoUrl.trim());
        }
        // ===== КОНЕЦ ОБРАБОТКИ ВИДЕО =====

        try {
            // Инициализация команды
            if (project.getTeamMembers() == null) {
                project.setTeamMembers(new HashSet<>());
            }

            // Сохранение проекта
            Project savedProject = projectService.save(project);

            // ===== ОБРАБОТКА КОМАНДЫ =====
            if (selectedTeamMemberIds != null && !selectedTeamMemberIds.trim().isEmpty()) {
                String[] ids = selectedTeamMemberIds.split(",");
                for (String idStr : ids) {
                    try {
                        Long memberId = Long.parseLong(idStr.trim());
                        teamMemberService.findById(memberId).ifPresent(member -> {
                            if (member.getProjects() == null) {
                                member.setProjects(new HashSet<>());
                            }
                            member.getProjects().add(savedProject);
                            savedProject.getTeamMembers().add(member);
                            teamMemberService.save(member);
                        });
                    } catch (NumberFormatException e) {
                        // Игнорируем некорректные ID
                    }
                }
                projectService.save(savedProject);
            }

            // ===== ОБРАБОТКА ФОТО =====
            if (selectedPhotoIds != null && !selectedPhotoIds.trim().isEmpty()) {
                try {
                    List<Long> photoIds = Arrays.stream(selectedPhotoIds.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::parseLong)
                            .limit(5)
                            .collect(Collectors.toList());

                    savedProject.setKeyPhotoIds(photoIds);
                    projectService.save(savedProject);

                } catch (Exception ignored) {}
            }


            redirectAttributes.addFlashAttribute("successMessage", "Проект успешно создан" +
                    (selectedTeamMemberIds != null && !selectedTeamMemberIds.trim().isEmpty() ?
                            " с командой из " + selectedTeamMemberIds.split(",").length + " человек" : "") +
                    (selectedPhotoIds != null && !selectedPhotoIds.trim().isEmpty() ?
                            " и " + selectedPhotoIds.split(",").length + " фото" : ""));

            return "redirect:/admin/projects";

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
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

                    // ДОБАВИТЬ: Получаем ключевые фото
                    List<Long> keyPhotoIds = project.getKeyPhotoIds();
                    if (keyPhotoIds != null && !keyPhotoIds.isEmpty()) {
                        System.out.println("Ключевые фото проекта " + id + ": " + keyPhotoIds);
                    }

                    model.addAttribute("project", project);
                    model.addAttribute("categories", projectService.findAllDistinctCategories());
                    model.addAttribute("statuses", Project.ProjectStatus.values());
                    model.addAttribute("allTeamMembers", allTeamMembers);
                    model.addAttribute("projectTeamMembers", projectTeamMembers); // ← ДОБАВИТЬ
                    model.addAttribute("availableMembers", availableMembers); // ← ДОБАВИТЬ
                    model.addAttribute("videoUrl", project.getVideoUrl());

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
    @Transactional
    public String updateProject(@PathVariable Long id,
                                @Valid @ModelAttribute("project") Project project,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                @RequestParam(value = "newCategoryName", required = false) String newCategoryName,
                                @RequestParam(value = "selectedTeamMemberIds", required = false) String selectedTeamMemberIds,
                                @RequestParam(value = "selectedPhotoIds", required = false) String selectedPhotoIds,
                                // ↓ ДОБАВЛЯЕМ ЭТОТ ПАРАМЕТР ↓
                                @RequestParam(value = "videoUrl", required = false) String videoUrl) {

        // ===== ВАЛИДАЦИЯ ДАТ =====
        if (project.getStartDate() != null && project.getEndDate() != null) {
            if (project.getStartDate().isAfter(project.getEndDate())) {
                System.out.println("ERROR: Start date is after end date");
                bindingResult.rejectValue("startDate", "error.project",
                        "Дата начала не может быть позже даты окончания");
            }
        }

        if (project.getEventDate() != null && project.getStartDate() != null && project.getEndDate() != null) {
            if (project.getEventDate().isBefore(project.getStartDate()) ||
                    project.getEventDate().isAfter(project.getEndDate())) {
                System.out.println("ERROR: Event date is outside project dates");
                bindingResult.rejectValue("eventDate", "error.project",
                        "Дата события должна быть в рамках проекта");
            }
        }
        // ===== КОНЕЦ ВАЛИДАЦИИ ДАТ =====

        System.out.println("=== DEBUG UPDATE PROJECT ===");
        System.out.println("Project ID: " + id);
        System.out.println("Title: " + project.getTitle());
        System.out.println("Selected team member IDs: " + selectedTeamMemberIds);
        System.out.println("Video URL: " + videoUrl); // ← ДОБАВЛЯЕМ ЛОГ
        System.out.println("Has errors? " + bindingResult.hasErrors());

        // Восстанавливаем списки для формы (на случай ошибки)
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("statuses", Project.ProjectStatus.values());
        model.addAttribute("allTeamMembers", teamMemberService.findAllActiveOrderBySortOrder());

        // ===== ОБРАБОТКА КАТЕГОРИИ =====
        if ("__NEW__".equals(project.getCategory())) {
            System.out.println("User selected: CREATE NEW CATEGORY");

            if (newCategoryName == null || newCategoryName.trim().isEmpty()) {
                System.out.println("ERROR: New category name is empty!");
                bindingResult.rejectValue("category", "error.project",
                        "Введите название новой категории");

                // Восстанавливаем данные команды для формы
                Project existingProject = projectService.findById(id).orElse(null);
                if (existingProject != null) {
                    List<TeamMember> projectTeamMembers = teamMemberService.findByProject(existingProject);
                    List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();
                    List<TeamMember> availableMembers = allTeamMembers.stream()
                            .filter(member -> !projectTeamMembers.contains(member))
                            .collect(Collectors.toList());

                    model.addAttribute("projectTeamMembers", projectTeamMembers);
                    model.addAttribute("availableMembers", availableMembers);
                }

                return "admin/projects/edit";
            } else {
                // Устанавливаем новую категорию в проект
                String cleanedCategory = newCategoryName.trim();
                System.out.println("Setting new category: " + cleanedCategory);
                project.setCategory(cleanedCategory);

                // Проверка уникальности категории
                List<String> allCategories = projectService.findAllDistinctCategories();
                boolean alreadyExists = false;
                String existingCategory = null;

                for (String cat : allCategories) {
                    if (cat != null && cleanedCategory != null) {
                        String normalizedExisting = cat.trim().toLowerCase().replaceAll("\\s+", " ");
                        String normalizedNew = cleanedCategory.trim().toLowerCase().replaceAll("\\s+", " ");

                        if (normalizedExisting.equals(normalizedNew)) {
                            alreadyExists = true;
                            existingCategory = cat;
                            break;
                        }
                    }
                }

                if (alreadyExists) {
                    System.out.println("ERROR: Category already exists: " + existingCategory);
                    bindingResult.rejectValue("category", "error.project",
                            "Категория \"" + existingCategory + "\" уже существует. " +
                                    "Используйте существующую или введите другое название.");

                    // Восстанавливаем данные команды для формы
                    Project existingProject = projectService.findById(id).orElse(null);
                    if (existingProject != null) {
                        List<TeamMember> projectTeamMembers = teamMemberService.findByProject(existingProject);
                        List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();
                        List<TeamMember> availableMembers = allTeamMembers.stream()
                                .filter(member -> !projectTeamMembers.contains(member))
                                .collect(Collectors.toList());

                        model.addAttribute("projectTeamMembers", projectTeamMembers);
                        model.addAttribute("availableMembers", availableMembers);
                    }

                    return "admin/projects/edit";
                }
            }
        } else if (project.getCategory() == null || project.getCategory().trim().isEmpty()) {
            System.out.println("ERROR: No category selected!");
            bindingResult.rejectValue("category", "error.project",
                    "Выберите категорию проекта");

            // Восстанавливаем данные команды для формы
            Project existingProject = projectService.findById(id).orElse(null);
            if (existingProject != null) {
                List<TeamMember> projectTeamMembers = teamMemberService.findByProject(existingProject);
                List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();
                List<TeamMember> availableMembers = allTeamMembers.stream()
                        .filter(member -> !projectTeamMembers.contains(member))
                        .collect(Collectors.toList());

                model.addAttribute("projectTeamMembers", projectTeamMembers);
                model.addAttribute("availableMembers", availableMembers);
            }

            return "admin/projects/edit";
        }

        if (bindingResult.hasErrors()) {
            System.out.println("Errors: " + bindingResult.getAllErrors());

            // Восстанавливаем данные команды для формы при ошибках валидации
            Project existingProject = projectService.findById(id).orElse(null);
            if (existingProject != null) {
                List<TeamMember> projectTeamMembers = teamMemberService.findByProject(existingProject);
                List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();
                List<TeamMember> availableMembers = allTeamMembers.stream()
                        .filter(member -> !projectTeamMembers.contains(member))
                        .collect(Collectors.toList());

                model.addAttribute("projectTeamMembers", projectTeamMembers);
                model.addAttribute("availableMembers", availableMembers);
            }

            return "admin/projects/edit";
        }

        // Проверка уникальности slug (исключая текущий проект)
        projectService.findBySlug(project.getSlug())
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    bindingResult.rejectValue("slug", "error.project", "Проект с таким URL уже существует");
                });

        if (bindingResult.hasErrors()) {
            // Восстанавливаем данные команды для формы
            Project existingProject = projectService.findById(id).orElse(null);
            if (existingProject != null) {
                List<TeamMember> projectTeamMembers = teamMemberService.findByProject(existingProject);
                List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();
                List<TeamMember> availableMembers = allTeamMembers.stream()
                        .filter(member -> !projectTeamMembers.contains(member))
                        .collect(Collectors.toList());

                model.addAttribute("projectTeamMembers", projectTeamMembers);
                model.addAttribute("availableMembers", availableMembers);
            }

            return "admin/projects/edit";
        }

        try {
            // Получаем существующий проект из БД
            Project existingProject = projectService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Проект не найден"));

            System.out.println("DEBUG: Found existing project with " + existingProject.getTeamMembers().size() + " team members");

            // ===== ДОБАВЛЯЕМ ОБРАБОТКУ ВИДЕО URL =====
            if (videoUrl != null) {
                existingProject.setVideoUrl(videoUrl.trim().isEmpty() ? null : videoUrl.trim());
            }
            // ===== КОНЕЦ ОБРАБОТКИ ВИДЕО =====

            // Обновляем основные поля проекта
            existingProject.setTitle(project.getTitle());
            existingProject.setSlug(project.getSlug());
            existingProject.setCategory(project.getCategory());
            existingProject.setStatus(project.getStatus());
            existingProject.setShortDescription(project.getShortDescription());
            existingProject.setFullDescription(project.getFullDescription());
            existingProject.setStartDate(project.getStartDate());
            existingProject.setEndDate(project.getEndDate());
            existingProject.setEventDate(project.getEventDate());
            existingProject.setLocation(project.getLocation());
            existingProject.setShowDescription(project.isShowDescription());
            existingProject.setShowPhotos(project.isShowPhotos());
            existingProject.setShowVideos(project.isShowVideos());
            existingProject.setShowTeam(project.isShowTeam());
            existingProject.setShowParticipation(project.isShowParticipation());
            existingProject.setShowPartners(project.isShowPartners());
            existingProject.setShowRelated(project.isShowRelated());

            // ===== ОБРАБОТКА ОБНОВЛЕНИЯ КОМАНДЫ =====
            System.out.println("DEBUG: Updating team members for project " + id);
            System.out.println("DEBUG: Selected team member IDs: " + selectedTeamMemberIds);

            // Получаем текущих членов команды проекта
            List<TeamMember> currentMembers = teamMemberService.findByProject(existingProject);
            System.out.println("DEBUG: Current team members count: " + currentMembers.size());

            // 1. Удаляем всех текущих членов из проекта
            for (TeamMember member : currentMembers) {
                // Удаляем проект из члена команды
                if (member.getProjects() != null) {
                    member.getProjects().remove(existingProject);
                    System.out.println("DEBUG: Removed project from member: " + member.getFullName());
                }
                // Сохраняем обновленного члена команды
                teamMemberService.save(member);
            }

            // 2. Очищаем команду в проекте
            existingProject.getTeamMembers().clear();
            System.out.println("DEBUG: Cleared team members from project");

            // 3. Добавляем новых членов (если есть)
            if (selectedTeamMemberIds != null && !selectedTeamMemberIds.trim().isEmpty()) {
                String[] ids = selectedTeamMemberIds.split(",");
                System.out.println("DEBUG: Adding " + ids.length + " new team members");

                for (String idStr : ids) {
                    try {
                        Long memberId = Long.parseLong(idStr.trim());
                        teamMemberService.findById(memberId).ifPresent(member -> {
                            // Добавляем проект к члену команды
                            if (member.getProjects() == null) {
                                member.setProjects(new HashSet<>());
                            }
                            if (!member.getProjects().contains(existingProject)) {
                                member.getProjects().add(existingProject);
                                System.out.println("DEBUG: Added project to member: " + member.getFullName());
                            }

                            // Добавляем члена команды к проекту
                            existingProject.getTeamMembers().add(member);

                            // Сохраняем обновленного члена команды
                            teamMemberService.save(member);
                            System.out.println("DEBUG: Saved member: " + member.getFullName() + " (ID: " + memberId + ")");
                        });
                    } catch (NumberFormatException e) {
                        System.out.println("ERROR: Invalid member ID format: " + idStr);
                    }
                }
            } else {
                System.out.println("DEBUG: No new team members selected");
            }

            // 4. Сохраняем проект с обновленной командой
            System.out.println("DEBUG: Saving project with " + existingProject.getTeamMembers().size() + " team members");
            Project updatedProject = projectService.save(existingProject);

            // 5. Проверяем результат
            System.out.println("DEBUG: Project saved with ID: " + updatedProject.getId());
            System.out.println("DEBUG: Final team members count: " + updatedProject.getTeamMembers().size());

            // 6. Получаем обновленный проект для проверки
            Project finalProject = projectService.findById(updatedProject.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Проект не найден после сохранения"));
            System.out.println("DEBUG: Verified team members count: " + finalProject.getTeamMembers().size());
            // ===== КОНЕЦ ОБРАБОТКИ КОМАНДЫ =====

            // ===== ОБРАБОТКА ФОТО =====
            if (selectedPhotoIds != null) {
                try {
                    List<Long> photoIds = Arrays.stream(selectedPhotoIds.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::parseLong)
                            .limit(5)
                            .collect(Collectors.toList());

                    existingProject.setKeyPhotoIds(photoIds);
                    projectService.save(existingProject);

                    System.out.println("Обновлены фото проекта: " + photoIds.size() + " шт.");
                } catch (Exception e) {
                    System.out.println("Ошибка обновления фото: " + e.getMessage());
                }
            }
            // ===== КОНЕЦ ОБРАБОТКИ ФОТО =====


            redirectAttributes.addFlashAttribute("successMessage", "Проект успешно обновлен" +
                    (selectedTeamMemberIds != null && !selectedTeamMemberIds.trim().isEmpty() ?
                            " с командой из " + selectedTeamMemberIds.split(",").length + " человек" : ""));
            return "redirect:/admin/projects";

        } catch (Exception e) {
            System.err.println("ERROR saving project: " + e.getMessage());
            e.printStackTrace();

            // Восстанавливаем данные команды для формы при ошибке
            Project existingProject = projectService.findById(id).orElse(null);
            if (existingProject != null) {
                List<TeamMember> projectTeamMembers = teamMemberService.findByProject(existingProject);
                List<TeamMember> allTeamMembers = teamMemberService.findAllActiveOrderBySortOrder();
                List<TeamMember> availableMembers = allTeamMembers.stream()
                        .filter(member -> !projectTeamMembers.contains(member))
                        .collect(Collectors.toList());

                model.addAttribute("projectTeamMembers", projectTeamMembers);
                model.addAttribute("availableMembers", availableMembers);
            }

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

    /**
     * Получает все доступные фото из всех галерей.
     * Gets all available photos from all galleries.
     *
     * @return список всех доступных фото / list of all available photos
     * @deprecated Используйте {@link #getAvailableGalleries()} и {@link #getGalleryPhotos(Long)} вместо этого
     * @deprecated Use {@link #getAvailableGalleries()} and {@link #getGalleryPhotos(Long)} instead
     */
    @GetMapping("/available-photos")
    @ResponseBody
    public List<PhotoDTO> getAvailablePhotos() {
        List<PhotoDTO> result = new ArrayList<>();

        try {
            // Получаем все галереи
            List<PhotoGallery> galleries = photoGalleryService.getAllPhotoGalleryItems();

            for (PhotoGallery gallery : galleries) {
                // Пропускаем неопубликованные галереи
                if (gallery.getPublished() != null && !gallery.getPublished()) {
                    continue;
                }

                // Добавляем все фото галереи
                List<MediaFile> photos = gallery.getImages();
                for (MediaFile photo : photos) {
                    PhotoDTO dto = new PhotoDTO(
                            photo.getId(),
                            photo.getFileName(),
                            photo.getWebPath(),
                            photo.getWebPath(), // thumbnail
                            photo.getFileName(),
                            gallery.getId(),
                            gallery.getTitle(),
                            gallery.getYear(),
                            photo.getIsPrimary()
                    );

                    result.add(dto);
                }
            }

            System.out.println("Отправлено фото (старый endpoint): " + result.size());

        } catch (Exception e) {
            System.err.println("Ошибка загрузки фото (старый endpoint): " + e.getMessage());
        }

        return result;
    }

    // ================== УПРАВЛЕНИЕ ФОТО ИЗ ГАЛЕРЕЙ ==================

    /**
     * Получает список всех доступных галерей с количеством фото.
     * Gets list of all available galleries with photo count.
     *
     * @return список галерей / list of galleries
     */
    @GetMapping("/available-galleries")
    @ResponseBody
    public List<GalleryDTO> getAvailableGalleries() {
        List<GalleryDTO> result = new ArrayList<>();

        try {
            // Получаем все элементы фото-галереи
            List<PhotoGallery> galleries = photoGalleryService.getAllPhotoGalleryItems();

            for (PhotoGallery gallery : galleries) {
                // Пропускаем неопубликованные галереи
                if (gallery.getPublished() != null && !gallery.getPublished()) {
                    continue;
                }

                GalleryDTO dto = new GalleryDTO(
                        gallery.getId(),
                        gallery.getTitle(),
                        gallery.getYear(),
                        gallery.getDescription(),
                        gallery.getImagesCount(),
                        gallery.getPublished()
                );

                result.add(dto);
            }

            // Сортируем по году (новые сначала)
            result.sort((a, b) -> b.getYear().compareTo(a.getYear()));

            System.out.println("Отправлено галерей: " + result.size());

        } catch (Exception e) {
            System.err.println("Ошибка получения списка галерей: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Получает все фото из указанной галереи.
     * Gets all photos from specified gallery.
     *
     * @param galleryId ID галереи / gallery ID
     * @return список фото галереи / list of gallery photos
     */
    @GetMapping("/gallery/{galleryId}/photos")
    @ResponseBody
    public List<PhotoDTO> getGalleryPhotos(@PathVariable Long galleryId) {
        List<PhotoDTO> result = new ArrayList<>();

        try {
            // Получаем галерею по ID
            PhotoGallery gallery = photoGalleryService.getPhotoGalleryItemById(galleryId);

            // Получаем все фото галереи
            List<MediaFile> photos = gallery.getImages();

            for (MediaFile photo : photos) {
                PhotoDTO dto = new PhotoDTO(
                        photo.getId(),
                        photo.getFileName(),
                        photo.getWebPath(),
                        photo.getWebPath(), // Используем webPath как thumbnail (можно оптимизировать позже)
                        photo.getFileName(),
                        gallery.getId(),
                        gallery.getTitle(),
                        gallery.getYear(),
                        photo.getIsPrimary()
                );

                result.add(dto);
            }

            System.out.println("Галерея " + galleryId + " (" + gallery.getTitle() + "): " + result.size() + " фото");

        } catch (EntityNotFoundException e) {
            System.err.println("Галерея не найдена: " + galleryId);
        } catch (Exception e) {
            System.err.println("Ошибка получения фото галереи " + galleryId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Получает информацию о конкретных фото по их ID.
     * Gets information about specific photos by their IDs.
     * Используется для отображения уже выбранных фото при редактировании.
     * Used to display already selected photos when editing.
     *
     * @param photoIds список ID фото через запятую / comma-separated list of photo IDs
     * @return список информации о фото / list of photo information
     */
    @GetMapping("/photos-info")
    @ResponseBody
    public List<PhotoDTO> getPhotosInfo(@RequestParam String photoIds) {
        List<PhotoDTO> result = new ArrayList<>();

        try {
            if (photoIds == null || photoIds.trim().isEmpty()) {
                return result;
            }

            // Парсим ID фото
            String[] ids = photoIds.split(",");

            for (String idStr : ids) {
                try {
                    Long photoId = Long.parseLong(idStr.trim());

                    // Находим фото и его галерею
                    // (Здесь нужен метод поиска фото по ID, пока используем существующий endpoint)
                    // TODO: Оптимизировать - добавить метод в PhotoGalleryService для поиска фото по ID

                } catch (NumberFormatException e) {
                    System.err.println("Некорректный ID фото: " + idStr);
                }
            }

        } catch (Exception e) {
            System.err.println("Ошибка получения информации о фото: " + e.getMessage());
        }

        return result;
    }

    /**
     * Метод для отладки поиска проектов.
     * Показывает, как работает поисковый механизм.
     */
    @GetMapping("/debug-search")
    @ResponseBody
    public Map<String, Object> debugSearch(@RequestParam String search) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("=== ДЕБАГ ПОИСКА ===");
            System.out.println("Запрос: '" + search + "'");

            // 1. Через сервис
            List<Project> serviceResults = projectService.search(search);
            System.out.println("Через сервис: " + serviceResults.size() + " результатов");

            List<Map<String, Object>> serviceData = new ArrayList<>();
            for (Project p : serviceResults) {
                Map<String, Object> projectData = new HashMap<>();
                projectData.put("id", p.getId());
                projectData.put("title", p.getTitle());
                projectData.put("slug", p.getSlug());
                projectData.put("shortDescription", p.getShortDescription());
                serviceData.add(projectData);
            }

            // 2. Через репозиторий напрямую
            List<Project> repoResults = projectRepository.searchByTitleOrDescription(search);
            System.out.println("Через репозиторий: " + repoResults.size() + " результатов");

            // 3. Простой поиск по названию
            List<Project> titleResults = projectRepository.findByTitleContainingIgnoreCase(search);
            System.out.println("Поиск по названию: " + titleResults.size() + " результатов");

            result.put("searchQuery", search);
            result.put("serviceResults", serviceData);
            result.put("serviceCount", serviceResults.size());
            result.put("repoCount", repoResults.size());
            result.put("titleSearchCount", titleResults.size());
            result.put("success", true);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }


}

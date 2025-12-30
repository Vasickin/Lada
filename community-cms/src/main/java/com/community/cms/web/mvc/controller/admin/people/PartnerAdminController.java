package com.community.cms.web.mvc.controller.admin.people;

import com.community.cms.domain.model.people.Partner;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.Partner.PartnerType;
import com.community.cms.domain.service.people.PartnerService;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.util.PartnerTypeUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер административной панели для управления партнерами проектов.
 */
@Controller
@RequestMapping("/admin/partners")
public class PartnerAdminController {

    private final PartnerService partnerService;
    private final ProjectService projectService;

    @Autowired
    public PartnerAdminController(PartnerService partnerService,
                                  ProjectService projectService) {
        this.partnerService = partnerService;
        this.projectService = projectService;
    }

    // ================== СПИСОК ВСЕХ ПАРТНЕРОВ С ФИЛЬТРАЦИЕЙ ==================

    /**
     * Отображает список всех партнеров с фильтрацией (единая точка входа).
     */
    @GetMapping
    public String listAllPartners(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String partnerType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String logoFilter,
            @RequestParam(required = false) String websiteFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        // Получаем все проекты для фильтра
        List<Project> allProjects = projectService.findAllActive();
        model.addAttribute("allProjects", allProjects);
        model.addAttribute("partnerTypes", PartnerType.values());

        // Ищем партнеров с учетом фильтров
        List<Partner> filteredPartners = applyFilters(search, partnerType, status, projectId, logoFilter, websiteFilter);

        // Пагинация вручную
        int totalItems = filteredPartners.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // Получаем страницу
        int start = page * size;
        int end = Math.min(start + size, totalItems);
        List<Partner> pagePartners = (start < totalItems) ?
                filteredPartners.subList(start, end) : new ArrayList<>();

        // Добавляем атрибуты в модель
        model.addAttribute("partners", pagePartners);
        model.addAttribute("selectedSearch", search);

        // Безопасное преобразование PartnerType с помощью утилиты
        PartnerType selectedType = null;
        if (partnerType != null && !partnerType.isEmpty()) {
            selectedType = PartnerTypeUtils.safeValueOf(partnerType);
        }
        model.addAttribute("selectedPartnerType", selectedType);

        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("selectedLogoFilter", logoFilter);
        model.addAttribute("selectedWebsiteFilter", websiteFilter);

        // Добавляем информацию о проекте для отображения названия
        if (projectId != null) {
            Optional<Project> projectOpt = projectService.findById(projectId);
            projectOpt.ifPresent(project ->
                    model.addAttribute("selectedProjectTitle", project.getTitle()));
        }

        // Пагинационные атрибуты
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPages", totalPages);

        model.addAttribute("title", "Все партнеры проектов");

        return "admin/partners/list";
    }

    /**
     * Применяет фильтры к списку партнеров.
     */
    private List<Partner> applyFilters(String search, String partnerType, String status,
                                       Long projectId, String logoFilter, String websiteFilter) {

        List<Partner> partners = new ArrayList<>();

        // 1. Базовый запрос - все партнеры
        if (search != null && !search.isEmpty()) {
            partners = partnerService.findByNameContaining(search);
        } else {
            // Получаем всех партнеров
            partners = partnerService.findByNameContaining("");
        }

        // 2. Фильтр по типу партнерства
        if (partnerType != null && !partnerType.isEmpty()) {
            PartnerType type = PartnerTypeUtils.safeValueOf(partnerType);
            final PartnerType filterType = type;
            partners = partners.stream()
                    .filter(p -> p.getPartnerType() == filterType)
                    .collect(Collectors.toList());
        }

        // 3. Фильтр по статусу активности
        if ("ACTIVE".equals(status)) {
            partners = partners.stream()
                    .filter(Partner::isActive)
                    .collect(Collectors.toList());
        } else if ("INACTIVE".equals(status)) {
            partners = partners.stream()
                    .filter(p -> !p.isActive())
                    .collect(Collectors.toList());
        }

        // 4. Фильтр по проекту
        if (projectId != null) {
            Optional<Project> projectOpt = projectService.findById(projectId);
            if (projectOpt.isPresent()) {
                Project project = projectOpt.get();
                List<Partner> projectPartners = partnerService.findByProject(project);
                // Пересечение с уже отфильтрованным списком
                List<Long> projectPartnerIds = projectPartners.stream()
                        .map(Partner::getId)
                        .collect(Collectors.toList());

                partners = partners.stream()
                        .filter(p -> projectPartnerIds.contains(p.getId()))
                        .collect(Collectors.toList());
            }
        }

        // 5. Фильтр по логотипу
        if ("yes".equals(logoFilter)) {
            partners = partners.stream()
                    .filter(Partner::hasLogo)
                    .collect(Collectors.toList());
        } else if ("no".equals(logoFilter)) {
            partners = partners.stream()
                    .filter(p -> !p.hasLogo())
                    .collect(Collectors.toList());
        }

        // 6. Фильтр по сайту
        if ("yes".equals(websiteFilter)) {
            partners = partners.stream()
                    .filter(Partner::hasWebsite)
                    .collect(Collectors.toList());
        } else if ("no".equals(websiteFilter)) {
            partners = partners.stream()
                    .filter(p -> !p.hasWebsite())
                    .collect(Collectors.toList());
        }

        return partners;
    }

    // ================== УПРОЩЕННЫЕ МЕТОДЫ ==================

    /**
     * Поиск партнеров по названию.
     */
    @GetMapping("/search")
    public String searchPartners(@RequestParam String searchTerm,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 Model model) {
        // Просто перенаправляем на основной метод с параметром поиска
        return "redirect:/admin/partners?search=" + searchTerm + "&page=" + page + "&size=" + size;
    }

    /**
     * Отображает партнеров по типу.
     */
    @GetMapping("/type/{partnerType}")
    public String listPartnersByType(@PathVariable String partnerType,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     Model model) {
        // Перенаправляем на основной метод с фильтром по типу
        return "redirect:/admin/partners?partnerType=" + partnerType + "&page=" + page + "&size=" + size;
    }

    /**
     * Партнеры конкретного проекта (упрощенный вариант).
     */
    @GetMapping("/project/{projectId}")
    public String listProjectPartners(@PathVariable Long projectId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      RedirectAttributes redirectAttributes) {
        // Просто перенаправляем на основной метод с фильтром по проекту
        return "redirect:/admin/partners?projectId=" + projectId + "&page=" + page + "&size=" + size;
    }

    // ================== СОЗДАНИЕ ПАРТНЕРА ==================

    /**
     * Отображает форму создания нового партнера.
     */
    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Long projectId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Partner partner = new Partner();

        if (projectId != null) {
            Optional<Project> projectOpt = projectService.findById(projectId);
            if (projectOpt.isPresent()) {
                Project project = projectOpt.get();
                // Предварительно устанавливаем проект для нового партнера
                partner.setProject(project);
                model.addAttribute("suggestedProjectTitle", project.getTitle());
            }
        }

        List<Project> projects = projectService.findAllActive();
        model.addAttribute("partner", partner);
        model.addAttribute("projects", projects);
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("title", "Создание партнера");

        return "admin/partners/create";
    }

    /**
     * Обрабатывает создание нового партнера.
     */
    @PostMapping("/create")
    public String createPartner(@Valid @ModelAttribute("partner") Partner partner,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        if (partner.getWebsiteUrl() != null && !partner.getWebsiteUrl().isEmpty()) {
            String url = partner.getWebsiteUrl().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://") &&
                    !url.startsWith("www.") && url.contains(".")) {
                partner.setWebsiteUrl("https://" + url);
            }
        }

        if (bindingResult.hasErrors()) {
            List<Project> projects = projectService.findAllActive();
            model.addAttribute("projects", projects);
            model.addAttribute("partnerTypes", PartnerType.values());
            model.addAttribute("title", "Создание партнера");
            return "admin/partners/create";
        }

        try {
            Partner savedPartner = partnerService.save(partner);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + savedPartner.getName() + "' успешно создан!");
            return "redirect:/admin/partners";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании партнера: " + e.getMessage());
            return "redirect:/admin/partners/create";
        }
    }

    // ================== РЕДАКТИРОВАНИЕ ПАРТНЕРА ==================

    /**
     * Отображает форму редактирования партнера.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<Partner> partnerOpt = partnerService.findById(id);

        if (partnerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Партнер с ID " + id + " не найден");
            return "redirect:/admin/partners";
        }

        Partner partner = partnerOpt.get();
        List<Project> projects = projectService.findAllActive();

        model.addAttribute("partner", partner);
        model.addAttribute("projects", projects);
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("title", "Редактирование партнера: " + partner.getName());

        return "admin/partners/edit";
    }

    /**
     * Обрабатывает обновление партнера.
     */
    @PostMapping("/edit/{id}")
    public String updatePartner(@PathVariable Long id,
                                @Valid @ModelAttribute("partner") Partner partner,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (partner.getWebsiteUrl() != null && !partner.getWebsiteUrl().isEmpty()) {
            String url = partner.getWebsiteUrl().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://") &&
                    !url.startsWith("www.") && url.contains(".")) {
                partner.setWebsiteUrl("https://" + url);
            }
        }

        if (bindingResult.hasErrors()) {
            partner.setId(id);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.partner", bindingResult);
            redirectAttributes.addFlashAttribute("partner", partner);
            redirectAttributes.addFlashAttribute("partnerTypes", PartnerType.values());
            return "redirect:/admin/partners/edit/" + id;
        }

        try {
            Optional<Partner> existingPartnerOpt = partnerService.findById(id);
            if (existingPartnerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Партнер с ID " + id + " не найден");
                return "redirect:/admin/partners";
            }

            Partner existingPartner = existingPartnerOpt.get();
            partner.setId(id);

            if (partner.getProject() == null) {
                partner.setProject(existingPartner.getProject());
            }

            Partner updatedPartner = partnerService.update(partner);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + updatedPartner.getName() + "' успешно обновлен!");
            return "redirect:/admin/partners";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении партнера: " + e.getMessage());
            return "redirect:/admin/partners/edit/" + id;
        }
    }

    // ================== УПРАВЛЕНИЕ АКТИВНОСТЬЮ ==================

    /**
     * Активирует партнера.
     */
    @PostMapping("/activate/{id}")
    public String activatePartner(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            Partner activatedPartner = partnerService.activateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + activatedPartner.getName() + "' активирован!");
            return "redirect:/admin/partners";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при активации партнера: " + e.getMessage());
            return "redirect:/admin/partners";
        }
    }

    /**
     * Деактивирует партнера.
     */
    @PostMapping("/deactivate/{id}")
    public String deactivatePartner(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            Partner deactivatedPartner = partnerService.deactivateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + deactivatedPartner.getName() + "' деактивирован!");
            return "redirect:/admin/partners";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при деактивации партнера: " + e.getMessage());
            return "redirect:/admin/partners";
        }
    }

    // ================== УДАЛЕНИЕ ПАРТНЕРА ==================

    /**
     * Отображает страницу подтверждения удаления партнера.
     */
    @GetMapping("/delete/{id}")
    public String showDeleteConfirmation(@PathVariable Long id,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        Optional<Partner> partnerOpt = partnerService.findById(id);

        if (partnerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Партнер с ID " + id + " не найден");
            return "redirect:/admin/partners";
        }

        Partner partner = partnerOpt.get();
        model.addAttribute("partner", partner);
        model.addAttribute("title", "Удаление партнера: " + partner.getName());

        return "admin/partners/delete";
    }

    /**
     * Обрабатывает удаление партнера.
     */
    @PostMapping("/delete/{id}")
    public String deletePartner(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Partner> partnerOpt = partnerService.findById(id);
            if (partnerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Партнер с ID " + id + " не найден");
                return "redirect:/admin/partners";
            }

            Partner partner = partnerOpt.get();
            String partnerName = partner.getName();

            partnerService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + partnerName + "' успешно удален!");
            return "redirect:/admin/partners";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении партнера: " + e.getMessage());
            return "redirect:/admin/partners";
        }
    }

    // ================== УПРАВЛЕНИЕ ПОРЯДКОМ СОРТИРОВКИ ==================

    /**
     * Обновляет порядок сортировки партнеров.
     */
    @PostMapping("/update-order/{projectId}")
    public String updatePartnerOrder(@PathVariable Long projectId,
                                     @RequestParam Long[] partnerIds,
                                     RedirectAttributes redirectAttributes) {
        try {
            Optional<Project> projectOpt = projectService.findById(projectId);
            if (projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Проект с ID " + projectId + " не найден");
                return "redirect:/admin/projects";
            }

            for (int i = 0; i < partnerIds.length; i++) {
                Optional<Partner> partnerOpt = partnerService.findById(partnerIds[i]);
                if (partnerOpt.isPresent()) {
                    Partner partner = partnerOpt.get();
                    partner.setSortOrder(i);
                    partnerService.update(partner);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Порядок партнеров успешно обновлен!");
            return "redirect:/admin/partners?projectId=" + projectId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении порядка партнеров: " + e.getMessage());
            return "redirect:/admin/partners?projectId=" + projectId;
        }
    }

    /**
     * Быстрое добавление партнера к проекту.
     */
    @PostMapping("/quick-add/{projectId}")
    public String quickAddPartner(@PathVariable Long projectId,
                                  @RequestParam String name,
                                  @RequestParam PartnerType partnerType,
                                  RedirectAttributes redirectAttributes) {
        try {
            Optional<Project> projectOpt = projectService.findById(projectId);
            if (projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Проект с ID " + projectId + " не найден");
                return "redirect:/admin/projects";
            }

            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Название партнера обязательно");
                return "redirect:/admin/partners?projectId=" + projectId;
            }

            Project project = projectOpt.get();
            Partner partner = new Partner(project, name.trim(), partnerType);
            partner.setActive(true);

            long partnerCount = partnerService.countByProject(project);
            partner.setSortOrder((int) partnerCount);

            partnerService.save(partner);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + name + "' быстро добавлен к проекту!");
            return "redirect:/admin/partners?projectId=" + projectId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при быстром добавлении партнера: " + e.getMessage());
            return "redirect:/admin/partners?projectId=" + projectId;
        }
    }
}
package com.community.cms.web.mvc.controller.admin.people;

import com.community.cms.domain.enums.PartnerType;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.Partner;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.domain.service.people.PartnerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер административной панели для управления партнерами.
 *
 * <p>Предоставляет интерфейс для создания, редактирования, удаления
 * и управления партнерами организации "ЛАДА". Интегрирован с системой
 * проектов для назначения партнеров на проекты.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see Partner
 * @see PartnerService
 */
@Controller
@RequestMapping("/admin/partners")
public class PartnerAdminController {

    private final PartnerService partnerService;
    private final ProjectService projectService;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param partnerService сервис для работы с партнерами
     * @param projectService сервис для работы с проектами
     */
    @Autowired
    public PartnerAdminController(PartnerService partnerService,
                                  ProjectService projectService) {
        this.partnerService = partnerService;
        this.projectService = projectService;
    }

    // ================== СПИСОК ПАРТНЕРОВ ==================

    /**
     * Отображает список всех партнеров.
     *
     * @param model модель для передачи данных в представление
     * @return шаблон списка партнеров
     */
    @GetMapping
    public String listPartners(Model model) {
        List<Partner> partners = partnerService.findAllActiveOrderBySortOrder();
        model.addAttribute("partners", partners);
        model.addAttribute("title", "Управление партнерами");
        model.addAttribute("partnerTypes", PartnerType.values());
        // НЕ добавляем showInactive здесь - оно будет null для активного списка
        return "admin/partners/list";
    }

    /**
     * Отображает список неактивных партнеров.
     *
     * @param model модель для передачи данных в представление
     * @return шаблон списка неактивных партнеров
     */
    @GetMapping("/inactive")
    public String listInactivePartners(Model model) {
        List<Partner> inactivePartners = partnerService.findAllInactive();
        model.addAttribute("partners", inactivePartners);
        model.addAttribute("title", "Неактивные партнеры");
        model.addAttribute("showInactive", true); // Только здесь добавляем
        model.addAttribute("partnerTypes", PartnerType.values());
        return "admin/partners/list";
    }

    /**
     * Отображает список партнеров по типу.
     *
     * @param type тип партнера
     * @param model модель для передачи данных в представление
     * @return шаблон списка партнеров по типу
     */
    @GetMapping("/type/{type}")
    public String listPartnersByType(@PathVariable String type, Model model) {
        PartnerType partnerType = PartnerType.fromString(type);
        if (partnerType == null) {
            return "redirect:/admin/partners";
        }

        List<Partner> partners = partnerService.findByType(partnerType);
        model.addAttribute("partners", partners);
        model.addAttribute("title", "Партнеры: " + partnerType.getDisplayName());
        model.addAttribute("filterType", partnerType);
        model.addAttribute("partnerTypes", PartnerType.values());
        return "admin/partners/list";
    }

    // ================== СОЗДАНИЕ ПАРТНЕРА ==================

    /**
     * Отображает форму создания нового партнера.
     *
     * @param model модель для передачи данных в представление
     * @return шаблон формы создания партнера
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("partner", new Partner());
        model.addAttribute("title", "Создание партнера");
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("allProjects", projectService.findAllActive());
        return "admin/partners/create";
    }

    /**
     * Обрабатывает создание нового партнера.
     *
     * @param partner создаваемый партнер
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров или форму с ошибками
     */
    @PostMapping("/create")
    public String createPartner(@Valid @ModelAttribute("partner") Partner partner,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
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
     *
     * @param id идентификатор партнера
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон формы редактирования или редирект с ошибкой
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
        model.addAttribute("partner", partner);
        model.addAttribute("title", "Редактирование: " + partner.getName());
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("allProjects", projectService.findAllActive());
        model.addAttribute("partnerProjects", partner.getProjects());

        return "admin/partners/edit";
    }

    /**
     * Обрабатывает обновление партнера.
     *
     * @param id идентификатор партнера
     * @param partner обновленные данные партнера
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров или форму с ошибками
     */
    @PostMapping("/edit/{id}")
    public String updatePartner(@PathVariable Long id,
                                @Valid @ModelAttribute("partner") Partner partner,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            partner.setId(id); // Сохраняем ID для формы
            return "admin/partners/edit";
        }

        try {
            // Проверяем существование партнера
            Optional<Partner> existingPartnerOpt = partnerService.findById(id);
            if (existingPartnerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Партнер с ID " + id + " не найден");
                return "redirect:/admin/partners";
            }

            // Сохраняем существующие связи с проектами
            Partner existingPartner = existingPartnerOpt.get();
            partner.setProjects(existingPartner.getProjects());
            partner.setId(id); // Убедимся, что ID сохраняется

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
     *
     * @param id идентификатор партнера
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров
     */
    @PostMapping("/activate/{id}")
    public String activatePartner(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            Partner activatedPartner = partnerService.activateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + activatedPartner.getName() + "' активирован!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при активации партнера: " + e.getMessage());
        }
        return "redirect:/admin/partners";
    }

    /**
     * Деактивирует партнера.
     *
     * @param id идентификатор партнера
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров
     */
    @PostMapping("/deactivate/{id}")
    public String deactivatePartner(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            Partner deactivatedPartner = partnerService.deactivateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + deactivatedPartner.getName() + "' деактивирован!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при деактивации партнера: " + e.getMessage());
        }
        return "redirect:/admin/partners";
    }

    // ================== УДАЛЕНИЕ ПАРТНЕРА ==================

    /**
     * Отображает страницу подтверждения удаления партнера.
     *
     * @param id идентификатор партнера
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон подтверждения удаления или редирект с ошибкой
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

        // Проверяем участие в проектах
        int projectCount = partner.getProjectsCount();
        model.addAttribute("projectCount", projectCount);

        return "admin/partners/delete";
    }

    /**
     * Обрабатывает удаление партнера.
     *
     * @param id идентификатор партнера
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров
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

            // Удаляем связи с проектами перед удалением
            partner.getProjects().clear();
            partnerService.update(partner);

            // Удаляем партнера
            partnerService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + partnerName + "' успешно удален!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении партнера: " + e.getMessage());
        }
        return "redirect:/admin/partners";
    }

    // ================== УПРАВЛЕНИЕ ПРОЕКТАМИ ==================

    /**
     * Отображает форму управления проектами партнера.
     *
     * @param id идентификатор партнера
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон управления проектами или редирект с ошибкой
     */
    @GetMapping("/projects/{id}")
    public String manageProjects(@PathVariable Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Optional<Partner> partnerOpt = partnerService.findById(id);

        if (partnerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Партнер с ID " + id + " не найден");
            return "redirect:/admin/partners";
        }

        Partner partner = partnerOpt.get();
        List<Project> allProjects = projectService.findAllActive();
        List<Project> partnerProjects = List.copyOf(partner.getProjects());
        List<Project> availableProjects = allProjects.stream()
                .filter(project -> !partnerProjects.contains(project))
                .toList();

        model.addAttribute("partner", partner);
        model.addAttribute("title", "Управление проектами: " + partner.getName());
        model.addAttribute("partnerProjects", partnerProjects);
        model.addAttribute("availableProjects", availableProjects);

        return "admin/partners/projects";
    }

    /**
     * Добавляет проект к партнеру.
     *
     * @param partnerId идентификатор партнера
     * @param projectId идентификатор проекта
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу управления проектами
     */
    @PostMapping("/projects/{partnerId}/add")
    public String addProjectToPartner(@PathVariable Long partnerId,
                                      @RequestParam Long projectId,
                                      RedirectAttributes redirectAttributes) {
        try {
            Optional<Partner> partnerOpt = partnerService.findById(partnerId);
            Optional<Project> projectOpt = projectService.findById(projectId);

            if (partnerOpt.isEmpty() || projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Партнер или проект не найден");
                return "redirect:/admin/partners/projects/" + partnerId;
            }

            Partner partner = partnerOpt.get();
            Project project = projectOpt.get();

            partnerService.addProjectToPartner(partner, project);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Проект '" + project.getTitle() + "' добавлен к партнеру '" +
                            partner.getName() + "'");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении проекта: " + e.getMessage());
        }

        return "redirect:/admin/partners/projects/" + partnerId;
    }

    /**
     * Удаляет проект у партнера.
     *
     * @param partnerId идентификатор партнера
     * @param projectId идентификатор проекта
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу управления проектами
     */
    @PostMapping("/projects/{partnerId}/remove")
    public String removeProjectFromPartner(@PathVariable Long partnerId,
                                           @RequestParam Long projectId,
                                           RedirectAttributes redirectAttributes) {
        try {
            Optional<Partner> partnerOpt = partnerService.findById(partnerId);
            Optional<Project> projectOpt = projectService.findById(projectId);

            if (partnerOpt.isEmpty() || projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Партнер или проект не найден");
                return "redirect:/admin/partners/projects/" + partnerId;
            }

            Partner partner = partnerOpt.get();
            Project project = projectOpt.get();

            partnerService.removeProjectFromPartner(partner, project);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Проект '" + project.getTitle() + "' удален у партнера '" +
                            partner.getName() + "'");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении проекта: " + e.getMessage());
        }

        return "redirect:/admin/partners/projects/" + partnerId;
    }

    // ================== ПОИСК И ФИЛЬТРАЦИЯ ==================

    /**
     * Поиск партнеров по названию или описанию.
     *
     * @param searchTerm поисковый запрос
     * @param model модель для передачи данных в представление
     * @return шаблон списка с результатами поиска
     */
    @GetMapping("/search")
    public String searchPartners(@RequestParam String searchTerm, Model model) {
        List<Partner> searchResults = partnerService.searchByNameOrDescription(searchTerm);
        model.addAttribute("partners", searchResults);
        model.addAttribute("title", "Результаты поиска: " + searchTerm);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("partnerTypes", PartnerType.values());
        return "admin/partners/list";
    }

    /**
     * Фильтрация партнеров по типу.
     *
     * @param type тип партнера
     * @param model модель для передачи данных в представление
     * @return шаблон списка с отфильтрованными партнерами
     */
    @GetMapping("/filter")
    public String filterByType(@RequestParam(required = false) PartnerType type,
                               Model model) {
        List<Partner> partners;
        if (type != null) {
            partners = partnerService.findByType(type);
            model.addAttribute("title", "Партнеры: " + type.getDisplayName());
            model.addAttribute("filterType", type);
        } else {
            partners = partnerService.findAllActiveOrderBySortOrder();
            model.addAttribute("title", "Все партнеры");
        }

        model.addAttribute("partners", partners);
        model.addAttribute("partnerTypes", PartnerType.values());
        return "admin/partners/list";
    }
}
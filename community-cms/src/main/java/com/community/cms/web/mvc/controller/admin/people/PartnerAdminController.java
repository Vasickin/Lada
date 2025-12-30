package com.community.cms.web.mvc.controller.admin.people;

import com.community.cms.domain.model.people.Partner;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.Partner.PartnerType;
import com.community.cms.domain.service.people.PartnerService;
import com.community.cms.domain.service.content.ProjectService;
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
 * Контроллер административной панели для управления партнерами проектов.
 *
 * <p>Предоставляет интерфейс для управления партнерами, связанными с проектами.
 * Поддерживает различные типы партнерства (спонсоры, информационные партнеры и т.д.).</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see Partner
 * @see PartnerService
 */
@Controller
@RequestMapping("/admin/project-partners")
public class PartnerAdminController {

    private final PartnerService partnerService;
    private final ProjectService projectService;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param partnerService сервис для работы с партнерами проектов
     * @param projectService сервис для работы с проектами
     */
    @Autowired
    public PartnerAdminController(PartnerService partnerService,
                                  ProjectService projectService) {
        this.partnerService = partnerService;
        this.projectService = projectService;
    }

    // ================== СПИСОК ПАРТНЕРОВ ПРОЕКТА ==================

    /**
     * Отображает список партнеров указанного проекта.
     *
     * @param projectId идентификатор проекта
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон списка партнеров или редирект с ошибкой
     */
    @GetMapping("/project/{projectId}")
    public String listProjectPartners(@PathVariable Long projectId,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Optional<Project> projectOpt = projectService.findById(projectId);

        if (projectOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Проект с ID " + projectId + " не найден");
            return "redirect:/admin/projects";
        }

        Project project = projectOpt.get();
        List<Partner> partners = partnerService.findActiveByProjectOrderBySortOrder(project);
        List<Partner> sponsors = partnerService.findSponsorsByProject(project);
        List<Partner> infoPartners = partnerService.findInformationPartnersByProject(project);

        model.addAttribute("project", project);
        model.addAttribute("partners", partners);
        model.addAttribute("sponsors", sponsors);
        model.addAttribute("infoPartners", infoPartners);
        model.addAttribute("title", "Партнеры проекта: " + project.getTitle());
        model.addAttribute("partnerTypes", PartnerType.values());

        return "admin/project-partners/list";
    }

    /**
     * Отображает список всех партнеров (без привязки к проекту).
     *
     * @param model модель для передачи данных в представление
     * @return шаблон списка всех партнеров
     */
    @GetMapping
    public String listAllPartners(Model model) {
        List<Partner> partners = partnerService.findActiveByNameContaining(""); // Все активные
        model.addAttribute("partners", partners);
        model.addAttribute("title", "Все партнеры проектов");
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("showAll", true);
        return "admin/project-partners/list";
    }

    /**
     * Отображает список партнеров по типу.
     *
     * @param partnerType тип партнерства
     * @param model модель для передачи данных в представление
     * @return шаблон списка партнеров по типу
     */
    @GetMapping("/type/{partnerType}")
    public String listPartnersByType(@PathVariable String partnerType,
                                     Model model) {
        try {
            PartnerType type = PartnerType.valueOf(partnerType.toUpperCase());
            List<Partner> partners = partnerService.findActiveByNameContaining("")
                    .stream()
                    .filter(partner -> partner.getPartnerType() == type)
                    .toList();

            model.addAttribute("partners", partners);
            model.addAttribute("title", "Партнеры: " + type.getNameRu());
            model.addAttribute("partnerType", type);
            model.addAttribute("partnerTypes", PartnerType.values());
            model.addAttribute("filterByType", true);

            return "admin/project-partners/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Неверный тип партнерства: " + partnerType);
            return listAllPartners(model);
        }
    }

    // ================== СОЗДАНИЕ ПАРТНЕРА ==================

    /**
     * Отображает форму создания нового партнера для проекта.
     *
     * @param projectId идентификатор проекта (опционально)
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон формы создания партнера
     */
    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Long projectId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Partner partner = new Partner();

        // Если указан projectId, привязываем партнера к проекту
        if (projectId != null) {
            Optional<Project> projectOpt = projectService.findById(projectId);
            if (projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Проект с ID " + projectId + " не найден");
                return "redirect:/admin/projects";
            }
            partner.setProject(projectOpt.get());
        }

        List<Project> projects = projectService.findAllActive();
        model.addAttribute("partner", partner);
        model.addAttribute("projects", projects);
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("title", "Создание партнера");

        return "admin/project-partners/create";
    }

    /**
     * Обрабатывает создание нового партнера.
     *
     * @param partner создаваемый партнер
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров проекта или форму с ошибками
     */
    @PostMapping("/create")
    public String createPartner(@Valid @ModelAttribute("partner") Partner partner,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        // Валидация URL сайта, если указан
        if (partner.getWebsiteUrl() != null && !partner.getWebsiteUrl().isEmpty()) {
            String url = partner.getWebsiteUrl().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://") &&
                    !url.startsWith("www.") && url.contains(".")) {
                // Добавляем https:// если нет протокола
                partner.setWebsiteUrl("https://" + url);
            }
        }

        if (bindingResult.hasErrors()) {
            List<Project> projects = projectService.findAllActive();
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.partner", bindingResult);
            redirectAttributes.addFlashAttribute("partner", partner);
            redirectAttributes.addFlashAttribute("projects", projects);
            redirectAttributes.addFlashAttribute("partnerTypes", PartnerType.values());
            return "redirect:/admin/project-partners/create" +
                    (partner.getProject() != null ? "?projectId=" + partner.getProject().getId() : "");
        }

        try {
            Partner savedPartner = partnerService.save(partner);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + savedPartner.getName() + "' успешно создан!");
            return "redirect:/admin/project-partners/project/" + savedPartner.getProject().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании партнера: " + e.getMessage());
            return "redirect:/admin/project-partners/create";
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
            return "redirect:/admin/project-partners";
        }

        Partner partner = partnerOpt.get();
        List<Project> projects = projectService.findAllActive();

        model.addAttribute("partner", partner);
        model.addAttribute("projects", projects);
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("title", "Редактирование партнера: " + partner.getName());

        return "admin/project-partners/edit";
    }

    /**
     * Обрабатывает обновление партнера.
     *
     * @param id идентификатор партнера
     * @param partner обновленные данные партнера
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров проекта или форму с ошибками
     */
    @PostMapping("/edit/{id}")
    public String updatePartner(@PathVariable Long id,
                                @Valid @ModelAttribute("partner") Partner partner,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        // Валидация URL сайта, если указан
        if (partner.getWebsiteUrl() != null && !partner.getWebsiteUrl().isEmpty()) {
            String url = partner.getWebsiteUrl().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://") &&
                    !url.startsWith("www.") && url.contains(".")) {
                // Добавляем https:// если нет протокола
                partner.setWebsiteUrl("https://" + url);
            }
        }

        if (bindingResult.hasErrors()) {
            partner.setId(id); // Сохраняем ID для формы
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.partner", bindingResult);
            redirectAttributes.addFlashAttribute("partner", partner);
            redirectAttributes.addFlashAttribute("partnerTypes", PartnerType.values());
            return "redirect:/admin/project-partners/edit/" + id;
        }

        try {
            // Проверяем существование партнера
            Optional<Partner> existingPartnerOpt = partnerService.findById(id);
            if (existingPartnerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Партнер с ID " + id + " не найден");
                return "redirect:/admin/project-partners";
            }

            Partner existingPartner = existingPartnerOpt.get();
            partner.setId(id);

            // Сохраняем проект, если он не установлен в форме
            if (partner.getProject() == null) {
                partner.setProject(existingPartner.getProject());
            }

            Partner updatedPartner = partnerService.update(partner);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + updatedPartner.getName() + "' успешно обновлен!");
            return "redirect:/admin/project-partners/project/" + updatedPartner.getProject().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении партнера: " + e.getMessage());
            return "redirect:/admin/project-partners/edit/" + id;
        }
    }

    // ================== УПРАВЛЕНИЕ АКТИВНОСТЬЮ ==================

    /**
     * Активирует партнера.
     *
     * @param id идентификатор партнера
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров проекта
     */
    @PostMapping("/activate/{id}")
    public String activatePartner(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            Partner activatedPartner = partnerService.activateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + activatedPartner.getName() + "' активирован!");
            return "redirect:/admin/project-partners/project/" + activatedPartner.getProject().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при активации партнера: " + e.getMessage());
            return "redirect:/admin/project-partners";
        }
    }

    /**
     * Деактивирует партнера.
     *
     * @param id идентификатор партнера
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров проекта
     */
    @PostMapping("/deactivate/{id}")
    public String deactivatePartner(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            Partner deactivatedPartner = partnerService.deactivateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + deactivatedPartner.getName() + "' деактивирован!");
            return "redirect:/admin/project-partners/project/" + deactivatedPartner.getProject().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при деактивации партнера: " + e.getMessage());
            return "redirect:/admin/project-partners";
        }
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
            return "redirect:/admin/project-partners";
        }

        Partner partner = partnerOpt.get();
        model.addAttribute("partner", partner);
        model.addAttribute("title", "Удаление партнера: " + partner.getName());

        return "admin/project-partners/delete";
    }

    /**
     * Обрабатывает удаление партнера.
     *
     * @param id идентификатор партнера
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров проекта
     */
    @PostMapping("/delete/{id}")
    public String deletePartner(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Partner> partnerOpt = partnerService.findById(id);
            if (partnerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Партнер с ID " + id + " не найден");
                return "redirect:/admin/project-partners";
            }

            Partner partner = partnerOpt.get();
            Long projectId = partner.getProject().getId();
            String partnerName = partner.getName();

            partnerService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + partnerName + "' успешно удален!");
            return "redirect:/admin/project-partners/project/" + projectId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении партнера: " + e.getMessage());
            return "redirect:/admin/project-partners";
        }
    }

    // ================== УПРАВЛЕНИЕ ПОРЯДКОМ СОРТИРОВКИ ==================

    /**
     * Обновляет порядок сортировки партнеров.
     *
     * @param projectId идентификатор проекта
     * @param partnerIds массив ID партнеров в новом порядке
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров проекта
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

            // Обновляем sortOrder для каждого партнера
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
            return "redirect:/admin/project-partners/project/" + projectId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении порядка партнеров: " + e.getMessage());
            return "redirect:/admin/project-partners/project/" + projectId;
        }
    }

    // ================== ПОИСК ПАРТНЕРОВ ==================

    /**
     * Поиск партнеров по названию.
     *
     * @param searchTerm поисковый запрос
     * @param model модель для передачи данных в представление
     * @return шаблон списка с результатами поиска
     */
    @GetMapping("/search")
    public String searchPartners(@RequestParam String searchTerm, Model model) {
        List<Partner> searchResults = partnerService.findByNameContaining(searchTerm);
        model.addAttribute("partners", searchResults);
        model.addAttribute("title", "Результаты поиска: " + searchTerm);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("partnerTypes", PartnerType.values());
        model.addAttribute("showAll", true);
        return "admin/project-partners/list";
    }

    /**
     * Быстрое добавление партнера к проекту (мини-форма).
     *
     * @param projectId идентификатор проекта
     * @param name название партнера
     * @param partnerType тип партнерства
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список партнеров проекта
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
                return "redirect:/admin/project-partners/project/" + projectId;
            }

            Project project = projectOpt.get();
            Partner partner = new Partner(project, name.trim(), partnerType);
            partner.setActive(true);

            // Определяем следующий порядок сортировки
            long partnerCount = partnerService.countByProject(project);
            partner.setSortOrder((int) partnerCount);

            partnerService.save(partner);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Партнер '" + name + "' быстро добавлен к проекту!");
            return "redirect:/admin/project-partners/project/" + projectId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при быстром добавлении партнера: " + e.getMessage());
            return "redirect:/admin/project-partners/project/" + projectId;
        }
    }
}

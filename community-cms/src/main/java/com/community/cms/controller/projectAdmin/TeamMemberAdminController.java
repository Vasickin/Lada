package com.community.cms.controller.projectAdmin;

import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.service.project.TeamMemberService;
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
 * Контроллер административной панели для управления членами команды.
 *
 * <p>Предоставляет интерфейс для создания, редактирования, удаления
 * и управления членами команды организации "ЛАДА". Интегрирован с системой
 * проектов для назначения участников на проекты с указанием ролей.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see TeamMember
 * @see TeamMemberService
 */
@Controller
@RequestMapping("/admin/team-members")
public class TeamMemberAdminController {

    private final TeamMemberService teamMemberService;
    private final ProjectService projectService;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param teamMemberService сервис для работы с членами команды
     * @param projectService сервис для работы с проектами
     */
    @Autowired
    public TeamMemberAdminController(TeamMemberService teamMemberService,
                                     ProjectService projectService) {
        this.teamMemberService = teamMemberService;
        this.projectService = projectService;
    }

    // ================== СПИСОК ЧЛЕНОВ КОМАНДЫ ==================

    /**
     * Отображает список всех членов команды.
     *
     * @param model модель для передачи данных в представление
     * @return шаблон списка членов команды
     */
    @GetMapping
    public String listTeamMembers(Model model) {
        List<TeamMember> teamMembers = teamMemberService.findAllActiveOrderBySortOrder();
        model.addAttribute("teamMembers", teamMembers);
        model.addAttribute("title", "Управление командой");
        return "admin/team-members/list";
    }

    /**
     * Отображает список неактивных членов команды.
     *
     * @param model модель для передачи данных в представление
     * @return шаблон списка неактивных членов команды
     */
    @GetMapping("/inactive")
    public String listInactiveTeamMembers(Model model) {
        List<TeamMember> inactiveMembers = teamMemberService.findAllInactive();
        model.addAttribute("teamMembers", inactiveMembers);
        model.addAttribute("title", "Неактивные члены команды");
        model.addAttribute("showInactive", true);
        return "admin/team-members/list";
    }

    // ================== СОЗДАНИЕ ЧЛЕНА КОМАНДЫ ==================

    /**
     * Отображает форму создания нового члена команды.
     *
     * @param model модель для передачи данных в представление
     * @return шаблон формы создания члена команды
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("teamMember", new TeamMember());
        model.addAttribute("title", "Создание члена команды");
        model.addAttribute("allProjects", projectService.findAllActive());
        return "admin/team-members/create";
    }

    /**
     * Обрабатывает создание нового члена команды.
     *
     * @param teamMember создаваемый член команды
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список членов команды или форму с ошибками
     */
    @PostMapping("/create")
    public String createTeamMember(@Valid @ModelAttribute("teamMember") TeamMember teamMember,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/team-members/create";
        }

        try {
            TeamMember savedMember = teamMemberService.save(teamMember);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Член команды '" + savedMember.getFullName() + "' успешно создан!");
            return "redirect:/admin/team-members";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании члена команды: " + e.getMessage());
            return "redirect:/admin/team-members/create";
        }
    }

    // ================== РЕДАКТИРОВАНИЕ ЧЛЕНА КОМАНДЫ ==================

    /**
     * Отображает форму редактирования члена команды.
     *
     * @param id идентификатор члена команды
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон формы редактирования или редирект с ошибкой
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);

        if (teamMemberOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Член команды с ID " + id + " не найден");
            return "redirect:/admin/team-members";
        }

        TeamMember teamMember = teamMemberOpt.get();
        model.addAttribute("teamMember", teamMember);
        model.addAttribute("title", "Редактирование: " + teamMember.getFullName());
        model.addAttribute("allProjects", projectService.findAllActive());
        model.addAttribute("memberProjects", teamMember.getProjects());

        return "admin/team-members/edit";
    }

    /**
     * Обрабатывает обновление члена команды.
     *
     * @param id идентификатор члена команды
     * @param teamMember обновленные данные члена команды
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список членов команды или форму с ошибками
     */
    @PostMapping("/edit/{id}")
    public String updateTeamMember(@PathVariable Long id,
                                   @Valid @ModelAttribute("teamMember") TeamMember teamMember,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            teamMember.setId(id); // Сохраняем ID для формы
            return "admin/team-members/edit";
        }

        try {
            // Проверяем существование члена команды
            Optional<TeamMember> existingMemberOpt = teamMemberService.findById(id);
            if (existingMemberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Член команды с ID " + id + " не найден");
                return "redirect:/admin/team-members";
            }

            // Сохраняем существующие связи с проектами
            TeamMember existingMember = existingMemberOpt.get();
            teamMember.setProjects(existingMember.getProjects());
            teamMember.setId(id); // Убедимся, что ID сохраняется

            TeamMember updatedMember = teamMemberService.update(teamMember);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Член команды '" + updatedMember.getFullName() + "' успешно обновлен!");
            return "redirect:/admin/team-members";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении члена команды: " + e.getMessage());
            return "redirect:/admin/team-members/edit/" + id;
        }
    }

    // ================== УПРАВЛЕНИЕ АКТИВНОСТЬЮ ==================

    /**
     * Активирует члена команды.
     *
     * @param id идентификатор члена команды
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список членов команды
     */
    @PostMapping("/activate/{id}")
    public String activateTeamMember(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            TeamMember activatedMember = teamMemberService.activateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Член команды '" + activatedMember.getFullName() + "' активирован!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при активации члена команды: " + e.getMessage());
        }
        return "redirect:/admin/team-members";
    }

    /**
     * Деактивирует члена команды.
     *
     * @param id идентификатор члена команды
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список членов команды
     */
    @PostMapping("/deactivate/{id}")
    public String deactivateTeamMember(@PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        try {
            TeamMember deactivatedMember = teamMemberService.deactivateById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Член команды '" + deactivatedMember.getFullName() + "' деактивирован!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при деактивации члена команды: " + e.getMessage());
        }
        return "redirect:/admin/team-members";
    }

    // ================== УДАЛЕНИЕ ЧЛЕНА КОМАНДЫ ==================

    /**
     * Отображает страницу подтверждения удаления члена команды.
     *
     * @param id идентификатор члена команды
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон подтверждения удаления или редирект с ошибкой
     */
    @GetMapping("/delete/{id}")
    public String showDeleteConfirmation(@PathVariable Long id,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);

        if (teamMemberOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Член команды с ID " + id + " не найден");
            return "redirect:/admin/team-members";
        }

        TeamMember teamMember = teamMemberOpt.get();
        model.addAttribute("teamMember", teamMember);
        model.addAttribute("title", "Удаление члена команды: " + teamMember.getFullName());

        // Проверяем участие в проектах
        int projectCount = teamMember.getProjectsCount();
        model.addAttribute("projectCount", projectCount);

        return "admin/team-members/delete";
    }

    /**
     * Обрабатывает удаление члена команды.
     *
     * @param id идентификатор члена команды
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список членов команды
     */
    @PostMapping("/delete/{id}")
    public String deleteTeamMember(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);
            if (teamMemberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Член команды с ID " + id + " не найден");
                return "redirect:/admin/team-members";
            }

            TeamMember teamMember = teamMemberOpt.get();
            String memberName = teamMember.getFullName();

            // Удаляем связи с проектами перед удалением
            teamMember.getProjects().clear();
            teamMemberService.update(teamMember);

            // Удаляем члена команды
            teamMemberService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Член команды '" + memberName + "' успешно удален!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении члена команды: " + e.getMessage());
        }
        return "redirect:/admin/team-members";
    }

    // ================== УПРАВЛЕНИЕ ПРОЕКТАМИ И РОЛЯМИ ==================

    /**
     * Отображает форму управления проектами члена команды.
     *
     * @param id идентификатор члена команды
     * @param model модель для передачи данных в представление
     * @param redirectAttributes атрибуты для редиректа
     * @return шаблон управления проектами или редирект с ошибкой
     */
    @GetMapping("/projects/{id}")
    public String manageProjects(@PathVariable Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);

        if (teamMemberOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Член команды с ID " + id + " не найден");
            return "redirect:/admin/team-members";
        }

        TeamMember teamMember = teamMemberOpt.get();
        List<Project> allProjects = projectService.findAllActive();
        List<Project> memberProjects = List.copyOf(teamMember.getProjects());
        List<Project> availableProjects = allProjects.stream()
                .filter(project -> !memberProjects.contains(project))
                .toList();

        model.addAttribute("teamMember", teamMember);
        model.addAttribute("title", "Управление проектами: " + teamMember.getFullName());
        model.addAttribute("memberProjects", memberProjects);
        model.addAttribute("availableProjects", availableProjects);

        return "admin/team-members/projects";
    }

    /**
     * Добавляет проект к члену команды.
     *
     * @param memberId идентификатор члена команды
     * @param projectId идентификатор проекта
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу управления проектами
     */
    @PostMapping("/projects/{memberId}/add")
    public String addProjectToMember(@PathVariable Long memberId,
                                     @RequestParam Long projectId,
                                     RedirectAttributes redirectAttributes) {
        try {
            Optional<TeamMember> teamMemberOpt = teamMemberService.findById(memberId);
            Optional<Project> projectOpt = projectService.findById(projectId);

            if (teamMemberOpt.isEmpty() || projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Член команды или проект не найден");
                return "redirect:/admin/team-members/projects/" + memberId;
            }

            TeamMember teamMember = teamMemberOpt.get();
            Project project = projectOpt.get();

            teamMemberService.addProjectToTeamMember(teamMember, project);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Проект '" + project.getTitle() + "' добавлен к члену команды '" +
                            teamMember.getFullName() + "'");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении проекта: " + e.getMessage());
        }

        return "redirect:/admin/team-members/projects/" + memberId;
    }

    /**
     * Удаляет проект у члена команды.
     *
     * @param memberId идентификатор члена команды
     * @param projectId идентификатор проекта
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу управления проектами
     */
    @PostMapping("/projects/{memberId}/remove")
    public String removeProjectFromMember(@PathVariable Long memberId,
                                          @RequestParam Long projectId,
                                          RedirectAttributes redirectAttributes) {
        try {
            Optional<TeamMember> teamMemberOpt = teamMemberService.findById(memberId);
            Optional<Project> projectOpt = projectService.findById(projectId);

            if (teamMemberOpt.isEmpty() || projectOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Член команды или проект не найден");
                return "redirect:/admin/team-members/projects/" + memberId;
            }

            TeamMember teamMember = teamMemberOpt.get();
            Project project = projectOpt.get();

            teamMemberService.removeProjectFromTeamMember(teamMember, project);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Проект '" + project.getTitle() + "' удален у члена команды '" +
                            teamMember.getFullName() + "'");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении проекта: " + e.getMessage());
        }

        return "redirect:/admin/team-members/projects/" + memberId;
    }

    /**
     * Устанавливает роль члена команды в проекте.
     *
     * @param memberId идентификатор члена команды
     * @param projectId идентификатор проекта
     * @param role роль в проекте
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу управления проектами
     */
    @PostMapping("/projects/{memberId}/role")
    public String setRoleForProject(@PathVariable Long memberId,
                                    @RequestParam Long projectId,
                                    @RequestParam String role,
                                    RedirectAttributes redirectAttributes) {
        try {
            Optional<TeamMember> teamMemberOpt = teamMemberService.findById(memberId);

            if (teamMemberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Член команды не найден");
                return "redirect:/admin/team-members";
            }

            TeamMember teamMember = teamMemberOpt.get();
            teamMemberService.setRoleForProject(teamMember, projectId, role);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Роль для проекта установлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при установке роли: " + e.getMessage());
        }

        return "redirect:/admin/team-members/projects/" + memberId;
    }

    // ================== ПОИСК И ФИЛЬТРАЦИЯ ==================

    /**
     * Поиск членов команды по имени или должности.
     *
     * @param searchTerm поисковый запрос
     * @param model модель для передачи данных в представление
     * @return шаблон списка с результатами поиска
     */
    @GetMapping("/search")
    public String searchTeamMembers(@RequestParam String searchTerm, Model model) {
        List<TeamMember> searchResults = teamMemberService.searchByNameOrPosition(searchTerm);
        model.addAttribute("teamMembers", searchResults);
        model.addAttribute("title", "Результаты поиска: " + searchTerm);
        model.addAttribute("searchTerm", searchTerm);
        return "admin/team-members/list";
    }
}

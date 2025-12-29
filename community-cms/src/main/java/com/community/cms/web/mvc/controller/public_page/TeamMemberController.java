package com.community.cms.web.mvc.controller.public_page;

import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.domain.service.content.ProjectService;
import com.community.cms.domain.service.people.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

/**
 * Публичный контроллер для отображения страницы "Наша команда" и информации о членах команды.
 *
 * <p>Предоставляет интерфейс для просмотра членов команды организации "ЛАДА",
 * их участия в проектах и ролей. Интегрирован с системой проектов для отображения
 * связей между членами команды и проектами.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see TeamMember
 * @see TeamMemberService
 */
@Controller
public class TeamMemberController {

    private final TeamMemberService teamMemberService;
    private final ProjectService projectService;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param teamMemberService сервис для работы с членами команды
     * @param projectService сервис для работы с проектами
     */
    @Autowired
    public TeamMemberController(TeamMemberService teamMemberService,
                                ProjectService projectService) {
        this.teamMemberService = teamMemberService;
        this.projectService = projectService;
    }

    // ================== СТРАНИЦА "НАША КОМАНДА" ==================

    /**
     * Отображает главную страницу "Наша команда" со списком всех активных членов команды.
     *
     * @param model модель для передачи данных в представление
     * @return шаблон страницы команды
     */
    @GetMapping("/team")
    public String teamPage(Model model) {
        List<TeamMember> teamMembers = teamMemberService.findAllActiveOrderBySortOrder();
        List<Project> allProjects = projectService.findAllActive();

        model.addAttribute("teamMembers", teamMembers);
        model.addAttribute("allProjects", allProjects);
        model.addAttribute("title", "Наша команда");
        model.addAttribute("pageDescription", "Команда организации 'ЛАДА' - талантливые люди, которые делают наши проекты возможными");

        return "team";
    }

    /**
     * Отображает страницу команды с фильтрацией по проекту.
     *
     * @param projectId идентификатор проекта для фильтрации
     * @param model модель для передачи данных в представление
     * @return шаблон страницы команды с фильтрацией
     */
    @GetMapping("/team/project/{projectId}")
    public String teamByProject(@PathVariable Long projectId,
                                Model model) {
        Optional<Project> projectOpt = projectService.findById(projectId);

        if (projectOpt.isEmpty()) {
            // Если проект не найден, показываем всех
            return "redirect:/team";
        }

        Project project = projectOpt.get();
        List<TeamMember> teamMembers = teamMemberService.findByProject(project);
        List<Project> allProjects = projectService.findAllActive();

        model.addAttribute("teamMembers", teamMembers);
        model.addAttribute("allProjects", allProjects);
        model.addAttribute("selectedProject", project);
        model.addAttribute("title", "Команда проекта: " + project.getTitle());
        model.addAttribute("pageDescription", "Команда проекта '" + project.getTitle() + "' организации 'ЛАДА'");

        return "team";
    }

    /**
     * Отображает список членов команды по должности.
     *
     * @param position должность для фильтрации
     * @param model модель для передачи данных в представление
     * @return шаблон страницы команды с фильтрацией по должности
     */
    @GetMapping("/team/position")
    public String teamByPosition(@RequestParam String position,
                                 Model model) {
        List<TeamMember> teamMembers = teamMemberService.findActiveByPositionContaining(position);
        List<Project> allProjects = projectService.findAllActive();

        model.addAttribute("teamMembers", teamMembers);
        model.addAttribute("allProjects", allProjects);
        model.addAttribute("selectedPosition", position);
        model.addAttribute("title", "Команда: " + position);
        model.addAttribute("pageDescription", "Члены команды организации 'ЛАДА' с должностью: " + position);

        return "team";
    }

    // ================== ДЕТАЛЬНАЯ СТРАНИЦА ЧЛЕНА КОМАНДЫ ==================

    /**
     * Отображает детальную страницу члена команды.
     *
     * @param id идентификатор члена команды
     * @param model модель для передачи данных в представление
     * @return шаблон детальной страницы или редирект на список
     */
    @GetMapping("/team/member/{id}")
    public String teamMemberDetail(@PathVariable Long id,
                                   Model model) {
        Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);

        if (teamMemberOpt.isEmpty() || !teamMemberOpt.get().isActive()) {
            // Если член команды не найден или не активен, редиректим на общую страницу
            return "redirect:/team";
        }

        TeamMember teamMember = teamMemberOpt.get();
        List<Project> memberProjects = List.copyOf(teamMember.getProjects());

        model.addAttribute("teamMember", teamMember);
        model.addAttribute("memberProjects", memberProjects);
        model.addAttribute("title", teamMember.getFullName() + " - " + teamMember.getPosition());
        model.addAttribute("pageDescription", teamMember.getFullName() + " - " +
                teamMember.getPosition() + " организации 'ЛАДА'. " +
                (teamMember.getBio() != null && teamMember.getBio().length() > 150 ?
                        teamMember.getBio().substring(0, 150) + "..." :
                        (teamMember.getBio() != null ? teamMember.getBio() : "")));

        return "team-member";
    }

    /**
     * Отображает детальную страницу члена команды по slug (альтернативный вариант).
     *
     * @param slug уникальный идентификатор члена команды
     * @param model модель для передачи данных в представление
     * @return шаблон детальной страницы или редирект на список
     */
    @GetMapping("/team/{slug}")
    public String teamMemberBySlug(@PathVariable String slug,
                                   Model model) {
        // TODO: Если в будущем добавится поле slug в TeamMember
        // Пока что используем ID-подход, но оставляем endpoint для будущего расширения

        // Временная реализация - пытаемся преобразовать slug в ID
        try {
            Long id = Long.parseLong(slug);
            return teamMemberDetail(id, model);
        } catch (NumberFormatException e) {
            // Если не число, ищем по имени (для будущей реализации)
            List<TeamMember> members = teamMemberService.findByNameContaining(slug.replace("-", " "));
            if (!members.isEmpty() && members.get(0).isActive()) {
                return teamMemberDetail(members.get(0).getId(), model);
            }
            return "redirect:/team";
        }
    }

    // ================== РУКОВОДСТВО И КЛЮЧЕВЫЕ СОТРУДНИКИ ==================

    /**
     * Отображает страницу руководства организации.
     *
     * @param model модель для передачи данных в представление
     * @return шаблон страницы руководства
     */
    @GetMapping("/team/leadership")
    public String leadershipTeam(Model model) {
        // Ищем ключевых членов команды (сортировка по sortOrder)
        List<TeamMember> teamMembers = teamMemberService.findAllActiveOrderBySortOrder();

        // Фильтруем руководство (первые N в списке или по определенным должностям)
        List<TeamMember> leadership = teamMembers.stream()
                .filter(member -> member.getSortOrder() < 10 ||
                        isLeadershipPosition(member.getPosition()))
                .toList();

        model.addAttribute("teamMembers", leadership);
        model.addAttribute("title", "Руководство организации");
        model.addAttribute("pageDescription", "Руководство организации 'ЛАДА' - люди, которые определяют стратегию и направление развития");
        model.addAttribute("isLeadershipPage", true);

        return "team-leadership";
    }

    /**
     * Проверяет, является ли должность руководящей.
     *
     * @param position должность для проверки
     * @return true если должность считается руководящей
     */
    private boolean isLeadershipPosition(String position) {
        if (position == null) return false;

        String lowerPosition = position.toLowerCase();
        return lowerPosition.contains("руководитель") ||
                lowerPosition.contains("директор") ||
                lowerPosition.contains("координатор") ||
                lowerPosition.contains("глава") ||
                lowerPosition.contains("начальник") ||
                lowerPosition.contains("заведующий");
    }

    // ================== ПОИСК ЧЛЕНОВ КОМАНДЫ ==================

    /**
     * Поиск членов команды по имени или должности.
     *
     * @param query поисковый запрос
     * @param model модель для передачи данных в представление
     * @return шаблон страницы с результатами поиска
     */
    @GetMapping("/team/search")
    public String searchTeamMembers(@RequestParam String query,
                                    Model model) {
        List<TeamMember> searchResults = teamMemberService.searchActiveByNameOrPosition(query);
        List<Project> allProjects = projectService.findAllActive();

        model.addAttribute("teamMembers", searchResults);
        model.addAttribute("allProjects", allProjects);
        model.addAttribute("searchQuery", query);
        model.addAttribute("title", "Результаты поиска: " + query);
        model.addAttribute("pageDescription", "Поиск членов команды организации 'ЛАДА' по запросу: " + query);
        model.addAttribute("isSearchResults", true);

        return "team";
    }

    // ================== ВИДЖЕТЫ И ДОПОЛНИТЕЛЬНЫЕ СТРАНИЦЫ ==================

    /**
     * Отображает виджет команды для встраивания на другие страницы.
     *
     * @param limit ограничение количества отображаемых членов команды
     * @param model модель для передачи данных в представление
     * @return шаблон виджета команды
     */
    @GetMapping("/team/widget")
    public String teamWidget(@RequestParam(defaultValue = "6") int limit,
                             Model model) {
        List<TeamMember> teamMembers = teamMemberService.findKeyTeamMembers(limit);

        model.addAttribute("teamMembers", teamMembers);
        model.addAttribute("isWidget", true);

        return "fragments/team-widget";
    }

    /**
     * Отображает последних добавленных членов команды.
     *
     * @param model модель для передачи данных в представление
     * @return шаблон страницы новых членов команды
     */
    @GetMapping("/team/new")
    public String newTeamMembers(Model model) {
        List<TeamMember> newMembers = teamMemberService.findRecentTeamMembers(10);
        List<Project> allProjects = projectService.findAllActive();

        model.addAttribute("teamMembers", newMembers);
        model.addAttribute("allProjects", allProjects);
        model.addAttribute("title", "Новые члены команды");
        model.addAttribute("pageDescription", "Новые участники команды организации 'ЛАДА'");

        return "team-new";
    }

    // ================== AJAX ENDPOINTS ДЛЯ ДИНАМИЧЕСКОЙ ЗАГРУЗКИ ==================

    /**
     * Возвращает JSON с информацией о члене команды (AJAX endpoint).
     *
     * @param id идентификатор члена команды
     * @return JSON представление члена команды
     */
    @GetMapping("/api/team/member/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public TeamMember getTeamMemberJson(@PathVariable Long id) {
        Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);
        return teamMemberOpt.orElse(null);
    }

    /**
     * Возвращает JSON со списком членов команды проекта (AJAX endpoint).
     *
     * @param projectId идентификатор проекта
     * @return JSON список членов команды проекта
     */
    @GetMapping("/api/team/project/{projectId}")
    @org.springframework.web.bind.annotation.ResponseBody
    public List<TeamMember> getTeamMembersByProjectJson(@PathVariable Long projectId) {
        return teamMemberService.findByProjectId(projectId);
    }
}

package com.community.cms.controller;

import com.community.cms.model.project.Project;
import com.community.cms.model.project.Project.ProjectStatus;
import com.community.cms.model.project.ProjectArticle;
import com.community.cms.model.project.TeamMember;
import com.community.cms.service.project.ProjectArticleService;
import com.community.cms.service.project.ProjectImageService;
import com.community.cms.service.project.ProjectPartnerService;
import com.community.cms.service.project.ProjectService;
import com.community.cms.service.project.ProjectVideoService;
import com.community.cms.service.project.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для публичной части проектов организации "ЛАДА".
 * Использует английское написание "projekts" для избежания конфликтов.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Controller
@RequestMapping("/projekts") // ИЗМЕНЕНО: projects → projekts
public class ProjectController { // ИЗМЕНЕНО: ProjectController → ProjectController

    private final ProjectService projectService;
    private final ProjectArticleService projectArticleService;
    private final ProjectImageService projectImageService;
    private final ProjectVideoService projectVideoService;
    private final ProjectPartnerService projectPartnerService;
    private final TeamMemberService teamMemberService;

    @Autowired
    public ProjectController(ProjectService projectService,
                             ProjectArticleService projectArticleService,
                             ProjectImageService projectImageService,
                             ProjectVideoService projectVideoService,
                             ProjectPartnerService projectPartnerService,
                             TeamMemberService teamMemberService) {
        this.projectService = projectService;
        this.projectArticleService = projectArticleService;
        this.projectImageService = projectImageService;
        this.projectVideoService = projectVideoService;
        this.projectPartnerService = projectPartnerService;
        this.teamMemberService = teamMemberService;
    }

    // ================== ГЛАВНАЯ СТРАНИЦА ПРОЕКТОВ ==================

    @GetMapping
    public String listProjects(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "12") int size,
                               @RequestParam(required = false) String category,
                               @RequestParam(defaultValue = "date") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<Project> projektsPage = getProjektsPage(category, pageable);

        model.addAttribute("projektsPage", projektsPage);
        model.addAttribute("featuredProjekts", projectService.findFeaturedProjects(6));
        model.addAttribute("recentProjekts", projectService.findRecentProjects(6));
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);

        if (category != null) {
            model.addAttribute("category", category);
        }

        return "projekts/list";
    }

    // ================== ДЕТАЛЬНАЯ СТРАНИЦА ПРОЕКТА ==================

    @GetMapping("/{slug}")
    public String viewProject(@PathVariable String slug, Model model) {
        Optional<Project> projectOpt = projectService.findBySlugForPublic(slug);

        if (projectOpt.isEmpty()) {
            return "redirect:/projekts";
        }

        Project project = projectOpt.get();
        loadProjectDetails(project, model);

        model.addAttribute("project", project);
        model.addAttribute("currentDate", LocalDate.now());

        return "projekts/view";
    }

    // ================== ФИЛЬТРЫ И СПЕЦИАЛЬНЫЕ СТРАНИЦЫ ==================

    @GetMapping("/kategoria/{category}")
    public String projektsByCategory(@PathVariable String category,
                                     Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Project> projektsPage = projectService.findActiveByCategory(category, pageable);

        if (projektsPage.isEmpty()) {
            return "redirect:/projekts";
        }

        model.addAttribute("projectsPage", projektsPage);
        model.addAttribute("category", category);
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("currentPage", page);

        return "projekts/kategoria";
    }

    @GetMapping("/arhiv")
    public String archivedProjects(Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Project> projektsPage = projectService.findByStatus(ProjectStatus.ARCHIVED, pageable);

        model.addAttribute("projectsPage", projektsPage);
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("isArchive", true);

        return "projekts/arhiv";
    }

    @GetMapping("/ezhegodnye")
    public String annualProjects(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").ascending());
        Page<Project> projektsPage = projectService.findByStatus(ProjectStatus.ANNUAL, pageable);

        model.addAttribute("projektsPage", projektsPage);
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("isAnnual", true);

        return "projekts/ezhegodnye";
    }

    @GetMapping("/predstoyashchie")
    public String upcomingEvents(Model model) {
        LocalDate today = LocalDate.now();
        List<Project> upcomingEvents = projectService.findUpcomingEvents(today);

        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("currentDate", today);

        return "projekts/predstoyashchie";
    }

    @GetMapping("/proshyedshie")
    public String pastEvents(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size) {

        LocalDate today = LocalDate.now();
        List<Project> pastEvents = projectService.findPastEvents(today);
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> eventsPage = createPageFromList(pastEvents, pageable);

        model.addAttribute("eventsPage", eventsPage);
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("currentDate", today);

        return "projekts/proshyedshie";
    }

    @GetMapping("/poisk")
    public String searchProjects(@RequestParam String query,
                                 Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size) {

        if (query == null || query.trim().isEmpty()) {
            return "redirect:/projekts";
        }

        Pageable pageable = PageRequest.of(page, size);
        List<Project> searchResults = projectService.search(query);
        Page<Project> resultsPage = createPageFromList(searchResults, pageable);

        model.addAttribute("resultsPage", resultsPage);
        model.addAttribute("query", query);
        model.addAttribute("categories", projectService.findAllDistinctCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("resultCount", searchResults.size());

        return "projekts/poisk";
    }

    @GetMapping("/komanda")
    public String teamPage(Model model,
                           @RequestParam(required = false) Long projektId) {

        List<TeamMember> teamMembers = getTeamMembers(projektId);
        Map<String, List<TeamMember>> groupedByPosition = groupTeamMembersByPosition(teamMembers);

        model.addAttribute("teamMembers", teamMembers);
        model.addAttribute("groupedByPosition", groupedByPosition);
        model.addAttribute("keyMembers", teamMemberService.findKeyTeamMembers(5));
        model.addAttribute("projekts", projectService.findAllActive());

        if (projektId != null) {
            projectService.findById(projektId).ifPresent(p -> model.addAttribute("projekt", p));
        }

        return "projekts/komanda";
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    private Pageable createPageable(int page, int size, String sort) {
        return switch (sort) {
            case "name" -> PageRequest.of(page, size, Sort.by("title").ascending());
            case "featured" -> PageRequest.of(page, size, Sort.by("sortOrder").ascending());
            default -> PageRequest.of(page, size, Sort.by("createdAt").descending());
        };
    }

    private Page<Project> getProjektsPage(String category, Pageable pageable) {
        if (category != null && !category.trim().isEmpty()) {
            return projectService.findActiveByCategory(category, pageable);
        }
        return projectService.findActive(pageable);
    }

    private Page<Project> createPageFromList(List<Project> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        return new org.springframework.data.domain.PageImpl<>(
                list.subList(start, end),
                pageable,
                list.size()
        );
    }

    private List<TeamMember> getTeamMembers(Long projectId) {
        if (projectId != null) {
            return teamMemberService.findByProjectId(projectId);
        }
        return teamMemberService.findAllActiveOrderBySortOrder();
    }

    private Map<String, List<TeamMember>> groupTeamMembersByPosition(List<TeamMember> teamMembers) {
        return teamMembers.stream()
                .collect(Collectors.groupingBy(TeamMember::getPosition));
    }

    private void loadProjectDetails(Project projekt, Model model) {
        if (projekt.isShowPhotos()) {
            model.addAttribute("projectImages", projectImageService.findByProjectOrderBySortOrder(projekt));
        }

        if (projekt.isShowVideos()) {
            model.addAttribute("projectVideos", projectVideoService.findByProjectOrderBySortOrder(projekt));
            projectVideoService.findMainVideoByProject(projekt)
                    .ifPresent(video -> model.addAttribute("mainVideo", video));
        }

        if (projekt.isShowTeam()) {
            model.addAttribute("teamMembers", teamMemberService.findByProjectOrderBySortOrder(projekt));
        }

        if (projekt.isShowPartners()) {
            model.addAttribute("projectPartners", projectPartnerService.findActiveByProjectOrderBySortOrder(projekt));
        }

        if (projekt.isShowRelated()) {
            List<Project> similarProjects = projectService.findSimilarProjects(
                    projekt.getCategory(), projekt.getId(), 3
            );
            model.addAttribute("similarProjects", similarProjects);
        }

        List<ProjectArticle> projectArticles = projectArticleService.findPublishedByProject(projekt);
        if (!projectArticles.isEmpty()) {
            model.addAttribute("hasArticles", true);
            model.addAttribute("recentArticles", projectArticleService.findRecentArticlesByProject(projekt, 3));
        }
    }
}

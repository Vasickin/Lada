package com.community.cms.web.mvc.controller.public_page;

import com.community.cms.domain.model.page.CustomPage;
import com.community.cms.domain.enums.PageType;
import com.community.cms.domain.service.page.CustomPageService;
import com.community.cms.domain.model.people.TeamMember;
import com.community.cms.domain.service.people.TeamMemberService;
import com.community.cms.web.mvc.dto.people.TeamMemberDTO;
import com.community.cms.web.mvc.mapper.people.TeamMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для главной страницы и публичных разделов сайта.
 */
@Controller
public class HomeController {

    private final CustomPageService pageService;
    private final TeamMemberService teamMemberService;
    private final TeamMemberMapper teamMemberMapper;

    /**
     * Конструктор с внедрением зависимостей.
     */
    @Autowired
    public HomeController(CustomPageService pageService,
                          TeamMemberService teamMemberService,
                          TeamMemberMapper teamMemberMapper) {
        this.pageService = pageService;
        this.teamMemberService = teamMemberService;
        this.teamMemberMapper = teamMemberMapper;
    }

    // ================== СУЩЕСТВУЮЩИЕ МЕТОДЫ (БЕЗ ИЗМЕНЕНИЙ) ==================

    @GetMapping("/")
    public String home(Model model) {
        List<CustomPage> publishedPages = pageService.findAllPublishedPages();
        model.addAttribute("publishedPages", publishedPages);
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        Optional<CustomPage> aboutPage = pageService.findPublishedPageByType(PageType.ABOUT);
        model.addAttribute("hasContent", aboutPage.isPresent());
        aboutPage.ifPresent(page -> {
            model.addAttribute("page", page);
            model.addAttribute("pageTitle", page.getTitle());
            model.addAttribute("metaDescription", page.getMetaDescription());
        });
        if (aboutPage.isEmpty()) {
            model.addAttribute("pageTitle", "О нас");
            model.addAttribute("metaDescription", "Информация о нашей организации");
        }
        return "about";
    }

    @GetMapping("/projects")
    public String projects(Model model) {
        Optional<CustomPage> projectsPage = pageService.findPublishedPageByType(PageType.PROJECTS);
        model.addAttribute("hasContent", projectsPage.isPresent());
        projectsPage.ifPresent(page -> {
            model.addAttribute("page", page);
            model.addAttribute("pageTitle", page.getTitle());
            model.addAttribute("metaDescription", page.getMetaDescription());
        });
        if (projectsPage.isEmpty()) {
            model.addAttribute("pageTitle", "Наши проекты");
            model.addAttribute("metaDescription", "Наши текущие и завершенные проекты");
        }
        return "projects";
    }

    @GetMapping("/gallery")
    public String gallery(Model model) {
        Optional<CustomPage> galleryPage = pageService.findPublishedPageByType(PageType.GALLERY);
        model.addAttribute("hasContent", galleryPage.isPresent());
        galleryPage.ifPresent(page -> {
            model.addAttribute("page", page);
            model.addAttribute("pageTitle", page.getTitle());
            model.addAttribute("metaDescription", page.getMetaDescription());
        });
        if (galleryPage.isEmpty()) {
            model.addAttribute("pageTitle", "Галерея");
            model.addAttribute("metaDescription", "Фотографии и видео наших мероприятий");
        }
        return "gallery";
    }

    @GetMapping("/patrons")
    public String patrons(Model model) {
        Optional<CustomPage> patronsPage = pageService.findPublishedPageByType(PageType.PATRONS);
        model.addAttribute("hasContent", patronsPage.isPresent());
        patronsPage.ifPresent(page -> {
            model.addAttribute("page", page);
            model.addAttribute("pageTitle", page.getTitle());
            model.addAttribute("metaDescription", page.getMetaDescription());
        });
        if (patronsPage.isEmpty()) {
            model.addAttribute("pageTitle", "Меценатам");
            model.addAttribute("metaDescription", "Информация для меценатов и партнеров");
        }
        return "patrons";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        Optional<CustomPage> contactPage = pageService.findPublishedPageByType(PageType.CONTACT);
        model.addAttribute("hasContent", contactPage.isPresent());
        contactPage.ifPresent(page -> {
            model.addAttribute("page", page);
            model.addAttribute("pageTitle", page.getTitle());
            model.addAttribute("metaDescription", page.getMetaDescription());
        });
        if (contactPage.isEmpty()) {
            model.addAttribute("pageTitle", "Контакты");
            model.addAttribute("metaDescription", "Контактная информация организации");
        }
        return "contact";
    }

    @GetMapping("/pages/{slug}")
    public String showPublicPage(@PathVariable String slug, Model model) {
        Optional<CustomPage> page = pageService.findPageBySlugAndPublished(slug, true);
        if (page.isPresent()) {
            CustomPage foundPage = page.get();
            model.addAttribute("page", foundPage);
            model.addAttribute("pageTitle", foundPage.getTitle());
            model.addAttribute("metaDescription", foundPage.getMetaDescription());
            model.addAttribute("hasContent", true);
            return "pages/view";
        } else {
            return "error/404";
        }
    }

    @GetMapping("/test-fragments")
    public String testFragments() {
        return "test-fragments";
    }

    @GetMapping("/sitemap")
    public String sitemap(Model model) {
        List<CustomPage> sitePages = pageService.findPublishedSitePages();
        model.addAttribute("sitePages", sitePages);
        return "sitemap";
    }

    // ================== НОВЫЕ МЕТОДЫ ДЛЯ КОМАНДЫ ==================

    /**
     * Отображает страницу "Наша команда" со списком активных участников.
     */
    @GetMapping("/team")
    public String showTeamPage(Model model) {
        try {
            // Получаем активных членов команды, отсортированных по порядку
            List<TeamMember> activeMembers = teamMemberService.findAllActiveOrderBySortOrder();

            // Преобразуем в DTO для безопасного отображения
            List<TeamMemberDTO> teamMembers = teamMemberMapper.toDTOList(activeMembers);

            // Мета-данные для SEO
            model.addAttribute("teamMembers", teamMembers);
            model.addAttribute("pageTitle", "Наша команда");
            model.addAttribute("metaDescription", "Команда организации 'ЛАДА' - талантливые специалисты, художники и организаторы");
            model.addAttribute("teamSize", teamMembers.size());
            model.addAttribute("hasContent", !teamMembers.isEmpty());

            return "public/team/list";

        } catch (Exception e) {
            // Логируем ошибку
            System.err.println("Ошибка при загрузке страницы команды: " + e.getMessage());
            model.addAttribute("error", "Временные технические проблемы. Пожалуйста, попробуйте позже.");
            return "error/500";
        }
    }

    /**
     * Отображает детальную страницу члена команды.
     */
    @GetMapping("/team/{id}")
    public String showTeamMember(@PathVariable Long id, Model model) {
        try {
            Optional<TeamMember> teamMemberOpt = teamMemberService.findById(id);

            // Проверяем существует ли член команды и активен ли он
            if (teamMemberOpt.isEmpty()) {
                model.addAttribute("errorTitle", "Член команды не найден");
                model.addAttribute("errorMessage", "Запрошенный участник команды не существует.");
                return "error/404";
            }

            TeamMember teamMember = teamMemberOpt.get();
            if (!teamMember.isActive()) {
                model.addAttribute("errorTitle", "Член команды не доступен");
                model.addAttribute("errorMessage", "Данный участник команды временно не отображается на сайте.");
                return "error/404";
            }

            // Преобразуем в DTO
            TeamMemberDTO memberDTO = teamMemberMapper.toDTO(teamMember);

            // Мета-данные для SEO
            model.addAttribute("member", memberDTO);
            model.addAttribute("pageTitle", memberDTO.getFullName() + " - " + memberDTO.getPosition());
            model.addAttribute("metaDescription",
                    memberDTO.getPosition() + " организации 'ЛАДА'. " +
                            (memberDTO.getBio() != null && memberDTO.getBio().length() > 150 ?
                                    memberDTO.getBio().substring(0, 150) + "..." :
                                    "Член команды организации 'ЛАДА'."));
            model.addAttribute("hasContent", true);

            return "public/team/detail";

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке страницы члена команды: " + e.getMessage());
            model.addAttribute("error", "Временные технические проблемы. Пожалуйста, попробуйте позже.");
            return "error/500";
        }
    }

    /**
     * Альтернативный вариант отображения команды - по алфавиту.
     */
    @GetMapping("/team/sorted-by-name")
    public String showTeamSortedByName(Model model) {
        try {
            List<TeamMember> activeMembers = teamMemberService.findAllActiveOrderByName();
            List<TeamMemberDTO> teamMembers = teamMemberMapper.toDTOList(activeMembers);

            model.addAttribute("teamMembers", teamMembers);
            model.addAttribute("pageTitle", "Наша команда (по алфавиту)");
            model.addAttribute("metaDescription", "Команда организации 'ЛАДА' в алфавитном порядке");
            model.addAttribute("teamSize", teamMembers.size());
            model.addAttribute("sortedByName", true);
            model.addAttribute("hasContent", !teamMembers.isEmpty());

            return "public/team/list";

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке команды по имени: " + e.getMessage());
            return "redirect:/team";
        }
    }

    /**
     * Страница с ключевыми членами команды (руководство).
     */
    @GetMapping("/team/key-members")
    public String showKeyTeamMembers(Model model) {
        try {
            List<TeamMember> keyMembers = teamMemberService.findKeyTeamMembers(10);
            List<TeamMemberDTO> teamMembers = teamMemberMapper.toDTOList(keyMembers);

            model.addAttribute("teamMembers", teamMembers);
            model.addAttribute("pageTitle", "Ключевая команда");
            model.addAttribute("metaDescription", "Руководство и ключевые специалисты организации 'ЛАДА'");
            model.addAttribute("teamSize", teamMembers.size());
            model.addAttribute("keyMembers", true);
            model.addAttribute("hasContent", !teamMembers.isEmpty());

            return "public/team/list";

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке ключевых членов команды: " + e.getMessage());
            return "redirect:/team";
        }
    }
}
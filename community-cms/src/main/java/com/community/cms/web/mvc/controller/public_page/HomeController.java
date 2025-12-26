package com.community.cms.web.mvc.controller.public_page;

import com.community.cms.domain.model.page.CustomPage;
import com.community.cms.domain.enums.PageType;
import com.community.cms.domain.service.page.CustomPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для главной страницы и публичных разделов сайта.
 *
 * <p>Обеспечивает отображение публичной части сайта, доступной всем посетителям
 * без необходимости аутентификации. Включает главную страницу, информацию об организации
 * и другие публичные разделы.</p>
 *
 * <p>Обновлен для работы с динамическим контентом из базы данных.
 * Если контент отсутствует - страница отображается пустой.</p>
 *
 * @author Vasickin
 * @version 1.1
 * @since 2025
 */
@Controller
public class HomeController {

    private final CustomPageService pageService;

    /**
     * Конструктор с внедрением зависимости CustomPageService.
     *
     * @param pageService сервис для работы со страницами
     */
    @Autowired
    public HomeController(CustomPageService pageService) {
        this.pageService = pageService;
    }

    /**
     * Отображает главную страницу сайта.
     *
     * <p>На главной странице отображаются опубликованные страницы организации,
     * краткая информация и навигационные элементы для посетителей.</p>
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона главной страницы ("index")
     */
    @GetMapping("/")
    public String home(Model model) {
        // Получаем опубликованные страницы для отображения на главной
        List<CustomPage> publishedPages = pageService.findAllPublishedPages();
        model.addAttribute("publishedPages", publishedPages);

        return "index";
    }

    /**
     * Отображает страницу "О нас" с динамическим контентом.
     * Если страница не найдена или не опубликована - отображается пустой шаблон.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы "О нас" ("about")
     */
    @GetMapping("/about")
    public String about(Model model) {
        Optional<CustomPage> aboutPage = pageService.findPublishedPageByType(PageType.ABOUT);

        // Передаем флаг наличия контента и саму страницу если она есть
        model.addAttribute("hasContent", aboutPage.isPresent());
        aboutPage.ifPresent(page -> {
            model.addAttribute("page", page);
            model.addAttribute("pageTitle", page.getTitle());
            model.addAttribute("metaDescription", page.getMetaDescription());
        });

        // Если контента нет - устанавливаем значения по умолчанию
        if (aboutPage.isEmpty()) {
            model.addAttribute("pageTitle", "О нас");
            model.addAttribute("metaDescription", "Информация о нашей организации");
        }

        return "about";
    }

    /**
     * Отображает страницу "Наши проекты" с динамическим контентом.
     * Если страница не найдена или не опубликована - отображается пустой шаблон.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы "Наши проекты" ("projects")
     */
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

    /**
     * Отображает страницу "Галерея" с динамическим контентом.
     * Если страница не найдена или не опубликована - отображается пустой шаблон.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы "Галерея" ("gallery")
     */
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

    /**
     * Отображает страницу "Меценатам" с динамическим контентом.
     * Если страница не найдена или не опубликована - отображается пустой шаблон.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы "Меценатам" ("patrons")
     */
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

    /**
     * Отображает страницу "Контакты" с динамическим контентом.
     * Если страница не найдена или не опубликована - отображается пустой шаблон.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы "Контакты" ("contact")
     */
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

    /**
     * Универсальный обработчик для динамических страниц по slug.
     * Используется для произвольных страниц созданных через админку.
     *
     * @param slug уникальный идентификатор страницы
     * @param model модель для передачи данных в представление
     * @return имя шаблона или страница 404 если не найдено
     */
    @GetMapping("/pages/{slug}")
    public String showPublicPage(@PathVariable String slug, Model model) {
        Optional<CustomPage> page = pageService.findPageBySlugAndPublished(slug, true);

        if (page.isPresent()) {
            CustomPage foundPage = page.get();
            model.addAttribute("page", foundPage);
            model.addAttribute("pageTitle", foundPage.getTitle());
            model.addAttribute("metaDescription", foundPage.getMetaDescription());
            model.addAttribute("hasContent", true);

            // Для кастомных страниц используем общий шаблон
            return "pages/view";
        } else {
            // Страница не найдена или не опубликована
            return "error/404";
        }
    }

    /**
     * Отображает тестовую страницу для проверки системы фрагментов.
     *
     * <p>Страница предназначена для безопасного тестирования каждого фрагмента
     * по отдельности перед их интеграцией в основные страницы сайта.
     * Позволяет идентифицировать конкретный фрагмент вызывающий ошибки.</p>
     *
     * @return имя шаблона тестовой страницы ("test-fragments")
     */
    @GetMapping("/test-fragments")
    public String testFragments() {
        return "test-fragments";
    }

    /**
     * Возвращает список всех опубликованных основных страниц сайта.
     * Используется для навигации или карты сайта.
     *
     * @param model модель для передачи данных в представление
     * @return список страниц
     */
    @GetMapping("/sitemap")
    public String sitemap(Model model) {
        List<CustomPage> sitePages = pageService.findPublishedSitePages();
        model.addAttribute("sitePages", sitePages);
        return "sitemap";
    }
}
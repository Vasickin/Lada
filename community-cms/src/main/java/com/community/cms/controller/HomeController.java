package com.community.cms.controller;

import com.community.cms.model.Page;
import com.community.cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Контроллер для главной страницы и публичных разделов сайта.
 *
 * <p>Обеспечивает отображение публичной части сайта, доступной всем посетителям
 * без необходимости аутентификации. Включает главную страницу, информацию об организации
 * и другие публичные разделы.</p>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 */
@Controller
public class HomeController {

    private final PageService pageService;

    /**
     * Конструктор с внедрением зависимости PageService.
     *
     * @param pageService сервис для работы со страницами
     */
    @Autowired
    public HomeController(PageService pageService) {
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
        List<Page> publishedPages = pageService.findAllPublishedPages();
        model.addAttribute("publishedPages", publishedPages);

        return "index"; // Убедитесь что ваш файл называется index.html
    }

    /**
     * Отображает страницу "О нас".
     *
     * <p>Страница содержит информацию об организации, ее миссии, целях
     * и основных направлениях деятельности.</p>
     *
     * @return имя шаблона страницы "О нас" ("about")
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    /**
     * Отображает страницу "Наши проекты".
     *
     * <p>Страница содержит информацию о текущих и завершенных проектах
     * организации, их целях и результатах.</p>
     *
     * @return имя шаблона страницы "Наши проекты" ("projects")
     */
    @GetMapping("/projects")
    public String projects() {
        return "projects";
    }

    /**
     * Отображает страницу "Галерея".
     *
     * <p>Страница содержит фотографии и видео с мероприятий организации,
     * иллюстрирующие ее деятельность и достижения.</p>
     *
     * @return имя шаблона страницы "Галерея" ("gallery")
     */
    @GetMapping("/gallery")
    public String gallery() {
        return "gallery";
    }

    /**
     * Отображает страницу "Меценатам".
     *
     * <p>Страница содержит информацию для потенциальных спонсоров и партнеров,
     * описание возможностей поддержки и сотрудничества.</p>
     *
     * @return имя шаблона страницы "Меценатам" ("patrons")
     */
    @GetMapping("/patrons")
    public String patrons() {
        return "patrons";
    }

    /**
     * Отображает страницу "Контакты".
     *
     * <p>Страница содержит контактную информацию организации, адрес,
     * телефоны, электронную почту и форму обратной связи.</p>
     *
     * @return имя шаблона страницы "Контакты" ("contact")
     */
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
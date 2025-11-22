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

        return "index";
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

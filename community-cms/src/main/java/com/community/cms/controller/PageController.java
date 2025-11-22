package com.community.cms.controller;

import com.community.cms.model.Page;
import com.community.cms.service.PageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для обработки HTTP запросов связанных со страницами.
 * Обеспечивает взаимодействие между веб-интерфейсом и сервисным слоем.
 *
 * <p>Основные responsibilities:
 * <ul>
 *   <li>Обработка HTTP запросов (GET, POST, PUT, DELETE)</li>
 *   <li>Валидация входящих данных</li>
 *   <li>Подготовка данных для представления (Model)</li>
 *   <li>Обработка исключений и перенаправлений</li>
 * </ul>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see PageService
 * @see Page
 */
@Controller
@RequestMapping("/pages")
public class PageController {

    private final PageService pageService;

    /**
     * Конструктор с внедрением зависимости PageService.
     *
     * @param pageService сервис для работы со страницами
     */
    @Autowired
    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    /**
     * Отображает список всех страниц (для административной части).
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона для отображения списка страниц
     */
    @GetMapping
    public String listPages(Model model) {
        List<Page> pages = pageService.findAllPages();
        model.addAttribute("pages", pages);
        return "pages/list";
    }

    /**
     * Отображает форму для создания новой страницы.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы создания страницы
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("page", new Page());
        return "pages/create";
    }

    /**
     * Обрабатывает отправку формы создания новой страницы.
     * Выполняет валидацию данных и сохраняет страницу.
     *
     * @param page создаваемая страница
     * @param bindingResult результаты валидации
     * @param model модель для передачи данных в представление
     * @return перенаправление на список страниц или возврат к форме при ошибках
     */
    @PostMapping("/create")
    public String createPage(@Valid @ModelAttribute Page page,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "pages/create";
        }

        try {
            pageService.savePage(page);
            return "redirect:/pages";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "pages/create";
        }
    }

    /**
     * Отображает форму редактирования существующей страницы.
     *
     * @param id идентификатор редактируемой страницы
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы редактирования или перенаправление при ошибке
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Page> page = pageService.findPageById(id);
        if (page.isPresent()) {
            model.addAttribute("page", page.get());
            return "pages/edit";
        } else {
            return "redirect:/pages";
        }
    }

    /**
     * Обрабатывает отправку формы редактирования страницы.
     *
     * @param id идентификатор редактируемой страницы
     * @param page обновленные данные страницы
     * @param bindingResult результаты валидации
     * @param model модель для передачи данных в представление
     * @return перенаправление на список страниц или возврат к форме при ошибках
     */
    @PostMapping("/edit/{id}")
    public String updatePage(@PathVariable Long id,
                             @Valid @ModelAttribute Page page,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "pages/edit";
        }

        try {
            page.setId(id); // Убеждаемся что ID сохраняется
            pageService.savePage(page);
            return "redirect:/pages";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "pages/edit";
        }
    }

    /**
     * Удаляет страницу по идентификатору.
     *
     * @param id идентификатор удаляемой страницы
     * @return перенаправление на список страниц
     */
    @PostMapping("/delete/{id}")
    public String deletePage(@PathVariable Long id) {
        try {
            pageService.deletePage(id);
        } catch (IllegalArgumentException e) {
            // Лог ошибку, но не прерываем выполнение
            System.err.println("Ошибка при удалении страницы: " + e.getMessage());
        }
        return "redirect:/pages";
    }

    /**
     * Публикует страницу (устанавливает статус published = true).
     *
     * @param id идентификатор страницы
     * @return перенаправление на список страниц
     */
    @PostMapping("/publish/{id}")
    public String publishPage(@PathVariable Long id) {
        try {
            pageService.publishPage(id);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка при публикации страницы: " + e.getMessage());
        }
        return "redirect:/pages";
    }

    /**
     * Снимает страницу с публикации (устанавливает статус published = false).
     *
     * @param id идентификатор страницы
     * @return перенаправление на список страниц
     */
    @PostMapping("/unpublish/{id}")
    public String unpublishPage(@PathVariable Long id) {
        try {
            pageService.unpublishPage(id);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка при снятии с публикации: " + e.getMessage());
        }
        return "redirect:/pages";
    }

    /**
     * Отображает публичную страницу по slug.
     * Используется для конечных пользователей сайта.
     *
     * @param slug уникальный идентификатор страницы
     * @param model модель для передачи данных в представление
     * @return имя шаблона для отображения страницы или 404 при отсутствии
     */
    @GetMapping("/{slug}")
    public String showPublicPage(@PathVariable String slug, Model model) {
        Optional<Page> page = pageService.findPageBySlug(slug);
        if (page.isPresent() && page.get().getIsPublished()) {
            model.addAttribute("page", page.get());
            return "pages/view";
        } else {
            return "error/404";
        }
    }
}

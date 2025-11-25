package com.community.cms.controller;

import com.community.cms.dto.PageStatistics;
import com.community.cms.model.Page;
import com.community.cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для административной панели управления.
 *
 * <p>Обеспечивает отображение дашборда и других административных страниц,
 * доступных только аутентифицированным пользователям с соответствующими правами.</p>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Отображение главного дашборда с общей статистикой</li>
 *   <li>Предоставление обзора системы и быстрого доступа к функциям</li>
 *   <li>Отображение последней активности и состояния контента</li>
 * </ul>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see PageService
 */
@Controller
public class AdminController {

    private final PageService pageService;

    /**
     * Конструктор с внедрением зависимости PageService.
     *
     * @param pageService сервис для работы со страницами
     */
    @Autowired
    public AdminController(PageService pageService) {
        this.pageService = pageService;
    }

    /**
     * Отображает главную страницу административной панели (дашборд).
     *
     * <p>На дашборде отображается общая статистика по страницам,
     * последние созданные страницы и быстрые ссылки для управления контентом.</p>
     *
     * @param model модель для передачи данных в представление
     * @param authentication объект аутентификации Spring Security
     * @return имя шаблона дашборда ("admin/dashboard")
     */
    @GetMapping("/admin")
    public String dashboard(Model model, Authentication authentication) {
        // Используем эффективные методы сервиса
        PageStatistics statistics = pageService.getPageStatistics();
        List<Page> recentPages = pageService.findRecentPages(5);

        // Передаем данные в модель
        model.addAttribute("totalPages", statistics.totalPages());
        model.addAttribute("publishedCount", statistics.publishedCount());
        model.addAttribute("draftCount", statistics.draftCount());
        model.addAttribute("recentPages", recentPages);

        // Добавляем информацию о текущем пользователе
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            model.addAttribute("currentUsername", username);

            // Получаем роли пользователя
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            model.addAttribute("currentUserRoles", roles);

            // Добавляем флаг аутентификации
            model.addAttribute("isAuthenticated", true);
        } else {
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("currentUsername", "Гость");
            model.addAttribute("currentUserRoles", List.of("ROLE_ANONYMOUS"));
        }

        return "admin/dashboard";
    }
}
package com.community.cms.controller;

import com.community.cms.model.User;
import com.community.cms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

/**
 * Контроллер для управления пользователями в административной панели.
 *
 * <p>Предоставляет функциональность для создания, просмотра, редактирования
 * и удаления пользователей, а также управления их ролями и статусами.</p>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Просмотр списка всех пользователей системы</li>
 *   <li>Создание новых пользователей с назначением ролей</li>
 *   <li>Редактирование существующих пользователей</li>
 *   <li>Управление активностью учетных записей</li>
 *   <li>Назначение и снятие ролей</li>
 *   <li>Удаление пользователей (с ограничениями)</li>
 * </ul>
 *
 * <p>Доступ к функциям управления пользователями ограничен ролью ADMIN.
 * Это обеспечивает безопасность системы и предотвращает несанкционированное
 * изменение прав доступа.</p>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see UserService
 * @see User
 */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    /**
     * Конструктор с внедрением зависимости UserService.
     *
     * @param userService сервис для работы с пользователями
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Отображает список всех пользователей системы.
     *
     * <p>На странице отображается таблица с информацией о пользователях:
     * имя пользователя, email, роли, статус активности и дата создания.
     * Предоставляет возможности для управления каждым пользователем.</p>
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона списка пользователей ("admin/users/list")
     */
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users/list";
    }

    /**
     * Отображает форму для создания нового пользователя.
     *
     * <p>Форма содержит поля для ввода основных данных пользователя:
     * имя пользователя, email и пароль. Также предоставляет выбор ролей
     * из предопределенного набора.</p>
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы создания пользователя ("admin/users/create")
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("availableRoles", getAvailableRoles());
        return "admin/users/create";
    }

    /**
     * Обрабатывает отправку формы создания пользователя.
     *
     * <p>Выполняет валидацию введенных данных и сохраняет нового пользователя
     * в базе данных. В случае успеха перенаправляет на список пользователей
     * с сообщением об успешном создании.</p>
     *
     * @param user создаваемый пользователь
     * @param bindingResult результаты валидации формы
     * @param redirectAttributes атрибуты для перенаправления
     * @param model модель для передачи данных в представление
     * @return перенаправление на список пользователей или возврат к форме при ошибках
     */
    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute User user,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        // Проверяем ошибки валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("availableRoles", getAvailableRoles());
            return "admin/users/create";
        }

        try {
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success",
                    "Пользователь " + user.getUsername() + " успешно создан");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("availableRoles", getAvailableRoles());
            return "admin/users/create";
        }
    }

    /**
     * Отображает форму для редактирования существующего пользователя.
     *
     * <p>Форма предзаполняется текущими данными пользователя и позволяет
     * изменять email, роли и статус активности. Изменение имени пользователя
     * не поддерживается для сохранения целостности данных.</p>
     *
     * @param id идентификатор редактируемого пользователя
     * @param model модель для передачи данных в представление
     * @return имя шаблона формы редактирования пользователя ("admin/users/edit")
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        model.addAttribute("user", user);
        model.addAttribute("availableRoles", getAvailableRoles());
        return "admin/users/edit";
    }

    /**
     * Обрабатывает отправку формы редактирования пользователя.
     *
     * <p>Обновляет данные пользователя в базе данных. Поддерживает изменение
     * email, ролей и статуса активности. Пароль обновляется только если
     * указано новое значение.</p>
     *
     * @param id идентификатор редактируемого пользователя
     * @param user обновленные данные пользователя
     * @param bindingResult результаты валидации формы
     * @param redirectAttributes атрибуты для перенаправления
     * @param model модель для передачи данных в представление
     * @return перенаправление на список пользователей или возврат к форме при ошибках
     */
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute User user,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        // Проверяем ошибки валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("availableRoles", getAvailableRoles());
            return "admin/users/edit";
        }

        try {
            // Сохраняем пользователя (UserService обработает шифрование пароля если нужно)
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success",
                    "Пользователь " + user.getUsername() + " успешно обновлен");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("availableRoles", getAvailableRoles());
            return "admin/users/edit";
        }
    }

    /**
     * Активирует учетную запись пользователя.
     *
     * <p>Позволяет разблокировать ранее заблокированную учетную запись.
     * После активации пользователь сможет войти в систему.</p>
     *
     * @param id идентификатор пользователя
     * @param redirectAttributes атрибуты для перенаправления
     * @return перенаправление на список пользователей
     */
    @PostMapping("/enable/{id}")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.enableUser(id);
            redirectAttributes.addFlashAttribute("success",
                    "Пользователь " + user.getUsername() + " активирован");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Деактивирует учетную запись пользователя.
     *
     * <p>Блокирует учетную запись пользователя. После деактивации
     * пользователь не сможет войти в систему до повторной активации.</p>
     *
     * @param id идентификатор пользователя
     * @param redirectAttributes атрибуты для перенаправления
     * @return перенаправление на список пользователей
     */
    @PostMapping("/disable/{id}")
    public String disableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.disableUser(id);
            redirectAttributes.addFlashAttribute("success",
                    "Пользователь " + user.getUsername() + " деактивирован");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Добавляет роль пользователю.
     *
     * <p>Назначает дополнительную роль существующему пользователю.
     * Расширяет права доступа пользователя в системе.</p>
     *
     * @param id идентификатор пользователя
     * @param role добавляемая роль
     * @param redirectAttributes атрибуты для перенаправления
     * @return перенаправление на список пользователей
     */
    @PostMapping("/add-role/{id}")
    public String addRoleToUser(@PathVariable Long id,
                                @RequestParam String role,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.addRoleToUser(id, role);
            redirectAttributes.addFlashAttribute("success",
                    "Роль " + role + " добавлена пользователю " + user.getUsername());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Удаляет роль у пользователя.
     *
     * <p>Снимает роль с пользователя. Уменьшает права доступа
     * пользователя в системе. Не позволяет удалить последнюю роль.</p>
     *
     * @param id идентификатор пользователя
     * @param role удаляемая роль
     * @param redirectAttributes атрибуты для перенаправления
     * @return перенаправление на список пользователей
     */
    @PostMapping("/remove-role/{id}")
    public String removeRoleFromUser(@PathVariable Long id,
                                     @RequestParam String role,
                                     RedirectAttributes redirectAttributes) {
        try {
            User user = userService.removeRoleFromUser(id, role);
            redirectAttributes.addFlashAttribute("success",
                    "Роль " + role + " удалена у пользователя " + user.getUsername());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Возвращает набор доступных ролей для назначения пользователям.
     *
     * <p>Определяет перечень ролей, которые могут быть назначены
     * пользователям через административный интерфейс.</p>
     *
     * @return множество доступных ролей
     */
    private Set<String> getAvailableRoles() {
        return Set.of("ADMIN", "EDITOR", "USER");
    }
}

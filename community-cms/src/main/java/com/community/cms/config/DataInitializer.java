package com.community.cms.config;

import com.community.cms.model.User;
import com.community.cms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Компонент для инициализации начальных данных в базе данных при запуске приложения.
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Создание административных учетных записей при первом запуске</li>
 *   <li>Настройка ролей и прав доступа по умолчанию</li>
 *   <li>Обеспечение работоспособности системы после развертывания</li>
 * </ul>
 *
 * <p>Учетные записи создаются только если они еще не существуют в базе данных.
 * Это предотвращает дублирование при повторных запусках приложения.</p>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see UserService
 * @see User
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    /**
     * Конструктор с внедрением зависимости UserService.
     *
     * @param userService сервис для работы с пользователями
     */
    @Autowired
    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    /**
     * Метод, выполняемый при запуске приложения.
     * Создает начальные учетные записи пользователей если они не существуют.
     *
     * @param args аргументы командной строки (не используются)
     * @throws Exception если произошла ошибка при инициализации данных
     */
    @Override
    public void run(String... args) throws Exception {
        createAdminUser();
        createEditorUser();
        createTestUser();
    }

    /**
     * Создает учетную запись администратора системы.
     * Администратор имеет полные права доступа ко всем функциям системы.
     */
    private void createAdminUser() {
        // Проверяем, существует ли уже пользователь с именем admin
        if (userService.userExistsByUsername("admin")) {
            System.out.println("✅ Администратор уже существует");
            return;
        }

        try {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@lada-org.ru");
            admin.setPassword("admin123"); // Пароль будет зашифрован автоматически
            admin.setEnabled(true);
            admin.setCreatedAt(LocalDateTime.now()); // Устанавливаем дату вручную
            admin.addRole("ADMIN");
            admin.addRole("EDITOR");
            admin.addRole("USER");

            userService.saveUser(admin);
            System.out.println("✅ Создан администратор: admin / admin123");
        } catch (Exception e) {
            System.err.println("❌ Ошибка при создании администратора: " + e.getMessage());
        }
    }

    /**
     * Создает учетную запись редактора контента.
     * Редактор имеет права на управление контентом, но не на управление пользователями.
     */
    private void createEditorUser() {
        // Проверяем, существует ли уже пользователь с именем editor
        if (userService.userExistsByUsername("editor")) {
            System.out.println("✅ Редактор уже существует");
            return;
        }

        try {
            User editor = new User();
            editor.setUsername("editor");
            editor.setEmail("editor@lada-org.ru");
            editor.setPassword("editor123"); // Пароль будет зашифрован автоматически
            editor.setEnabled(true);
            editor.setCreatedAt(LocalDateTime.now()); // Устанавливаем дату вручную
            editor.addRole("EDITOR");
            editor.addRole("USER");

            userService.saveUser(editor);
            System.out.println("✅ Создан редактор: editor / editor123");
        } catch (Exception e) {
            System.err.println("❌ Ошибка при создании редактора: " + e.getMessage());
        }
    }

    /**
     * Создает тестовую учетную запись обычного пользователя.
     * Обычный пользователь имеет ограниченные права доступа.
     */
    private void createTestUser() {
        // Проверяем, существует ли уже пользователь с именем user
        if (userService.userExistsByUsername("user")) {
            System.out.println("✅ Тестовый пользователь уже существует");
            return;
        }

        try {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@lada-org.ru");
            user.setPassword("user123"); // Пароль будет зашифрован автоматически
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now()); // Устанавливаем дату вручную
            user.addRole("USER");

            userService.saveUser(user);
            System.out.println("✅ Создан тестовый пользователь: user / user123");
        } catch (Exception e) {
            System.err.println("❌ Ошибка при создании тестового пользователя: " + e.getMessage());
        }
    }
}
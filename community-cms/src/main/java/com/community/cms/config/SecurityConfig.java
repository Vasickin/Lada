package com.community.cms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности Spring Security для Community CMS.
 *
 * <p>Этот класс настраивает систему аутентификации и авторизации приложения,
 * определяет правила доступа к URL, настраивает форму входа и обработку выхода.</p>
 *
 * <p>Основные функции конфигурации:
 * <ul>
 *   <li>Защита административных URL от несанкционированного доступа</li>
 *   <li>Настройка кастомной формы аутентификации</li>
 *   <li>Управление ролями и правами пользователей</li>
 *   <li>Обработка выхода из системы</li>
 *   <li>Настройка исключений безопасности</li>
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @since 2025
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Настраивает цепочку фильтров безопасности для HTTP запросов.
     *
     * <p>Определяет правила доступа к различным URL приложения, настраивает
     * форму входа, обработку выхода и исключения безопасности.</p>
     *
     * @param http объект для настройки веб-безопасности Spring Security
     * @return сконфигурированная цепочка фильтров безопасности
     * @throws Exception если произошла ошибка в процессе конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //
                // AUTHORIZATION CONFIGURATION / КОНФИГУРАЦИЯ АВТОРИЗАЦИИ
                // Define URL access rules / Определяем правила доступа к URL
                //
                .authorizeHttpRequests(authorize -> authorize
                                //
                                // PUBLIC ENDPOINTS / ПУБЛИЧНЫЕ ENDPOINTS
                                // Accessible without authentication / Доступны без аутентификации
                                //
                                .requestMatchers("/", "/h2-console/**", "/css/**", "/js/**", "/images/**", "/error").permitAll()

                                //
                                // PUBLIC PAGE VIEWING / ПУБЛИЧНЫЙ ПРОСМОТР СТРАНИЦ
                                // Page viewing by slug is public / Просмотр страниц по slug доступен всем
                                //
                                .requestMatchers("/pages/{slug}").permitAll()

                                //
                                // ADMIN ENDPOINTS / АДМИНИСТРАТИВНЫЕ ENDPOINTS
                                // Require authentication / Требуют аутентификации
                                // These URLs manage content and require login /
                                // Эти URL управляют контентом и требуют входа в систему
                                //
                                .requestMatchers("/pages", "/pages/create", "/pages/edit/**",
                                        "/pages/delete/**", "/pages/publish/**", "/pages/unpublish/**").authenticated()

                                //
                                // ADMIN DASHBOARD / АДМИНИСТРАТИВНЫЙ ДАШБОРД  ✅ NEW
                                // Dashboard and admin panel access / Доступ к дашборду и админ панели
                                //
                                .requestMatchers("/admin", "/admin/**").authenticated()
                                //
                                // ALL OTHER REQUESTS / ВСЕ ОСТАЛЬНЫЕ ЗАПРОСЫ
                                // Require authentication / Требуют аутентификации
                                //
                                .anyRequest().authenticated()
                )

                //
                // LOGIN CONFIGURATION / КОНФИГУРАЦИЯ ЛОГИНА
                // Custom login page and processing / Кастомная страница логина и обработка
                //
                .formLogin(form -> form
                        .loginPage("/login")          // Custom login page URL / URL кастомной страницы логина
                        .loginProcessingUrl("/login") // URL for processing login / URL для обработки логина
                        .defaultSuccessUrl("/pages")  // Redirect after successful login / Перенаправление после успешного логина
                        .failureUrl("/login?error")   // Redirect after failed login / Перенаправление после неудачного логина
                        .permitAll()                  // Login page is accessible to all / Страница логина доступна всем
                )

                //
                // LOGOUT CONFIGURATION / КОНФИГУРАЦИЯ ЛОГАУТА
                // Logout URL and redirect / URL выхода и перенаправление
                //
                .logout(logout -> logout
                        .logoutUrl("/logout")         // URL for logout / URL для выхода
                        .logoutSuccessUrl("/login?logout") // Redirect after logout / Перенаправление после выхода
                        .invalidateHttpSession(true)  // Invalidate session / Инвалидировать сессию
                        .deleteCookies("JSESSIONID")  // Delete cookies / Удалить cookies
                        .permitAll()                  // Logout accessible to all / Выход доступен всем
                )

                //
                // EXCEPTION HANDLING / ОБРАБОТКА ИСКЛЮЧЕНИЙ
                // Custom access denied page / Кастомная страница отказа в доступе
                //
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied") // Custom access denied page / Кастомная страница отказа в доступе
                );

        //
        // H2 CONSOLE CONFIGURATION (for development only) / КОНФИГУРАЦИЯ H2 CONSOLE (только для разработки)
        // Disable CSRF and frame options for H2 console /
        // Отключаем CSRF и frame options для H2 console
        //
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * Создает сервис для работы с пользователями, хранящимися в памяти.
     *
     * <p>ВНИМАНИЕ: Это решение только для разработки и демонстрации.
     * В production окружении необходимо заменить на реализацию,
     * использующую базу данных.</p>
     *
     * @param passwordEncoder кодировщик паролей для безопасного хранения
     * @return сервис деталей пользователя с предустановленными учетными записями
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        //
        // IN-MEMORY USER STORAGE / ХРАНИЛИЩЕ ПОЛЬЗОВАТЕЛЕЙ В ПАМЯТИ
        // For development only - replace with database in production /
        // Только для разработки - заменить на базу данных в продакшене
        //

        /**
         * Администратор системы с полными правами доступа.
         * Имеет роли ADMIN и USER.
         */
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN", "USER")
                .build();

        /**
         * Редактор контента с ограниченными правами.
         * Имеет роли EDITOR и USER.
         */
        UserDetails editor = User.builder()
                .username("editor")
                .password(passwordEncoder.encode("editor123"))
                .roles("EDITOR", "USER")
                .build();

        return new InMemoryUserDetailsManager(admin, editor);
    }

    /**
     * Создает и настраивает кодировщик паролей BCrypt.
     *
     * <p>BCrypt является надежным алгоритмом хеширования паролей,
     * который автоматически добавляет "соль" и обеспечивает
     * безопасное хранение учетных данных.</p>
     *
     * @return кодировщик паролей BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
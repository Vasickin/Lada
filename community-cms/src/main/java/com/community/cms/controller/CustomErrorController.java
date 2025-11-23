package com.community.cms.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom error controller for handling HTTP errors and displaying user-friendly error pages.
 * Implements Spring Boot's ErrorController interface to override default white-label error pages.
 *
 * Пользовательский контроллер для обработки HTTP ошибок и отображения пользовательских страниц ошибок.
 * Реализует интерфейс ErrorController Spring Boot для переопределения стандартных страниц ошибок.
 *
 * @author Vasickin
 * @version 1.0
 * @since 2024
 * @see org.springframework.boot.web.servlet.error.ErrorController
 * @see HttpStatus
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * Handles all error requests by analyzing the HTTP status code and routing to appropriate error pages.
     * Extracts error information from the request attributes and provides user-friendly error messages.
     *
     * Обрабатывает все запросы ошибок путем анализа HTTP кода статуса и направления на соответствующие страницы ошибок.
     * Извлекает информацию об ошибке из атрибутов запроса и предоставляет пользовательские сообщения об ошибках.
     *
     * @param request the HTTP request object containing error attributes /
     *                HTTP объект запроса, содержащий атрибуты ошибки
     * @param model the Spring UI model for passing data to the view /
     *              Spring UI модель для передачи данных в представление
     * @return the name of the error page template to render /
     *         имя шаблона страницы ошибки для отображения
     * @see HttpServletRequest
     * @see Model
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get the error status code from request attributes /
        // Получаем код статуса ошибки из атрибутов запроса
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // Add error code to model for displaying in the template /
            // Добавляем код ошибки в модель для отображения в шаблоне
            model.addAttribute("errorCode", statusCode);

            // Route to specific error pages based on status code /
            // Направляем на конкретные страницы ошибок на основе кода статуса
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            }
            if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/500";
            }
        }

        // Default fallback - will be improved later
        // Запасной вариант по умолчанию - будет улучшен позже
        return "error/404";
    }

    /**
     * Returns the error path that this controller handles.
     * Required by the ErrorController interface implementation.
     *
     * Возвращает путь ошибки, который обрабатывает этот контроллер.
     * Требуется для реализации интерфейса ErrorController.
     *
     * @return the error path string / строку пути ошибки
     * @see org.springframework.boot.web.servlet.error.ErrorController#getErrorPath()
     */
    public String getErrorPath() {
        return "/error";
    }
}

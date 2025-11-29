
package com.community.cms.controller;

import com.community.cms.model.GalleryItem;
import com.community.cms.model.MediaType;
import com.community.cms.service.GalleryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для административного управления галереей
 * Controller for administrative gallery management
 *
 * Обеспечивает CRUD операции для элементов галереи через веб-интерфейс
 * Provides CRUD operations for gallery items through web interface
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see GalleryService
 * @see GalleryItem
 */
@Controller
@RequestMapping("/admin/gallery")
public class GalleryAdminController {

    private final GalleryService galleryService;

    /**
     * Конструктор с внедрением зависимости сервиса
     * Constructor with service dependency injection
     *
     * @param galleryService сервис для работы с галереей / gallery service
     */
    @Autowired
    public GalleryAdminController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    /**
     * Отображает список всех элементов галереи (админка)
     * Displays list of all gallery items (admin panel)
     *
     * @param model модель для передачи данных в представление / model for view data
     * @return имя шаблона списка галереи / gallery list template name
     */
    @GetMapping
    public String manageGallery(Model model) {
        List<GalleryItem> galleryItems = galleryService.findAllItems();
        GalleryService.GalleryStatistics statistics = galleryService.getGalleryStatistics();

        model.addAttribute("galleryItems", galleryItems);
        model.addAttribute("statistics", statistics);
        model.addAttribute("availableYears", galleryService.getAvailableYears());
        model.addAttribute("availableCategories", galleryService.getAvailableCategories());
        model.addAttribute("mediaTypes", MediaType.values());

        return "admin/gallery/list";
    }

    /**
     * Отображает форму создания нового элемента галереи
     * Displays form for creating new gallery item
     *
     * @param model модель для передачи данных в представление / model for view data
     * @return имя шаблона формы создания / create form template name
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("galleryItem", new GalleryItem());
        model.addAttribute("mediaTypes", MediaType.values());
        model.addAttribute("availableYears", List.of(2023, 2024, 2025, 2026));
        model.addAttribute("availableCategories", List.of("education", "volunteering", "projects", "events", "community", "ecology"));

        return "admin/gallery/create";
    }

    /**
     * Обрабатывает отправку формы создания элемента галереи
     * Processes gallery item creation form submission
     *
     * @param galleryItem создаваемый элемент галереи / gallery item to create
     * @param bindingResult результаты валидации / validation results
     * @param model модель для передачи данных в представление / model for view data
     * @param redirectAttributes атрибуты для перенаправления / redirect attributes
     * @return перенаправление на список или возврат к форме / redirect to list or return to form
     */
    @PostMapping("/create")
    public String createGalleryItem(@Valid @ModelAttribute GalleryItem galleryItem,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("mediaTypes", MediaType.values());
            model.addAttribute("availableYears", List.of(2023, 2024, 2025, 2026));
            model.addAttribute("availableCategories", List.of("education", "volunteering", "projects", "events", "community", "ecology"));
            return "admin/gallery/create";
        }

        try {
            galleryService.saveGalleryItem(galleryItem);
            redirectAttributes.addFlashAttribute("success", "gallery_item_created");
            return "redirect:/admin/gallery";
        } catch (Exception e) {
            model.addAttribute("error", "creation_failed");
            model.addAttribute("mediaTypes", MediaType.values());
            model.addAttribute("availableYears", List.of(2023, 2024, 2025, 2026));
            model.addAttribute("availableCategories", List.of("education", "volunteering", "projects", "events", "community", "ecology"));
            return "admin/gallery/create";
        }
    }

    /**
     * Отображает форму редактирования элемента галереи
     * Displays gallery item editing form
     *
     * @param id идентификатор редактируемого элемента / item identifier to edit
     * @param model модель для передачи данных в представление / model for view data
     * @return имя шаблона формы редактирования / edit form template name
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<GalleryItem> galleryItem = galleryService.findGalleryItemById(id);

        if (galleryItem.isPresent()) {
            model.addAttribute("galleryItem", galleryItem.get());
            model.addAttribute("mediaTypes", MediaType.values());
            model.addAttribute("availableYears", List.of(2023, 2024, 2025, 2026));
            model.addAttribute("availableCategories", List.of("education", "volunteering", "projects", "events", "community", "ecology"));
            return "admin/gallery/edit";
        } else {
            return "redirect:/admin/gallery?error=item_not_found";
        }
    }

    /**
     * Обрабатывает отправку формы редактирования элемента галереи
     * Processes gallery item editing form submission
     *
     * @param id идентификатор редактируемого элемента / item identifier to edit
     * @param galleryItem обновленные данные элемента / updated item data
     * @param bindingResult результаты валидации / validation results
     * @param model модель для передачи данных в представление / model for view data
     * @param redirectAttributes атрибуты для перенаправления / redirect attributes
     * @return перенаправление на список или возврат к форме / redirect to list or return to form
     */
    @PostMapping("/edit/{id}")
    public String updateGalleryItem(@PathVariable Long id,
                                    @Valid @ModelAttribute GalleryItem galleryItem,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("mediaTypes", MediaType.values());
            model.addAttribute("availableYears", List.of(2023, 2024, 2025, 2026));
            model.addAttribute("availableCategories", List.of("education", "volunteering", "projects", "events", "community", "ecology"));
            return "admin/gallery/edit";
        }

        try {
            galleryItem.setId(id); // Ensure ID is preserved / Сохраняем ID
            galleryService.saveGalleryItem(galleryItem);
            redirectAttributes.addFlashAttribute("success", "gallery_item_updated");
            return "redirect:/admin/gallery";
        } catch (Exception e) {
            model.addAttribute("error", "update_failed");
            model.addAttribute("mediaTypes", MediaType.values());
            model.addAttribute("availableYears", List.of(2023, 2024, 2025, 2026));
            model.addAttribute("availableCategories", List.of("education", "volunteering", "projects", "events", "community", "ecology"));
            return "admin/gallery/edit";
        }
    }

    /**
     * Удаляет элемент галереи
     * Deletes gallery item
     *
     * @param id идентификатор удаляемого элемента / item identifier to delete
     * @param redirectAttributes атрибуты для перенаправления / redirect attributes
     * @return перенаправление на список галереи / redirect to gallery list
     */
    @PostMapping("/delete/{id}")
    public String deleteGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            galleryService.deleteGalleryItem(id);
            redirectAttributes.addFlashAttribute("success", "gallery_item_deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "delete_failed");
        }
        return "redirect:/admin/gallery";
    }

    /**
     * Публикует элемент галереи
     * Publishes gallery item
     *
     * @param id идентификатор элемента / item identifier
     * @param redirectAttributes атрибуты для перенаправления / redirect attributes
     * @return перенаправление на список галереи / redirect to gallery list
     */
    @PostMapping("/publish/{id}")
    public String publishGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = galleryService.publishGalleryItem(id);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "gallery_item_published");
        } else {
            redirectAttributes.addFlashAttribute("error", "publish_failed");
        }
        return "redirect:/admin/gallery";
    }

    /**
     * Снимает элемент галереи с публикации
     * Unpublishes gallery item
     *
     * @param id идентификатор элемента / item identifier
     * @param redirectAttributes атрибуты для перенаправления / redirect attributes
     * @return перенаправление на список галереи / redirect to gallery list
     */
    @PostMapping("/unpublish/{id}")
    public String unpublishGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = galleryService.unpublishGalleryItem(id);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "gallery_item_unpublished");
        } else {
            redirectAttributes.addFlashAttribute("error", "unpublish_failed");
        }
        return "redirect:/admin/gallery";
    }

    /**
     * Отображает предпросмотр элемента галереи
     * Displays gallery item preview
     *
     * @param id идентификатор элемента / item identifier
     * @param model модель для передачи данных в представление / model for view data
     * @return имя шаблона предпросмотра / preview template name
     */
    @GetMapping("/preview/{id}")
    public String previewGalleryItem(@PathVariable Long id, Model model) {
        Optional<GalleryItem> galleryItem = galleryService.findGalleryItemById(id);

        if (galleryItem.isPresent()) {
            model.addAttribute("galleryItem", galleryItem.get());
            return "admin/gallery/preview";
        } else {
            return "redirect:/admin/gallery?error=item_not_found";
        }
    }
}
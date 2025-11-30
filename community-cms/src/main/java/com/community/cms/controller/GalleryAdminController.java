package com.community.cms.controller;

import com.community.cms.model.GalleryItem;
import com.community.cms.model.GalleryMedia;
import com.community.cms.model.MediaType;
import com.community.cms.service.FileStorageService;
import com.community.cms.service.GalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер для административного управления галереей с поддержкой множественных медиафайлов
 * Controller for administrative gallery management with multiple media files support
 *
 * Расширен для поддержки загрузки нескольких файлов на элемент с сохранением обратной совместимости
 * Extended to support multiple file uploads per item while maintaining backward compatibility
 *
 * @author Vasickin
 * @version 2.0
 * @since 2025
 * @see GalleryService
 * @see GalleryItem
 * @see GalleryMedia
 */
@Controller
@RequestMapping("/admin/gallery")
public class GalleryAdminController {

    private final GalleryService galleryService;
    private final FileStorageService fileStorageService;

    /**
     * Конструктор с внедрением зависимости сервиса
     * Constructor with service dependency injection
     *
     * @param galleryService сервис для работы с галереей / gallery service
     * @param fileStorageService сервис для работы с файлами / file storage service
     */
    @Autowired
    public GalleryAdminController(GalleryService galleryService, FileStorageService fileStorageService) {
        this.galleryService = galleryService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * ОСНОВНЫЕ СТРАНИЦЫ АДМИНКИ - СТАРЫЕ МЕТОДЫ ДЛЯ ОБРАТНОЙ СОВМЕСТИМОСТИ
     * ADMIN PANEL MAIN PAGES - OLD METHODS FOR BACKWARD COMPATIBILITY
     */

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
        model.addAttribute("maxFiles", 20); // Максимальное количество файлов / Maximum files

        return "admin/gallery/create";
    }

    /**
     * Обрабатывает отправку формы создания элемента галереи (старая версия - без файлов)
     * Processes gallery item creation form submission (old version - without files)
     *
     * @param galleryItem создаваемый элемент галереи / gallery item to create
     * @param bindingResult результаты валидации / validation results
     * @param model модель для передачи данных в представление / model for view data
     * @param redirectAttributes атрибуты для перенаправления / redirect attributes
     * @return перенаправление на список или возврат к форме / redirect to list or return to form
     */
    @PostMapping("/create")
    public String createGalleryItem(@ModelAttribute GalleryItem galleryItem,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        return createGalleryItemWithFiles(galleryItem, bindingResult, null, model, redirectAttributes);
    }

    /**
     * Обрабатывает отправку формы создания элемента галереи с файлами (новая версия)
     * Processes gallery item creation form submission with files (new version)
     *
     * @param galleryItem создаваемый элемент галереи / gallery item to create
     * @param bindingResult результаты валидации / validation results
     * @param files загружаемые файлы / uploaded files
     * @param model модель для передачи данных в представление / model for view data
     * @param redirectAttributes атрибуты для перенаправления / redirect attributes
     * @return перенаправление на список или возврат к форме / redirect to list or return to form
     */
    @PostMapping("/create-with-files")
    public String createGalleryItemWithFiles(@ModelAttribute GalleryItem galleryItem,
                                             BindingResult bindingResult,
                                             @RequestParam(value = "files", required = false) MultipartFile[] files,
                                             Model model,
                                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);
            return "admin/gallery/create";
        }

        try {
            GalleryItem savedItem;

            if (files != null && files.length > 0) {
                // Используем новую функциональность с файлами
                // Use new functionality with files
                savedItem = galleryService.createGalleryItemWithFiles(galleryItem, files);
                redirectAttributes.addFlashAttribute("success", "gallery_item_created_with_files");
                redirectAttributes.addFlashAttribute("message",
                        "Элемент галереи успешно создан! Загружено файлов: " + files.length +
                                " / Gallery item created successfully! Files uploaded: " + files.length);
            } else {
                // Используем старую функциональность без файлов
                // Use old functionality without files
                savedItem = galleryService.saveGalleryItem(galleryItem);
                redirectAttributes.addFlashAttribute("success", "gallery_item_created");
                redirectAttributes.addFlashAttribute("message",
                        "Элемент галереи успешно создан! / Gallery item created successfully!");
            }

            return "redirect:/admin/gallery";

        } catch (FileStorageService.FileStorageException e) {
            model.addAttribute("error", "file_validation_failed");
            model.addAttribute("errorMessage", e.getMessage());
            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);
            return "admin/gallery/create";

        } catch (IOException e) {
            model.addAttribute("error", "file_upload_failed");
            model.addAttribute("errorMessage", "Ошибка при загрузке файлов: " + e.getMessage() + " / File upload error: " + e.getMessage());
            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);
            return "admin/gallery/create";

        } catch (Exception e) {
            model.addAttribute("error", "creation_failed");
            model.addAttribute("errorMessage", "Ошибка при создании элемента: " + e.getMessage() + " / Creation error: " + e.getMessage());
            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);
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
        Optional<GalleryItem> galleryItemOpt = galleryService.findGalleryItemWithMediaFiles(id);

        if (galleryItemOpt.isPresent()) {
            GalleryItem galleryItem = galleryItemOpt.get();
            model.addAttribute("galleryItem", galleryItem);
            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);
            model.addAttribute("currentFilesCount", galleryItem.getMediaFilesCount());

            return "admin/gallery/edit";
        } else {
            return "redirect:/admin/gallery?error=item_not_found";
        }
    }

    /**
     * Обрабатывает отправку формы редактирования элемента галереи (старая версия)
     * Processes gallery item editing form submission (old version)
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
                                    @ModelAttribute GalleryItem galleryItem,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        return updateGalleryItemWithFiles(id, galleryItem, bindingResult, null, model, redirectAttributes);
    }

    /**
     * Обрабатывает отправку формы редактирования элемента галереи с файлами (новая версия)
     * Processes gallery item editing form submission with files (new version)
     *
     * @param id идентификатор редактируемого элемента / item identifier to edit
     * @param galleryItem обновленные данные элемента / updated item data
     * @param bindingResult результаты валидации / validation results
     * @param newFiles новые файлы для добавления / new files to add
     * @param model модель для передачи данных в представление / model for view data
     * @param redirectAttributes атрибуты для перенаправления / redirect attributes
     * @return перенаправление на список или возврат к форме / redirect to list or return to form
     */
    @PostMapping("/edit-with-files/{id}")
    public String updateGalleryItemWithFiles(@PathVariable Long id,
                                             @ModelAttribute GalleryItem galleryItem,
                                             BindingResult bindingResult,
                                             @RequestParam(value = "newFiles", required = false) MultipartFile[] newFiles,
                                             Model model,
                                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);

            // Загружаем текущие файлы для отображения
            // Load current files for display
            Optional<GalleryItem> currentItemOpt = galleryService.findGalleryItemWithMediaFiles(id);
            currentItemOpt.ifPresent(item -> model.addAttribute("currentFilesCount", item.getMediaFilesCount()));

            return "admin/gallery/edit";
        }

        try {
            galleryItem.setId(id); // Ensure ID is preserved / Сохраняем ID

            GalleryItem updatedItem;
            String message;

            if (newFiles != null && newFiles.length > 0) {
                // Используем новую функциональность с файлами
                // Use new functionality with files
                updatedItem = galleryService.updateGalleryItemWithFiles(id, galleryItem, newFiles);
                message = "Элемент галереи успешно обновлен! Добавлено новых файлов: " + newFiles.length +
                        " / Gallery item updated successfully! New files added: " + newFiles.length;
                redirectAttributes.addFlashAttribute("success", "gallery_item_updated_with_files");
            } else {
                // Используем старую функциональность без файлов
                // Use old functionality without files
                updatedItem = galleryService.saveGalleryItem(galleryItem);
                message = "Элемент галереи успешно обновлен! / Gallery item updated successfully!";
                redirectAttributes.addFlashAttribute("success", "gallery_item_updated");
            }

            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/admin/gallery";

        } catch (FileStorageService.FileStorageException e) {
            model.addAttribute("error", "file_validation_failed");
            model.addAttribute("errorMessage", e.getMessage());
            prepareModelForForm(model);
            return "admin/gallery/edit";

        } catch (IOException e) {
            model.addAttribute("error", "file_upload_failed");
            model.addAttribute("errorMessage", "Ошибка при загрузке файлов: " + e.getMessage() + " / File upload error: " + e.getMessage());
            prepareModelForForm(model);
            return "admin/gallery/edit";

        } catch (Exception e) {
            model.addAttribute("error", "update_failed");
            model.addAttribute("errorMessage", "Ошибка при обновлении элемента: " + e.getMessage() + " / Update error: " + e.getMessage());
            prepareModelForForm(model);
            return "admin/gallery/edit";
        }
    }

    /**
     * НОВЫЕ МЕТОДЫ ДЛЯ УПРАВЛЕНИЯ МЕДИАФАЙЛАМИ
     * NEW METHODS FOR MEDIA FILES MANAGEMENT
     */

    /**
     * Добавляет файлы к существующему элементу галереи
     * Adds files to existing gallery item
     *
     * @param id идентификатор элемента / item identifier
     * @param files файлы для добавления / files to add
     * @param redirectAttributes атрибуты для перенаправления / redirect attributes
     * @return перенаправление на форму редактирования / redirect to edit form
     */
    @PostMapping("/{id}/add-files")
    public String addFilesToGalleryItem(@PathVariable Long id,
                                        @RequestParam("files") MultipartFile[] files,
                                        RedirectAttributes redirectAttributes) {
        try {
            Optional<GalleryItem> itemOpt = galleryService.findGalleryItemById(id);

            if (itemOpt.isPresent()) {
                galleryService.addMediaFilesToItem(itemOpt.get(), files);

                redirectAttributes.addFlashAttribute("success", "files_added");
                redirectAttributes.addFlashAttribute("message",
                        "Файлы успешно добавлены! Загружено: " + files.length + " файл(ов) / " +
                                "Files added successfully! Uploaded: " + files.length + " file(s)");
            } else {
                redirectAttributes.addFlashAttribute("error", "item_not_found");
            }

        } catch (FileStorageService.FileStorageException e) {
            redirectAttributes.addFlashAttribute("error", "file_validation_failed");
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "file_upload_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при загрузке файлов: " + e.getMessage() + " / File upload error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "add_files_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении файлов: " + e.getMessage() + " / Add files error: " + e.getMessage());
        }

        return "redirect:/admin/gallery/edit/" + id;
    }

    /**
     * Удаляет медиафайл из элемента галереи
     * Removes media file from gallery item
     *
     * @param id идентификатор элемента / item identifier
     * @param mediaFileId идентификатор медиафайла / media file identifier
     * @param redirectAttributes атрибуты для перенаправления / redirect attributes
     * @return перенаправление на форму редактирования / redirect to edit form
     */
    @PostMapping("/{id}/remove-file/{mediaFileId}")
    public String removeFileFromGalleryItem(@PathVariable Long id,
                                            @PathVariable Long mediaFileId,
                                            RedirectAttributes redirectAttributes) {
        try {
            boolean success = galleryService.removeMediaFileFromItem(id, mediaFileId);

            if (success) {
                redirectAttributes.addFlashAttribute("success", "file_removed");
                redirectAttributes.addFlashAttribute("message",
                        "Файл успешно удален / File removed successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "file_not_found");
            }

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "file_deletion_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении файла: " + e.getMessage() + " / File deletion error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "remove_file_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении файла: " + e.getMessage() + " / Remove file error: " + e.getMessage());
        }

        return "redirect:/admin/gallery/edit/" + id;
    }

    /**
     * Устанавливает основной медиафайл для элемента
     * Sets primary media file for item
     *
     * @param id идентификатор элемента / item identifier
     * @param mediaFileId идентификатор медиафайла / media file identifier
     * @param redirectAttributes атрибуты для перенаправления / redirect attributes
     * @return перенаправление на форму редактирования / redirect to edit form
     */
    @PostMapping("/{id}/set-primary/{mediaFileId}")
    public String setPrimaryMediaFile(@PathVariable Long id,
                                      @PathVariable Long mediaFileId,
                                      RedirectAttributes redirectAttributes) {
        boolean success = galleryService.setPrimaryMediaFile(id, mediaFileId);

        if (success) {
            redirectAttributes.addFlashAttribute("success", "primary_file_set");
            redirectAttributes.addFlashAttribute("message",
                    "Основной файл успешно установлен / Primary file set successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "set_primary_failed");
        }

        return "redirect:/admin/gallery/edit/" + id;
    }

    /**
     * ОСНОВНЫЕ ДЕЙСТВИЯ С ЭЛЕМЕНТАМИ - СТАРЫЕ МЕТОДЫ
     * BASIC ITEM ACTIONS - OLD METHODS
     */

    @PostMapping("/delete/{id}")
    public String deleteGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            galleryService.deleteGalleryItem(id);
            redirectAttributes.addFlashAttribute("success", "gallery_item_deleted");
            redirectAttributes.addFlashAttribute("message",
                    "Элемент галереи и все связанные файлы успешно удалены / " +
                            "Gallery item and all related files deleted successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "file_deletion_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Элемент удален, но возникли ошибки при удалении файлов: " + e.getMessage() + " / " +
                            "Item deleted, but file deletion errors occurred: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "delete_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении элемента: " + e.getMessage() + " / Delete error: " + e.getMessage());
        }
        return "redirect:/admin/gallery";
    }

    @PostMapping("/publish/{id}")
    public String publishGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = galleryService.publishGalleryItem(id);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "gallery_item_published");
            redirectAttributes.addFlashAttribute("message",
                    "Элемент галереи опубликован / Gallery item published");
        } else {
            redirectAttributes.addFlashAttribute("error", "publish_failed");
        }
        return "redirect:/admin/gallery";
    }

    @PostMapping("/unpublish/{id}")
    public String unpublishGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = galleryService.unpublishGalleryItem(id);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "gallery_item_unpublished");
            redirectAttributes.addFlashAttribute("message",
                    "Элемент галереи снят с публикации / Gallery item unpublished");
        } else {
            redirectAttributes.addFlashAttribute("error", "unpublish_failed");
        }
        return "redirect:/admin/gallery";
    }

    /**
     * ПРЕДПРОСМОТР И ДОПОЛНИТЕЛЬНЫЕ СТРАНИЦЫ
     * PREVIEW AND ADDITIONAL PAGES
     */

    @GetMapping("/preview/{id}")
    public String previewGalleryItem(@PathVariable Long id, Model model) {
        Optional<GalleryItem> galleryItemOpt = galleryService.findGalleryItemWithMediaFiles(id);

        if (galleryItemOpt.isPresent()) {
            GalleryItem galleryItem = galleryItemOpt.get();
            model.addAttribute("galleryItem", galleryItem);

            // Разделяем фото и видео для удобного отображения
            // Separate photos and videos for convenient display
            model.addAttribute("photos", galleryItem.getPhotos());
            model.addAttribute("videos", galleryItem.getVideos());
            model.addAttribute("primaryMedia", galleryItem.getPrimaryMedia());

            return "admin/gallery/preview";
        } else {
            return "redirect:/admin/gallery?error=item_not_found";
        }
    }

    /**
     * ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
     * HELPER METHODS
     */

    /**
     * Подготавливает модель для форм создания/редактирования
     * Prepares model for create/edit forms
     *
     * @param model модель для заполнения / model to populate
     */
    private void prepareModelForForm(Model model) {
        model.addAttribute("mediaTypes", MediaType.values());
        model.addAttribute("availableYears", List.of(2023, 2024, 2025, 2026));
        model.addAttribute("availableCategories", List.of("education", "volunteering", "projects", "events", "community", "ecology"));
    }

    /**
     * Обрабатывает исключения файлового хранилища
     * Handles file storage exceptions
     */
    @ExceptionHandler(FileStorageService.FileStorageException.class)
    public String handleFileStorageException(FileStorageService.FileStorageException ex,
                                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "file_storage_error");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/admin/gallery";
    }

    /**
     * Обрабатывает исключения ввода-вывода
     * Handles IO exceptions
     */
    @ExceptionHandler(IOException.class)
    public String handleIOException(IOException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "io_error");
        redirectAttributes.addFlashAttribute("errorMessage",
                "Ошибка ввода-вывода: " + ex.getMessage() + " / IO error: " + ex.getMessage());
        return "redirect:/admin/gallery";
    }
}
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер для административного управления галереей с поддержкой множественных медиафайлов
 * Controller for administrative gallery management with multiple media files support
 */
@Controller
@RequestMapping("/admin/gallery")
public class GalleryAdminController {

    private final GalleryService galleryService;
    private final FileStorageService fileStorageService;

    @Autowired
    public GalleryAdminController(GalleryService galleryService, FileStorageService fileStorageService) {
        this.galleryService = galleryService;
        this.fileStorageService = fileStorageService;
    }

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

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        GalleryItem galleryItem = new GalleryItem();

        // Устанавливаем значения по умолчанию
        galleryItem.setImageUrl("");
        galleryItem.setPublished(true);
        galleryItem.setSortOrder(0);

        model.addAttribute("galleryItem", galleryItem);
        prepareModelForForm(model);
        model.addAttribute("maxFiles", 20);

        return "admin/gallery/create";
    }

    @PostMapping("/create")
    public String createGalleryItem(@ModelAttribute GalleryItem galleryItem,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        return createGalleryItemWithFiles(galleryItem, bindingResult, null, model, redirectAttributes);
    }

    @PostMapping("/create-with-files")
    public String createGalleryItemWithFiles(@ModelAttribute GalleryItem galleryItem,
                                             BindingResult bindingResult,
                                             @RequestParam(value = "files", required = false) MultipartFile[] files,
                                             Model model,
                                             RedirectAttributes redirectAttributes) {

        // Ручная валидация обязательных полей
        if (galleryItem.getTitle() == null || galleryItem.getTitle().trim().isEmpty()) {
            bindingResult.rejectValue("title", "notblank", "Название обязательно");
        }
        if (galleryItem.getYear() == null) {
            bindingResult.rejectValue("year", "notnull", "Год обязателен");
        }
        if (galleryItem.getCategory() == null || galleryItem.getCategory().trim().isEmpty()) {
            bindingResult.rejectValue("category", "notblank", "Категория обязательна");
        }
        if (galleryItem.getMediaType() == null) {
            bindingResult.rejectValue("mediaType", "notnull", "Тип медиа обязателен");
        }

        if (bindingResult.hasErrors()) {
            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);
            return "admin/gallery/create";
        }

        try {
            // Обеспечиваем что поля не будут null
            if (galleryItem.getImageUrl() == null) {
                galleryItem.setImageUrl("");
            }
            if (galleryItem.getVideoUrl() == null) {
                galleryItem.setVideoUrl("");
            }
            if (galleryItem.getPublished() == null) {
                galleryItem.setPublished(true);
            }
            if (galleryItem.getSortOrder() == null) {
                galleryItem.setSortOrder(0);
            }
            if (galleryItem.getCreatedAt() == null) {
                galleryItem.setCreatedAt(LocalDateTime.now());
            }

            GalleryItem savedItem;

            if (files != null && files.length > 0) {
                savedItem = galleryService.createGalleryItemWithFiles(galleryItem, files);
                redirectAttributes.addFlashAttribute("success", "gallery_item_created_with_files");
                redirectAttributes.addFlashAttribute("message",
                        "Элемент галереи успешно создан! Загружено файлов: " + files.length);
            } else {
                savedItem = galleryService.saveGalleryItem(galleryItem);
                redirectAttributes.addFlashAttribute("success", "gallery_item_created");
                redirectAttributes.addFlashAttribute("message",
                        "Элемент галереи успешно создан!");
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
            model.addAttribute("errorMessage", "Ошибка при загрузке файлов: " + e.getMessage());
            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);
            return "admin/gallery/create";

        } catch (Exception e) {
            model.addAttribute("error", "creation_failed");
            model.addAttribute("errorMessage", "Ошибка при создании элемента: " + e.getMessage());
            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);
            return "admin/gallery/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<GalleryItem> galleryItemOpt = galleryService.findGalleryItemWithMediaFiles(id);

        if (galleryItemOpt.isPresent()) {
            GalleryItem galleryItem = galleryItemOpt.get();

            // ОБЕСПЕЧИВАЕМ ЧТО ПОЛЯ НЕ NULL
            if (galleryItem.getImageUrl() == null) {
                galleryItem.setImageUrl("");
            }
            if (galleryItem.getVideoUrl() == null) {
                galleryItem.setVideoUrl("");
            }
            if (galleryItem.getPublished() == null) {
                galleryItem.setPublished(true);
            }
            if (galleryItem.getSortOrder() == null) {
                galleryItem.setSortOrder(0);
            }

            model.addAttribute("galleryItem", galleryItem);
            prepareModelForForm(model);

            model.addAttribute("maxFiles", 20);
            model.addAttribute("currentFilesCount", galleryItem.getMediaFilesCount());

            return "admin/gallery/edit";
        } else {
            return "redirect:/admin/gallery?error=item_not_found";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateGalleryItem(@PathVariable Long id,
                                    @ModelAttribute GalleryItem galleryItem,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        // Ручная валидация обязательных полей
        if (galleryItem.getTitle() == null || galleryItem.getTitle().trim().isEmpty()) {
            bindingResult.rejectValue("title", "notblank", "Название обязательно");
        }
        if (galleryItem.getYear() == null) {
            bindingResult.rejectValue("year", "notnull", "Год обязателен");
        }
        if (galleryItem.getCategory() == null || galleryItem.getCategory().trim().isEmpty()) {
            bindingResult.rejectValue("category", "notblank", "Категория обязательна");
        }
        if (galleryItem.getMediaType() == null) {
            bindingResult.rejectValue("mediaType", "notnull", "Тип медиа обязателен");
        }

        if (bindingResult.hasErrors()) {
            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);

            // Загружаем текущие файлы для отображения
            Optional<GalleryItem> currentItemOpt = galleryService.findGalleryItemWithMediaFiles(id);
            currentItemOpt.ifPresent(item -> model.addAttribute("currentFilesCount", item.getMediaFilesCount()));

            return "admin/gallery/edit";
        }

        try {
            // Устанавливаем ID
            galleryItem.setId(id);

            // Загружаем существующий элемент
            Optional<GalleryItem> existingItemOpt = galleryService.findGalleryItemWithMediaFiles(id);

            if (existingItemOpt.isEmpty()) {
                model.addAttribute("error", "item_not_found");
                prepareModelForForm(model);
                return "admin/gallery/edit";
            }

            GalleryItem existingItem = existingItemOpt.get();

            // КОПИРУЕМ ВАЖНЫЕ ДАННЫЕ ИЗ СУЩЕСТВУЮЩЕГО ЭЛЕМЕНТА
            // 1. Сохраняем время создания
            galleryItem.setCreatedAt(existingItem.getCreatedAt());

            // 2. Сохраняем медиафайлы
            galleryItem.setMediaFiles(existingItem.getMediaFiles());

            // 3. Обеспечиваем что поля не null
            if (galleryItem.getImageUrl() == null) {
                galleryItem.setImageUrl(existingItem.getImageUrl() != null ? existingItem.getImageUrl() : "");
            }
            if (galleryItem.getVideoUrl() == null) {
                galleryItem.setVideoUrl(existingItem.getVideoUrl());
            }
            if (galleryItem.getPublished() == null) {
                galleryItem.setPublished(existingItem.getPublished() != null ? existingItem.getPublished() : true);
            }
            if (galleryItem.getSortOrder() == null) {
                galleryItem.setSortOrder(existingItem.getSortOrder() != null ? existingItem.getSortOrder() : 0);
            }

            // 4. Устанавливаем время обновления
            galleryItem.setUpdatedAt(LocalDateTime.now());

            // Обновляем элемент
            GalleryItem updatedItem = galleryService.saveGalleryItem(galleryItem);

            redirectAttributes.addFlashAttribute("success", "gallery_item_updated");
            redirectAttributes.addFlashAttribute("message", "Элемент галереи успешно обновлен!");

            return "redirect:/admin/gallery";

        } catch (Exception e) {
            model.addAttribute("error", "update_failed");
            model.addAttribute("errorMessage", "Ошибка при обновлении элемента: " + e.getMessage());

            // Загружаем текущие файлы для отображения
            Optional<GalleryItem> currentItemOpt = galleryService.findGalleryItemWithMediaFiles(id);
            currentItemOpt.ifPresent(item -> model.addAttribute("currentFilesCount", item.getMediaFilesCount()));

            prepareModelForForm(model);
            return "admin/gallery/edit";
        }
    }

    @PostMapping("/edit-with-files/{id}")
    public String updateGalleryItemWithFiles(@PathVariable Long id,
                                             @ModelAttribute GalleryItem galleryItem,
                                             BindingResult bindingResult,
                                             @RequestParam(value = "newFiles", required = false) MultipartFile[] newFiles,
                                             Model model,
                                             RedirectAttributes redirectAttributes) {

        // Ручная валидация обязательных полей
        if (galleryItem.getTitle() == null || galleryItem.getTitle().trim().isEmpty()) {
            bindingResult.rejectValue("title", "notblank", "Название обязательно");
        }
        if (galleryItem.getYear() == null) {
            bindingResult.rejectValue("year", "notnull", "Год обязателен");
        }
        if (galleryItem.getCategory() == null || galleryItem.getCategory().trim().isEmpty()) {
            bindingResult.rejectValue("category", "notblank", "Категория обязательна");
        }
        if (galleryItem.getMediaType() == null) {
            bindingResult.rejectValue("mediaType", "notnull", "Тип медиа обязателен");
        }

        if (bindingResult.hasErrors()) {
            // Загружаем существующий элемент для правильного отображения формы
            Optional<GalleryItem> currentItemOpt = galleryService.findGalleryItemWithMediaFiles(id);
            if (currentItemOpt.isPresent()) {
                GalleryItem currentItem = currentItemOpt.get();

                // Сохраняем существующие файлы
                galleryItem.setMediaFiles(currentItem.getMediaFiles());
                galleryItem.setCreatedAt(currentItem.getCreatedAt());

                model.addAttribute("currentFilesCount", currentItem.getMediaFilesCount());
            }

            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);
            return "admin/gallery/edit";
        }

        try {
            // Загружаем существующий элемент
            Optional<GalleryItem> existingItemOpt = galleryService.findGalleryItemWithMediaFiles(id);

            if (existingItemOpt.isEmpty()) {
                model.addAttribute("error", "item_not_found");
                prepareModelForForm(model);
                return "admin/gallery/edit";
            }

            GalleryItem existingItem = existingItemOpt.get();

            // КОПИРУЕМ ВСЕ ДАННЫЕ ИЗ СУЩЕСТВУЮЩЕГО ЭЛЕМЕНТА
            // 1. Сохраняем время создания
            galleryItem.setCreatedAt(existingItem.getCreatedAt());

            // 2. Сохраняем медиафайлы
            galleryItem.setMediaFiles(existingItem.getMediaFiles());

            // 3. Обеспечиваем что поля не null
            if (galleryItem.getImageUrl() == null) {
                galleryItem.setImageUrl(existingItem.getImageUrl() != null ? existingItem.getImageUrl() : "");
            }
            if (galleryItem.getVideoUrl() == null) {
                galleryItem.setVideoUrl(existingItem.getVideoUrl());
            }
            if (galleryItem.getPublished() == null) {
                galleryItem.setPublished(existingItem.getPublished() != null ? existingItem.getPublished() : true);
            }
            if (galleryItem.getSortOrder() == null) {
                galleryItem.setSortOrder(existingItem.getSortOrder() != null ? existingItem.getSortOrder() : 0);
            }

            // 4. Устанавливаем время обновления
            galleryItem.setUpdatedAt(LocalDateTime.now());

            // 5. Устанавливаем ID
            galleryItem.setId(id);

            GalleryItem updatedItem;
            String message;

            if (newFiles != null && newFiles.length > 0) {
                // Обновляем элемент с новыми файлами
                updatedItem = galleryService.updateGalleryItemWithFiles(id, galleryItem, newFiles);
                message = "Элемент галереи успешно обновлен! Добавлено новых файлов: " + newFiles.length;
                redirectAttributes.addFlashAttribute("success", "gallery_item_updated_with_files");
            } else {
                // Если нет новых файлов, используем обычное сохранение
                updatedItem = galleryService.saveGalleryItem(galleryItem);
                message = "Элемент галереи успешно обновлен!";
                redirectAttributes.addFlashAttribute("success", "gallery_item_updated");
            }

            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/admin/gallery";

        } catch (FileStorageService.FileStorageException e) {
            model.addAttribute("error", "file_validation_failed");
            model.addAttribute("errorMessage", e.getMessage());

            // Загружаем существующий элемент для отображения
            Optional<GalleryItem> currentItemOpt = galleryService.findGalleryItemWithMediaFiles(id);
            currentItemOpt.ifPresent(item -> {
                model.addAttribute("currentFilesCount", item.getMediaFilesCount());
            });

            prepareModelForForm(model);
            return "admin/gallery/edit";

        } catch (IOException e) {
            model.addAttribute("error", "file_upload_failed");
            model.addAttribute("errorMessage", "Ошибка при загрузке файлов: " + e.getMessage());

            // Загружаем существующий элемент для отображения
            Optional<GalleryItem> currentItemOpt = galleryService.findGalleryItemWithMediaFiles(id);
            currentItemOpt.ifPresent(item -> {
                model.addAttribute("currentFilesCount", item.getMediaFilesCount());
            });

            prepareModelForForm(model);
            return "admin/gallery/edit";

        } catch (Exception e) {
            model.addAttribute("error", "update_failed");
            model.addAttribute("errorMessage", "Ошибка при обновлении элемента: " + e.getMessage());

            // Загружаем существующий элемент для отображения
            Optional<GalleryItem> currentItemOpt = galleryService.findGalleryItemWithMediaFiles(id);
            currentItemOpt.ifPresent(item -> {
                model.addAttribute("currentFilesCount", item.getMediaFilesCount());
            });

            prepareModelForForm(model);
            return "admin/gallery/edit";
        }
    }

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
                        "Файлы успешно добавлены! Загружено: " + files.length + " файл(ов)");
            } else {
                redirectAttributes.addFlashAttribute("error", "item_not_found");
            }

        } catch (FileStorageService.FileStorageException e) {
            redirectAttributes.addFlashAttribute("error", "file_validation_failed");
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "file_upload_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при загрузке файлов: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "add_files_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении файлов: " + e.getMessage());
        }

        return "redirect:/admin/gallery/edit/" + id;
    }

    @PostMapping("/{id}/remove-file/{mediaFileId}")
    public String removeFileFromGalleryItem(@PathVariable Long id,
                                            @PathVariable Long mediaFileId,
                                            RedirectAttributes redirectAttributes) {
        try {
            boolean success = galleryService.removeMediaFileFromItem(id, mediaFileId);

            if (success) {
                redirectAttributes.addFlashAttribute("success", "file_removed");
                redirectAttributes.addFlashAttribute("message", "Файл успешно удален");
            } else {
                redirectAttributes.addFlashAttribute("error", "file_not_found");
            }

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "file_deletion_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении файла: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "remove_file_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении файла: " + e.getMessage());
        }

        return "redirect:/admin/gallery/edit/" + id;
    }

    @PostMapping("/{id}/set-primary/{mediaFileId}")
    public String setPrimaryMediaFile(@PathVariable Long id,
                                      @PathVariable Long mediaFileId,
                                      RedirectAttributes redirectAttributes) {
        boolean success = galleryService.setPrimaryMediaFile(id, mediaFileId);

        if (success) {
            redirectAttributes.addFlashAttribute("success", "primary_file_set");
            redirectAttributes.addFlashAttribute("message", "Основной файл успешно установлен");
        } else {
            redirectAttributes.addFlashAttribute("error", "set_primary_failed");
        }

        return "redirect:/admin/gallery/edit/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            galleryService.deleteGalleryItem(id);
            redirectAttributes.addFlashAttribute("success", "gallery_item_deleted");
            redirectAttributes.addFlashAttribute("message",
                    "Элемент галереи и все связанные файлы успешно удалены");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "file_deletion_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Элемент удален, но возникли ошибки при удалении файлов: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "delete_failed");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении элемента: " + e.getMessage());
        }
        return "redirect:/admin/gallery";
    }

    @PostMapping("/publish/{id}")
    public String publishGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = galleryService.publishGalleryItem(id);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "gallery_item_published");
            redirectAttributes.addFlashAttribute("message", "Элемент галереи опубликован");
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
            redirectAttributes.addFlashAttribute("message", "Элемент галереи снят с публикации");
        } else {
            redirectAttributes.addFlashAttribute("error", "unpublish_failed");
        }
        return "redirect:/admin/gallery";
    }

    @GetMapping("/preview/{id}")
    public String previewGalleryItem(@PathVariable Long id, Model model) {
        Optional<GalleryItem> galleryItemOpt = galleryService.findGalleryItemWithMediaFiles(id);

        if (galleryItemOpt.isPresent()) {
            GalleryItem galleryItem = galleryItemOpt.get();
            model.addAttribute("galleryItem", galleryItem);

            model.addAttribute("photos", galleryItem.getPhotos());
            model.addAttribute("videos", galleryItem.getVideos());
            model.addAttribute("primaryMedia", galleryItem.getPrimaryMedia());

            return "admin/gallery/preview";
        } else {
            return "redirect:/admin/gallery?error=item_not_found";
        }
    }

    private void prepareModelForForm(Model model) {
        model.addAttribute("mediaTypes", MediaType.values());
        model.addAttribute("availableYears", List.of(2023, 2024, 2025, 2026));
        model.addAttribute("availableCategories", List.of("education", "volunteering", "projects", "events", "community", "ecology"));
    }

    @ExceptionHandler(FileStorageService.FileStorageException.class)
    public String handleFileStorageException(FileStorageService.FileStorageException ex,
                                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "file_storage_error");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/admin/gallery";
    }

    @ExceptionHandler(IOException.class)
    public String handleIOException(IOException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "io_error");
        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка ввода-вывода: " + ex.getMessage());
        return "redirect:/admin/gallery";
    }
}
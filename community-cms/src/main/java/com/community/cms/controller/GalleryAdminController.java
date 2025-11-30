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
        model.addAttribute("galleryItem", new GalleryItem());
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

        if (bindingResult.hasErrors()) {
            prepareModelForForm(model);
            model.addAttribute("maxFiles", 20);
            return "admin/gallery/create";
        }

        try {
            // Обеспечиваем что imageUrl не будет null
            if (galleryItem.getImageUrl() == null) {
                galleryItem.setImageUrl("");
            }

            GalleryItem savedItem;

            if (files != null && files.length > 0) {
                savedItem = galleryService.createGalleryItemWithFiles(galleryItem, files);
                redirectAttributes.addFlashAttribute("success", "gallery_item_created_with_files");
                redirectAttributes.addFlashAttribute("message",
                        "Элемент галереи успешно создан! Загружено файлов: " + files.length +
                                " / Gallery item created successfully! Files uploaded: " + files.length);
            } else {
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

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<GalleryItem> galleryItemOpt = galleryService.findGalleryItemWithMediaFiles(id);

        if (galleryItemOpt.isPresent()) {
            GalleryItem galleryItem = galleryItemOpt.get();
            model.addAttribute("galleryItem", galleryItem);
            prepareModelForForm(model);

            // ИСПРАВЛЕНИЕ: Добавляем значения по умолчанию
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

        return updateGalleryItemWithFiles(id, galleryItem, bindingResult, null, model, redirectAttributes);
    }

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
            Optional<GalleryItem> currentItemOpt = galleryService.findGalleryItemWithMediaFiles(id);
            currentItemOpt.ifPresent(item -> model.addAttribute("currentFilesCount", item.getMediaFilesCount()));

            return "admin/gallery/edit";
        }

        try {
            galleryItem.setId(id); // Ensure ID is preserved

            // Обеспечиваем что imageUrl не будет null
            if (galleryItem.getImageUrl() == null) {
                galleryItem.setImageUrl("");
            }

            GalleryItem updatedItem;
            String message;

            if (newFiles != null && newFiles.length > 0) {
                updatedItem = galleryService.updateGalleryItemWithFiles(id, galleryItem, newFiles);
                message = "Элемент галереи успешно обновлен! Добавлено новых файлов: " + newFiles.length +
                        " / Gallery item updated successfully! New files added: " + newFiles.length;
                redirectAttributes.addFlashAttribute("success", "gallery_item_updated_with_files");
            } else {
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
        redirectAttributes.addFlashAttribute("errorMessage",
                "Ошибка ввода-вывода: " + ex.getMessage() + " / IO error: " + ex.getMessage());
        return "redirect:/admin/gallery";
    }
}